package cz.loono.backend.schedule

import cz.loono.backend.api.dto.BadgeTypeDto
import cz.loono.backend.api.dto.PreventionStatusDto
import cz.loono.backend.api.dto.SelfExaminationPreventionStatusDto
import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationTypeDto
import cz.loono.backend.api.service.PreventionService
import cz.loono.backend.api.service.PushNotificationService
import cz.loono.backend.createAccount
import cz.loono.backend.db.repository.AccountRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.time.LocalDate

class SelfExamReminderTaskTest {

    private val accountRepository: AccountRepository = mock()
    private val preventionService: PreventionService = mock()
    private val notificationService: PushNotificationService = mock()

    @Test
    fun `first self-exam`() {
        val selfExaminationReminderTask = SelfExamReminderTask(accountRepository, preventionService, notificationService)
        `when`(accountRepository.findAll()).thenReturn(listOf(createAccount()))
        `when`(preventionService.getPreventionStatus(any())).thenReturn(
            PreventionStatusDto(
                emptyList(),
                listOf(
                    SelfExaminationPreventionStatusDto(
                        type = SelfExaminationTypeDto.TESTICULAR,
                        plannedDate = null,
                        badge = BadgeTypeDto.SHIELD,
                        points = 50,
                        history = emptyList()
                    )
                )
            )
        )

        selfExaminationReminderTask.run()

        verify(notificationService, times(1)).sendFirstSelfExamNotification(any(), any())
    }

    @Test
    fun `self-exam trigger`() {
        val selfExaminationReminderTask = SelfExamReminderTask(accountRepository, preventionService, notificationService)
        `when`(accountRepository.findAll()).thenReturn(listOf(createAccount()))
        `when`(preventionService.getPreventionStatus(any())).thenReturn(
            PreventionStatusDto(
                emptyList(),
                listOf(
                    SelfExaminationPreventionStatusDto(
                        type = SelfExaminationTypeDto.TESTICULAR,
                        plannedDate = LocalDate.now(),
                        badge = BadgeTypeDto.SHIELD,
                        points = 50,
                        history = listOf(SelfExaminationStatusDto.COMPLETED, SelfExaminationStatusDto.PLANNED)
                    )
                )
            )
        )

        selfExaminationReminderTask.run()

        verify(notificationService, times(1)).sendSelfExamNotification(any(), any())
    }
}
