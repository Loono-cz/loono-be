package cz.loono.backend.schedule

import cz.loono.backend.api.dto.ExaminationCategoryTypeDto
import cz.loono.backend.api.dto.ExaminationPreventionStatusDto
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
                        if (status.isNewExamAhead(2, today)) {
                            notificationService.sendNewExam2MonthsAheadNotificationToOrder(
                                setOf(account),
                                status.examinationType,
                                status.intervalYears,
                                status.examinationCategoryType ?: ExaminationCategoryTypeDto.MANDATORY,
                                status.uuid
                            )
                        }
                        if (status.isNewExamAhead(1, today)) {
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
                            if (status.isNewExamAhead(2, today)) {
                                notificationService.sendNewExam2MonthsAheadNotificationToOrder(
                                    setOf(account),
                                    status.examinationType,
                                    status.intervalYears,
                                    status.examinationCategoryType ?: ExaminationCategoryTypeDto.CUSTOM,
                                    status.uuid
                                )
                            }
                            if (status.isNewExamAhead(1, today)) {
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

    companion object {
        private fun ExaminationPreventionStatusDto.isNewExamAhead(months: Long, now: LocalDate): Boolean {
            val last = this.lastConfirmedDate ?: return false

            val period = Period.between(last.toLocalDate(), now)
            val interval = (this.customInterval ?: (this.intervalYears * 12)) - months
            return period.toTotalMonths() == interval && period.days == 0
        }
    }
}
