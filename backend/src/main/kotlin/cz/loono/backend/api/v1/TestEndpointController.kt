package cz.loono.backend.api.v1

import cz.loono.backend.api.dto.ExaminationIdDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.service.TestEndpointService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/testCall", produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["app-version"])
class TestEndpointController(private val testEndpointService: TestEndpointService) {

    @PostMapping
    fun getTestEndpoint(
        @RequestBody
        idDto: ExaminationIdDto
    ): String =
        if (!idDto.uuid.isNullOrEmpty()) {
            testEndpointService.getTestEndpoint(idDto.uuid)
        } else {
            throw LoonoBackendException(HttpStatus.BAD_REQUEST)
        }
}
