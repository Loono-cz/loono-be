package cz.loono.backend.schedule

import cz.loono.backend.api.dto.BadgeTypeDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.service.AccountService
import cz.loono.backend.api.service.BadgesPointsProvider.BADGES_TO_EXAMS
import cz.loono.backend.api.service.PreventionService
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.ExaminationRecord
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.extensions.toLocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime

@Component
class BadgeDowngradeTask(
    @Value("\${task.badge-downgrade.tolerance-months}")
    private val toleranceMonths: Long,
    @Value("\${task.badge-downgrade.page-size}")
    private val pageSize: Int,
    private val accountRepository: AccountRepository,
    private val accountService: AccountService,
    private val preventionService: PreventionService,
    private val clock: Clock,
) : DailySchedulerTask {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run() {
        logger.info(
            "BadgeDowngradeTask executed with the following settings 'toleranceMonths '$toleranceMonths', pageSize '$pageSize'"
        )
        accountService.paginateOverAccounts { nextPage ->
            val accountsToUpdate = nextPage.mapNotNull { account ->
                val now = clock.instant().toLocalDateTime()
                val userBadges = account.badges
                // Don't do anything when no badges to downgrade
                userBadges.ifEmpty {
                    return@mapNotNull null
                }

                val examsRequests = preventionService.getExaminationRequests(account)

                val downgradedBadges = userBadges.map { badge ->
                    val examType = BADGES_TO_EXAMS.getValue(BadgeTypeDto.valueOf(badge.type))
                    val intervalYears = examsRequests.firstOrNull { it.examinationType == examType }?.intervalYears
                    val latestExam = getLatestExam(account.examinationRecords, examType)

                    intervalYears?.toLong()?.let {
                        val plannedDate = latestExam?.plannedDate?.plusYears(it)?.plusMonths(toleranceMonths)
                        val lastUpdatedDate = badge.lastUpdateOn
                        if (shouldDowngradeBadge(now, lastUpdatedDate, plannedDate)) {
                            badge.copy(level = badge.level.dec(), lastUpdateOn = now.plusYears(it))
                        } else {
                            badge
                        }
                    } ?: badge
                }.toSet()

                if (downgradedBadges != account.badges) account.copy(badges = downgradedBadges) else null
            }
            val accountsWithDowngradedBadges = removeZeroLevelBadges(accountsToUpdate)
            logger.debug("Updating badges for the following accounts '$accountsWithDowngradedBadges'")

            accountRepository.saveAll(accountsWithDowngradedBadges)
        }
        logger.info("BadgeDowngradeTask finished")
    }

    private fun shouldDowngradeBadge(
        now: LocalDateTime,
        lastUpdatedDate: LocalDateTime,
        plannedDate: LocalDateTime?
    ) = now.isAfter(lastUpdatedDate) && (plannedDate == null || now.isAfter(plannedDate))

    private fun removeZeroLevelBadges(accounts: List<Account>) =
        accounts.map { account -> account.copy(badges = account.badges.filter { it.level > 0 }.toSet()) }

    private fun getLatestExam(examRecords: List<ExaminationRecord>, examType: ExaminationTypeDto) =
        examRecords.lastOrNull { it.plannedDate != null && it.type == examType }
}
