package com.blockchain.core.dynamicassets.impl

import com.blockchain.api.services.AssetDiscoveryService
import com.blockchain.api.services.DynamicAsset
import com.blockchain.api.services.DynamicAssetProducts
import com.blockchain.core.dynamicassets.CryptoAssetList
import com.blockchain.core.dynamicassets.DynamicAssetsDataManager
import com.blockchain.core.dynamicassets.FiatAssetList
import com.blockchain.core.dynamicassets.FiatInfo
import com.blockchain.preferences.CurrencyPrefs
import info.blockchain.balance.AssetCategory
import info.blockchain.balance.AssetInfo
import info.blockchain.balance.CryptoCurrency
import io.reactivex.rxjava3.core.Single

internal class DynamicAssetsDataManagerImpl(
    private val discoveryService: AssetDiscoveryService,
    private val currencyPrefs: CurrencyPrefs,
    private val cache: AssetInfoCache
) : DynamicAssetsDataManager {

    override fun availableCryptoAssets(): Single<CryptoAssetList> =
        Single.zip(
            discoveryService.getErc20Assets(),
            discoveryService.getCustodialAssets()
        ) { erc20, custodial ->
            val cryptoAssets = erc20 + custodial
            cryptoAssets.filterNot { it.isFiat }
                .toSet() // Remove dups
                .filter { it.hasSupport() }
                .map { it.toAssetInfo() }
        }.doOnSuccess { assetList ->
            val timestamp = System.currentTimeMillis()
            cache.put(assetList, timestamp)
        }

    override fun availableFiatAssets(): Single<FiatAssetList> =
        discoveryService.getFiatAssets()
            .map { list -> list.map { it.toFiatInfo() } }
}

private fun DynamicAsset.hasSupport() =
    this.products.intersect(requiredProducts).isNotEmpty()

private fun DynamicAsset.toAssetInfo(): AssetInfo =
    CryptoCurrency(
        displayTicker = displayTicker,
        networkTicker = networkTicker,
        name = assetName,
        categories = mapCategories(products),
        precisionDp = precision,
        l1chainTicker = when (parentChain) {
            AssetDiscoveryService.ETHERIUM -> CryptoCurrency.ETHER.networkTicker
            null -> null
            else -> throw IllegalStateException("Unknown l1 chain")
        },
        l2identifier = chainIdentifier,
        requiredConfirmations = minConfirmations,
        startDate = BTC_START_DATE,
        colour = mapColour(),
        logo = logoUrl ?: "" // TODO: Um?
    )

private const val BTC_START_DATE = 1282089600L

private fun mapCategories(products: Set<DynamicAssetProducts>): Set<AssetCategory> =
    products.mapNotNull {
        when (it) {
            DynamicAssetProducts.PrivateKey -> AssetCategory.NON_CUSTODIAL
            DynamicAssetProducts.CustodialWalletBalance -> AssetCategory.CUSTODIAL
            DynamicAssetProducts.InterestBalance -> AssetCategory.CUSTODIAL
            else -> null
        }
    }.toSet()

private fun DynamicAsset.mapColour(): String =
    when {
        colourLookup.containsKey(networkTicker) -> colourLookup[networkTicker] ?: "#000000"
        chainIdentifier != null -> CryptoCurrency.ETHER.colour
        else -> "#000000"
    }

private val requiredProducts = setOf(
    DynamicAssetProducts.PrivateKey,
    DynamicAssetProducts.CustodialWalletBalance,
    DynamicAssetProducts.InterestBalance
)

private val colourLookup = mapOf(
    "PAX" to "#00522C",
    "USDT" to "#26A17B",
    "WDGLD" to "#A39424",
    "AAVE" to "#2EBAC6",
    "YFI" to "#0074FA",
    "ALGO" to "#000000",
    "DOT" to "#E6007A",
    "DOGE" to "#C2A633",
    "CLOUT" to "#000000",
    "LTC" to "#BFBBBB",
    "ETC" to "#33FF99",
    "ZEN" to "#041742",
    "XTZ" to "#2C7DF7",
    "STX" to "#211F6D",
    "MOB" to "#243855",
    "THETA" to "#2AB8E6",
    "NEAR" to "#000000",
    "EOS" to "#000000",
    "OGN" to "#1A82FF",
    "ENJ" to "#624DBF",
    "COMP" to "#00D395",
    "LINK" to "#2A5ADA",
    "USDC" to "#2775CA",
    "UNI" to "#FF007A",
    "DAI" to "#F5AC37",
    "BAT" to "#FF4724"
)

private fun DynamicAsset.toFiatInfo(): FiatInfo =
    FiatInfo(
        name = assetName,
        displayTicker = displayTicker,
        networkTicker = networkTicker
    )
