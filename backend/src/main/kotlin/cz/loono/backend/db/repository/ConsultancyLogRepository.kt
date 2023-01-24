package cz.loono.backend.db.repository

import cz.loono.backend.db.model.ConsultancyLog
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ConsultancyLogRepository : CrudRepository<ConsultancyLog, Long>