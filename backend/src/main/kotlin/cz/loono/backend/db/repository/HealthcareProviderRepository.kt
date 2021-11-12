package cz.loono.backend.db.repository

import cz.loono.backend.db.model.HealthcareProvider
import cz.loono.backend.db.model.HealthcareProviderId
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface HealthcareProviderRepository : PagingAndSortingRepository<HealthcareProvider, HealthcareProviderId>
