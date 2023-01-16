package cz.loono.backend.db.repository

import cz.loono.backend.db.model.CronLog
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CronLogRepository : CrudRepository<CronLog, Long>
