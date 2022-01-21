package cz.loono.backend.api.controller

import cz.loono.backend.api.dto.HealthcareProviderDetailListDto
import cz.loono.backend.api.dto.HealthcareProviderIdListDto
import cz.loono.backend.api.dto.HealthcareProviderLastUpdateDto
import cz.loono.backend.api.dto.UpdateStatusMessageDto
import cz.loono.backend.api.service.HealthcareProvidersService
import org.springframework.beans.factory.annotation.Autowired
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
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class HealthcareProvidersController {

    @Autowired
    private lateinit var healthCareProvidersService: HealthcareProvidersService

    @GetMapping(value = ["$DOCTORS_PATH/lastupdate"])
    fun lastUpdate(): HealthcareProviderLastUpdateDto =
        HealthcareProviderLastUpdateDto(
            lastUpdate = healthCareProvidersService.lastUpdate
        )

    @GetMapping(value = ["$DOCTORS_PATH/update"])
    fun updateData(): UpdateStatusMessageDto = healthCareProvidersService.updateData()

    @GetMapping(value = ["$DOCTORS_PATH/all"], produces = ["application/zip"])
    fun getAll(response: HttpServletResponse): FileSystemResource =
        healthCareProvidersService.getAllSimpleData().let { path ->
            response.setHeader(
                "Content-Disposition",
                "attachment; filename=${path.fileName}"
            )
            FileSystemResource(path)
        }

    @PostMapping(value = ["$DOCTORS_PATH/details"])
    fun getDetail(
        @RequestBody
        @Valid
        providerIdListDto: HealthcareProviderIdListDto
    ): HealthcareProviderDetailListDto =
        healthCareProvidersService.getMultipleHealthcareProviderDetails(providerIdListDto)

    companion object {
        const val DOCTORS_PATH = "/providers"
    }
}
