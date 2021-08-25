package cz.loono.backend.api

import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.service.JwtAuthService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.mock
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import javax.servlet.http.HttpServletResponse

internal class BearerTokenAuthenticatorTest {

    @Test
    fun `missing authorization throws 401`() {
        val response = mock<HttpServletResponse>(defaultAnswer = { fail("Shouldn't touch the response.") })
        val authenticator = BearerTokenAuthenticator { fail("Auth service must not be called with missing auth header.") }
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
        val authenticator = BearerTokenAuthenticator { fail("Auth service must not be called with invalid token format.") }
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
    fun `valid token is saved to attributes`() {
        val decodedUid = "decodedUid"
        val authenticator = BearerTokenAuthenticator { JwtAuthService.VerificationResult.Success(decodedUid) }
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer token")
        }
        val response = MockHttpServletResponse()

        val shouldContinue = authenticator.preHandle(request, response, Any())

        assertTrue { shouldContinue }
        assertFalse { response.isCommitted }
        assertEquals(decodedUid, request.getAttribute(Attributes.ATTR_UID))
    }

}