package cz.loono.backend.api.service

import cz.loono.backend.api.dto.LeaderboardDto
import cz.loono.backend.api.dto.LeaderboardUserDto
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.repository.AccountRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class LeaderboardService(
    private val accountRepository: AccountRepository
) {

    fun getLeaderboard(uuid: String): LeaderboardDto {
        val top3 = findTop3Accounts(uuid)
        val currentAcc = accountRepository.findByUid(uuid)
        val (myOrder, peers) = currentAcc?.points?.let { points ->
            positionInLeaderBoard(points) to findAccountPeers(points, uuid)
        } ?: (0 to emptyList())

        return LeaderboardDto(
            top = top3,
            peers = peers,
            myOrder = myOrder
        )
    }

    private fun findAccountPeers(
        points: Int,
        uuid: String
    ): List<LeaderboardUserDto> = accountRepository.findPeers(points).map { prepareLeaderboardUser(uuid, it) }

    private fun positionInLeaderBoard(points: Int) = accountRepository.findNumberOfAccountsAbove(points) + 1

    private fun findTop3Accounts(uid: String) = accountRepository.findAllByOrderByPointsDesc(PageRequest.of(0, 3)).map {
        prepareLeaderboardUser(uid, it)
    }

    private fun prepareLeaderboardUser(uid: String, account: Account): LeaderboardUserDto = LeaderboardUserDto(
        name = account.nickname,
        points = account.points,
        profileImageUrl = account.profileImageUrl,
        isThisMe = uid == account.uid
    )
}
