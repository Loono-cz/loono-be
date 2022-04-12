package cz.loono.backend.api.v1

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class SwaggerController {

    @GetMapping(value = ["v1/api-docs"], produces = ["application/json"])
    @ResponseBody
    fun getOpenAPI(): String = javaClass
        .getResourceAsStream("/doc/openapi.json")
        .bufferedReader().use {
            it.readText()
        }
}
