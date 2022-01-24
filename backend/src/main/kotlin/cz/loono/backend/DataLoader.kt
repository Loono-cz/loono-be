package cz.loono.backend

import cz.loono.backend.api.service.HealthcareProvidersService
import cz.loono.backend.db.model.ServerProperties
import cz.loono.backend.db.repository.ServerPropertiesRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DataLoader(
    private val serverPropertiesRepository: ServerPropertiesRepository,
    private val healthcareProvidersService: HealthcareProvidersService
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        serverPropsSetup()
        healthcareProvidersService.updateData()
    }

    @Transactional
    fun serverPropsSetup() {
        if (serverPropertiesRepository.findAll().isEmpty()) {
            serverPropertiesRepository.save(ServerProperties())
        }
    }
}
