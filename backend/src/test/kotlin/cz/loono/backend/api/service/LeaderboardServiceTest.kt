package cz.loono.backend.api.service

import cz.loono.backend.api.dto.LeaderboardUserDto
import cz.loono.backend.createAccount
import cz.loono.backend.db.repository.AccountRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(properties = ["spring.profiles.active=test"])
class LeaderboardServiceTest(
    private val accountRepo: AccountRepository
) {

    private val leaderboardService = LeaderboardService(accountRepo)

    @AfterEach
    fun tearDown() {
        accountRepo.deleteAll()
    }

    @Test
    fun `empty db`() {
        val leaderboard = leaderboardService.getLeaderboard("uid")

        assert(leaderboard.top.isEmpty())
        assert(leaderboard.peers.isEmpty())
        assert(leaderboard.myOrder == 0)
    }

    @Test
    fun `me in top`() {
        accountRepo.save(createAccount(uid = "uid"))

        val leaderboard = leaderboardService.getLeaderboard("uid")

        assert(leaderboard.top.first().isThisMe == true)
        assert(leaderboard.peers.first().isThisMe == true)
        assert(leaderboard.myOrder == 1)
    }

    @Test
    fun `happy case`() {
        accountRepo.save(createAccount(uid = "1", points = 0))
        accountRepo.save(createAccount(uid = "2", points = 100))
        accountRepo.save(createAccount(uid = "3", points = 200))
        accountRepo.save(createAccount(uid = "4", points = 10))
        accountRepo.save(createAccount(uid = "5", points = 5))
        accountRepo.save(createAccount(uid = "6", points = 1000))

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
        accountRepo.save(createAccount(uid = "1", points = 200))
        accountRepo.save(createAccount(uid = "2", points = 10))

        val leaderboard = leaderboardService.getLeaderboard("2")

        assert(leaderboard.top.size == 2)
        assert(leaderboard.peers.size == 2)
        assert(leaderboard.myOrder == 2)
    }

    @Test
    fun `leader detail`() {
        accountRepo.save(
            createAccount(
                uid = "1",
                points = 200,
                nickname = "boss",
                profileImageUrl = "image"
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
