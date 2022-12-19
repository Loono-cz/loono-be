package cz.loono.backend

import cz.loono.backend.metrics.LoonoMXBean
import cz.loono.backend.security.AccountCreatingInterceptor
import cz.loono.backend.security.BearerTokenAuthenticator
import cz.loono.backend.security.SupportedAppVersionInterceptor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.lang.management.ManagementFactory
import java.rmi.registry.LocateRegistry
import javax.management.MBeanServer
import javax.management.ObjectName
import javax.management.remote.JMXConnectorServerFactory
import javax.management.remote.JMXServiceURL

@SpringBootApplication
@EntityScan(basePackages = ["cz.loono.backend.db.model"])
@EnableJpaRepositories(basePackages = ["cz.loono.backend.db.repository"])
@EnableTransactionManagement
@EnableScheduling
class Application

fun main(args: Array<String>) {
    setupMBeans()
    runApplication<Application>(*args)
}

@Configuration
class Config(
    private val supportedAppVersionInterceptor: SupportedAppVersionInterceptor,
    private val authenticator: BearerTokenAuthenticator,
    private val accountCreatingInterceptor: AccountCreatingInterceptor,
) : WebMvcConfigurer {

    val apiVersion = "/v1"

    val swaggerEndpoints = listOf(
        "/v3/api-docs",
        "/v3/api-docs/**",
        "/swagger-ui.html",
        "/swagger-ui/**",
    )

    val unauthenticatedEndpoints = listOf(
        "$apiVersion/api-docs",
        "/actuator/health",
        "/error",
        "$apiVersion/providers/update",
        "$apiVersion/notify",
        "/favicon.ico",
        "/notification/*",
        // Temporary Auth disabled for endpoints bellow
        "$apiVersion/providers/all",
        "$apiVersion/providers/details",
        "$apiVersion/providers/lastupdate",
        "$apiVersion/feedback",
        "$apiVersion/testCall",
        "$apiVersion/testEmail",
    )

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(supportedAppVersionInterceptor)
            .excludePathPatterns(swaggerEndpoints)
            .order(0)

        registry.addInterceptor(authenticator)
            .excludePathPatterns(unauthenticatedEndpoints)
            .excludePathPatterns(swaggerEndpoints)
            .order(1)

        registry.addInterceptor(accountCreatingInterceptor)
            .excludePathPatterns(unauthenticatedEndpoints)
            .excludePathPatterns(swaggerEndpoints)
            .order(2)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:static/")
    }
}
fun setupMBeans() {
    LocateRegistry.createRegistry(49185)
    val mBeanServer: MBeanServer = ManagementFactory.getPlatformMBeanServer()
    val url = JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:49185/jmxrmi")
    val svr = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mBeanServer)
    val objName = ObjectName("cz.loono.backend:type=basic,name=LoonoMXBean")
    val loonoMBean = LoonoMXBean()
    mBeanServer.registerMBean(loonoMBean, objName)
    svr.start()
}
