package cz.loono.backend.schedule

import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationTypeDto
import cz.loono.backend.createAccount
import cz.loono.backend.db.model.SelfExaminationRecord
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.time.LocalDate

class SelfExaminationWaitingTaskTest {

    private var selfExaminationRecordRepository: SelfExaminationRecordRepository = mock()

    @Test
    fun `still waiting`() {
        val selfExaminationWaitingTask = SelfExaminationWaitingTask(selfExaminationRecordRepository)
        `when`(selfExaminationRecordRepository.findAllByStatus(any()))
            .thenReturn(
                setOf(
                    SelfExaminationRecord(
                        type = SelfExaminationTypeDto.BREAST,
                        account = createAccount(),
                        status = SelfExaminationStatusDto.WAITING_FOR_CHECKUP,
                        waitingTo = LocalDate.now().plusDays(10)
                    )
                )
            )

        selfExaminationWaitingTask.run()

        verify(selfExaminationRecordRepository, times(0)).save(any())
    }

    @Test
    fun `waiting finished`() {
        val selfExaminationWaitingTask = SelfExaminationWaitingTask(selfExaminationRecordRepository)
        `when`(selfExaminationRecordRepository.findAllByStatus(any()))
            .thenReturn(
                setOf(
                    SelfExaminationRecord(
                        type = SelfExaminationTypeDto.BREAST,
                        account = createAccount(),
                        status = SelfExaminationStatusDto.WAITING_FOR_CHECKUP,
                        waitingTo = LocalDate.now()
                    )
                )
            )

        selfExaminationWaitingTask.run()

        verify(selfExaminationRecordRepository, times(1)).save(any())
    }
}
