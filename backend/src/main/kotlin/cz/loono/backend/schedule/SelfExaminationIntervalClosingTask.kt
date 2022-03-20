package cz.loono.backend.schedule

import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.db.model.SelfExaminationRecord
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SelfExaminationIntervalClosingTask(
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository
) : DailySchedulerTask {

    override fun run() {
        val now = LocalDate.now()
        selfExaminationRecordRepository.findAllByStatus(SelfExaminationStatusDto.PLANNED).forEach {
            if (it.dueDate?.isBefore(now) == true) {
                selfExaminationRecordRepository.save(it.copy(status = SelfExaminationStatusDto.MISSED))
                selfExaminationRecordRepository.save(
                    SelfExaminationRecord(
                        type = it.type,
                        status = SelfExaminationStatusDto.PLANNED,
                        account = it.account,
                        dueDate = it.dueDate.plusMonths(1)
                    )
                )
            }
        }
    }
}
