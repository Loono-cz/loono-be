package cz.loono.backend.api.service

import cz.loono.backend.api.UpdateStatusMessage
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.data.HealthcareCSVParser
import cz.loono.backend.data.constants.CategoryValues
import cz.loono.backend.data.constants.Constants.OPEN_DATA_URL
import cz.loono.backend.db.model.HealthcareCategory
import cz.loono.backend.db.repository.HealthcareCategoryRepository
import cz.loono.backend.db.repository.HealthcareProviderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URL

@Service
class HealthcareProvidersService @Autowired constructor(
    private val healthcareProviderRepository: HealthcareProviderRepository,
    private val healthcareCategoryRepository: HealthcareCategoryRepository
) {

    @Transactional(rollbackFor = [Exception::class])
    fun updateData(): UpdateStatusMessage {
        val input = URL(OPEN_DATA_URL).openStream()
        val providers = HealthcareCSVParser().parse(input)
        if (providers.isNotEmpty()) {
            val categoryValues = CategoryValues.values().map { HealthcareCategory(value = it.value) }
            healthcareCategoryRepository.saveAll(categoryValues)
            healthcareProviderRepository.saveAll(providers)
        } else {
            throw LoonoBackendException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                errorCode = HttpStatus.UNPROCESSABLE_ENTITY.value().toString(),
                errorMessage = "Data update failed."
            )
        }
        return UpdateStatusMessage("Data successfully updated.")
    }
}
