package cz.loono.backend.api.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class SwaggerController {

    @GetMapping(value = ["v3/api-docs"], produces = ["application/json"])
    @ResponseBody
    fun getOpenAPI(): String {
        return javaClass.getResource("/doc/openapi.json").readText()
    }
}
