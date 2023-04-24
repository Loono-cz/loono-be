package cz.loono.backend.api.service

import cz.loono.backend.api.dto.HealthcareProviderDetailDto
import cz.loono.backend.api.dto.HealthcareProviderIdDto
import cz.loono.backend.api.dto.HealthcareProviderIdListDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.db.model.HealthcareProvider
import cz.loono.backend.db.repository.HealthcareProviderRepository
import cz.loono.backend.db.repository.ServerPropertiesRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Path
import java.time.LocalDate
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.fileSize

@SpringBootTest(properties = ["spring.profiles.active=test"])
@Transactional
class HealthcareProvidersServiceTest(
    private val healthcareProviderRepository: HealthcareProviderRepository,
    private val serverPropertiesRepository: ServerPropertiesRepository
) {

    private lateinit var healthcareProvidersService: HealthcareProvidersService

    @BeforeEach
    fun init() {
        healthcareProvidersService =
            HealthcareProvidersService(
                healthcareProviderRepository,
                serverPropertiesRepository
            )
    }

    // Working but long test
    @Test
    @Disabled
    fun `complete save`() {
        val msg = healthcareProvidersService.updateData()

        assert(healthcareProviderRepository.count() > 30000)
        assert(msg.message == "Data successfully updated.")
    }

    @Test
    fun `zip not initialized`() {
        val path = Path.of("providers-${healthcareProvidersService.lastUpdate}.zip")

        healthcareProvidersService.prepareAllProviders()

        try {
            assert(path.exists())
            assert(path.fileSize() < 1000000L)
        } finally {
            path.deleteExisting()
        }
    }

    @Test
    fun `last update`() {
        val today = LocalDate.now()

        healthcareProvidersService.setLastUpdate()

        assert(today == healthcareProvidersService.lastUpdate)
        assert(serverPropertiesRepository.findAll().first().lastUpdate == LocalDate.now())
    }

    @Test
    fun `get zipFilePath not init`() {
        healthcareProvidersService.setLastUpdate()

        val path = healthcareProvidersService.getAllSimpleData()

        assert(path.fileName.toString() == "init")
    }

    @Test
    fun `get zipFilePath`() {
        healthcareProvidersService.setLastUpdate()
        healthcareProvidersService.prepareAllProviders()

        val path = healthcareProvidersService.getAllSimpleData()

        path.deleteExisting()
        assert(path.fileName.toString() == "providers-${healthcareProvidersService.lastUpdate}.zip")
    }

    @Test
    fun `get details of multiple records`() {
        healthcareProviderRepository.saveAll(
            listOf(
                HealthcareProvider(locationId = 1, institutionId = 1, lat = 0.0, lng = 0.0),
                HealthcareProvider(locationId = 2, institutionId = 2, lat = 0.0, lng = 0.0),
                HealthcareProvider(locationId = 3, institutionId = 3, lat = 0.0, lng = 0.0)
            )
        )

        val result = healthcareProvidersService.getMultipleHealthcareProviderDetails(
            HealthcareProviderIdListDto(
                listOf(
                    HealthcareProviderIdDto(locationId = 1, institutionId = 1),
                    HealthcareProviderIdDto(locationId = 3, institutionId = 3)
                )
            )
        )

        assert(result.healthcareProvidersDetails.size == 2)
        assert(
            result.healthcareProvidersDetails == listOf(
                HealthcareProviderDetailDto(
                    locationId = 1,
                    institutionId = 1,
                    title = "",
                    institutionType = "",
                    houseNumber = "",
                    city = "",
                    postalCode = "",
                    ico = "",
                    lat = 0.0,
                    lng = 0.0,
                    category = emptyList()
                ),
                HealthcareProviderDetailDto(
                    locationId = 3,
                    institutionId = 3,
                    title = "",
                    institutionType = "",
                    houseNumber = "",
                    city = "",
                    postalCode = "",
                    ico = "",
                    lat = 0.0,
                    lng = 0.0,
                    category = emptyList()
                )
            )
        )
    }

    @Test
    fun `trying get details of a non-existing record`() {
        healthcareProviderRepository.saveAll(
            listOf(
                HealthcareProvider(locationId = 1, institutionId = 1),
                HealthcareProvider(locationId = 2, institutionId = 2),
                HealthcareProvider(locationId = 3, institutionId = 3)
            )
        )

        assertThrows<LoonoBackendException> {
            healthcareProvidersService.getHealthcareProviderDetail(
                HealthcareProviderIdDto(locationId = 4, institutionId = 4)
            )
        }
    }
}
