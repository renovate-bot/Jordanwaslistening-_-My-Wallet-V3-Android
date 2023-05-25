package piuk.blockchain.android.ui.coinview.presentation.composable

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import com.blockchain.analytics.Analytics
import com.blockchain.analytics.events.LaunchOrigin
import com.blockchain.chrome.composable.ANIMATION_DURATION
import com.blockchain.componentlib.alert.PillAlert
import com.blockchain.componentlib.alert.PillAlertType
import com.blockchain.componentlib.alert.SnackbarAlert
import com.blockchain.componentlib.basic.ImageResource
import com.blockchain.componentlib.icons.Icons
import com.blockchain.componentlib.icons.Star
import com.blockchain.componentlib.navigation.NavigationBar
import com.blockchain.componentlib.navigation.NavigationBarButton
import com.blockchain.componentlib.tablerow.custom.StackedIcon
import com.blockchain.componentlib.theme.AppTheme
import com.blockchain.componentlib.utils.TextValue
import com.blockchain.componentlib.utils.collectAsStateLifecycleAware
import com.blockchain.componentlib.utils.previewAnalytics
import com.blockchain.core.price.HistoricalTimeSpan
import com.blockchain.data.DataResource
import com.blockchain.home.presentation.R
import com.github.mikephil.charting.data.Entry
import info.blockchain.balance.CryptoCurrency
import org.koin.androidx.compose.get
import piuk.blockchain.android.ui.coinview.domain.model.CoinviewAccount
import piuk.blockchain.android.ui.coinview.presentation.CoinviewAccountsState
import piuk.blockchain.android.ui.coinview.presentation.CoinviewAssetInfoState
import piuk.blockchain.android.ui.coinview.presentation.CoinviewAssetState
import piuk.blockchain.android.ui.coinview.presentation.CoinviewAssetTradeableState
import piuk.blockchain.android.ui.coinview.presentation.CoinviewIntent
import piuk.blockchain.android.ui.coinview.presentation.CoinviewNewsState
import piuk.blockchain.android.ui.coinview.presentation.CoinviewPillAlertState
import piuk.blockchain.android.ui.coinview.presentation.CoinviewPriceState
import piuk.blockchain.android.ui.coinview.presentation.CoinviewQuickActionState
import piuk.blockchain.android.ui.coinview.presentation.CoinviewRecurringBuysState
import piuk.blockchain.android.ui.coinview.presentation.CoinviewSnackbarAlertState
import piuk.blockchain.android.ui.coinview.presentation.CoinviewViewModel
import piuk.blockchain.android.ui.coinview.presentation.CoinviewViewState
import piuk.blockchain.android.ui.coinview.presentation.toModelState
import piuk.blockchain.android.ui.dashboard.coinview.CoinViewAnalytics

@Composable
fun Coinview(
    viewModel: CoinviewViewModel,
    backOnClick: () -> Unit
) {
    val viewState: CoinviewViewState by viewModel.viewState.collectAsStateLifecycleAware()

    CoinviewScreen(
        backOnClick = backOnClick,
        asset = viewState.asset,
        onContactSupportClick = {
            viewModel.onIntent(CoinviewIntent.ContactSupport)
        },
        price = viewState.assetPrice,
        onChartEntryHighlighted = { entry ->
            viewModel.onIntent(CoinviewIntent.UpdatePriceForChartSelection(entry))
        },
        resetPriceInformation = {
            viewModel.onIntent(CoinviewIntent.ResetPriceSelection)
        },
        onNewTimeSpanSelected = { timeSpan ->
            viewModel.onIntent(CoinviewIntent.NewTimeSpanSelected(timeSpan))
        },
        tradeable = viewState.tradeable,

        watchlist = viewState.watchlist,
        onWatchlistClick = {
            viewModel.onIntent(CoinviewIntent.ToggleWatchlist)
        },

        accounts = viewState.accounts,
        onAccountClick = { account ->
            if (account.isClickable) {
                viewModel.onIntent(CoinviewIntent.AccountSelected(account))
            }
        },
        onLockedAccountClick = {
            viewModel.onIntent(CoinviewIntent.LockedAccountSelected)
        },
        quickActionsCenter = viewState.centerQuickAction,
        recurringBuys = viewState.recurringBuys,
        onRecurringBuyUpsellClick = {
            viewModel.onIntent(CoinviewIntent.RecurringBuysUpsell)
        },
        onRecurringBuyItemClick = { recurringBuyId ->
            viewModel.onIntent(CoinviewIntent.ShowRecurringBuyDetail(recurringBuyId))
        },
        quickActionsBottom = viewState.bottomQuickAction,
        onQuickActionClick = { quickAction ->
            viewModel.onIntent(CoinviewIntent.QuickActionSelected(quickAction.toModelState()))
        },
        assetInfo = viewState.assetInfo,
        onWebsiteClick = {
            viewModel.onIntent(CoinviewIntent.VisitAssetWebsite)
        },
        newsArticles = viewState.news,
        pillAlert = viewState.pillAlert,
        snackbarAlert = viewState.snackbarError
    )
}

@Composable
fun CoinviewScreen(
    analytics: Analytics = get(),

    backOnClick: () -> Unit,

    asset: DataResource<CoinviewAssetState>,
    onContactSupportClick: () -> Unit,

    price: CoinviewPriceState,
    onChartEntryHighlighted: (Entry) -> Unit,
    resetPriceInformation: () -> Unit,
    onNewTimeSpanSelected: (HistoricalTimeSpan) -> Unit,

    tradeable: CoinviewAssetTradeableState,

    watchlist: DataResource<Boolean>,
    onWatchlistClick: () -> Unit,

    accounts: DataResource<CoinviewAccountsState?>,
    onAccountClick: (CoinviewAccount) -> Unit,
    onLockedAccountClick: () -> Unit,

    quickActionsCenter: DataResource<List<CoinviewQuickActionState>>,

    recurringBuys: DataResource<CoinviewRecurringBuysState>,
    onRecurringBuyUpsellClick: () -> Unit,
    onRecurringBuyItemClick: (String) -> Unit,

    quickActionsBottom: DataResource<List<CoinviewQuickActionState>>,
    onQuickActionClick: (CoinviewQuickActionState) -> Unit,

    assetInfo: CoinviewAssetInfoState,
    onWebsiteClick: () -> Unit,

    newsArticles: CoinviewNewsState,

    pillAlert: CoinviewPillAlertState,

    snackbarAlert: CoinviewSnackbarAlertState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppTheme.colors.backgroundMuted)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NavigationBar(
                title = (asset as? DataResource.Data)?.data?.asset?.displayTicker ?: "",
                icon = (asset as? DataResource.Data)?.data?.let { assetState ->
                    assetState.l1Network?.let { l1Network ->
                        StackedIcon.SmallTag(
                            main = ImageResource.Remote(assetState.asset.logo),
                            tag = ImageResource.Remote(l1Network.logo)
                        )
                    } ?: StackedIcon.SingleIcon(ImageResource.Remote(assetState.asset.logo))
                } ?: StackedIcon.None,
                onBackButtonClick = backOnClick,
                navigationBarButtons = listOfNotNull(
                    (watchlist as? DataResource.Data)?.data?.let { isInWatchlist ->
                        NavigationBarButton.IconResource(
                            image = if (isInWatchlist) {
                                Icons.Filled.Star
                            } else {
                                Icons.Star
                            }.copy(
                                contentDescription = stringResource(
                                    com.blockchain.stringResources.R.string.accessibility_filter
                                )
                            ),
                            onIconClick = {
                                (asset as? DataResource.Data)?.data?.asset?.networkTicker?.let {
                                    analytics.logEvent(
                                        if (isInWatchlist) {
                                            CoinViewAnalytics.CoinRemovedFromWatchlist(
                                                origin = LaunchOrigin.COIN_VIEW,
                                                currency = it
                                            )
                                        } else {
                                            CoinViewAnalytics.CoinAddedFromWatchlist(
                                                origin = LaunchOrigin.COIN_VIEW,
                                                currency = it
                                            )
                                        }
                                    )

                                    onWatchlistClick()
                                }
                            }
                        )
                    }
                )
            )

            when (asset) {
                DataResource.Loading -> {
                    // n/a
                }

                is DataResource.Error -> {
                    UnknownAsset(onContactSupportClick = onContactSupportClick)
                }

                is DataResource.Data -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1F)
                                .verticalScroll(rememberScrollState())
                        ) {
                            AssetPrice(
                                data = price,
                                assetTicker = asset.data.asset.networkTicker,
                                onChartEntryHighlighted = onChartEntryHighlighted,
                                resetPriceInformation = resetPriceInformation,
                                onNewTimeSpanSelected = onNewTimeSpanSelected
                            )

                            Box(
                                modifier = Modifier.padding(
                                    start = AppTheme.dimensions.smallSpacing,
                                    end = AppTheme.dimensions.smallSpacing,
                                    bottom = AppTheme.dimensions.smallSpacing
                                )
                            ) {
                                CenterQuickActions(
                                    data = quickActionsCenter,
                                    onQuickActionClick = onQuickActionClick
                                )
                            }

                            Box(
                                modifier = Modifier.padding(AppTheme.dimensions.smallSpacing)
                            ) {
                                AssetAccounts(
                                    analytics = analytics,
                                    data = accounts,
                                    l1Network = asset.data.l1Network,
                                    assetTicker = asset.data.asset.networkTicker,
                                    onAccountClick = onAccountClick,
                                    onLockedAccountClick = onLockedAccountClick
                                )
                            }

                            Box(
                                modifier = Modifier.padding(AppTheme.dimensions.smallSpacing)
                            ) {
                                RecurringBuys(
                                    analytics = analytics,
                                    rBuysState = recurringBuys,
                                    assetTicker = asset.data.asset.networkTicker,
                                    onRecurringBuyUpsellClick = onRecurringBuyUpsellClick,
                                    onRecurringBuyItemClick = onRecurringBuyItemClick
                                )
                            }

                            Box(
                                modifier = Modifier.padding(AppTheme.dimensions.smallSpacing)
                            ) {
                                AssetInfo(
                                    analytics = analytics,
                                    data = assetInfo,
                                    assetTicker = asset.data.asset.networkTicker,
                                    onWebsiteClick = onWebsiteClick
                                )
                            }

                            Box(
                                modifier = Modifier.padding(AppTheme.dimensions.smallSpacing)
                            ) {
                                News(
                                    data = newsArticles
                                )
                            }
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            BottomQuickActions(
                                data = quickActionsBottom,
                                onQuickActionClick = onQuickActionClick
                            )
                        }
                    }
                }
            }
        }

        val pillAlertOffsetY by animateIntAsState(
            targetValue = if (pillAlert is CoinviewPillAlertState.None) -300 else 0,
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION
            )
        )

        var savedAlertForAnimation: CoinviewPillAlertState? by remember { mutableStateOf(null) }
        if (pillAlert != CoinviewPillAlertState.None) {
            savedAlertForAnimation = pillAlert
        }
        savedAlertForAnimation?.let {
            PillAlert(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(AppTheme.dimensions.tinySpacing)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = pillAlertOffsetY
                        )
                    },
                config = PillAlert(
                    text = TextValue.IntResValue(it.message),
                    icon = it.icon,
                    type = PillAlertType.Info
                )
            )
        }

        if (snackbarAlert != CoinviewSnackbarAlertState.None) {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                SnackbarAlert(
                    message = stringResource(snackbarAlert.message),
                    type = snackbarAlert.snackbarType
                )
            }
        }
    }
}

/**
 * For when no view is needed todo maybe moved to compose module
 */
@Composable
fun Empty() {
}

@Preview(name = "CoinviewScreen", showBackground = true)
@Composable
fun PreviewCoinviewScreen() {
    CoinviewScreen(
        analytics = previewAnalytics,

        backOnClick = {},

        asset = DataResource.Data(CoinviewAssetState(CryptoCurrency.ETHER, null)),
        onContactSupportClick = {},

        price = CoinviewPriceState.Loading,
        onChartEntryHighlighted = {},
        resetPriceInformation = {},
        onNewTimeSpanSelected = {},

        tradeable = CoinviewAssetTradeableState.Tradeable,

        watchlist = DataResource.Loading,
        onWatchlistClick = {},

        accounts = DataResource.Loading,
        onAccountClick = {},
        onLockedAccountClick = {},

        quickActionsCenter = DataResource.Loading,

        recurringBuys = DataResource.Loading,
        onRecurringBuyUpsellClick = {},
        onRecurringBuyItemClick = {},

        quickActionsBottom = DataResource.Loading,
        onQuickActionClick = {},

        assetInfo = CoinviewAssetInfoState.Loading,
        onWebsiteClick = {},

        newsArticles = CoinviewNewsState(emptyList()),

        pillAlert = CoinviewPillAlertState.None,
        snackbarAlert = CoinviewSnackbarAlertState.None
    )
}

@Preview(name = "CoinviewScreen unknown", showBackground = true)
@Composable
fun PreviewCoinviewScreen_Unknown() {
    CoinviewScreen(
        analytics = previewAnalytics,

        backOnClick = {},

        asset = DataResource.Error(Exception()),
        onContactSupportClick = {},

        price = CoinviewPriceState.Loading,
        onChartEntryHighlighted = {},
        resetPriceInformation = {},
        onNewTimeSpanSelected = {},

        tradeable = CoinviewAssetTradeableState.Tradeable,

        watchlist = DataResource.Loading,
        onWatchlistClick = {},

        accounts = DataResource.Loading,
        onAccountClick = {},
        onLockedAccountClick = {},

        quickActionsCenter = DataResource.Loading,

        recurringBuys = DataResource.Loading,
        onRecurringBuyUpsellClick = {},
        onRecurringBuyItemClick = {},

        quickActionsBottom = DataResource.Loading,
        onQuickActionClick = {},

        assetInfo = CoinviewAssetInfoState.Loading,
        onWebsiteClick = {},

        newsArticles = CoinviewNewsState(emptyList()),

        pillAlert = CoinviewPillAlertState.None,
        snackbarAlert = CoinviewSnackbarAlertState.None
    )
}
