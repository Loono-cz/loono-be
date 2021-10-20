package cz.loono.backend.api.service

import cz.loono.backend.api.UpdateStatusMessage
import cz.loono.backend.db.repository.HealthcareCategoryRepository
import cz.loono.backend.db.repository.HealthcareProviderRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class HealthcareProvidersServiceTest {

    @Autowired
    private lateinit var healthcareProviderRepository: HealthcareProviderRepository

    @Autowired
    private lateinit var healthcareCategoryRepository: HealthcareCategoryRepository

    @Test
    fun `initial providers processing`() {
        val healthcareProvidersService = HealthcareProvidersService(healthcareProviderRepository, healthcareCategoryRepository)
        assert(healthcareProviderRepository.count() == 0L)

        val msg = healthcareProvidersService.updateData()

        assert(msg == UpdateStatusMessage("Data successfully updated."))
        assert(healthcareProviderRepository.count() > 30000)
    }
}
