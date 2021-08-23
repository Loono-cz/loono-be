package cz.loono.backend

import cz.loono.backend.api.BearerTokenAuthenticator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@SpringBootApplication
@EntityScan(basePackages = ["cz.loono.backend.data.model"])
@EnableJpaRepositories(basePackages = ["cz.loono.backend.data.repository"])
@EnableTransactionManagement
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Configuration
class Config(private val authenticator: BearerTokenAuthenticator) : WebMvcConfigurer {
    val publicEndpoints = listOf(
        "/v3/api-docs",
    )

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticator).excludePathPatterns(publicEndpoints)
    }

}
