package cz.loono.backend.api.controller

import cz.loono.backend.api.dto.AccountDto
import cz.loono.backend.api.dto.SettingsDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.dto.UserDto
import cz.loono.backend.api.dto.UserPatchDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.service.AccountService
import cz.loono.backend.createAccount
import cz.loono.backend.createBasicUser
import cz.loono.backend.data.model.Settings
import cz.loono.backend.data.model.UserAuxiliary
import cz.loono.backend.data.repository.AccountRepository
import cz.loono.backend.let3
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import java.time.LocalDate

/**
 * TODO configure in-memory database
 *  LOON-191
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class AccountControllerTest {

    @Autowired
    private lateinit var repo: AccountRepository

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
        val service = AccountService(repo)
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
    fun `updateSettings with existing account`() {
        // Arrange
        val service = AccountService(repo)
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
        val actualDomainSettings = repo.findByIdOrNull(existingAccount.uid)!!.settings

        assertEquals(expectedDomainSettings, actualDomainSettings)
    }

    @Test
    fun `updateUserAuxiliary with existing account`() {
        // Arrange
        val service = AccountService(repo)
        val controller = AccountController(service, repo)
        val basicUser = createBasicUser()
        val existingAccount = createAccount()
        repo.save(existingAccount)
        val userPatch = UserPatchDto()
        // Sanity precondition check that they aren't already the same - that would lead to a false positive test!
        val userDtoFromPatch = UserDto(
            basicUser.uid, basicUser.email, basicUser.name,
            userPatch.sex,
            birthdateMonth = userPatch.birthdateMonth,
            birthdateYear = userPatch.birthdateYear,
            preferredEmail = userPatch.preferredEmail,
        )
        assertNotEquals(controller.getAccount(basicUser).user, userDtoFromPatch)

        // Act
        val actualDto = controller.updateUserAuxiliary(basicUser, userPatch)

        // Assert DTO shape
        val expectedDto = AccountDto(
            userDtoFromPatch, // <-- This is under test
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
            preferredEmail = userPatch.preferredEmail,
            sex = userPatch.sex?.name,
            birthdate = let3(userPatch.birthdateYear, userPatch.birthdateMonth, 1, LocalDate::of)
        )
        val actualDomainUser = repo.findByIdOrNull(basicUser.uid)!!.userAuxiliary

        assertEquals(expectedDomainUser, actualDomainUser)
    }
}
