package cz.loono.backend.schedule

import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SelfExaminationWaitingTask(
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository
) : SchedulerTask {

    override fun run() {
        selfExaminationRecordRepository.findAllByStatus(SelfExaminationStatusDto.WAITING_FOR_CHECKUP).forEach {
            if (it.waitingTo == LocalDate.now()) {
                selfExaminationRecordRepository.save(
                    it.copy(
                        status = SelfExaminationStatusDto.WAITING_FOR_RESULT,
                        waitingTo = null
                    )
                )
                // TODO Sending notification
            }
        }
    }
}
