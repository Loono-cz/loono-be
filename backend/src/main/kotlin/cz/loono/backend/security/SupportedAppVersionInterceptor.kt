package cz.loono.backend.security

import cz.loono.backend.db.repository.ServerPropertiesRepository
import org.apache.http.HttpStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SupportedAppVersionInterceptor(
    private val serverPropertiesRepository: ServerPropertiesRepository
) : HandlerInterceptor {

    private val supportedVersion: Int by lazy {
        serverPropertiesRepository.findAll().first().supportedAppVersion.replace(".", "").toInt()
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        val appVersion = request.getHeader("app-version")
        val supported = isSupported(appVersion)

        return if (!supported) {
            response.status = HttpStatus.SC_GONE
            false
        } else {
            true
        }
    }

    private fun isSupported(appVersion: String): Boolean {
        val appVersionsParts = appVersion.replace(".", "").toInt()

        if (supportedVersion > appVersionsParts) {
            logger.warn("Usage of an older version: $appVersion")
            return false
        }

        return true
    }
}
