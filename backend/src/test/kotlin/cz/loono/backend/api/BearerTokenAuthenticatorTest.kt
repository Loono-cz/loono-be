package cz.loono.backend.api

import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.service.JwtAuthService
import cz.loono.backend.security.BearerTokenAuthenticator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.mock
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.net.URL
import javax.servlet.http.HttpServletResponse

class BearerTokenAuthenticatorTest {
    @Test
    fun `missing authorization throws 401`() {
        val response = mock<HttpServletResponse>(defaultAnswer = { fail("Shouldn't touch the response.") })
        val authenticator =
            BearerTokenAuthenticator { fail("Auth service must not be called with missing auth header.") }
        val request = MockHttpServletRequest()

        val error = assertThrows<LoonoBackendException> {
            authenticator.preHandle(request, response, Any())
        }

        assertEquals(HttpStatus.UNAUTHORIZED, error.status)
        assertNull(error.errorCode)
        assertEquals("Missing Authorization header.", error.errorMessage)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "",
            " ",
            "   ",
            "Bearer",
            "Bearer ",
            "Bearer   ",
            "Basic token",
            "Bearer t1 t2",
        ]
    )
    fun `invalid token format returns 400`(header: String) {
        val response = mock<HttpServletResponse>(defaultAnswer = { fail("Shouldn't touch the response.") })
        val authenticator =
            BearerTokenAuthenticator { fail("Auth service must not be called with invalid token format.") }
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", header)
        }

        val error = assertThrows<LoonoBackendException> {
            authenticator.preHandle(request, response, Any())
        }

        assertEquals(HttpStatus.BAD_REQUEST, error.status)
        assertNull(error.errorCode)
        assertEquals("Invalid format of Bearer token.", error.errorMessage)
    }

    @Test
    fun `AuthService error reason is reported back with 401`() {
        val response = mock<HttpServletResponse>(defaultAnswer = { fail("Shouldn't touch the response.") })
        val expectedReason = "Failure reason"
        val authenticator = BearerTokenAuthenticator { JwtAuthService.VerificationResult.Error(expectedReason) }
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer token")
        }

        val error = assertThrows<LoonoBackendException> {
            authenticator.preHandle(request, response, Any())
        }

        assertEquals(HttpStatus.UNAUTHORIZED, error.status)
        assertNull(error.errorCode)
        assertEquals(expectedReason, error.errorMessage)
    }

    @Test
    fun `valid token saves basic user data to attributes`() {
        val decodedUser = BasicUser("uid", "email@example.com", "Zilvar z chudobince", URL("https://example.com/"))
        val authenticator = BearerTokenAuthenticator {
            JwtAuthService.VerificationResult.Success(decodedUser)
        }
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer token")
        }
        val response = MockHttpServletResponse()

        val shouldContinue = authenticator.preHandle(request, response, Any())

        assertTrue { shouldContinue }
        assertFalse { response.isCommitted }
        assertEquals(decodedUser, request.getAttribute(Attributes.ATTR_BASIC_USER))
    }
}
