package piuk.blockchain.android.ui.account;

import com.google.zxing.BarcodeFormat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import info.blockchain.wallet.payload.PayloadManager;
import info.blockchain.wallet.payload.data.Account;
import info.blockchain.wallet.payload.data.LegacyAddress;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import piuk.blockchain.android.R;
import piuk.blockchain.android.data.connectivity.ConnectivityStatus;
import piuk.blockchain.android.databinding.ActivityAccountsBinding;
import piuk.blockchain.android.databinding.AlertPromptTransferFundsBinding;
import piuk.blockchain.android.ui.backup.ConfirmFundsTransferDialogFragment;
import piuk.blockchain.android.ui.balance.BalanceFragment;
import piuk.blockchain.android.ui.base.BaseAuthActivity;
import piuk.blockchain.android.ui.customviews.MaterialProgressDialog;
import piuk.blockchain.android.ui.customviews.ToastCustom;
import piuk.blockchain.android.ui.zxing.CaptureActivity;
import piuk.blockchain.android.ui.zxing.Intents;
import piuk.blockchain.android.util.MonetaryUtil;
import piuk.blockchain.android.util.PermissionUtil;
import piuk.blockchain.android.util.PrefsUtil;
import piuk.blockchain.android.util.ViewUtils;
import piuk.blockchain.android.util.annotations.Thunk;

import static piuk.blockchain.android.ui.account.AccountViewModel.KEY_WARN_TRANSFER_ALL;

public class AccountActivity extends BaseAuthActivity implements AccountViewModel.DataListener {

    private static final int IMPORT_PRIVATE_REQUEST_CODE = 2006;
    private static final int EDIT_ACTIVITY_REQUEST_CODE = 2007;
    private static final int ADDRESS_LABEL_MAX_LENGTH = 17;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (BalanceFragment.ACTION_INTENT.equals(intent.getAction())) {
                onUpdateAccountsList();
                // Check if we need to hide/show the transfer funds icon in the Toolbar
                viewModel.checkTransferableLegacyFunds(false, false);
            }
        }
    };
    private ArrayList<AccountItem> accountsAndImportedList;
    private AccountAdapter accountsAdapter;
    private List<LegacyAddress> legacy;
    private MenuItem transferFundsMenuItem;
    private PrefsUtil prefsUtil;
    private MonetaryUtil monetaryUtil;
    @Thunk MaterialProgressDialog progress;
    @Thunk PayloadManager payloadManager;
    @Thunk AccountViewModel viewModel;
    @Thunk ActivityAccountsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefsUtil = new PrefsUtil(this);
        payloadManager = PayloadManager.getInstance();
        monetaryUtil = new MonetaryUtil(prefsUtil.getValue(PrefsUtil.KEY_BTC_UNITS, MonetaryUtil.UNIT_BTC));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_accounts);

        viewModel = new AccountViewModel(this);

        setupToolbar(binding.toolbarContainer.toolbarGeneral, R.string.addresses);

        binding.accountsList.setLayoutManager(new LinearLayoutManager(this));
        binding.accountsList.setHasFixedSize(true);
        accountsAndImportedList = new ArrayList<>();

        onUpdateAccountsList();
    }

    @Thunk
    void onRowClick(int position) {
        Intent intent = new Intent(this, AccountEditActivity.class);
        if (position >= payloadManager.getPayload().getHdWallets().get(0).getAccounts().size()) {
            intent.putExtra("address_index", position - payloadManager.getPayload().getHdWallets().get(0).getAccounts().size());
        } else {
            intent.putExtra("account_index", position);
        }
        startActivityForResult(intent, EDIT_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_account, menu);
        transferFundsMenuItem = menu.findItem(R.id.action_transfer_funds);
        viewModel.checkTransferableLegacyFunds(true, true);//Auto popup

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_transfer_funds:
                showProgressDialog(R.string.please_wait);
                viewModel.checkTransferableLegacyFunds(false, true);//Not auto popup
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void startScanForResult() {
        Intent intent = new Intent(AccountActivity.this, CaptureActivity.class);
        intent.putExtra(Intents.Scan.FORMATS, EnumSet.allOf(BarcodeFormat.class));
        intent.putExtra(Intents.Scan.MODE, Intents.Scan.QR_CODE_MODE);
        startActivityForResult(intent, IMPORT_PRIVATE_REQUEST_CODE);
    }

    @Thunk
    void importAddress() {
        if (ContextCompat.checkSelfPermission(AccountActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            PermissionUtil.requestCameraPermissionFromActivity(binding.mainLayout, AccountActivity.this);
        } else {
            onScanButtonClicked();
        }
    }

    private void onScanButtonClicked() {
        new SecondPasswordHandler(this).validate(new SecondPasswordHandler.ResultListener() {
            @Override
            public void onNoSecondPassword() {
                viewModel.onScanButtonClicked();
            }

            @Override
            public void onSecondPasswordValidated(String validateSecondPassword) {
                viewModel.setDoubleEncryptionPassword(validateSecondPassword);
                viewModel.onScanButtonClicked();
            }
        });
    }

    @Thunk
    void createNewAccount() {
        new SecondPasswordHandler(this).validate(new SecondPasswordHandler.ResultListener() {
            @Override
            public void onNoSecondPassword() {
                promptForAccountLabel();
            }

            @Override
            public void onSecondPasswordValidated(String validateSecondPassword) {
                viewModel.setDoubleEncryptionPassword(validateSecondPassword);
                promptForAccountLabel();
            }
        });
    }

    @Thunk
    void promptForAccountLabel() {
        AppCompatEditText editText = getAddressLabelEditText();

        new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(R.string.label)
                .setMessage(R.string.assign_display_name)
                .setView(ViewUtils.getAlertDialogPaddedView(this, editText))
                .setCancelable(false)
                .setPositiveButton(R.string.save_name, (dialog, whichButton) -> {
                    if (!editText.getText().toString().trim().isEmpty()) {
                        addAccount(editText.getText().toString().trim());
                    } else {
                        ToastCustom.makeText(AccountActivity.this, getResources().getString(R.string.label_cant_be_empty), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void addAccount(final String accountLabel) {
        if (!ConnectivityStatus.hasConnectivity(AccountActivity.this)) {
            ToastCustom.makeText(AccountActivity.this, getString(R.string.check_connectivity_exit), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
        } else {
            viewModel.createNewAccount(accountLabel);
        }
    }

    @Override
    public void onUpdateAccountsList() {

//        accountsAndImportedList is linked to AccountAdapter - do not reconstruct or loose reference otherwise notifyDataSetChanged won't work
        accountsAndImportedList.clear();
        int correctedPosition = 0;
        List<Account> accounts = payloadManager.getPayload().getHdWallets().get(0).getAccounts();
        List<Account> accountClone = new ArrayList<>(accounts.size());
        accountClone.addAll(accounts);

        if (accountClone.get(accountClone.size() - 1) instanceof ConsolidatedAccount) {
            accountClone.remove(accountClone.size() - 1);
        }

        // Create New Wallet button at top position
        accountsAndImportedList.add(new AccountItem(AccountItem.TYPE_CREATE_NEW_WALLET_BUTTON));

        int defaultIndex = payloadManager.getPayload().getHdWallets().get(0).getDefaultAccountIdx();
        Account defaultAccount = payloadManager.getPayload().getHdWallets().get(0).getAccounts().get(defaultIndex);

        for (int i = 0; i < accountClone.size(); i++) {
            String label = accountClone.get(i).getLabel();
            String balance = getAccountBalance(i);

            if (label != null && label.length() > ADDRESS_LABEL_MAX_LENGTH) {
                label = label.substring(0, ADDRESS_LABEL_MAX_LENGTH) + "...";
            }
            if (label == null || label.isEmpty()) label = "";

            accountsAndImportedList.add(new AccountItem(correctedPosition,
                    label,
                    null,
                    balance,
                    accountClone.get(i).isArchived(),
                    false,
                    defaultAccount.getXpub().equals(accountClone.get(i).getXpub()),
                    AccountItem.TYPE_ACCOUNT));
            correctedPosition++;
        }

        // Import Address button at first position after wallets
        accountsAndImportedList.add(new AccountItem(AccountItem.TYPE_IMPORT_ADDRESS_BUTTON));

        ConsolidatedAccount consolidatedAccount = null;
        if (!payloadManager.getPayload().getLegacyAddressList().isEmpty()) {

            consolidatedAccount = new ConsolidatedAccount(getString(R.string.imported_addresses),
                    payloadManager.getPayload().getLegacyAddressList(),
                    payloadManager.getImportedAddressesBalance().longValue());
        }

        if (consolidatedAccount != null) {
            legacy = consolidatedAccount.getLegacyAddresses();
            for (int j = 0; j < legacy.size(); j++) {

                String label = legacy.get(j).getLabel();
                String address = legacy.get(j).getAddress();
                String balance = getAddressBalance(j);

                if (label != null && label.length() > ADDRESS_LABEL_MAX_LENGTH)
                    label = label.substring(0, ADDRESS_LABEL_MAX_LENGTH) + "...";
                if (label == null || label.isEmpty()) label = "";
                if (address == null || address.isEmpty()) address = "";

                accountsAndImportedList.add(new AccountItem(correctedPosition,
                        label,
                        address,
                        balance,
                        legacy.get(j).getTag() == LegacyAddress.ARCHIVED_ADDRESS,
                        legacy.get(j).isWatchOnly(),
                        false,
                        AccountItem.TYPE_ACCOUNT));
                correctedPosition++;
            }
        }

        if (accountsAdapter == null) {
            accountsAdapter = new AccountAdapter(accountsAndImportedList);
            accountsAdapter.setAccountHeaderListener(new AccountAdapter.AccountHeadersListener() {
                @Override
                public void onCreateNewClicked() {
                    createNewAccount();
                }

                @Override
                public void onImportAddressClicked() {
                    importAddress();
                }

                @Override
                public void onAccountClicked(int correctedPosition) {
                    onRowClick(correctedPosition);
                }
            });

            binding.accountsList.setAdapter(accountsAdapter);
        } else {
            // Notify adapter of items changes
            accountsAdapter.notifyDataSetChanged();
        }
    }

    private String getAccountBalance(int index) {
        String address = payloadManager.getXpubFromAccountIndex(index);
        BigInteger addressBalance = payloadManager.getAddressBalance(address);
        // Archived addresses/xPubs aren't parsed, so balance will be null
        Long amount = addressBalance != null ? addressBalance.longValue() : 0L;

        String unit = (String) monetaryUtil.getBTCUnits()[prefsUtil.getValue(PrefsUtil.KEY_BTC_UNITS, MonetaryUtil.UNIT_BTC)];

        return monetaryUtil.getDisplayAmount(amount) + " " + unit;
    }

    private String getAddressBalance(int index) {
        String address = legacy.get(index).getAddress();
        Long amount = payloadManager.getAddressBalance(address).longValue();
        String unit = (String) monetaryUtil.getBTCUnits()[prefsUtil.getValue(PrefsUtil.KEY_BTC_UNITS, MonetaryUtil.UNIT_BTC)];

        return monetaryUtil.getDisplayAmount(amount) + " " + unit;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BalanceFragment.ACTION_INTENT);
        LocalBroadcastManager.getInstance(AccountActivity.this).registerReceiver(receiver, filter);
        onUpdateAccountsList();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(AccountActivity.this).unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMPORT_PRIVATE_REQUEST_CODE
                && data != null && data.getStringExtra(CaptureActivity.SCAN_RESULT) != null) {

            String strResult = data.getStringExtra(CaptureActivity.SCAN_RESULT);
            viewModel.onAddressScanned(strResult);
        } else if (resultCode == Activity.RESULT_OK && requestCode == EDIT_ACTIVITY_REQUEST_CODE) {
            onUpdateAccountsList();
        }
    }

    @Override
    public void showBip38PasswordDialog(String data) {
        AppCompatEditText password = new AppCompatEditText(this);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(R.string.app_name)
                .setMessage(R.string.bip38_password_entry)
                .setView(ViewUtils.getAlertDialogPaddedView(this, password))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, whichButton) ->
                        viewModel.importBip38Address(data, password.getText().toString()))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void showWatchOnlyWarningDialog(String address) {
        new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(R.string.warning)
                .setCancelable(false)
                .setMessage(getString(R.string.watch_only_import_warning))
                .setPositiveButton(R.string.dialog_continue, (dialog, whichButton) -> viewModel.confirmImportWatchOnly(address))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void showRenameImportedAddressDialog(LegacyAddress address) {
        AppCompatEditText editText = getAddressLabelEditText();

        new AlertDialog.Builder(AccountActivity.this, R.style.AlertDialogStyle)
                .setTitle(R.string.app_name)
                .setMessage(R.string.label_address)
                .setView(ViewUtils.getAlertDialogPaddedView(this, editText))
                .setCancelable(false)
                .setPositiveButton(R.string.save_name, (dialog, whichButton) -> {
                    String label = editText.getText().toString();
                    if (!label.trim().isEmpty()) {
                        address.setLabel(label);
                    } else {
                        address.setLabel(address.getAddress());
                    }

                    remoteSaveNewAddress(address);
                })
                .setNegativeButton(R.string.polite_no, (dialog, whichButton) -> {
                    address.setLabel(address.getAddress());
                    remoteSaveNewAddress(address);

                }).show();
    }

    @Override
    public void showToast(@StringRes int message, @ToastCustom.ToastType String toastType) {
        ToastCustom.makeText(this, getString(message), ToastCustom.LENGTH_SHORT, toastType);
    }

    @Override
    public void broadcastIntent(Intent intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void remoteSaveNewAddress(final LegacyAddress legacy) {
        if (!ConnectivityStatus.hasConnectivity(AccountActivity.this)) {
            ToastCustom.makeText(AccountActivity.this, getString(R.string.check_connectivity_exit), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
        } else {
            viewModel.updateLegacyAddress(legacy);
        }
    }

    @Override
    public void onShowTransferableLegacyFundsWarning(boolean isAutoPopup) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        AlertPromptTransferFundsBinding dialogBinding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.alert_prompt_transfer_funds, null, false);
        dialogBuilder.setView(dialogBinding.getRoot());

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        if (!isAutoPopup) {
            dialogBinding.confirmDontAskAgain.setVisibility(View.GONE);
        }

        dialogBinding.confirmCancel.setOnClickListener(v -> {
            if (dialogBinding.confirmDontAskAgain.isChecked())
                prefsUtil.setValue(KEY_WARN_TRANSFER_ALL, false);
            alertDialog.dismiss();
        });

        dialogBinding.confirmSend.setOnClickListener(v -> {
            if (dialogBinding.confirmDontAskAgain.isChecked())
                prefsUtil.setValue(KEY_WARN_TRANSFER_ALL, false);
            transferSpendableFunds();
            alertDialog.dismiss();
        });

        if (!isFinishing()) {
            alertDialog.show();

            // This corrects the layout size after view drawn
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            if (alertDialog.getWindow() != null) {
                lp.copyFrom(alertDialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                alertDialog.getWindow().setAttributes(lp);
            }
        }
    }

    private void transferSpendableFunds() {
        ConfirmFundsTransferDialogFragment fragment = ConfirmFundsTransferDialogFragment.newInstance();
        fragment.show(getSupportFragmentManager(), ConfirmFundsTransferDialogFragment.TAG);
    }

    @Override
    public void onSetTransferLegacyFundsMenuItemVisible(boolean visible) {
        transferFundsMenuItem.setVisible(visible);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtil.PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onScanButtonClicked();
            } else {
                // Permission request was denied.
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @NonNull
    private AppCompatEditText getAddressLabelEditText() {
        AppCompatEditText editText = new AppCompatEditText(this);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(ADDRESS_LABEL_MAX_LENGTH)});
        return editText;
    }

    @Override
    public void showProgressDialog(@StringRes int message) {
        dismissProgressDialog();
        if (!isFinishing()) {
            progress = new MaterialProgressDialog(this);
            progress.setMessage(message);
            progress.setCancelable(false);
            progress.show();
        }
    }

    @Override
    public void dismissProgressDialog() {
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
            progress = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
        viewModel.destroy();
    }
}
