package com.blockchain.home.presentation.koin

import com.blockchain.coincore.NullFiatAccount.currency
import com.blockchain.home.presentation.activity.detail.custodial.CustodialActivityDetailViewModel
import com.blockchain.home.presentation.activity.detail.privatekey.PrivateKeyActivityDetailViewModel
import com.blockchain.home.presentation.activity.list.custodial.CustodialActivityViewModel
import com.blockchain.home.presentation.activity.list.privatekey.PrivateKeyActivityViewModel
import com.blockchain.home.presentation.allassets.AssetsViewModel
import com.blockchain.home.presentation.allassets.EmptyScreenViewModel
import com.blockchain.home.presentation.dashboard.CustodialEmptyCardViewModel
import com.blockchain.home.presentation.earn.EarnViewModel
import com.blockchain.home.presentation.fiat.actions.FiatActionsViewModel
import com.blockchain.home.presentation.fiat.fundsdetail.FiatFundsDetailViewModel
import com.blockchain.home.presentation.quickactions.QuickActionsViewModel
import com.blockchain.home.presentation.referral.ReferralViewModel
import com.blockchain.koin.payloadScopeQualifier
import com.blockchain.koin.superAppModeService
import info.blockchain.balance.FiatCurrency
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homePresentationModule = module {
    scope(payloadScopeQualifier) {
        viewModel {
            AssetsViewModel(
                homeAccountsService = get(),
                currencyPrefs = get(),
                exchangeRates = get(),
                filterService = get(),
                assetCatalogue = get(),
                walletModeService = get(superAppModeService)
            )
        }

        viewModel { (currency: FiatCurrency) ->
            FiatFundsDetailViewModel(
                currency = currency,
                coincore = get()
            )
        }

        viewModel {
            FiatActionsViewModel(
                dataRemediationService = get(),
                userIdentity = get(),
                linkedBanksFactory = get(),
                bankService = get()
            )
        }

        viewModel { (
            homeVm: AssetsViewModel,
            pkwActivityViewModel: PrivateKeyActivityViewModel,
            custodialActivityViewModel: CustodialActivityViewModel
        ) ->
            EmptyScreenViewModel(
                homeAssetsViewModel = homeVm,
                walletModeService = get(superAppModeService),
                pkwActivityViewModel = pkwActivityViewModel,
                custodialActivityViewModel = custodialActivityViewModel
            )
        }

        viewModel {
            PrivateKeyActivityViewModel(
                unifiedActivityService = get()
            )
        }

        viewModel {
            CustodialActivityViewModel(
                custodialActivityService = get()
            )
        }

        viewModel { (txId: String) ->
            PrivateKeyActivityDetailViewModel(
                activityTxId = txId,
                unifiedActivityService = get()
            )
        }

        viewModel { (txId: String) ->
            CustodialActivityDetailViewModel(
                activityTxId = txId,
                custodialActivityService = get(),
                paymentMethodService = get(),
                cardService = get(),
                bankService = get(),
                coincore = get(),
                defaultLabels = get()
            )
        }

        viewModel {
            QuickActionsViewModel(
                walletModeService = get(superAppModeService),
                userFeaturePermissionService = get(),
                coincore = get(),
                currencyPrefs = get()
            )
        }

        viewModel {
            EarnViewModel(
                walletModeService = get(superAppModeService),
                stakingService = get(),
                exchangeRates = get(),
                coincore = get(),
                interestService = get()
            )
        }

        viewModel {
            CustodialEmptyCardViewModel(
                fiatCurrenciesService = get(),
                userFeaturePermissionService = get(),
                onBoardingStepsService = get()
            )
        }

        viewModel {
            ReferralViewModel(
                referralService = get()
            )
        }
    }
}
