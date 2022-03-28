package cz.loono.backend

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(properties = ["spring.profiles.active=test"])
class ApplicationTest {

    @Test
    fun `application starts`() {
    }
}
