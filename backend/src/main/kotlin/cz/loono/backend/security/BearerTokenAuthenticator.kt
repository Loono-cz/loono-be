package cz.loono.backend.security

import cz.loono.backend.api.Attributes
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.service.JwtAuthService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class BearerTokenAuthenticator(
    private val authService: JwtAuthService,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val authorizationHeader = request.getHeader("Authorization")
            ?: throw LoonoBackendException(
                HttpStatus.UNAUTHORIZED,
                errorCode = null,
                errorMessage = "Missing Authorization header."
            )

        val token = parseToken(authorizationHeader)

        when (val result = authService.verifyToken(token)) {
            is JwtAuthService.VerificationResult.Success -> {
                request.setAttribute(Attributes.ATTR_BASIC_USER, result.basicUser)
                return true
            }
            is JwtAuthService.VerificationResult.Error -> {
                throw LoonoBackendException(
                    HttpStatus.UNAUTHORIZED,
                    errorCode = null,
                    errorMessage = result.reason
                )
            }
            JwtAuthService.VerificationResult.MissingPrimaryEmail -> {
                throw MissingPrimaryEmailException()
            }
            JwtAuthService.VerificationResult.MissingUserName -> {
                throw MissingUserNameException()
            }
        }
    }

    private fun parseToken(authHeader: String): String {
        val tokenParts = authHeader.split(" ")
        if (tokenParts.size == 2 &&
            tokenParts[0].equals("Bearer", ignoreCase = true) &&
            tokenParts[1].isNotBlank()
        ) {
            return tokenParts[1]
        }
        throw LoonoBackendException(
            HttpStatus.BAD_REQUEST,
            errorCode = null,
            errorMessage = "Invalid format of Bearer token."
        )
    }

    companion object {
        const val MISSING_PRIMARY_EMAIL_CODE = "MISSING_PRIMARY_EMAIL"
        const val MISSING_PRIMARY_EMAIL_MSG = "The 'email' property of the Firebase user is not filled in. " +
            "Loono only permits login providers with email address. (Social OAuth, Email + Password). " +
            "It is possible that you allowed another type of login, such as Phone or Anonymous. " +
            "Please update the primary email address in Firebase."

        const val MISSING_USER_NAME_CODE = "MISSING_USER_NAME"
        const val MISSING_USER_NAME_MSG = "The 'name' property of the Firebase user is not filled in. " +
            "Loono applications must ensure that the name is properly filled in before making requests. " +
            "This is especially important for Email + Password method. " +
            "Please update the user name in Firebase."
    }
}

class MissingPrimaryEmailException : LoonoBackendException(
    HttpStatus.BAD_REQUEST,
    BearerTokenAuthenticator.MISSING_PRIMARY_EMAIL_CODE,
    BearerTokenAuthenticator.MISSING_PRIMARY_EMAIL_MSG
)

class MissingUserNameException : LoonoBackendException(
    HttpStatus.BAD_REQUEST,
    BearerTokenAuthenticator.MISSING_USER_NAME_CODE,
    BearerTokenAuthenticator.MISSING_USER_NAME_MSG
)
