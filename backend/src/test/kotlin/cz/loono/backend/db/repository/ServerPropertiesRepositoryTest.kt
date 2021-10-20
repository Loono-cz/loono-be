package cz.loono.backend.db.repository

import cz.loono.backend.db.model.ServerProperties
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
internal class ServerPropertiesRepositoryTest {

    @Autowired
    private lateinit var serverPropsRepo: ServerPropertiesRepository

    @Test
    fun `findAllSuperUserNameAndPassword gets just SuperUser`() {
        val serverProperties = ServerProperties()
        serverPropsRepo.save(serverProperties)

        val superUsers: List<SuperUser> = serverPropsRepo.getSuperUserNameAndPassword()

        assert(superUsers.size == 1)
        assert(superUsers[0].superUserName == serverProperties.superUserName)
        assert(superUsers[0].superUserPassword == serverProperties.superUserPassword)
    }

    @Test
    fun `findAllUpdateInterval gets just OpenDataProperties`() {
        val serverProperties = ServerProperties()
        serverPropsRepo.save(serverProperties)

        val openDataProperties: List<OpenDataProperties> = serverPropsRepo.getUpdateInterval()

        assert(openDataProperties.size == 1)
        assert(openDataProperties[0].updateInterval == serverProperties.updateInterval)
    }
}
