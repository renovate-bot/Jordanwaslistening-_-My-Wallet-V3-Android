package piuk.blockchain.android.ui.linkbank.domain.yapily

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import piuk.blockchain.android.ui.linkbank.domain.yapily.service.SafeConnectService
import piuk.blockchain.android.ui.linkbank.domain.yapily.usecase.GetSafeConnectTosLinkUseCase
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class GetSafeConnectTosLinkUseCaseTest {
    private val service = mockk<SafeConnectService>()
    private val useCase = GetSafeConnectTosLinkUseCase(service)

    private val tosLink = "TosLink"

    @Before
    fun setUp() {
        coEvery { service.getTosLink() } returns tosLink
    }

    @Test
    fun `GIVEN service returns value, WHEN useCase is called, THEN value should be returned`() = runTest {
        val result = useCase()

        assertEquals(tosLink, result)
    }
}