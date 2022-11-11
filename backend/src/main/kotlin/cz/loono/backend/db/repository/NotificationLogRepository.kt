package cz.loono.backend.db.repository

import cz.loono.backend.db.model.NotificationLog
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationLogRepository : CrudRepository<NotificationLog, Long>
