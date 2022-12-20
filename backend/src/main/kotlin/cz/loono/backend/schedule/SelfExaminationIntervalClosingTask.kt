package cz.loono.backend.schedule

import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.db.model.CronControl
import cz.loono.backend.db.model.SelfExaminationRecord
import cz.loono.backend.db.repository.CronControlRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SelfExaminationIntervalClosingTask(
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository,
    private val cronControlRepository: CronControlRepository
) : DailySchedulerTask {

    override fun run() {
        try {
            val now = LocalDate.now()
            selfExaminationRecordRepository.findAllByStatus(SelfExaminationStatusDto.PLANNED).forEach {
                val dueDatePlus3Days = it.dueDate?.plusDays(3)
                if (dueDatePlus3Days?.isBefore(now) == true) {
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
            cronControlRepository.save(
                CronControl(
                    functionName = "SelfExaminationIntervalClosingTask",
                    status = "PASSED",
                    message = null,
                    createdAt = LocalDate.now().toString()
                )
            )
        } catch (e: Exception) {
            cronControlRepository.save(
                CronControl(
                    functionName = "SelfExaminationIntervalClosingTask",
                    status = "ERROR",
                    message = "$e",
                    createdAt = LocalDate.now().toString()
                )
            )
        }
    }
}
