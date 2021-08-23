package cz.loono.backend.api

import cz.loono.backend.api.service.JwtAuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class BearerTokenAuthenticator @Autowired constructor(
    private val authService: JwtAuthService,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val authorizationHeader = request.getHeader("Authorization")
        if (authorizationHeader == null) {
            response.sendError(401, "Missing Authorization header.")
            return false
        }

        val token = parseToken(authorizationHeader)
        if (token == null) {
            response.sendError(400, "Invalid format of Bearer token.")
            return false
        }

        return when (val result = authService.verifyToken(token)) {
            is JwtAuthService.VerificationResult.Success -> {
                request.setAttribute(Attributes.ATTR_UID, result.uid)
                true
            }
            is JwtAuthService.VerificationResult.Error -> {
                response.sendError(401, result.reason)
                false
            }
        }
    }

    private fun parseToken(authHeader: String): String? {
        val tokenParts = authHeader.split(" ")
        if (tokenParts.size == 2 &&
            tokenParts[0].equals("Bearer", ignoreCase = true) &&
            tokenParts[1].isNotBlank()
        ) {
            return tokenParts[1]
        }
        return null
    }
}
