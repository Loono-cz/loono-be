package cz.loono.backend.api.service

import cz.loono.backend.api.dto.LeaderboardUserDto
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.UserAuxiliary
import cz.loono.backend.db.repository.AccountRepository
import org.junit.jupiter.api.Test
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class LeaderboardServiceTest(
    private val accountRepo: AccountRepository
) {

    private val leaderboardService = LeaderboardService(accountRepo)

    @Test
    fun `empty db`() {
        val leaderboard = leaderboardService.getLeaderboard("uid")

        assert(leaderboard.top.isEmpty())
        assert(leaderboard.peers.isEmpty())
        assert(leaderboard.myOrder == 0)
    }

    @Test
    fun `me in top`() {
        accountRepo.save(Account(uid = "uid"))

        val leaderboard = leaderboardService.getLeaderboard("uid")

        assert(leaderboard.top.first().isThisMe == true)
        assert(leaderboard.peers.first().isThisMe == true)
        assert(leaderboard.myOrder == 1)
    }

    @Test
    fun `happy case`() {
        accountRepo.save(Account(uid = "1", points = 0))
        accountRepo.save(Account(uid = "2", points = 100))
        accountRepo.save(Account(uid = "3", points = 200))
        accountRepo.save(Account(uid = "4", points = 10))
        accountRepo.save(Account(uid = "5", points = 5))
        accountRepo.save(Account(uid = "6", points = 1000))

        val leaderboard = leaderboardService.getLeaderboard("5")

        assert(leaderboard.top.size == 3)
        assert(leaderboard.top[0].points == 1000)
        assert(leaderboard.top[1].points == 200)
        assert(leaderboard.top[2].points == 100)
        assert(leaderboard.peers[0].points == 10)
        assert(leaderboard.peers[1].points == 5)
        assert(leaderboard.peers[2].points == 0)
        assert(leaderboard.myOrder == 5)
    }

    @Test
    fun `top 3 with not enough accounts`() {
        accountRepo.save(Account(uid = "1", points = 200))
        accountRepo.save(Account(uid = "2", points = 10))

        val leaderboard = leaderboardService.getLeaderboard("2")

        assert(leaderboard.top.size == 2)
        assert(leaderboard.peers.size == 2)
        assert(leaderboard.myOrder == 2)
    }

    @Test
    fun `leader detail`() {
        accountRepo.save(
            Account(
                uid = "1",
                points = 200,
                userAuxiliary = UserAuxiliary(nickname = "boss", profileImageUrl = "image")
            )
        )

        val leaderboard = leaderboardService.getLeaderboard("uid")

        assert(
            leaderboard.top[0] == LeaderboardUserDto(
                name = "boss",
                profileImageUrl = "image",
                points = 200,
                isThisMe = false
            )
        )
    }
}
