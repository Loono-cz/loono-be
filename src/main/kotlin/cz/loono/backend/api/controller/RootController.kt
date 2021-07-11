package cz.loono.backend.api.controller

import cz.loono.backend.auth.GoogleAPIAuthentication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.net.URI

@Controller
class RootController {

    @GetMapping
    fun index(): ResponseEntity<Void> {
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
            .location(URI.create("https://www.loono.cz/"))
            .build()
    }

    // Testing Google Auth WIP
    @GetMapping(value = ["/auth"])
    @PreAuthorize("hasRole('ACTUATOR')")
    fun auth(@RequestParam("token") token: String): String {
        return GoogleAPIAuthentication().verifyUser(token)
    }
}
