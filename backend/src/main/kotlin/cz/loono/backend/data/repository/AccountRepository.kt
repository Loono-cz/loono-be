package cz.loono.backend.data.repository

import cz.loono.backend.data.model.Account
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : CrudRepository<Account, String> {
    fun existsByUid(uid: String): Boolean
}