package com.blockchain.koin

import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named

val applicationScope = StringQualifier("applicationScope")
val featureFlagsPrefs = StringQualifier("FeatureFlagsPrefs")
val accountUnificationFeatureFlag = StringQualifier("ff_sso_unification")
val googlePayFeatureFlag = StringQualifier("ff_gpay")
val metadataMigrationFeatureFlag = StringQualifier("ff_metadata_migration")
val superAppFeatureFlag = StringQualifier("ff_super_app")
val intercomChatFeatureFlag = StringQualifier("ff_intercom_chat")
val blockchainCardFeatureFlag = StringQualifier("ff_blockchain_card")
val deeplinkingFeatureFlag = StringQualifier("ff_deeplinking")
val coinWebSocketFeatureFlag = StringQualifier("ff_coinWebSocketFeatureFlag")
val sendToDomainsAnnouncementFeatureFlag = StringQualifier("ff_send_domain_announcement")
val notificationPreferencesFeatureFlag = StringQualifier("ff_notification_preferences_rework")
val ethLayerTwoFeatureFlag = StringQualifier("ff_eth_layer_two")
val appMaintenanceFeatureFlag = StringQualifier("ff_app_maintenance")
val appRatingFeatureFlag = StringQualifier("ff_app_rating")
val backupPhraseFeatureFlag = StringQualifier("ff_backup_phrase")
val referralsFeatureFlag = StringQualifier("ff_referrals")
val stxForAllFeatureFlag = StringQualifier("ff_stx_all")
val plaidFeatureFlag = StringQualifier("ff_plaid")
val bindFeatureFlag = StringQualifier("ff_bind")
val buyRefreshQuoteFeatureFlag = StringQualifier("ff_buy_refresh_quote")
val quickFillButtonsFeatureFlag = StringQualifier("ff_quick_fill_buttons")
val cardRejectionCheckFeatureFlag = StringQualifier("ff_card_rejection_check")
val authInterceptorFeatureFlag = StringQualifier("ff_auth_interceptor")
val cowboysPromoFeatureFlag = StringQualifier("ff_cowboys_promo")
val cardPaymentAsyncFeatureFlag = StringQualifier("ff_card_payment_async")
val rbFrequencyFeatureFlag = StringQualifier("ff_rb_frequency")
val nabu = StringQualifier("nabu")
val status = StringQualifier("status")
val authOkHttpClient = StringQualifier("authOkHttpClient")
val kotlinApiRetrofit = StringQualifier("kotlin-api")
val explorerRetrofit = StringQualifier("explorer")
val everypayRetrofit = StringQualifier("everypay")
val apiRetrofit = StringQualifier("api")
val kotlinXApiRetrofit = StringQualifier("kotlinx-api")
val serializerExplorerRetrofit = StringQualifier("serializer_explorer")
val gbp = StringQualifier("GBP")
val usd = StringQualifier("USD")
val eur = StringQualifier("EUR")
val ars = StringQualifier("ARS")
val priorityFee = StringQualifier("Priority")
val regularFee = StringQualifier("Regular")
val bigDecimal = StringQualifier("BigDecimal")
val kotlinJsonConverterFactory = StringQualifier("KotlinJsonConverterFactory")
val kotlinJsonAssetTicker = StringQualifier("KotlinJsonAssetTicker")
val bigInteger = StringQualifier("BigInteger")
val interestLimits = StringQualifier("InterestLimits")
val kyc = StringQualifier("kyc")
val uniqueId = StringQualifier("unique_id")
val uniqueUserAnalytics = StringQualifier("unique_user_analytics")
val userAnalytics = StringQualifier("user_analytics")
val walletAnalytics = StringQualifier("wallet_analytics")
val embraceLogger = StringQualifier("embrace_logger")
val payloadScopeQualifier = named("Payload")
val ioDispatcher = named("io_dispatcher")
