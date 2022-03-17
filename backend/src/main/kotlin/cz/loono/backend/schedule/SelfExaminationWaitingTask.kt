package cz.loono.backend.schedule

import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.service.PushNotificationService
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SelfExaminationWaitingTask(
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository,
    private val notificationService: PushNotificationService
) : DailySchedulerTask {

    override fun run() {
        selfExaminationRecordRepository.findAllByStatus(SelfExaminationStatusDto.WAITING_FOR_CHECKUP).forEach {
            if (it.waitingTo == LocalDate.now()) {
                selfExaminationRecordRepository.save(
                    it.copy(
                        status = SelfExaminationStatusDto.WAITING_FOR_RESULT,
                        waitingTo = null
                    )
                )
                notificationService.sendSelfExamIssueResultNotification(
                    setOf(it.account),
                    SexDto.valueOf(it.account.sex)
                )
            }
        }
    }
}
