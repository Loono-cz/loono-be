package cz.loono.backend.schedule

import cz.loono.backend.api.dto.ExaminationCategoryTypeDto
import cz.loono.backend.api.service.AccountService
import cz.loono.backend.api.service.PreventionService
import cz.loono.backend.api.service.PushNotificationService
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.Period

@Component
class PlanNewExamReminderTask(
    private val accountService: AccountService,
    private val preventionService: PreventionService,
    private val notificationService: PushNotificationService
) : DailySchedulerTask {

    override fun run() {
        val today = LocalDate.now()
        accountService.paginateOverAccounts { listOfAccounts ->
            listOfAccounts.forEach { account ->
                val examStatuses = preventionService.getPreventionStatus(account.uid).examinations
                val mandatoryExams = examStatuses.filter { it.examinationCategoryType == ExaminationCategoryTypeDto.MANDATORY }
                val customExams = examStatuses.filter { it.examinationCategoryType == ExaminationCategoryTypeDto.CUSTOM }
                mandatoryExams.forEach { status ->
                    status.lastConfirmedDate?.let {
                        val period = Period.between(status.lastConfirmedDate.toLocalDate(), today)
                        val passedMonths = period.years * 12 + period.months
                        if (passedMonths == (status.intervalYears * 12) - 2 && period.days == 0) {
                            notificationService.sendNewExam2MonthsAheadNotificationToOrder(
                                setOf(account),
                                status.examinationType,
                                status.intervalYears,
                                status.examinationCategoryType ?: ExaminationCategoryTypeDto.MANDATORY,
                                status.uuid
                            )
                        }
                        if (passedMonths == (status.intervalYears * 12) - 1 && period.days == 0) {
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
                            val period = Period.between(status.lastConfirmedDate.toLocalDate(), today)
                            if (period.months == (status.customInterval?.minus(2)) && period.days == 0) {
                                notificationService.sendNewExam2MonthsAheadNotificationToOrder(
                                    setOf(account),
                                    status.examinationType,
                                    status.intervalYears,
                                    status.examinationCategoryType ?: ExaminationCategoryTypeDto.CUSTOM,
                                    status.uuid
                                )
                            }
                            if (period.months == (status.customInterval?.minus(1)) && period.days == 0) {
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
    }
}
