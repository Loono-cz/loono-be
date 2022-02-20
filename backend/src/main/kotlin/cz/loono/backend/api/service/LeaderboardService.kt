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

    private val batchSize = 500

    fun getLeaderboard(uid: String): LeaderboardDto {

        val top3 = findTop3Accounts(uid)

        val iterations = accountRepository.count() / batchSize
        var userOrder = 0L
        val peers = mutableListOf<LeaderboardUserDto>()
        iter@ for (i in 0..iterations) {
            val accountsBatch = accountRepository.findAllByOrderByPointsDesc(PageRequest.of(i.toInt(), batchSize))
            for (j in accountsBatch.indices) {
                if (accountsBatch[j].uid == uid) {
                    userOrder = i * batchSize + j + 1
                    if (j > 0) {
                        peers.add(prepareLeaderboardUser(uid, accountsBatch[j - 1]))
                    } else if (i > 0) {
                        val previousPage =
                            accountRepository.findAllByOrderByPointsDesc(PageRequest.of((i - 1).toInt(), batchSize))
                        peers.add(prepareLeaderboardUser(uid, previousPage[batchSize - 1]))
                    }
                    peers.add(prepareLeaderboardUser(uid, accountsBatch[j]))
                    if (accountsBatch.size > 2 && j < batchSize - 1) {
                        peers.add(prepareLeaderboardUser(uid, accountsBatch[j + 1]))
                    } else if (accountsBatch.size == batchSize) {
                        val nextPage =
                            accountRepository.findAllByOrderByPointsDesc(PageRequest.of((i + 1).toInt(), batchSize))
                        peers.add(prepareLeaderboardUser(uid, nextPage[0]))
                    }
                    break@iter
                }
            }
        }

        return LeaderboardDto(
            top = top3,
            peers = peers,
            myOrder = userOrder.toInt()
        )
    }

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
