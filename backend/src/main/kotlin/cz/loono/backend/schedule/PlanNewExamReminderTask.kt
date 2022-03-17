package cz.loono.backend.schedule

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
        accountService.paginateOverAccounts {
            it.forEach { account ->
                val examStatuses = preventionService.getPreventionStatus(account.uid).examinations
                examStatuses.forEach { status ->
                    status.lastConfirmedDate?.let {
                        val period = Period.between(status.lastConfirmedDate.toLocalDate(), today)
                        val passedMonths = period.years * 12 + period.months
                        if (passedMonths == (status.intervalYears * 12) - 2) {
                            notificationService.sendNewExam2MonthsAheadNotificationToOrder(
                                setOf(account),
                                status.examinationType,
                                status.intervalYears
                            )
                        }
                        if (passedMonths == (status.intervalYears * 12) - 1) {
                            notificationService.sendNewExamMonthAheadNotificationToOrder(
                                setOf(account),
                                status.examinationType,
                                status.intervalYears,
                                status.badge
                            )
                        }
                    }
                }
            }
        }
    }
}
