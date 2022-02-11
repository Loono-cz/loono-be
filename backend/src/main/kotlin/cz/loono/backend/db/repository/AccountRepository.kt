package cz.loono.backend.db.repository

import cz.loono.backend.db.model.Account
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : CrudRepository<Account, Long> {
    fun existsByUid(uid: String): Boolean
    fun findByUid(uid: String): Account?
    fun deleteAccountByUid(uid: String): Long
    fun findAllByOrderByPointsDesc(pageable: Pageable): List<Account>
}
