package cz.loono.backend.api

import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.data.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class OnboardRequiredInterceptor @Autowired constructor(
    private val userRepository: UserRepository
): HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val isUserOnboarded = userRepository.existsByUid(request.getAttribute(Attributes.ATTR_UID) as String)
        if (!isUserOnboarded) {
            throw OnboardRequiredException()
        }
        return true
    }

    companion object {
        const val ONBOARD_REQUIRED_CODE = "ONBOARD_REQUIRED"
        const val ONBOARD_REQUIRED_MSG = "Onboard is required before any authenticated resources are accessible."
    }
}

class OnboardRequiredException : LoonoBackendException(
    HttpStatus.FORBIDDEN,
    OnboardRequiredInterceptor.ONBOARD_REQUIRED_CODE,
    OnboardRequiredInterceptor.ONBOARD_REQUIRED_MSG
)
