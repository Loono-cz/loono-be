package cz.loono.backend.api

import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.security.AccountCreatingInterceptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class AccountCreatingInterceptorTest {
    @Test
    fun `request without decoded user throws 500`() {
        val repo = mock<AccountRepository>()
        val interceptor = AccountCreatingInterceptor()
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        val ex = assertThrows<LoonoBackendException> {
            interceptor.preHandle(request, response, Any())
        }

        verifyNoInteractions(repo)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.status)
        assertNull(ex.errorCode)
        assertNull(ex.errorMessage)
    }

    @Test
    fun `request with invalid decoded user attribute throws 500`() {
        val repo = mock<AccountRepository>()
        val interceptor = AccountCreatingInterceptor()
        val request = MockHttpServletRequest().apply {
            setAttribute(Attributes.ATTR_BASIC_USER, Any())
        }
        val response = MockHttpServletResponse()

        val ex = assertThrows<LoonoBackendException> {
            interceptor.preHandle(request, response, Any())
        }

        verifyNoInteractions(repo)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.status)
        assertNull(ex.errorCode)
        assertNull(ex.errorMessage)
    }
}
