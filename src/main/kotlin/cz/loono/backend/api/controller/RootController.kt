package cz.loono.backend.api.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.net.URI

@Controller
class RootController {

    @GetMapping
    fun index(): ResponseEntity<Void> {
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
            .location(URI.create("https://www.loono.cz/"))
            .build()
    }
}
