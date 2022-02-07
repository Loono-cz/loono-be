package cz.loono.backend.db.repository

import cz.loono.backend.db.model.ServerProperties
import org.junit.jupiter.api.Test
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class ServerPropertiesRepositoryTest(
    private val serverPropsRepo: ServerPropertiesRepository
) {

    @Test
    fun `findAllSuperUserNameAndPassword gets just SuperUser`() {
        val serverProperties = ServerProperties()
        serverPropsRepo.save(serverProperties)

        val superUsers: Set<SuperUser> = serverPropsRepo.getSuperUserNameAndPassword()

        assert(superUsers.size == 1)
        assert(superUsers.first().superUserName == serverProperties.superUserName)
        assert(superUsers.first().superUserPassword == serverProperties.superUserPassword)
    }
}
