package cz.loono.backend.db.repository

import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.Badge
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BadgeRepository : CrudRepository<Badge, Long> {
    fun deleteAllByAccount(account: Account)
}
