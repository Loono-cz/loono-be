package cz.loono.backend

import cz.loono.backend.controller.HelloController
import org.junit.jupiter.api.Test

class HelloControllerTest {

    @Test
    fun firstTest() {
        val list = HelloController().index()
        assert(list.size == 3)
    }
}
