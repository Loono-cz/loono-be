package cz.loono.backend.schedule

import cz.loono.backend.api.service.AccountService
import cz.loono.backend.api.service.PreventionService
import cz.loono.backend.api.service.PushNotificationService
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.CronLog
import cz.loono.backend.db.repository.CronLogRepository
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.Period

@Component
class PreventionReminderTask(
    private val accountService: AccountService,
    private val preventionService: PreventionService,
    private val notificationService: PushNotificationService,
    private val cronLogRepository: CronLogRepository
) : DailySchedulerTask {

    override fun run() {
        try {
            val today = LocalDate.now()
            accountService.paginateOverAccounts { accounts ->
                val selectedAccounts = accounts.filter {
                    Period.between(it.created, today).months % 3 == 0
                }
                val notificationAccounts = mutableSetOf<Account>()
                selectedAccounts.forEach { account ->
                    val status = preventionService.getPreventionStatus(account.uid)
                    status.examinations.forEach examsLoop@{ exam ->
                        exam.lastConfirmedDate?.let {
                            val period = Period.between(it.toLocalDate(), today)
                            if (exam.plannedDate == null && period.years >= exam.intervalYears) {
                                notificationAccounts.add(account)
                                return@examsLoop
                            }
                        }
                    }
                }
                if (notificationAccounts.isNotEmpty()) {
                    notificationService.sendPreventionNotification(notificationAccounts)
                }
            }
            cronLogRepository.save(
                CronLog(
                    functionName = "PreventionReminderTask",
                    status = "PASSED",
                    message = null,
                    createdAt = LocalDate.now().toString()
                )
            )
        } catch (e: Exception) {
            cronLogRepository.save(
                CronLog(
                    functionName = "PreventionReminderTask",
                    status = "ERROR",
                    message = "$e",
                    createdAt = LocalDate.now().toString()
                )
            )
        }
    }
}
