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

class SelfExaminationIntervalClosingTaskTest {

    private var selfExaminationRecordRepository: SelfExaminationRecordRepository = mock()

    @Test
    fun `closing because time left`() {
        val selfExaminationIntervalClosingTask = SelfExaminationIntervalClosingTask(selfExaminationRecordRepository)
        `when`(selfExaminationRecordRepository.findAllByStatus(any()))
            .thenReturn(
                setOf(
                    SelfExaminationRecord(
                        type = SelfExaminationTypeDto.BREAST,
                        account = createAccount(),
                        status = SelfExaminationStatusDto.PLANNED,
                        dueDate = LocalDate.now().minusDays(1)
                    )
                )
            )

        selfExaminationIntervalClosingTask.run()

        verify(selfExaminationRecordRepository, times(2)).save(any())
    }

    @Test
    fun `no closing of self-exam`() {
        val selfExaminationIntervalClosingTask = SelfExaminationIntervalClosingTask(selfExaminationRecordRepository)
        `when`(selfExaminationRecordRepository.findAllByStatus(any()))
            .thenReturn(
                setOf(
                    SelfExaminationRecord(
                        type = SelfExaminationTypeDto.BREAST,
                        account = createAccount(),
                        status = SelfExaminationStatusDto.PLANNED,
                        dueDate = LocalDate.now()
                    )
                )
            )

        selfExaminationIntervalClosingTask.run()

        verify(selfExaminationRecordRepository, times(0)).save(any())
    }
}
