package com.blockchain.chrome.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.blockchain.chrome.composable.MultiAppChrome
import com.blockchain.commonarch.presentation.mvi_v2.compose.composable
import com.blockchain.commonarch.presentation.mvi_v2.compose.navigate
import com.blockchain.commonarch.presentation.mvi_v2.compose.navigates
import com.blockchain.commonarch.presentation.mvi_v2.compose.rememberBottomSheetNavigator
import com.blockchain.home.presentation.fiat.actions.FiatActionsNavigation
import com.blockchain.home.presentation.navigation.AssetActionsNavigation
import com.blockchain.home.presentation.navigation.HomeDestination
import com.blockchain.home.presentation.navigation.homeGraph
import com.blockchain.prices.navigation.PricesNavigation
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import info.blockchain.balance.FiatCurrency

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun MultiAppNavHost(
    assetActionsNavigation: AssetActionsNavigation,
    fiatActionsNavigation: FiatActionsNavigation,
    pricesNavigation: PricesNavigation
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator(skipHalfExpanded = true)
    val navController = rememberNavController(bottomSheetNavigator)

    ModalBottomSheetLayout(bottomSheetNavigator) {
        NavHost(
            navController = navController,
            startDestination = ChromeDestination.Main.route
        ) {
            // main chrome
            chrome(
                navController = navController,
                assetActionsNavigation = assetActionsNavigation,
                pricesNavigation = pricesNavigation
            )

            // home screens
            homeGraph(
                assetActionsNavigation = assetActionsNavigation,
                fiatActionsNavigation = fiatActionsNavigation,
                onBackPressed = navController::popBackStack
            )
        }
    }
}

private fun NavGraphBuilder.chrome(
    navController: NavHostController,
    assetActionsNavigation: AssetActionsNavigation,
    pricesNavigation: PricesNavigation
) {
    composable(navigationEvent = ChromeDestination.Main) {
        MultiAppChrome(
            assetActionsNavigation = assetActionsNavigation,
            pricesNavigation = pricesNavigation,
            openCryptoAssets = {
                navController.navigate(HomeDestination.CryptoAssets)
            },
            openActivity = {
                navController.navigate(HomeDestination.Activity)
            },
            openReferral = {
                navController.navigate(HomeDestination.Referral)
            },
            openFiatActionDetail = { fiatCurrency: FiatCurrency ->
                navController.navigates(
                    HomeDestination.FiatActionDetail.route,
                    Bundle().apply { putSerializable("fiatCurrency", fiatCurrency) }
                )
            }
        )
    }
}
