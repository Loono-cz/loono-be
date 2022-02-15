package cz.loono.backend.schedule

import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.service.ExaminationRecordService
import cz.loono.backend.api.service.PreventionService
import cz.loono.backend.db.repository.ExaminationRecordRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ExaminationCancellationTask(
    private val preventionService: PreventionService,
    private val examinationRecordService: ExaminationRecordService,
    private val examinationRecordRepository: ExaminationRecordRepository
) : SchedulerTask {

    override fun run() {
        val exams = examinationRecordRepository.findAll()
            .filter { it.status == ExaminationStatusDto.NEW && it.plannedDate != null }

        val now = LocalDateTime.now()
        exams.forEach { exam ->
            val interval = preventionService.getExaminationRequests(exam.account)
                .first { exam.type == it.examinationType }.intervalYears
            val deadline = exam.plannedDate?.plusMonths((interval * 12L) - 2)
            if (now.isAfter(deadline)) {
                examinationRecordService.cancelExam(exam.uuid, exam.account.uid)
            }
        }
    }
}
