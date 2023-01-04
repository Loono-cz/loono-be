package cz.loono.backend.schedule

import cz.loono.backend.api.dto.ExaminationCategoryTypeDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.service.ExaminationRecordService
import cz.loono.backend.api.service.PreventionService
import cz.loono.backend.db.model.CronLog
import cz.loono.backend.db.repository.CronLogRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class ExaminationCancellationTask(
    private val preventionService: PreventionService,
    private val examinationRecordService: ExaminationRecordService,
    private val examinationRecordRepository: ExaminationRecordRepository,
    private val cronLogRepository: CronLogRepository
) : DailySchedulerTask {

    override fun run() {
        try {
            val exams = examinationRecordRepository.findAll()
                .filter { it.status == ExaminationStatusDto.NEW && it.plannedDate != null }

            val now = LocalDateTime.now()
            val mandatoryExams = exams.filter { it.examinationCategoryType == ExaminationCategoryTypeDto.MANDATORY }
            mandatoryExams.forEach { exam ->
                // TODO - custom exam
                val interval = preventionService.getExaminationRequests(exam.account)
                    .first { exam.type == it.examinationType }.intervalYears
                val deadline = exam.plannedDate?.plusMonths((interval * 12L) - 2)
                if (now.isAfter(deadline)) {
                    exam.uuid?.let {
                        examinationRecordService.cancelExam(exam.uuid, exam.account.uid)
                    }
                }
            }
            cronLogRepository.save(
                CronLog(
                    functionName = "ExaminationCancellationTask",
                    status = "PASSED",
                    message = null,
                    createdAt = LocalDate.now().toString()
                )
            )
        } catch (e: Exception) {
            cronLogRepository.save(
                CronLog(
                    functionName = "ExaminationCancellationTask",
                    status = "ERROR",
                    message = "$e",
                    createdAt = LocalDate.now().toString()
                )
            )
        }
    }
}
