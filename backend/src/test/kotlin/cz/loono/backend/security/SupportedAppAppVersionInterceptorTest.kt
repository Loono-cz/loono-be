package cz.loono.backend.security

import cz.loono.backend.db.model.ServerProperties
import cz.loono.backend.db.repository.ServerPropertiesRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SupportedAppAppVersionInterceptorTest {

    private val response: HttpServletResponse = mock()
    private val serverPropertiesRepository: ServerPropertiesRepository = mock()
    private val interceptor = SupportedAppVersionInterceptor(serverPropertiesRepository)

    @Test
    fun `latest App version`() {
        val request: HttpServletRequest = mock()
        whenever(request.getHeader("app-version")).thenReturn("1.2.3")
        whenever(serverPropertiesRepository.findAll()).thenReturn(
            setOf(
                ServerProperties(
                    supportedAppVersion = "1.2.3"
                )
            )
        )

        assert(interceptor.preHandle(request, response, "handler"))
    }

    @Test
    fun `invalid critical App version`() {
        val request: HttpServletRequest = mock()
        whenever(request.getHeader("app-version")).thenReturn("0.2.3")
        whenever(serverPropertiesRepository.findAll()).thenReturn(
            setOf(
                ServerProperties(
                    supportedAppVersion = "1.2.3"
                )
            )
        )

        assert(!interceptor.preHandle(request, response, "handler"))
    }

    @Test
    fun `invalid major App version`() {
        val request: HttpServletRequest = mock()
        whenever(request.getHeader("app-version")).thenReturn("1.1.3")
        whenever(serverPropertiesRepository.findAll()).thenReturn(
            setOf(
                ServerProperties(
                    supportedAppVersion = "1.2.3"
                )
            )
        )

        assert(!interceptor.preHandle(request, response, "handler"))
    }

    @Test
    fun `invalid minor App version`() {
        val request: HttpServletRequest = mock()
        whenever(request.getHeader("app-version")).thenReturn("1.2.2")
        whenever(serverPropertiesRepository.findAll()).thenReturn(
            setOf(
                ServerProperties(
                    supportedAppVersion = "1.2.3"
                )
            )
        )

        assert(!interceptor.preHandle(request, response, "handler"))
    }
}
