package cz.loono.backend.api.service

import cz.loono.backend.data.constants.CategoryValues
import cz.loono.backend.db.model.HealthcareCategory
import cz.loono.backend.db.model.HealthcareProvider
import cz.loono.backend.db.repository.HealthcareCategoryRepository
import cz.loono.backend.db.repository.HealthcareProviderRepository
import cz.loono.backend.db.repository.ServerPropertiesRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

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
}
