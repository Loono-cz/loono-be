package cz.loono.backend.api.controller

import org.junit.jupiter.api.Test

class HelloControllerTest {

    @Test
    fun firstTest() {
        val list = HelloController().index()
        assert(list.size == 3)
    }
}
