package cz.loono.backend.api.controller

import cz.loono.backend.auth.GoogleAPIAuthentication
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {
    @GetMapping
    fun index(): String = "Welcome on the Loono Backend!"

    // Testing Google Auth WIP
    @GetMapping(value = ["/auth"])
    @PreAuthorize("hasRole('ACTUATOR')")
    fun auth(@RequestParam("token") token: String): String {
        return GoogleAPIAuthentication().verifyUser(token)
    }
}
