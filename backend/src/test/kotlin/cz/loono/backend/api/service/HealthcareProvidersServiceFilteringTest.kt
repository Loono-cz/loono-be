package cz.loono.backend.api.service

import cz.loono.backend.api.dto.HealthcareProviderIdDto
import cz.loono.backend.data.constants.CategoryValues
import cz.loono.backend.db.model.HealthcareCategory
import cz.loono.backend.db.model.HealthcareProvider
import cz.loono.backend.db.repository.HealthcareCategoryRepository
import cz.loono.backend.db.repository.HealthcareProviderRepository
import cz.loono.backend.db.repository.ServerPropertiesRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional

class HealthcareProvidersServiceFilteringTest {

    private var healthcareProviderRepository: HealthcareProviderRepository = mock()

    private var healthcareCategoryRepository: HealthcareCategoryRepository = mock()

    private var serverPropertiesRepository: ServerPropertiesRepository = mock()

    @Test
    fun `happy case returning identical object`() {
        val healthcareProvidersService = HealthcareProvidersService(
            healthcareProviderRepository,
            healthcareCategoryRepository,
            serverPropertiesRepository
        )
        val healthcareProvider = HealthcareProvider(
            lat = 42.0,
            lng = 50.6,
            category = setOf(HealthcareCategory(value = CategoryValues.GENERAL_PRACTICAL_MEDICINE.value))
        )

        `when`(healthcareProviderRepository.findAll(any<PageRequest>()))
            .thenReturn(
                PageImpl(listOf(healthcareProvider))
            )

        val providers = healthcareProvidersService.findPage(0)

        assert(providers.size == 1)
        val filteredProvider = providers.first()
        assert(filteredProvider.lat == healthcareProvider.lat)
        assert(filteredProvider.lng == healthcareProvider.lng)
        assert(filteredProvider.category.contains(CategoryValues.GENERAL_PRACTICAL_MEDICINE.value))
    }

    @Test
    fun `missing coordinates`() {
        val healthcareProvidersService = HealthcareProvidersService(
            healthcareProviderRepository,
            healthcareCategoryRepository,
            serverPropertiesRepository
        )
        val healthcareProvider = HealthcareProvider(
            category = setOf(HealthcareCategory(value = CategoryValues.GENERAL_PRACTICAL_MEDICINE.value))
        )

        `when`(healthcareProviderRepository.findAll(any<PageRequest>()))
            .thenReturn(
                PageImpl(listOf(healthcareProvider))
            )

        val providers = healthcareProvidersService.findPage(0)

        assert(providers.isEmpty())
    }

    @Test
    fun `missing imported coordinates`() {
        val healthcareProvidersService = HealthcareProvidersService(
            healthcareProviderRepository,
            healthcareCategoryRepository,
            serverPropertiesRepository
        )
        val healthcareProvider = HealthcareProvider(
            category = setOf(HealthcareCategory(value = CategoryValues.GENERAL_PRACTICAL_MEDICINE.value)),
            correctedLat = 22.22,
            correctedLng = 11.11
        )

        `when`(healthcareProviderRepository.findAll(any<PageRequest>()))
            .thenReturn(
                PageImpl(listOf(healthcareProvider))
            )

        val providers = healthcareProvidersService.findPage(0)

        assert(providers.size == 1)
    }

    @Test
    fun `missing corrected coordinates`() {
        val healthcareProvidersService = HealthcareProvidersService(
            healthcareProviderRepository,
            healthcareCategoryRepository,
            serverPropertiesRepository
        )
        val healthcareProvider = HealthcareProvider(
            category = setOf(HealthcareCategory(value = CategoryValues.GENERAL_PRACTICAL_MEDICINE.value)),
            lat = 22.22,
            lng = 11.11
        )

        `when`(healthcareProviderRepository.findAll(any<PageRequest>()))
            .thenReturn(
                PageImpl(listOf(healthcareProvider))
            )

        val providers = healthcareProvidersService.findPage(0)

        assert(providers.size == 1)
    }

    @Test
    fun `missing category`() {
        val healthcareProvidersService = HealthcareProvidersService(
            healthcareProviderRepository,
            healthcareCategoryRepository,
            serverPropertiesRepository
        )
        val healthcareProvider = HealthcareProvider(
            lat = 42.0,
            lng = 50.6,
            category = emptySet()
        )

        `when`(healthcareProviderRepository.findAll(any<PageRequest>()))
            .thenReturn(
                PageImpl(listOf(healthcareProvider))
            )

        val providers = healthcareProvidersService.findPage(0)

        assert(providers.isEmpty())
    }

    @Test
    fun `corrected category`() {
        val healthcareProvidersService = HealthcareProvidersService(
            healthcareProviderRepository,
            healthcareCategoryRepository,
            serverPropertiesRepository
        )
        val healthcareProvider = HealthcareProvider(
            lat = 42.0,
            lng = 50.6,
            category = emptySet(),
            correctedCategory = setOf(HealthcareCategory(value = CategoryValues.GENERAL_PRACTICAL_MEDICINE.value))
        )

        `when`(healthcareProviderRepository.findAll(any<PageRequest>()))
            .thenReturn(
                PageImpl(listOf(healthcareProvider))
            )

        val providers = healthcareProvidersService.findPage(0)

        assert(providers.size == 1)
    }

    @Test
    fun `removing unwanted categories`() {
        val healthcareProvidersService = HealthcareProvidersService(
            healthcareProviderRepository,
            healthcareCategoryRepository,
            serverPropertiesRepository
        )
        val healthcareProvider = HealthcareProvider(
            lat = 42.0,
            lng = 50.6,
            category = setOf(
                HealthcareCategory(value = CategoryValues.GENERAL_PRACTICAL_MEDICINE.value),
                HealthcareCategory(value = CategoryValues.PHARMACOLOGY.value)
            )
        )

        `when`(healthcareProviderRepository.findAll(any<PageRequest>()))
            .thenReturn(
                PageImpl(listOf(healthcareProvider))
            )

        val providers = healthcareProvidersService.findPage(0)

        assert(providers.size == 1)
        val filteredProvider = providers.first()
        assert(filteredProvider.lat == healthcareProvider.lat)
        assert(filteredProvider.lng == healthcareProvider.lng)
        assert(filteredProvider.category.contains(CategoryValues.GENERAL_PRACTICAL_MEDICINE.value))
    }

    @Test
    fun `detail correction`() {
        val healthcareProvidersService = HealthcareProvidersService(
            healthcareProviderRepository,
            healthcareCategoryRepository,
            serverPropertiesRepository
        )
        val healthcareProvider = HealthcareProvider(
            correctedLng = 42.0,
            correctedLat = 50.6,
            correctedCategory = setOf(HealthcareCategory(value = CategoryValues.DENTIST.value)),
            correctedWebsite = "website",
            correctedPhoneNumber = "phone"
        )

        `when`(healthcareProviderRepository.findById(any())).thenReturn(Optional.of(healthcareProvider))

        val provider = healthcareProvidersService.getHealthcareProviderDetail(
            HealthcareProviderIdDto(
                locationId = 1L,
                institutionId = 1L
            )
        )

        assertEquals("website", provider.website)
        assertEquals("phone", provider.phoneNumber)
        assertEquals(42.0, provider.lng)
        assertEquals(50.6, provider.lat)
        assert(provider.category.size == 1)
    }
}
