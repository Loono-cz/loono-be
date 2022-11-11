package cz.loono.backend.db.repository

import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.UserFeedback
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserFeedbackRepository : CrudRepository<UserFeedback, Long> {
    fun deleteAllByAccount(account: Account)
}
