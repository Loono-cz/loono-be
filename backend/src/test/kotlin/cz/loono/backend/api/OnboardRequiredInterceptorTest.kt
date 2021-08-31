package cz.loono.backend.api

import cz.loono.backend.data.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

internal class OnboardRequiredInterceptorTest {

    @Test
    fun `missing onboard throws Onboard Required`() {
        val repo = mock<UserRepository>()
        whenever(repo.existsByUid(any())).thenReturn(false)
        val handler = OnboardRequiredInterceptor(repo)
        val request = MockHttpServletRequest().apply {
            setAttribute(Attributes.ATTR_UID, "uid")
        }
        assertThrows<OnboardRequiredException> {
            handler.preHandle(request, MockHttpServletResponse(), Any())
        }
    }

    @Test
    fun `onboarded user lets request pass`() {
        val repo = mock<UserRepository>()
        whenever(repo.existsByUid(any())).thenReturn(true)
        val handler = OnboardRequiredInterceptor(repo)
        val request = MockHttpServletRequest().apply {
            setAttribute(Attributes.ATTR_UID, "uid")
        }
        val response = MockHttpServletResponse()

        val shouldContinue = handler.preHandle(request, response, Any())

        assertTrue(shouldContinue)
        assertFalse(response.isCommitted)
        assertNull(response.errorMessage)
    }
}
