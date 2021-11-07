package cz.loono.backend.security

import cz.loono.backend.api.service.HealthcareProvidersService
import cz.loono.backend.db.model.ServerProperties
import cz.loono.backend.db.repository.ServerPropertiesRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class DataLoader @Autowired constructor(
    private val serverPropertiesRepository: ServerPropertiesRepository,
    private val healthcareProvidersService: HealthcareProvidersService
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        logger.info("JAVA_OPTS: ${System.getenv("JAVA_OPTS")}")
        logger.info("Max memory: ${Runtime.getRuntime().maxMemory()}")
        if (serverPropertiesRepository.findAll().isEmpty()) {
            serverPropertiesRepository.save(ServerProperties())
        }
        healthcareProvidersService.updateData()
    }
}
