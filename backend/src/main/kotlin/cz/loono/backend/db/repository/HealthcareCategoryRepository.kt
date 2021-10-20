package cz.loono.backend.db.repository

import cz.loono.backend.db.model.HealthcareCategory
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HealthcareCategoryRepository : CrudRepository<HealthcareCategory, String>
