package cz.loono.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EntityScan(basePackages = ["cz.loono.backend.data.model"])
@EnableJpaRepositories(basePackages = ["cz.loono.backend.data.repository"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
