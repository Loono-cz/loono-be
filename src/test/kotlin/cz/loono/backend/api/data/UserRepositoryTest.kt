package cz.loono.backend.api.data

import cz.loono.backend.data.model.User
import cz.loono.backend.data.repository.UserRepository
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
class UserRepositoryTest {

    @Autowired
    private lateinit var userRepo: UserRepository

    @Test
    fun `existsByUid with existing user`() {
        userRepo.save(User(uid = "uid"))

        assertTrue(userRepo.existsByUid("uid"))
    }

    @Test
    fun `existsByUid with missing user`() {
        assertFalse(userRepo.existsByUid("uid"))
    }
}
