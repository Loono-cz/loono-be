package cz.loono.backend.schedule

import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.service.PushNotificationService
import cz.loono.backend.db.model.CronLog
import cz.loono.backend.db.repository.CronLogRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SelfExaminationWaitingTask(
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository,
    private val notificationService: PushNotificationService,
    private val cronLogRepository: CronLogRepository
) : DailySchedulerTask {

    override fun run() {
        try {
            selfExaminationRecordRepository.findAllByStatus(SelfExaminationStatusDto.WAITING_FOR_CHECKUP).forEach {
                if (it.waitingTo == LocalDate.now()) {
                    selfExaminationRecordRepository.save(
                        it.copy(
                            status = SelfExaminationStatusDto.WAITING_FOR_RESULT,
                            waitingTo = null
                        )
                    )
                    notificationService.sendSelfExamIssueResultNotification(
                        setOf(it.account)
                    )
                }
            }
            cronLogRepository.save(
                CronLog(
                    functionName = "SelfExaminationWaitingTask",
                    status = "PASSED",
                    message = null,
                    createdAt = LocalDate.now().toString()
                )
            )
        } catch (e: Exception) {
            cronLogRepository.save(
                CronLog(
                    functionName = "SelfExaminationWaitingTask",
                    status = "ERROR",
                    message = "$e",
                    createdAt = LocalDate.now().toString()
                )
            )
        }
    }
}
