package cz.loono.backend.api.v1

import cz.loono.backend.api.Attributes
import cz.loono.backend.api.BasicUser
import cz.loono.backend.api.dto.ConsultancyFormContentDto
import cz.loono.backend.api.service.ConsultancyFormService
import org.springframework.http.MediaType
import org.springframework.jmx.export.annotation.ManagedOperation
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/v1/consultancyForm", produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["app-version"])
class ConsultancyFormController(
    private val consultancyFormService: ConsultancyFormService,
) {

    @PostMapping
    @ManagedOperation
    fun updateOrCreate(
        @RequestAttribute(Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser,

        @RequestBody
        content: ConsultancyFormContentDto
    ) = consultancyFormService.sendEmailQuestion(basicUser.uid, content)
}