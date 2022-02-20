package cz.loono.backend.db.repository

import cz.loono.backend.db.model.Account
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : JpaRepository<Account, Long> {
    fun existsByUid(uid: String): Boolean
    fun findByUid(uid: String): Account?
    fun findAllByOrderByPointsDesc(pageable: Pageable): List<Account>
}
