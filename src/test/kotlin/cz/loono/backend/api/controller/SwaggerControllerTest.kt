package cz.loono.backend.api.controller

import org.junit.jupiter.api.Test

class SwaggerControllerTest {

    @Test
    fun getDocs() {
        val original = javaClass.getResource("/doc/openapi.json").readText()

        val docs = SwaggerController().getOpenAPI()

        assert(
            docs == original
        )
    }
}
