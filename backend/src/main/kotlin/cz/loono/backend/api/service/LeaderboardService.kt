package cz.loono.backend.api.service

import cz.loono.backend.api.dto.LeaderboardDto
import cz.loono.backend.api.dto.LeaderboardUserDto
import cz.loono.backend.db.repository.AccountRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class LeaderboardService(
    private val accountRepository: AccountRepository
) {

    fun getLeaderboard(uid: String, leaderboardSize: Int): LeaderboardDto =
        LeaderboardDto(
            accountRepository.findAllByOrderByPointsDesc(PageRequest.of(0, leaderboardSize)).map {
                LeaderboardUserDto(
                    name = it.userAuxiliary.nickname,
                    points = it.points,
                    profileImageUrl = it.userAuxiliary.profileImageUrl,
                    isThisMe = uid == it.uid
                )
            }
        )
}
