package cz.loono.backend.schedule

import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.service.PushNotificationService
import cz.loono.backend.db.model.CronControl
import cz.loono.backend.db.repository.CronControlRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Component
class ComingAndPassedExamNotificationTask(
    private val examinationRecordRepository: ExaminationRecordRepository,
    private val notificationService: PushNotificationService,
    private val cronControlRepository: CronControlRepository
) : DailySchedulerTask {

    override fun run() {
        try {
            val now = LocalDateTime.now()
            val plannedExams = examinationRecordRepository.findAllByStatus(ExaminationStatusDto.NEW)
            val plannedPeriodicExams = plannedExams.filter { it.periodicExam != false }
            plannedPeriodicExams.forEach { record ->
                record.plannedDate?.let {
                    val hours = ChronoUnit.HOURS.between(record.plannedDate, now)
                    if (hours in -47..-24) {
                        notificationService.sendComingVisitNotification(
                            setOf(record.account),
                            record.type,
                            record.plannedDate.plusHours(2).format(DateTimeFormatter.ofPattern("HH:mm")),
                            record.uuid
                        )
                    }
                    if (hours in 0..24) {
                        notificationService.sendCompletionNotification(
                            setOf(record.account),
                            record.plannedDate.plusHours(2).format(DateTimeFormatter.ofPattern("HH:mm")),
                            record.type,
                            record.uuid
                        )
                    }
                }
            }
            cronControlRepository.save(
                CronControl(
                    functionName = "ComingAndPassedExamNotificationTask",
                    status = "PASSED",
                    message = null,
                    createdAt = LocalDate.now().toString()
                )
            )
        } catch (e: Exception) {
            cronControlRepository.save(
                CronControl(
                    functionName = "ComingAndPassedExamNotificationTask",
                    status = "ERROR",
                    message = "$e",
                    createdAt = LocalDate.now().toString()
                )
            )
        }
    }
}
