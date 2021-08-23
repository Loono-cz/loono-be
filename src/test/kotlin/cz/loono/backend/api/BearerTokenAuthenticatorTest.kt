package cz.loono.backend.api

import cz.loono.backend.api.service.JwtAuthService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

internal class BearerTokenAuthenticatorTest {

    @Test
    fun `missing authorization returns 401`() {
        val authenticator = BearerTokenAuthenticator { fail("Auth service must not be called with missing auth header.") }
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        val shouldContinue = authenticator.preHandle(request, response, Any())

        assertFalse { shouldContinue }
        assertTrue { response.isCommitted }
        assertEquals(401, response.status)
        assertEquals("Missing Authorization header.", response.errorMessage)
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
        val authenticator = BearerTokenAuthenticator { fail("Auth service must not be called with invalid token format.") }
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", header)
        }
        val response = MockHttpServletResponse()

        val shouldContinue = authenticator.preHandle(request, response, Any())

        assertFalse { shouldContinue }
        assertTrue { response.isCommitted }
        assertEquals(400, response.status)
        assertEquals("Invalid format of Bearer token.", response.errorMessage)
    }

    @Test
    fun `AuthService error reason is reported back with 401`() {
        val expectedReason = "Failure reason"
        val authenticator = BearerTokenAuthenticator { JwtAuthService.VerificationResult.Error(expectedReason) }
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer token")
        }
        val response = MockHttpServletResponse()

        val shouldContinue = authenticator.preHandle(request, response, Any())

        assertFalse { shouldContinue }
        assertTrue { response.isCommitted }
        assertEquals(401, response.status)
        assertEquals(expectedReason, response.errorMessage)
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