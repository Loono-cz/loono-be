package cz.loono.backend.schedule

import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationTypeDto
import cz.loono.backend.api.service.PushNotificationService
import cz.loono.backend.createAccount
import cz.loono.backend.db.model.SelfExaminationRecord
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.time.LocalDate

class SelfExamReminderTaskTest {

    private val accountRepository: AccountRepository = mock()
    private val notificationService: PushNotificationService = mock()
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository = mock()

    @Test
    fun `first self-exam`() {
        val selfExaminationReminderTask = SelfExamReminderTask(accountRepository, notificationService, selfExaminationRecordRepository)
        `when`(accountRepository.findAll()).thenReturn(listOf(createAccount()))
        `when`(selfExaminationRecordRepository.findAllByAccount(any())).thenReturn(
            setOf()
        )

        selfExaminationReminderTask.run()

        verify(notificationService, times(1)).sendFirstSelfExamNotification(any())
    }

    @Test
    fun `self-exam trigger`() {
        val selfExaminationReminderTask = SelfExamReminderTask(accountRepository, notificationService, selfExaminationRecordRepository)
        `when`(accountRepository.findAll()).thenReturn(listOf(createAccount()))
        `when`(selfExaminationRecordRepository.findAllByAccount(any())).thenReturn(
            setOf(
                SelfExaminationRecord(
                    type = SelfExaminationTypeDto.TESTICULAR,
                    uuid = "test1",
                    status = SelfExaminationStatusDto.PLANNED,
                    account = createAccount(),
                    dueDate = LocalDate.now()
                ),
                SelfExaminationRecord(
                    type = SelfExaminationTypeDto.SKIN,
                    uuid = "test2",
                    status = SelfExaminationStatusDto.PLANNED,
                    account = createAccount(),
                    dueDate = LocalDate.now()
                )
            )
        )

        selfExaminationReminderTask.run()

        verify(notificationService, times(1)).sendSelfExamNotification(any())
    }
}
