package piuk.blockchain.android.support

import com.blockchain.core.kyc.domain.KycService
import com.blockchain.core.kyc.domain.model.KycTier
import com.blockchain.featureflag.FeatureFlag
import com.blockchain.nabu.UserIdentity
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.Singles

class SupportInteractor(
    private val userIdentity: UserIdentity,
    private val kycService: KycService,
    private val isIntercomEnabledFlag: FeatureFlag
) {
    fun loadUserInformation(): Single<UserInfo> =
        Singles.zip(
            kycService.getHighestApprovedTierLevelLegacy(),
            userIdentity.getBasicProfileInformation(),
            isIntercomEnabledFlag.enabled
        ).map { (tier, basicInfo, isIntercomEnabled) ->
            UserInfo(tier == KycTier.GOLD, basicInfo, isIntercomEnabled)
        }
}
