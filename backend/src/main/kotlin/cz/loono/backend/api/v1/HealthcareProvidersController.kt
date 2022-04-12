package cz.loono.backend.api.v1

import cz.loono.backend.api.dto.HealthcareProviderDetailListDto
import cz.loono.backend.api.dto.HealthcareProviderIdListDto
import cz.loono.backend.api.dto.HealthcareProviderLastUpdateDto
import cz.loono.backend.api.dto.UpdateStatusMessageDto
import cz.loono.backend.api.service.HealthcareProvidersService
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@RestController
@RequestMapping("/v1/providers", produces = [MediaType.APPLICATION_JSON_VALUE])
class HealthcareProvidersController(
    private var healthCareProvidersService: HealthcareProvidersService
) {

    @GetMapping(value = ["/lastupdate"])
    fun lastUpdate(): HealthcareProviderLastUpdateDto =
        HealthcareProviderLastUpdateDto(
            lastUpdate = healthCareProvidersService.lastUpdate
        )

    @GetMapping(value = ["/update"])
    fun updateData(): UpdateStatusMessageDto = healthCareProvidersService.updateData()

    @GetMapping(value = ["/all"], produces = ["application/zip"])
    fun getAll(response: HttpServletResponse): FileSystemResource =
        healthCareProvidersService.getAllSimpleData().let { path ->
            response.setHeader(
                "Content-Disposition",
                "attachment; filename=${path.fileName}"
            )
            FileSystemResource(path)
        }

    @PostMapping(value = ["/details"])
    fun getDetail(
        @RequestBody
        @Valid
        providerIdListDto: HealthcareProviderIdListDto
    ): HealthcareProviderDetailListDto =
        healthCareProvidersService.getMultipleHealthcareProviderDetails(providerIdListDto)
}
