package cz.loono.backend.schedule

import cz.loono.backend.api.dto.ExaminationCategoryTypeDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.db.model.CronLog
import cz.loono.backend.db.repository.CronLogRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class CustomExamStatusChangeTask(
    private val examinationRecordRepository: ExaminationRecordRepository,
    private val cronLogRepository: CronLogRepository
) : DailySchedulerTask {
    override fun run() {
        try {
            val now = LocalDateTime.now()
            val plannedExams = examinationRecordRepository.findAllByStatus(status = ExaminationStatusDto.NEW)
            val customExams = plannedExams.filter { it.examinationCategoryType == ExaminationCategoryTypeDto.CUSTOM }
            val customExamNonPeriodic = customExams.filter { it.periodicExam == false }
            customExamNonPeriodic.forEach { record ->
                record.plannedDate?.let { plannedDate ->
                    if (now.isAfter(plannedDate)) {
                        examinationRecordRepository.save(record.copy(status = ExaminationStatusDto.CONFIRMED))
                    }
                }
            }
            cronLogRepository.save(
                CronLog(
                    functionName = "CustomExamStatusChangeTask",
                    status = "PASSED",
                    message = null,
                    createdAt = LocalDate.now().toString()
                )
            )
        } catch (e: Exception) {
            cronLogRepository.save(
                CronLog(
                    functionName = "CustomExamStatusChangeTask",
                    status = "ERROR",
                    message = "$e",
                    createdAt = LocalDate.now().toString()
                )
            )
        }
    }
}
