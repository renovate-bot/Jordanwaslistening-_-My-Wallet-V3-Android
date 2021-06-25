package piuk.blockchain.android.coincore.eth

import com.blockchain.annotations.CommonCode
import com.blockchain.featureflags.InternalFeatureFlagApi
import com.blockchain.logging.CrashLogger
import com.blockchain.preferences.CurrencyPrefs
import com.blockchain.preferences.WalletStatus
import com.blockchain.nabu.datamanagers.CustodialWalletManager
import com.blockchain.wallet.DefaultLabels
import info.blockchain.balance.AssetCatalogue
import info.blockchain.balance.AssetInfo
import info.blockchain.balance.CryptoCurrency
import info.blockchain.wallet.util.FormatsUtil
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import piuk.blockchain.android.coincore.AddressParseError
import piuk.blockchain.android.coincore.AddressParseError.Error.ETH_UNEXPECTED_CONTRACT_ADDRESS
import piuk.blockchain.android.coincore.CryptoAddress
import piuk.blockchain.android.coincore.ReceiveAddress
import piuk.blockchain.android.coincore.SingleAccountList
import piuk.blockchain.android.coincore.TxResult
import piuk.blockchain.android.coincore.impl.CryptoAssetBase
import piuk.blockchain.android.thepit.PitLinking
import piuk.blockchain.androidcore.data.ethereum.EthDataManager
import piuk.blockchain.androidcore.data.exchangerate.ExchangeRateDataManager
import piuk.blockchain.androidcore.data.exchangerate.ExchangeRateService
import piuk.blockchain.androidcore.data.fees.FeeDataManager
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.android.coincore.impl.BackendNotificationUpdater
import piuk.blockchain.android.coincore.impl.NotificationAddresses
import piuk.blockchain.android.identity.UserIdentity

internal class EthAsset(
    payloadManager: PayloadDataManager,
    private val ethDataManager: EthDataManager,
    private val feeDataManager: FeeDataManager,
    private val assetCatalogue: Lazy<AssetCatalogue>,
    custodialManager: CustodialWalletManager,
    exchangeRates: ExchangeRateDataManager,
    historicRates: ExchangeRateService,
    currencyPrefs: CurrencyPrefs,
    private val walletPrefs: WalletStatus,
    private val notificationUpdater: BackendNotificationUpdater,
    labels: DefaultLabels,
    pitLinking: PitLinking,
    crashLogger: CrashLogger,
    identity: UserIdentity,
    features: InternalFeatureFlagApi
) : CryptoAssetBase(
    payloadManager,
    exchangeRates,
    historicRates,
    currencyPrefs,
    labels,
    custodialManager,
    pitLinking,
    crashLogger,
    identity,
    features
) {
    override val asset: AssetInfo
        get() = CryptoCurrency.ETHER

    override val isCustodialOnly: Boolean = asset.isCustodialOnly
    override val multiWallet: Boolean = false

    override fun initToken(): Completable =
        ethDataManager.initEthereumWallet(labels.getDefaultNonCustodialWalletLabel())

    override fun loadNonCustodialAccounts(labels: DefaultLabels): Single<SingleAccountList> =
        Single.just(ethDataManager.getEthWallet() ?: throw Exception("No ether wallet found"))
            .map {
                EthCryptoWalletAccount(
                    payloadManager = payloadManager,
                    ethDataManager = ethDataManager,
                    fees = feeDataManager,
                    jsonAccount = it.account,
                    walletPreferences = walletPrefs,
                    exchangeRates = exchangeRates,
                    custodialWalletManager = custodialManager,
                    identity = identity,
                    assetCatalogue = assetCatalogue.value
                )
            }.doOnSuccess {
                updateBackendNotificationAddresses(it)
            }.map {
                listOf(it)
            }

    private fun updateBackendNotificationAddresses(account: EthCryptoWalletAccount) {
        val notify = NotificationAddresses(
            assetTicker = asset.ticker,
            addressList = listOf(account.address)
        )
        return notificationUpdater.updateNotificationBackend(notify)
    }

    @CommonCode("Exists in UsdtAsset and PaxAsset")
    override fun parseAddress(address: String, label: String?): Maybe<ReceiveAddress> =
        Single.just(isValidAddress(address)).flatMapMaybe { isValid ->
            if (isValid) {
                ethDataManager.isContractAddress(address).flatMapMaybe { isContract ->
                    if (isContract) {
                        throw AddressParseError(ETH_UNEXPECTED_CONTRACT_ADDRESS)
                    } else {
                        Maybe.just(EthAddress(address, label ?: address))
                    }
                }
            } else {
                Maybe.empty()
            }
        }

    override fun isValidAddress(address: String): Boolean =
        FormatsUtil.isValidEthereumAddress(address)
}

internal class EthAddress(
    override val address: String,
    override val label: String = address,
    override val onTxCompleted: (TxResult) -> Completable = { Completable.complete() }
) : CryptoAddress {
    override val asset: AssetInfo = CryptoCurrency.ETHER
}
