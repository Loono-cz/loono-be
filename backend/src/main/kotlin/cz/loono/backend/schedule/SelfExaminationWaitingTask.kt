package cz.loono.backend.schedule

import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.service.PushNotificationService
import cz.loono.backend.db.model.CronControl
import cz.loono.backend.db.repository.CronControlRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SelfExaminationWaitingTask(
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository,
    private val notificationService: PushNotificationService,
    private val cronControlRepository: CronControlRepository
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
            cronControlRepository.save(
                CronControl(
                    functionName = "SelfExaminationWaitingTask",
                    status = "PASSED",
                    message = null,
                    createdAt = LocalDate.now().toString()
                )
            )
        } catch (e: Exception){
            cronControlRepository.save(
                CronControl(
                    functionName = "SelfExaminationWaitingTask",
                    status = "ERROR",
                    message = "$e",
                    createdAt = LocalDate.now().toString()
                )
            )
        }
    }
}
