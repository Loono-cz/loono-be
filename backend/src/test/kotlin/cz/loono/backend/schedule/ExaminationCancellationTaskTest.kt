package cz.loono.backend.schedule

import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.service.ExaminationInterval
import cz.loono.backend.api.service.ExaminationRecordService
import cz.loono.backend.api.service.PreventionService
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.ExaminationRecord
import cz.loono.backend.db.model.UserAuxiliary
import cz.loono.backend.db.repository.ExaminationRecordRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.time.LocalDate
import java.time.LocalDateTime

class ExaminationCancellationTaskTest {

    private var preventionService: PreventionService = mock()

    private var examinationRecordService: ExaminationRecordService = mock()

    private var examinationRecordRepository: ExaminationRecordRepository = mock()

    @Test
    fun `examination cancellation`() {
        val examinationCancellationTask = ExaminationCancellationTask(
            preventionService,
            examinationRecordService,
            examinationRecordRepository
        )
        `when`(preventionService.getExaminationRequests(any())).thenReturn(definitionList())
        `when`(examinationRecordRepository.findAll())
            .thenReturn(
                listOf(
                    ExaminationRecord(
                        type = ExaminationTypeDto.GENERAL_PRACTITIONER,
                        plannedDate = LocalDateTime.now().minusYears(2),
                        status = ExaminationStatusDto.NEW,
                        uuid = "1",
                        account = getAccount()
                    )
                )
            )

        examinationCancellationTask.run()

        verify(examinationRecordService, times(1)).cancelExam("1", "2")
    }

    @Test
    fun `filter examinations to cancel`() {
        val examinationCancellationTask = ExaminationCancellationTask(
            preventionService,
            examinationRecordService,
            examinationRecordRepository
        )
        `when`(preventionService.getExaminationRequests(any())).thenReturn(definitionList())
        `when`(examinationRecordRepository.findAll())
            .thenReturn(
                listOf(
                    ExaminationRecord(
                        type = ExaminationTypeDto.GENERAL_PRACTITIONER,
                        plannedDate = LocalDateTime.now().minusYears(2),
                        status = ExaminationStatusDto.CONFIRMED,
                        uuid = "1",
                        account = getAccount()
                    ),
                    ExaminationRecord(
                        type = ExaminationTypeDto.DENTIST,
                        plannedDate = null,
                        status = ExaminationStatusDto.NEW,
                        uuid = "1",
                        account = getAccount()
                    )
                )

            )

        examinationCancellationTask.run()

        verify(examinationRecordService, times(0)).cancelExam("1", "2")
    }

    @Test
    fun `examination is valid`() {
        val examinationCancellationTask = ExaminationCancellationTask(
            preventionService,
            examinationRecordService,
            examinationRecordRepository
        )
        `when`(preventionService.getExaminationRequests(any())).thenReturn(definitionList())
        `when`(examinationRecordRepository.findAll())
            .thenReturn(
                listOf(
                    ExaminationRecord(
                        type = ExaminationTypeDto.GENERAL_PRACTITIONER,
                        plannedDate = LocalDateTime.now().minusYears(1),
                        status = ExaminationStatusDto.NEW,
                        uuid = "1",
                        account = getAccount()
                    )
                )
            )

        examinationCancellationTask.run()

        verify(examinationRecordService, times(0)).cancelExam("1", "2")
    }

    private fun definitionList(): List<ExaminationInterval> =
        listOf(
            ExaminationInterval(ExaminationTypeDto.GENERAL_PRACTITIONER, 2, 1),
            ExaminationInterval(ExaminationTypeDto.DENTIST, 1, 9)
        )

    private fun getAccount(): Account =
        Account(
            uid = "2",
            userAuxiliary = UserAuxiliary(
                sex = SexDto.MALE.value,
                birthdate = LocalDate.of(1988, 7, 30)
            )
        )
}
