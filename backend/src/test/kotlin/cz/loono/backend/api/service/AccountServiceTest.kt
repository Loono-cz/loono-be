package cz.loono.backend.api.service

import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.data.model.Account
import cz.loono.backend.data.model.Settings
import cz.loono.backend.data.model.UserAuxiliary
import cz.loono.backend.data.repository.AccountRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate

/**
 * TODO configure in-memory database
 *  LOON-191
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class AccountServiceTest {

    @Autowired
    private lateinit var repo: AccountRepository

    @Test
    fun `ensureAccountExists with missing account`() {
        val service = AccountService(repo)

        assertFalse(repo.existsByUid("uid"))

        service.ensureAccountExists("uid")

        assertTrue(repo.existsByUid("uid"))
    }

    @Test
    fun `ensureAccountExists with existing account`() {
        val initialAccount = Account("uid", points = 1000)
        repo.save(initialAccount)

        val service = AccountService(repo)

        service.ensureAccountExists("uid")

        // WTF is this check.
        // We could check some of the Account content against the initial Account to make sure content didn't change.
        checkNotNull(repo.findByIdOrNull("uid"))
    }

    @Test
    fun `updateSettings with missing account`() {
        val service = AccountService(repo)
        val settings = Settings(
            leaderboardAnonymizationOptIn = false,
            appointmentReminderEmailsOptIn = false,
            newsletterOptIn = true
        )

        val ex = assertThrows<IllegalStateException> {
            service.updateSettings("uid", settings)
        }
        assertEquals("Tried to update Account Settings for uid: uid but no such account exists.", ex.message)
    }

    @Test
    fun `updateSettings with existing account`() {
        val initialAccount = Account("uid", points = 1000)
        repo.save(initialAccount)
        val service = AccountService(repo)

        val newSettings = Settings(
            leaderboardAnonymizationOptIn = false,
            appointmentReminderEmailsOptIn = false,
            newsletterOptIn = true
        )
        // Sanity precondition check!
        assertNotEquals(initialAccount.settings, newSettings)

        val updatedAccount = service.updateSettings("uid", newSettings)
        val persistedUpdatedAccount = repo.findByIdOrNull("uid") ?: fail("Account should be persisted.")

        assertEquals(newSettings, updatedAccount.settings)
        assertEquals(newSettings, persistedUpdatedAccount.settings)
    }

    @Test
    fun `updateUserAuxiliary with missing account`() {
        val service = AccountService(repo)
        val aux = UserAuxiliary(
            preferredEmail = "zilvar@example.com",
            sex = SexDto.MALE.name,
            birthdate = LocalDate.of(2000, 1, 1)
        )

        val ex = assertThrows<IllegalStateException> {
            service.updateUserAuxiliary("uid", aux)
        }
        assertEquals("Tried to update User Auxiliary for uid: uid but no such account exists.", ex.message)
    }

    @Test
    fun `updateUserAuxiliary with existing account`() {
        val initialAccount = Account("uid", points = 1000)
        repo.save(initialAccount)
        val service = AccountService(repo)

        val newAux = UserAuxiliary(
            preferredEmail = "zilvar@example.com",
            sex = SexDto.MALE.name,
            birthdate = LocalDate.of(2000, 1, 1)
        )
        // Sanity precondition check!
        assertNotEquals(initialAccount.userAuxiliary, newAux)

        val updatedAccount = service.updateUserAuxiliary("uid", newAux)
        val persistedUpdatedAccount = repo.findByIdOrNull("uid") ?: fail("Account should be persisted.")

        assertEquals(newAux, updatedAccount.userAuxiliary)
        assertEquals(newAux, persistedUpdatedAccount.userAuxiliary)
    }
}
