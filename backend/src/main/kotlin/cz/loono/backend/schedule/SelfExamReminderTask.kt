package cz.loono.backend.schedule

import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.service.PreventionService
import cz.loono.backend.api.service.PushNotificationService
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.repository.AccountRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SelfExamReminderTask(
    private val accountRepository: AccountRepository,
    private val preventionService: PreventionService,
    private val notificationService: PushNotificationService
) : DailySchedulerTask {

    override fun run() {
        val accounts = accountRepository.findAll()
        val today = LocalDate.now()
        accounts.forEach { account ->
            val statuses = preventionService.getPreventionStatus(account.uid).selfexaminations
            statuses.forEach {
                if (account.created.dayOfMonth == today.dayOfMonth && it.plannedDate == null) {
                    notificationService.sendFirstSelfExamNotification(setOf<Account>(account), SexDto.valueOf(account.sex))
                }
                if (it.plannedDate == today) {
                    notificationService.sendSelfExamNotification(setOf<Account>(account), SexDto.valueOf(account.sex))
                }
            }
        }
    }
}
