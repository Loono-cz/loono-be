package cz.loono.backend.api.v1

import cz.loono.backend.api.service.EmailService
import org.springframework.http.MediaType
import org.springframework.jmx.export.annotation.ManagedOperation
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/v1/testEmail", produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["app-version"])
class EmailController(
    private val emailService: EmailService,
) {

    @PostMapping
    @ManagedOperation
    fun updateOrCreate() = emailService.addContactToContactList()
}