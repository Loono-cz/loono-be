package cz.loono.backend.security

import org.apache.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SupportedAppVersionInterceptor() : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // force Force-update for old mobile apps
        response.status = HttpStatus.SC_GONE
        return false
    }
}
