package cz.loono.backend.api.service

import cz.loono.backend.api.dto.HealthcareProviderDetailsDto
import cz.loono.backend.api.dto.HealthcareProviderIdDto
import cz.loono.backend.api.dto.UpdateStatusMessageDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.db.repository.HealthcareCategoryRepository
import cz.loono.backend.db.repository.HealthcareProviderRepository
import cz.loono.backend.db.repository.ServerPropertiesRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDate

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class HealthcareProvidersServiceTest {

    @Autowired
    private lateinit var healthcareProviderRepository: HealthcareProviderRepository

    @Autowired
    private lateinit var healthcareCategoryRepository: HealthcareCategoryRepository

    @Autowired
    private lateinit var serverPropertiesRepository: ServerPropertiesRepository

    private lateinit var healthcareProvidersService: HealthcareProvidersService

    fun `init data`() {
        healthcareProvidersService =
            HealthcareProvidersService(
                healthcareProviderRepository,
                healthcareCategoryRepository,
                serverPropertiesRepository
            )

        val msg = healthcareProvidersService.updateData()

        assert(msg == UpdateStatusMessageDto("Data successfully updated."))
        assert(healthcareProviderRepository.count() > 30000)
    }

    @Test
    fun `providing zip file with providers json`() {
        `init data`()

        val response = String(healthcareProvidersService.getAllSimpleData())

        assert(response.contains("providers.json"))
    }

    @Test
    fun `get provider detail`() {
        `init data`()
        val response = healthcareProvidersService.getHealthcareProviderDetail(
            HealthcareProviderIdDto(
                locationId = 239440,
                institutionId = 161061
            )
        )

        assert(
            response == HealthcareProviderDetailsDto(
                locationId = 239440,
                institutionId = 161061,
                title = "Unimedix Plus, s.r.o.",
                institutionType = "Samostatná ordinace lékaře specialisty",
                street = "Valovská",
                houseNumber = "869",
                city = "Podbořany",
                postalCode = "44101",
                phoneNumber = "+420415237167",
                fax = "",
                email = "recepce@unimedix.cz",
                website = "",
                ico = "01951939",
                category = listOf("Ortopedie"),
                specialization = "ortopedie a traumatologie pohybového ústrojí",
                careForm = "specializovaná ambulantní péče",
                careType = "",
                substitute = "Filip Veselý",
                lat = "50.225467144083",
                lng = "13.415804369252"
            )
        )
    }

    @Test
    fun `no exists provider`() {
        `init data`()

        assertThrows<LoonoBackendException> {
            healthcareProvidersService.getHealthcareProviderDetail(
                HealthcareProviderIdDto(
                    locationId = 239440,
                    institutionId = 161062
                )
            )
        }
    }

    @Test
    fun `last update`() {
        `init data`()
        val today = LocalDate.now()
        val lastUpdate = "${today.year}-${today.monthValue}"

        assert(lastUpdate == healthcareProvidersService.lastUpdate)
    }
}
