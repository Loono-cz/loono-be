package cz.loono.backend.api.controller

import cz.loono.backend.api.dto.AccountDto
import cz.loono.backend.api.dto.SettingsDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.dto.UserDto
import cz.loono.backend.api.dto.UserPatchDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.service.AccountService
import cz.loono.backend.api.service.FirebaseAuthService
import cz.loono.backend.createAccount
import cz.loono.backend.createBasicUser
import cz.loono.backend.db.model.Settings
import cz.loono.backend.db.model.UserAuxiliary
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.let3
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.http.HttpStatus
import java.time.LocalDate

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class AccountControllerTest(
    private val repo: AccountRepository
) {

    private val firebaseAuthService: FirebaseAuthService = mock()

    @Test
    fun `getAccount with missing account`() {
        val service = mock<AccountService>()
        val controller = AccountController(service, repo)

        val ex = assertThrows<LoonoBackendException> {
            controller.getAccount(createBasicUser())
        }

        assertEquals(HttpStatus.NOT_FOUND, ex.status)
        assertNull(ex.errorCode)
        assertNull(ex.errorMessage)
    }

    @Test
    fun `getAccount with existing account`() {
        // Arrange
        val service = AccountService(repo, firebaseAuthService)
        val controller = AccountController(service, repo)
        val basicUser = createBasicUser()
        val existingAccount = createAccount()
        repo.save(existingAccount)

        // Act
        val actualDto = controller.getAccount(basicUser)

        // Assert
        val expectedDto = AccountDto(
            UserDto(
                basicUser.uid, basicUser.email, basicUser.name,
                existingAccount.userAuxiliary.sex?.let(SexDto::valueOf),
                birthdateMonth = existingAccount.userAuxiliary.birthdate?.monthValue,
                birthdateYear = existingAccount.userAuxiliary.birthdate?.year,
                preferredEmail = existingAccount.userAuxiliary.preferredEmail,
                profileImageUrl = existingAccount.userAuxiliary.profileImageUrl
            ),
            SettingsDto(
                existingAccount.settings.leaderboardAnonymizationOptIn,
                existingAccount.settings.appointmentReminderEmailsOptIn,
                existingAccount.settings.newsletterOptIn,
            ),
            existingAccount.points
        )
        assertEquals(expectedDto, actualDto)
    }

    @Test
    fun `delete non-existing account`() {
        // Arrange
        val service = AccountService(repo, firebaseAuthService)
        val controller = AccountController(service, repo)

        // Act
        assertThrows<LoonoBackendException> {
            controller.deleteAccount(createBasicUser())
        }
    }

    @Test
    fun `delete existing account`() {
        // Arrange
        val service = AccountService(repo, firebaseAuthService)
        val controller = AccountController(service, repo)
        val basicUser = createBasicUser()
        val existingAccount = createAccount()
        repo.save(existingAccount)

        // Act
        controller.deleteAccount(basicUser)

        // Assert
        assert(repo.findByUid(basicUser.uid) == null)
        assert(repo.count() == 0L)
    }

    @Test
    fun `updateSettings with existing account`() {
        // Arrange
        val service = AccountService(repo, firebaseAuthService)
        val controller = AccountController(service, repo)
        val basicUser = createBasicUser()
        val existingAccount = createAccount()
        repo.save(existingAccount)
        val newSettings = SettingsDto()
        // Sanity precondition check that they aren't already the same - that would lead to a false positive test!
        assertNotEquals(controller.getAccount(basicUser).settings, newSettings)

        // Act
        val actualDto = controller.updateSettings(basicUser, newSettings)

        // Assert DTO shape
        val expectedDto = AccountDto(
            UserDto(
                basicUser.uid, basicUser.email, basicUser.name,
                existingAccount.userAuxiliary.sex?.let(SexDto::valueOf),
                birthdateMonth = existingAccount.userAuxiliary.birthdate?.monthValue,
                birthdateYear = existingAccount.userAuxiliary.birthdate?.year,
                preferredEmail = existingAccount.userAuxiliary.preferredEmail,
                profileImageUrl = existingAccount.userAuxiliary.profileImageUrl
            ),
            newSettings, // <-- This is under test
            existingAccount.points
        )
        assertEquals(expectedDto, actualDto)

        // Assert DB content
        val expectedDomainSettings = Settings(
            leaderboardAnonymizationOptIn = newSettings.leaderboardAnonymizationOptIn,
            appointmentReminderEmailsOptIn = newSettings.appointmentReminderEmailsOptIn,
            newsletterOptIn = newSettings.newsletterOptIn
        )
        val actualDomainSettings = repo.findByUid(existingAccount.uid)!!.settings

        assertEquals(expectedDomainSettings, actualDomainSettings)
    }

    @Test
    fun `updateUserAuxiliary with existing account`() {
        // Arrange
        val service = AccountService(repo, firebaseAuthService)
        val controller = AccountController(service, repo)
        val basicUser = createBasicUser()
        val existingAccount = createAccount()
        repo.save(existingAccount)
        // Sanity precondition check that they aren't already the same - that would lead to a false positive test!
        val userWithAux = UserDto(
            basicUser.uid, basicUser.email, "nickname",
            SexDto.MALE,
            birthdateMonth = 2,
            birthdateYear = 2002,
            preferredEmail = "preferredZilvar@example.net",
            profileImageUrl = "profileImage"
        )
        val userDtoPatch = UserPatchDto(
            nickname = "nickname",
            sex = SexDto.FEMALE,
            birthdateMonth = 2,
            birthdateYear = 2002,
            preferredEmail = "preferredZilvar@example.net",
            profileImageUrl = "profileImage"
        )
        assertNotEquals(controller.getAccount(basicUser).user, userDtoPatch)

        // Act
        val actualDto = controller.updateUserAuxiliary(basicUser, userDtoPatch)

        // Assert DTO shape
        val expectedDto = AccountDto(
            userWithAux, // <-- This is under test
            SettingsDto(
                existingAccount.settings.leaderboardAnonymizationOptIn,
                existingAccount.settings.appointmentReminderEmailsOptIn,
                existingAccount.settings.newsletterOptIn,
            ),
            existingAccount.points
        )
        assertEquals(expectedDto, actualDto)

        // Assert DB content
        val expectedDomainUser = UserAuxiliary(
            nickname = userDtoPatch.nickname,
            preferredEmail = userDtoPatch.preferredEmail,
            sex = SexDto.MALE.value,
            birthdate = let3(userDtoPatch.birthdateYear, userDtoPatch.birthdateMonth, 1, LocalDate::of),
            profileImageUrl = userDtoPatch.profileImageUrl
        )
        val actualDomainUser = repo.findByUid(basicUser.uid)!!.userAuxiliary

        assertEquals(expectedDomainUser, actualDomainUser)
    }
}
