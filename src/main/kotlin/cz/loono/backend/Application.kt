package cz.loono.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EntityScan(basePackages = ["cz.loono.backend.data.model"])
@EnableJpaRepositories(basePackages = ["cz.loono.backend.data.repository"])
@ComponentScan(basePackages = ["cz.loono.backend.api.actuator", "org.springframework.boot.actuate.metrics"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
