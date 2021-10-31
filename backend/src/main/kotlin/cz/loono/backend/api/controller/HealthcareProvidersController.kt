package cz.loono.backend.api.controller

import cz.loono.backend.api.dto.HealthcareProviderDetailsDto
import cz.loono.backend.api.dto.HealthcareProviderIdDto
import cz.loono.backend.api.dto.UpdateStatusMessageDto
import cz.loono.backend.api.service.HealthcareProvidersService
import org.springframework.beans.factory.annotation.Autowired
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

    @GetMapping(value = ["$DOCTORS_PATH/update"])
    fun updateData(): UpdateStatusMessageDto {
        return healthCareProvidersService.updateData()
    }

    @GetMapping(value = ["$DOCTORS_PATH/all"], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun getAll(response: HttpServletResponse): ByteArray {
        response.setHeader(
            "Content-Disposition",
            "attachment; filename=providers-${healthCareProvidersService.lastUpdate}.zip"
        )
        return healthCareProvidersService.getAllSimpleData()
    }

    @PostMapping(value = ["$DOCTORS_PATH/detail"])
    fun getDetail(
        @RequestBody
        @Valid
        healthcareProviderIdDto: HealthcareProviderIdDto
    ): HealthcareProviderDetailsDto {
        return healthCareProvidersService.getHealthcareProviderDetail(healthcareProviderIdDto)
    }

    companion object {
        const val DOCTORS_PATH = "/providers"
    }
}
