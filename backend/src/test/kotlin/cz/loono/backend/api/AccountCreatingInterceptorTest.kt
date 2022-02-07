package cz.loono.backend.api

import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.service.AccountService
import cz.loono.backend.api.service.FirebaseAuthService
import cz.loono.backend.createBasicUser
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.security.AccountCreatingInterceptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class AccountCreatingInterceptorTest(
    private val realRepo: AccountRepository
) {

    private val firebaseAuthService: FirebaseAuthService = mock()

    @Test
    fun `user is created if not exists`() {
        val interceptor = AccountCreatingInterceptor(AccountService(realRepo, firebaseAuthService))
        val request = MockHttpServletRequest().apply {
            setAttribute(Attributes.ATTR_BASIC_USER, createBasicUser())
        }
        val response = MockHttpServletResponse()
        // Sanity precondition check!
        assertFalse(realRepo.existsByUid("uid"))

        val shouldContinue = interceptor.preHandle(request, response, Any())

        assertTrue(shouldContinue)
        assertTrue(realRepo.existsByUid("uid"))
    }

    @Test
    fun `request without decoded user throws 500`() {
        val repo = mock<AccountRepository>()
        val interceptor = AccountCreatingInterceptor(AccountService(repo, firebaseAuthService))
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
        val interceptor = AccountCreatingInterceptor(AccountService(repo, firebaseAuthService))
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

    @Test
    fun `repository is not touched if user already exists`() {
        val repo = mock<AccountRepository>()
        whenever(repo.existsByUid("uid")).thenReturn(true)

        val interceptor = AccountCreatingInterceptor(AccountService(repo, firebaseAuthService))
        val request = MockHttpServletRequest().apply {
            setAttribute(Attributes.ATTR_BASIC_USER, createBasicUser())
        }
        val response = MockHttpServletResponse()

        val shouldContinue = interceptor.preHandle(request, response, Any())

        assertTrue(shouldContinue)
        verify(repo).existsByUid("uid")
        verifyNoMoreInteractions(repo)
    }
}
