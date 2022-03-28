package cz.loono.backend.security.basic

import cz.loono.backend.db.model.ServerProperties
import cz.loono.backend.db.repository.ServerPropertiesRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(properties = ["spring.profiles.active=test"])
@Transactional
class SuperUserDetailsServiceTest(
    private val serverPropertiesRepository: ServerPropertiesRepository
) {

    @Test
    fun `get loonoAdmin details`() {
        serverPropertiesRepository.save(ServerProperties())
        val superUserDetailsService = SuperUserDetailsService(serverPropertiesRepository)

        val userDetails = superUserDetailsService.loadUserByUsername("loonoAdmin")

        assert(
            userDetails == SuperUserDetails(
                username = "loonoAdmin",
                password = "\$2a\$10\$hx6i9opda20rbC81fJqUj.3mE.xZDB5OV5fApv9WlyEnkFNbFZUh2"
            )
        )
    }

    @Test
    fun `server properties not set`() {
        serverPropertiesRepository.deleteAll()
        val superUserDetailsService = SuperUserDetailsService(serverPropertiesRepository)

        assertThrows<NullPointerException> { superUserDetailsService.loadUserByUsername("loonoAdmin") }
    }
}
