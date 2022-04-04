package cz.loono.backend.schedule

import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.service.PushNotificationService
import cz.loono.backend.db.repository.ExaminationRecordRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class ComingAndPassedExamNotificationTask(
    private val examinationRecordRepository: ExaminationRecordRepository,
    private val notificationService: PushNotificationService
) : DailySchedulerTask {

    override fun run() {
        val now = LocalDateTime.now()
        val plannedExams = examinationRecordRepository.findAllByStatus(ExaminationStatusDto.NEW)
        plannedExams.forEach { record ->
            record.plannedDate?.let {
                val hours = ChronoUnit.HOURS.between(record.plannedDate, now)
                if (hours in 25..47) {
                    notificationService.sendComingVisitNotification(
                        setOf(record.account),
                        record.type,
                        "${record.plannedDate.hour}:${record.plannedDate.minute}"
                    )
                }
                if (hours in -24..0) {
                    notificationService.sendCompletionNotification(
                        setOf(record.account),
                        "${record.plannedDate.hour}:${record.plannedDate.minute}",
                        record.type
                    )
                }
            }
        }
    }
}
