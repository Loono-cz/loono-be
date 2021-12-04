package cz.loono.backend.api.service

import cz.loono.backend.db.model.HealthcareProvider
import cz.loono.backend.db.repository.HealthcareCategoryRepository
import cz.loono.backend.db.repository.HealthcareProviderRepository
import cz.loono.backend.db.repository.ServerPropertiesRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class HealthcareProvidersServiceBatchTest {

    private var healthcareProviderRepository: HealthcareProviderRepository = mock()

    private var healthcareCategoryRepository: HealthcareCategoryRepository = mock()

    private var serverPropertiesRepository: ServerPropertiesRepository = mock()

    @Test
    fun `save empty providers list`() {
        val healthcareProvidersService = HealthcareProvidersService(
            healthcareProviderRepository,
            healthcareCategoryRepository,
            serverPropertiesRepository
        )

        healthcareProvidersService.saveProviders(emptyList())

        verify(healthcareProviderRepository, times(0)).saveAll(any<List<HealthcareProvider>>())
    }

    @Test
    fun `save bellow 100 count`() {
        val healthcareProvidersService = HealthcareProvidersService(
            healthcareProviderRepository,
            healthcareCategoryRepository,
            serverPropertiesRepository
        )
        val list = generateProviderList(98)

        healthcareProvidersService.saveProviders(list)

        verify(healthcareProviderRepository, times(1)).saveAll(any<List<HealthcareProvider>>())
    }

    @Test
    fun `save almost 40k records`() {
        val healthcareProvidersService = HealthcareProvidersService(
            healthcareProviderRepository,
            healthcareCategoryRepository,
            serverPropertiesRepository
        )
        val list = generateProviderList(39920)

        healthcareProvidersService.saveProviders(list)

        verify(healthcareProviderRepository, times(80)).saveAll(any<List<HealthcareProvider>>())
    }

    private fun generateProviderList(size: Int): ArrayList<HealthcareProvider> {
        val list = ArrayList<HealthcareProvider>(size)
        for (i in 0 until size) {
            list.add(HealthcareProvider(locationId = i.toLong(), institutionId = i.toLong()))
        }
        return list
    }
}
