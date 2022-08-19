package piuk.blockchain.android.ui.kyc.veriffsplash

import com.blockchain.analytics.AnalyticsEvent
import com.blockchain.analytics.events.AnalyticsNames
import com.blockchain.core.kyc.domain.model.KycTier
import java.io.Serializable

sealed class VeriffAnalytics(override val event: String, override val params: Map<String, Serializable> = emptyMap()) :
    AnalyticsEvent {
    class VerifSubmissionFailed(
        tierUserIsAboutToUpgrade: KycTier,
        failureReason: String
    ) : VeriffAnalytics(
        event = AnalyticsNames.VERIFICATION_SUBMISSION_FAILED.eventName,
        params = mapOf(
            "failure_reason" to failureReason,
            "tier" to tierUserIsAboutToUpgrade.ordinal,
            "provider" to "BLOCKCHAIN"
        )
    )
}
