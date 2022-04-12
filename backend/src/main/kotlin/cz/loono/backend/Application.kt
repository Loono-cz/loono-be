package cz.loono.backend

import cz.loono.backend.security.AccountCreatingInterceptor
import cz.loono.backend.security.BearerTokenAuthenticator
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

@SpringBootApplication
@EntityScan(basePackages = ["cz.loono.backend.db.model"])
@EnableJpaRepositories(basePackages = ["cz.loono.backend.db.repository"])
@EnableTransactionManagement
@EnableScheduling
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Configuration
class Config(
    private val authenticator: BearerTokenAuthenticator,
    private val accountCreatingInterceptor: AccountCreatingInterceptor,
) : WebMvcConfigurer {

    val apiVersion = "/v1"

    val unauthenticatedEndpoints = listOf(
        "$apiVersion/api-docs",
        "/actuator/health",
        "/error",
        "$apiVersion/providers/update",
        "/favicon.ico",
        "/notification/*",
        // Temporary Auth disabled for endpoints bellow
        "$apiVersion/providers/all",
        "$apiVersion/providers/details",
        "$apiVersion/providers/lastupdate"
    )

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticator)
            .excludePathPatterns(unauthenticatedEndpoints)
            .order(0)

        registry.addInterceptor(accountCreatingInterceptor)
            .excludePathPatterns(unauthenticatedEndpoints)
            .order(1)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:static/")
    }
}
