package cz.loono.backend.db.repository

import cz.loono.backend.db.model.Account
import org.intellij.lang.annotations.Language
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : JpaRepository<Account, Long> {
    fun existsByUid(uid: String): Boolean
    fun findByUid(uid: String): Account?
    fun findAllByOrderByPointsDesc(pageable: Pageable): List<Account>
    // Looks for first account which has more and fewer points than current one
    @Language("SQL")
    @Query(
        """
            (SELECT * FROM account WHERE points <= :points ORDER BY points DESC limit 2) 
            UNION DISTINCT 
            (SELECT * FROM account WHERE points >= :points ORDER BY points ASC limit 2) ORDER BY points DESC
        """,
        nativeQuery = true
    )
    fun findPeers(points: Int): List<Account>
    @Language("SQL")
    @Query(
        """
            SELECT COUNT(*) FROM account WHERE points > :points
        """,
        nativeQuery = true
    )
    fun findNumberOfAccountsAbove(points: Int): Int
}
