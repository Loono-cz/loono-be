package cz.loono.backend.api.controller

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class RootControllerTest {

    @Test
    fun redirectTest() {
        val response = RootController().index()
        assert(response.statusCode == HttpStatus.PERMANENT_REDIRECT)
        assert(response.headers.location.toString().equals("https://www.loono.cz/"))
    }
}
