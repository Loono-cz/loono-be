package cz.loono.backend.schedule

import cz.loono.backend.api.dto.BadgeTypeDto
import cz.loono.backend.api.dto.ExaminationPreventionStatusDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.PreventionStatusDto
import cz.loono.backend.api.service.AccountService
import cz.loono.backend.api.service.PreventionService
import cz.loono.backend.api.service.PushNotificationService
import cz.loono.backend.createAccount
import cz.loono.backend.db.model.Account
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.time.LocalDate
import java.time.LocalDateTime

class PreventionReminderTaskTest {

    private val accountService: AccountService = mock()
    private val preventionService: PreventionService = mock()
    private val notificationService: PushNotificationService = mock()

    @Test
    fun `two users should be notified`() {
        val preventionReminderTask = PreventionReminderTask(accountService, preventionService, notificationService)
        val user1 = createAccount(uid = "1", created = LocalDate.now().minusMonths(3))
        val user2 = createAccount(uid = "2", created = LocalDate.now().minusYears(1))
        val user3 = createAccount(uid = "3", created = LocalDate.now().minusMonths(2))

        `when`(accountService.paginateOverAccounts(any())).then { invocation ->
            @Suppress("UNCHECKED_CAST")
            (invocation.arguments[0] as (List<Account>) -> Unit).invoke(listOf(user1, user2, user3))
        }
        `when`(preventionService.getPreventionStatus(any())).thenReturn(
            PreventionStatusDto(
                listOf(
                    ExaminationPreventionStatusDto(
                        uuid = "1",
                        examinationType = ExaminationTypeDto.GENERAL_PRACTITIONER,
                        intervalYears = 2,
                        firstExam = false,
                        priority = 1,
                        state = ExaminationStatusDto.CONFIRMED,
                        count = 1,
                        points = 200,
                        badge = BadgeTypeDto.HEADBAND,
                        plannedDate = null,
                        lastConfirmedDate = LocalDateTime.now().minusYears(2)
                    )
                ),
                emptyList()
            )
        )

        preventionReminderTask.run()

        verify(notificationService, times(1)).sendPreventionNotification(setOf(user1, user2))
    }

    @Test
    fun `user with first exam`() {
        val preventionReminderTask = PreventionReminderTask(accountService, preventionService, notificationService)
        val user = createAccount(uid = "1", created = LocalDate.now().minusMonths(3))

        `when`(accountService.paginateOverAccounts(any())).then { invocation ->
            @Suppress("UNCHECKED_CAST")
            (invocation.arguments[0] as (List<Account>) -> Unit).invoke(listOf(user))
        }
        `when`(preventionService.getPreventionStatus(any())).thenReturn(
            PreventionStatusDto(
                listOf(
                    ExaminationPreventionStatusDto(
                        uuid = "1",
                        examinationType = ExaminationTypeDto.GENERAL_PRACTITIONER,
                        intervalYears = 2,
                        firstExam = false,
                        priority = 1,
                        state = ExaminationStatusDto.UNKNOWN,
                        count = 1,
                        points = 200,
                        badge = BadgeTypeDto.HEADBAND,
                        plannedDate = null,
                        lastConfirmedDate = LocalDateTime.now().minusYears(2)
                    )
                ),
                emptyList()
            )
        )

        preventionReminderTask.run()

        verify(notificationService, times(1)).sendPreventionNotification(setOf(user))
    }

    @Test
    fun `without notification`() {
        val preventionReminderTask = PreventionReminderTask(accountService, preventionService, notificationService)
        val user = createAccount(uid = "1", created = LocalDate.now().minusMonths(3))

        `when`(accountService.paginateOverAccounts(any())).then { invocation ->
            @Suppress("UNCHECKED_CAST")
            (invocation.arguments[0] as (List<Account>) -> Unit).invoke(listOf(user))
        }
        `when`(preventionService.getPreventionStatus(any())).thenReturn(
            PreventionStatusDto(
                listOf(
                    ExaminationPreventionStatusDto(
                        uuid = "1",
                        examinationType = ExaminationTypeDto.GENERAL_PRACTITIONER,
                        intervalYears = 2,
                        firstExam = false,
                        priority = 1,
                        state = ExaminationStatusDto.UNKNOWN,
                        count = 1,
                        points = 200,
                        badge = BadgeTypeDto.HEADBAND,
                        plannedDate = null,
                        lastConfirmedDate = LocalDateTime.now()
                    )
                ),
                emptyList()
            )
        )

        preventionReminderTask.run()

        verify(notificationService, times(0)).sendPreventionNotification(setOf(user))
    }
}
