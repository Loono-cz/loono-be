package cz.loono.backend.security

import cz.loono.backend.db.repository.ServerPropertiesRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * An interceptor that ensures an account exists
 */
@Component
class SupportedAppVersionInterceptor(
    private val serverPropertiesRepository: ServerPropertiesRepository
) : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        val appVersion = request.getHeader("app-version")
        return isSupported(appVersion)
    }

    private fun isSupported(appVersion: String): Boolean {
        val supportedVersion = serverPropertiesRepository.findAll().first().supportedAppVersion.split(".")
        val appVersionsParts = appVersion.split(".")

        for (i in 0..2) {
            if (supportedVersion[i].toInt() > appVersionsParts[i].toInt()) {
                logger.warn("Usage of an older version: $appVersion")
                return false
            }
        }

        return true
    }
}
