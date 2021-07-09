package cz.loono.backend.api.controller

import org.junit.jupiter.api.Test

class HelloControllerTest {

    @Test
    fun firstTest() {
        assert(HelloController().index().equals("Welcome on the Loono Backend!"))
    }
}
