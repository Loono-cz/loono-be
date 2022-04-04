package cz.loono.backend.api.controller

import cz.loono.backend.api.dto.AccountDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.service.AccountService
import cz.loono.backend.api.service.ExaminationRecordService
import cz.loono.backend.api.service.FirebaseAuthService
import cz.loono.backend.createAccount
import cz.loono.backend.createBasicUser
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(properties = ["spring.profiles.active=test"])
@Transactional
class AccountControllerTest(
    private val repo: AccountRepository,
    private val examinationRecordService: ExaminationRecordService,
    private val examinationRecordRepository: ExaminationRecordRepository,
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository,
    @Value("\${task.badge-downgrade.page-size}")
    private val pageSize: Int,
) {

    private val firebaseAuthService: FirebaseAuthService = mock()

    @Test
    fun `getAccount with missing account`() {
        val service = mock<AccountService>()
        val controller = AccountController(service, repo)

        val ex = assertThrows<LoonoBackendException> {
            controller.getAccount(createBasicUser(uid = "non-existing"))
        }

        assertEquals(HttpStatus.NOT_FOUND, ex.status)
        assertNull(ex.errorCode)
        assertNull(ex.errorMessage)
    }

    @Test
    fun `getAccount with existing account`() {
        // Arrange
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize
        )
        val controller = AccountController(service, repo)
        val basicUser = createBasicUser()
        val existingAccount = createAccount()
        repo.save(existingAccount)

        // Act
        val actualDto = controller.getAccount(basicUser)

        // Assert
        val expectedDto = AccountDto(
            uid = basicUser.uid,
            nickname = existingAccount.nickname,
            sex = existingAccount.sex.let(SexDto::valueOf),
            birthdate = existingAccount.birthdate,
            preferredEmail = existingAccount.preferredEmail,
            profileImageUrl = existingAccount.profileImageUrl,
            leaderboardAnonymizationOptIn = existingAccount.leaderboardAnonymizationOptIn,
            appointmentReminderEmailsOptIn = existingAccount.appointmentReminderEmailsOptIn,
            newsletterOptIn = existingAccount.newsletterOptIn,
            points = existingAccount.points,
            badges = emptyList()
        )
        assertEquals(expectedDto, actualDto)
    }

    @Test
    fun `delete non-existing account`() {
        // Arrange
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize
        )
        val controller = AccountController(service, repo)

        // Act
        assertThrows<LoonoBackendException> {
            controller.deleteAccount(createBasicUser())
        }
    }
}
