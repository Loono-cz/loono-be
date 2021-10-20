package cz.loono.backend.api.controller

import cz.loono.backend.api.UpdateStatusMessage
import cz.loono.backend.api.service.HealthcareProvidersService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class HealthcareProvidersController {

    @Autowired
    private lateinit var healthCareProvidersService: HealthcareProvidersService

    @GetMapping(value = ["$DOCTORS_PATH/update"])
    fun updateData(): UpdateStatusMessage {
        return healthCareProvidersService.updateData()
    }

    @GetMapping(value = ["$DOCTORS_PATH/all"])
    fun getAll(): String {
        // TODO returns all providers in simple form
        return "{}"
    }

    @GetMapping(value = ["$DOCTORS_PATH/detail"])
    fun getDetail(): String {
        // TODO returns a concrete details of the given doctor
        return "{}"
    }

    companion object {
        const val DOCTORS_PATH = "/providers"
    }
}
