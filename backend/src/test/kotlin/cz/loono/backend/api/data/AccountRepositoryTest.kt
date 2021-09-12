package cz.loono.backend.api.data

import cz.loono.backend.data.model.Account
import cz.loono.backend.data.repository.AccountRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

/**
 * TODO configure in-memory database
 *  LOON-191
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTest {

    @Autowired
    private lateinit var accountRepo: AccountRepository

    @Test
    fun `existsByUid with existing user`() {
        accountRepo.save(Account(uid = "uid"))

        assertTrue(accountRepo.existsByUid("uid"))
    }

    @Test
    fun `existsByUid with missing user`() {
        assertFalse(accountRepo.existsByUid("uid"))
    }
}
