package cz.loono.backend.schedule

import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.SelfExaminationTypeDto
import cz.loono.backend.api.service.AccountService
import cz.loono.backend.api.service.BadgesPointsProvider.GENERAL_BADGES_TO_EXAMS
import cz.loono.backend.api.service.BadgesPointsProvider.getSelfExaminationType
import cz.loono.backend.api.service.ExaminationInterval
import cz.loono.backend.api.service.PreventionService
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.Badge
import cz.loono.backend.db.model.ExaminationRecord
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
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
    private val selfExaminationRecordRepo: SelfExaminationRecordRepository
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
                    getSelfExaminationType(badge.getBadgeAsEnum(), account.getSexAsEnum())?.let {
                        downgradeSelfExaminationBadge(account, badge, it)
                    } ?: downgradeGeneralBadge(account, badge, examsRequests, now)
                }.toSet()

                if (downgradedBadges != account.badges) account.copy(badges = downgradedBadges) else null
            }
            val accountsWithDowngradedBadges = removeZeroLevelBadges(accountsToUpdate)
            logger.debug("Updating badges for the following accounts '$accountsWithDowngradedBadges'")

            accountRepository.saveAll(accountsWithDowngradedBadges)
        }
        logger.info("BadgeDowngradeTask finished")
    }

    private fun downgradeSelfExaminationBadge(
        account: Account,
        candidate: Badge,
        selfExaminationTypeDto: SelfExaminationTypeDto
    ): Badge {
        val examType = selfExaminationRecordRepo.findFirstByAccountAndTypeOrderByDueDateDesc(account, selfExaminationTypeDto)
        val now = clock.instant().toLocalDateTime()
        val dueDate = examType.dueDate?.atTime(now.hour, now.minute, now.second)?.plusMonths(toleranceMonths)
        return candidate.takeIf { shouldDowngradeBadge(now, candidate.lastUpdateOn, dueDate) }?.let {
            candidate.copy(level = candidate.level.dec(), lastUpdateOn = now.plusYears(1))
        } ?: candidate
    }
    private fun downgradeGeneralBadge(
        account: Account,
        candidate: Badge,
        examsRequests: List<ExaminationInterval>,
        now: LocalDateTime
    ): Badge {
        val examType = GENERAL_BADGES_TO_EXAMS.getValue(candidate.getBadgeAsEnum())
        val intervalYears = examsRequests.firstOrNull { it.examinationType == examType }?.intervalYears
        val latestExam = getLatestExam(account.examinationRecords, examType)

        return intervalYears?.toLong()?.let {
            val plannedDate = latestExam?.plannedDate?.plusYears(it)?.plusMonths(toleranceMonths)
            val lastUpdatedDate = candidate.lastUpdateOn
            if (shouldDowngradeBadge(now, lastUpdatedDate, plannedDate)) {
                candidate.copy(level = candidate.level.dec(), lastUpdateOn = now.plusYears(it))
            } else {
                candidate
            }
        } ?: candidate
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
