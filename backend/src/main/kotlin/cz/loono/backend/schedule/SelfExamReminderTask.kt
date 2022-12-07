package cz.loono.backend.schedule

import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.service.PushNotificationService
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SelfExamReminderTask(
    private val accountRepository: AccountRepository,
    private val notificationService: PushNotificationService,
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository
) : DailySchedulerTask {

    override fun run() {
        val accounts = accountRepository.findAll()
        val today = LocalDate.now()
        accounts.forEach { account ->
            val statuses = selfExaminationRecordRepository.findAllByAccount(account)
            val todayNotifications = statuses.filter { it.dueDate == today && it.status == SelfExaminationStatusDto.PLANNED && it.result == null }
            if (todayNotifications.isNotEmpty()) {
                notificationService.sendSelfExamNotification(setOf(account), todayNotifications.first().uuid)
            }
            if (statuses.size < 2 && account.created.dayOfMonth == today.dayOfMonth) {
                val uuid = if(statuses.isNotEmpty()) statuses.first().uuid else ""
                notificationService.sendFirstSelfExamNotification(setOf(account), uuid)
            }
        }
    }
}
