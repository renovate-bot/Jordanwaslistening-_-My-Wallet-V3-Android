package com.blockchain.nabu.api.getuser.data

import com.blockchain.core.buy.domain.SimpleBuyService
import com.blockchain.core.interest.domain.InterestService
import com.blockchain.core.kyc.domain.KycService
import com.blockchain.core.sdd.domain.SddService
import com.blockchain.data.DataResource
import com.blockchain.data.FreshnessStrategy
import com.blockchain.data.anyError
import com.blockchain.data.anyLoading
import com.blockchain.data.getFirstError
import com.blockchain.domain.eligibility.EligibilityService
import com.blockchain.domain.eligibility.model.EligibleProduct
import com.blockchain.domain.eligibility.model.ProductEligibility
import com.blockchain.domain.eligibility.model.ProductNotEligibleReason
import com.blockchain.nabu.BlockedReason
import com.blockchain.nabu.Feature
import com.blockchain.nabu.FeatureAccess
import com.blockchain.nabu.api.getuser.domain.UserFeaturePermissionService
import com.blockchain.store.mapData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import piuk.blockchain.androidcore.utils.extensions.zipSingles

internal class UserFeaturePermissionRepository(
    private val kycService: KycService,
    private val interestService: InterestService,
    private val sddService: SddService,
    private val eligibilityService: EligibilityService,
    private val simpleBuyService: SimpleBuyService
) : UserFeaturePermissionService {

    override fun isEligibleFor(
        feature: Feature,
        freshnessStrategy: FreshnessStrategy
    ): Flow<DataResource<Boolean>> {
        return when (feature) {
            is Feature.TierLevel -> {
                kycService.getTiers(freshnessStrategy)
                    .mapData { it.isInitialisedFor(feature.tier).not() }
            }
            is Feature.Interest -> {
                interestService.getEligibilityForAssets(freshnessStrategy)
                    .mapData { mapAssetWithEligibility -> mapAssetWithEligibility.containsKey(feature.currency) }
            }
            is Feature.SimplifiedDueDiligence -> {
                sddService.isEligible(freshnessStrategy)
            }
            Feature.Buy,
            Feature.Swap,
            Feature.Sell,
            Feature.DepositCrypto,
            Feature.DepositFiat,
            Feature.DepositInterest,
            Feature.WithdrawFiat -> {
                getAccessForFeature(feature).mapData { it is FeatureAccess.Granted }
            }
        }
    }

    override fun getAccessForFeature(
        feature: Feature,
        freshnessStrategy: FreshnessStrategy
    ): Flow<DataResource<FeatureAccess>> {
        return when (feature) {
            Feature.Buy -> {
                combine(
                    eligibilityService.getProductEligibility(EligibleProduct.BUY),
                    simpleBuyService.getEligibility()
                ) { buyEligibility, simpleBuyEligibility ->
                    val results = listOf(buyEligibility, simpleBuyEligibility)

                    when {
                        results.anyLoading() -> {
                            DataResource.Loading
                        }

                        results.anyError() -> {
                            DataResource.Error(results.getFirstError().error)
                        }

                        else -> {
                            buyEligibility as DataResource.Data
                            simpleBuyEligibility as DataResource.Data

                            DataResource.Data(
                                when {
                                    buyEligibility.data.toFeatureAccess() !is FeatureAccess.Granted -> {
                                        buyEligibility.data.toFeatureAccess()
                                    }

                                    simpleBuyEligibility.data.isPendingDepositThresholdReached.not() -> {
                                        FeatureAccess.Granted()
                                    }

                                    else -> {
                                        FeatureAccess.Blocked(
                                            BlockedReason.TooManyInFlightTransactions(
                                                simpleBuyEligibility.data.maxPendingDepositSimpleBuyTrades
                                            )
                                        )
                                    }
                                }
                            )
                        }

                    }
                }
            }

            Feature.Swap -> {
                eligibilityService.getProductEligibility(EligibleProduct.SWAP)
                    .mapData(ProductEligibility::toFeatureAccess)
            }

            Feature.Sell -> {
                eligibilityService.getProductEligibility(EligibleProduct.SELL)
                    .mapData(ProductEligibility::toFeatureAccess)
            }

            Feature.DepositFiat -> {
                eligibilityService.getProductEligibility(EligibleProduct.DEPOSIT_FIAT)
                    .mapData(ProductEligibility::toFeatureAccess)
            }

            Feature.DepositCrypto -> {
                eligibilityService.getProductEligibility(EligibleProduct.DEPOSIT_CRYPTO)
                    .mapData(ProductEligibility::toFeatureAccess)
            }

            Feature.DepositInterest -> {
                eligibilityService.getProductEligibility(EligibleProduct.DEPOSIT_INTEREST)
                    .mapData(ProductEligibility::toFeatureAccess)
            }

            Feature.WithdrawFiat -> {
                eligibilityService.getProductEligibility(EligibleProduct.WITHDRAW_FIAT)
                    .mapData(ProductEligibility::toFeatureAccess)
            }

            is Feature.Interest,
            Feature.SimplifiedDueDiligence,
            is Feature.TierLevel -> {
                TODO("Not Implemented Yet")
            }
        }
    }

    override fun getAccessForFeatures(
        vararg features: Feature,
        freshnessStrategy: FreshnessStrategy
    ): Flow<DataResource<Map<Feature, FeatureAccess>>> {
        features.map { feature ->
            userAccessForFeature(feature).map { access ->
                Pair(feature, access)
            }
        }.zipSingles()
            .map { mapOf(*it.toTypedArray()) }
    }
}

private fun ProductEligibility.toFeatureAccess(): FeatureAccess {
    return if (canTransact) {
        FeatureAccess.Granted(maxTransactionsCap)
    } else {
        FeatureAccess.Blocked(
            when (val reason = reasonNotEligible) {
                ProductNotEligibleReason.InsufficientTier.Tier1TradeLimitExceeded -> {
                    BlockedReason.InsufficientTier.Tier1TradeLimitExceeded
                }
                ProductNotEligibleReason.InsufficientTier.Tier2Required -> {
                    BlockedReason.InsufficientTier.Tier2Required
                }
                is ProductNotEligibleReason.InsufficientTier.Unknown -> {
                    BlockedReason.InsufficientTier.Unknown(reason.message)
                }
                ProductNotEligibleReason.Sanctions.RussiaEU5 -> {
                    BlockedReason.Sanctions.RussiaEU5
                }
                is ProductNotEligibleReason.Sanctions.Unknown -> {
                    BlockedReason.Sanctions.Unknown(reason.message)
                }
                is ProductNotEligibleReason.Unknown -> {
                    BlockedReason.NotEligible
                }
                null -> {
                    BlockedReason.NotEligible
                }
            }
        )
    }
}

