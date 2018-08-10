package com.blockchain.koin.modules

import info.blockchain.wallet.ApiCode
import info.blockchain.wallet.BlockchainFramework
import info.blockchain.wallet.api.FeeApi
import info.blockchain.wallet.api.WalletApi
import info.blockchain.wallet.api.WalletExplorerEndpoints
import info.blockchain.wallet.ethereum.EthAccountApi
import info.blockchain.wallet.payment.Payment
import info.blockchain.wallet.prices.PriceApi
import info.blockchain.wallet.prices.PriceEndpoints
import info.blockchain.wallet.settings.SettingsManager
import info.blockchain.wallet.shapeshift.ShapeShiftApi
import org.koin.dsl.module.applicationContext
import piuk.blockchain.android.data.fingerprint.FingerprintAuth
import piuk.blockchain.android.data.fingerprint.FingerprintAuthImpl
import retrofit2.Retrofit

val serviceModule = applicationContext {

    bean { SettingsManager(get()) }

    bean { get<Retrofit>("explorer").create(WalletExplorerEndpoints::class.java) }

    bean { get<Retrofit>("api").create(PriceEndpoints::class.java) }

    factory { WalletApi(get()) }

    factory { Payment() }

    factory { FeeApi() }

    factory {
        object : ApiCode {
            override val apiCode: String
                get() = BlockchainFramework.getApiCode()
        } as ApiCode
    }

    factory { PriceApi(get(), get()) }

    factory { ShapeShiftApi(get()) }

    factory { FingerprintAuthImpl() as FingerprintAuth }

    factory { EthAccountApi() }
}
