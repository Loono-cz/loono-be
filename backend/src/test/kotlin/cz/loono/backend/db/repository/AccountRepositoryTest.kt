package cz.loono.backend.db.repository

import cz.loono.backend.createAccount
import cz.loono.backend.db.model.Account
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class AccountRepositoryTest {

    @Autowired
    private lateinit var accountRepo: AccountRepository

    @Test
    fun `existsByUid with existing account`() {
        accountRepo.save(Account(uid = "uid"))

        assertTrue(accountRepo.existsByUid("uid"))
    }

    @Test
    fun `existsByUid with missing account`() {
        assertFalse(accountRepo.existsByUid("uid"))
    }

    @Test
    fun `findByUid with missing account`() {
        assertNull(accountRepo.findByUid("uid"))
        assertFalse(accountRepo.existsByUid("uid"))
    }

    @Test
    fun `findByUid with existing account`() {
        accountRepo.save(createAccount())
        accountRepo.save(createAccount("uid2"))

        val account = accountRepo.findByUid("uid")
        assertNotNull(account)
        assertEquals("uid", account!!.uid)
    }
}
