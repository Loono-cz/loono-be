package cz.loono.backend.schedule

import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.service.PushNotificationService
import cz.loono.backend.db.model.CronControl
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.CronControlRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SelfExamReminderTask(
    private val accountRepository: AccountRepository,
    private val notificationService: PushNotificationService,
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository,
    private val cronControlRepository: CronControlRepository
) : DailySchedulerTask {

    override fun run() {
        try {
            val accounts = accountRepository.findAll()
            val today = LocalDate.now()
            accounts.forEach { account ->
                val statuses = selfExaminationRecordRepository.findAllByAccount(account)
                val todayNotifications = statuses.filter { it.dueDate == today && it.status == SelfExaminationStatusDto.PLANNED && it.result == null }
                if (todayNotifications.isNotEmpty()) {
                    notificationService.sendSelfExamNotification(setOf(account))
                }
                if (statuses.size < 2 && account.created.dayOfMonth == today.dayOfMonth) {
                    notificationService.sendFirstSelfExamNotification(setOf(account))
                }
            }
            cronControlRepository.save(
                CronControl(
                    functionName = "SelfExamReminderTask",
                    status = "PASSED",
                    message = null,
                    createdAt = LocalDate.now().toString()
                )
            )
        } catch (e: Exception) {
            cronControlRepository.save(
                CronControl(
                    functionName = "SelfExamReminderTask",
                    status = "ERROR",
                    message = "$e",
                    createdAt = LocalDate.now().toString()
                )
            )
        }
    }
}
