package com.dex.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.blockchain.componentlib.sheets.SheetFloatingHeader
import com.blockchain.componentlib.tablerow.custom.StackedIcon
import com.blockchain.componentlib.theme.AppTheme
import com.blockchain.componentlib.theme.BackgroundMuted
import com.blockchain.componentlib.utils.collectAsStateLifecycleAware
import com.blockchain.dex.presentation.R
import com.blockchain.koin.payloadScope
import org.koin.androidx.compose.getViewModel

@Composable
fun SelectSourceAccountBottomSheet(
    closeClicked: () -> Unit,
    viewModel: DexSourceAccountViewModel = getViewModel(scope = payloadScope)
) {
    val viewState: SourceAccountSelectionViewState by viewModel.viewState.collectAsStateLifecycleAware()

    DisposableEffect(key1 = viewModel) {
        viewModel.onIntent(SourceAccountIntent.LoadSourceAccounts)
        onDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BackgroundMuted)
    ) {
        SheetFloatingHeader(
            icon = StackedIcon.None,
            title = stringResource(id = R.string.select_token),
            onCloseClick = closeClicked
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimensions.smallSpacing)
        ) {
            DexAccountSelection(
                accounts = viewState.accounts,
                onAccountSelected = {
                    viewModel.onIntent(SourceAccountIntent.OnAccountSelected(it))
                    closeClicked()
                },
                onSearchTermUpdated = {
                    viewModel.onIntent(SourceAccountIntent.Search(it))
                }
            )
        }
    }
}
