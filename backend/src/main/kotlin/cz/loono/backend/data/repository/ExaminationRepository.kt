package cz.loono.backend.data.repository

import cz.loono.backend.data.model.Examination
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ExaminationRepository : CrudRepository<Examination, Long>
