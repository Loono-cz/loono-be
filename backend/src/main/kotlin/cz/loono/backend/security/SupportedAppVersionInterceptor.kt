package cz.loono.backend.security

import cz.loono.backend.db.repository.ServerPropertiesRepository
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.apache.http.HttpStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SupportedAppVersionInterceptor(
    private val serverPropertiesRepository: ServerPropertiesRepository,
    meterRegistry: MeterRegistry
) : HandlerInterceptor {

    private val supportedVersion: Int by lazy {
        serverPropertiesRepository.findAll().first().supportedAppVersion.replace(".", "").toInt()
    }

    private val logger = LoggerFactory.getLogger(javaClass)
    private val counter: Counter

    init {
        counter = meterRegistry.counter("supported.version.counter")
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        counter.increment()
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
