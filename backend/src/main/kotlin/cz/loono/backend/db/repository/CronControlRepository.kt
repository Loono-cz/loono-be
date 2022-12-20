package cz.loono.backend.db.repository

import cz.loono.backend.db.model.CronControl
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CronControlRepository : CrudRepository<CronControl, Long>
