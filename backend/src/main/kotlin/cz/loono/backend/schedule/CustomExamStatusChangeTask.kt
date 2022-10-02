package cz.loono.backend.schedule

import cz.loono.backend.api.dto.ExaminationCategoryTypeDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.db.repository.ExaminationRecordRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CustomExamStatusChangeTask(
    private val examinationRecordRepository: ExaminationRecordRepository
) : DailySchedulerTask {

    override fun run() {
        val now = LocalDateTime.now()
        val plannedExams = examinationRecordRepository.findAllByStatus(status = ExaminationStatusDto.NEW)
        val customExams = plannedExams.filter { it.examinationCategoryType == ExaminationCategoryTypeDto.CUSTOM }
        customExams.forEach { record ->
            record.plannedDate?.let { plannedDate ->
                if (now.isAfter(plannedDate)) {
                    examinationRecordRepository.save(record.copy(status = ExaminationStatusDto.CONFIRMED))
                }
            }
        }
    }
}