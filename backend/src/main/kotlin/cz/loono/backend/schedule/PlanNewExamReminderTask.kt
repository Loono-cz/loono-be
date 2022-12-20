package cz.loono.backend.schedule

import cz.loono.backend.api.dto.ExaminationCategoryTypeDto
import cz.loono.backend.api.dto.ExaminationPreventionStatusDto
import cz.loono.backend.api.service.AccountService
import cz.loono.backend.api.service.PreventionService
import cz.loono.backend.api.service.PushNotificationService
import cz.loono.backend.db.model.CronLog
import cz.loono.backend.db.repository.CronLogRepository
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Component
class PlanNewExamReminderTask(
    private val accountService: AccountService,
    private val preventionService: PreventionService,
    private val notificationService: PushNotificationService,
    private val cronLogRepository: CronLogRepository
) : DailySchedulerTask {

    override fun run() {
        try {
            val today = LocalDate.now()
            accountService.paginateOverAccounts { listOfAccounts ->
                listOfAccounts.forEach { account ->
                    val examStatuses = preventionService.getPreventionStatus(account.uid).examinations
                    val mandatoryExams = examStatuses.filter { it.examinationCategoryType == ExaminationCategoryTypeDto.MANDATORY }
                    val customExams = examStatuses.filter { it.examinationCategoryType == ExaminationCategoryTypeDto.CUSTOM }
                    mandatoryExams.forEach { status ->
                        status.lastConfirmedDate?.let {
                            if (status.isNewExamAhead(2, today, ExaminationCategoryTypeDto.MANDATORY)) {
                                notificationService.sendNewExam2MonthsAheadNotificationToOrder(
                                    setOf(account),
                                    status.examinationType,
                                    status.intervalYears,
                                    status.examinationCategoryType ?: ExaminationCategoryTypeDto.MANDATORY,
                                    status.uuid
                                )
                            }
                            if (status.isNewExamAhead(1, today, ExaminationCategoryTypeDto.MANDATORY)) {
                                notificationService.sendNewExamMonthAheadNotificationToOrder(
                                    setOf(account),
                                    status.examinationType,
                                    status.intervalYears,
                                    status.examinationCategoryType ?: ExaminationCategoryTypeDto.MANDATORY,
                                    status.uuid
                                )
                            }
                        }
                    }

                    customExams.forEach { status ->
                        status.lastConfirmedDate?.let {
                            if (status.periodicExam == true) {
                                if (status.isNewExamAhead(2, today, ExaminationCategoryTypeDto.CUSTOM)) {
                                    notificationService.sendNewExam2MonthsAheadNotificationToOrder(
                                        setOf(account),
                                        status.examinationType,
                                        status.intervalYears,
                                        status.examinationCategoryType ?: ExaminationCategoryTypeDto.CUSTOM,
                                        status.uuid
                                    )
                                }
                                if (status.isNewExamAhead(1, today, ExaminationCategoryTypeDto.CUSTOM)) {
                                    notificationService.sendNewExamMonthAheadNotificationToOrder(
                                        setOf(account),
                                        status.examinationType,
                                        status.intervalYears,
                                        status.examinationCategoryType ?: ExaminationCategoryTypeDto.CUSTOM,
                                        status.uuid
                                    )
                                }
                            }
                        }
                    }
                }
            }
            cronLogRepository.save(
                CronLog(
                    functionName = "PlanNewExamReminderTask",
                    status = "PASSED",
                    message = null,
                    createdAt = LocalDate.now().toString()
                )
            )
        } catch (e: Exception) {
            cronLogRepository.save(
                CronLog(
                    functionName = "PlanNewExamReminderTask",
                    status = "ERROR",
                    message = "$e",
                    createdAt = LocalDate.now().toString()
                )
            )
        }
    }

    private fun ExaminationPreventionStatusDto.isNewExamAhead(months: Long, today: LocalDate, category: ExaminationCategoryTypeDto): Boolean {
        val lastDayOfCurrentMonth = today.withDayOfMonth(today.month.length(today.isLeapYear))
        val last = this.lastConfirmedDate ?: return false

        val monthsPeriod = ChronoUnit.MONTHS.between(last.toLocalDate().withDayOfMonth(1), today.withDayOfMonth(1))
        val daysPeriod = last.toLocalDate().dayOfMonth - today.dayOfMonth
        val interval = if (category == ExaminationCategoryTypeDto.CUSTOM) {
            this.customInterval
        } else {
            (this.intervalYears * 12) - months
        }
        if (monthsPeriod == interval) {
            if (daysPeriod == 0)
                return true

            if (lastDayOfCurrentMonth == today && last.toLocalDate().dayOfMonth > today.dayOfMonth)
                return true
        }
        return false
    }
}
