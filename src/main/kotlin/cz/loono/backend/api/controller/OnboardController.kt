package cz.loono.backend.api.controller

import cz.loono.backend.api.dto.UserDTO
import cz.loono.backend.api.service.OnboardService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class OnboardController {

    @Autowired
    private lateinit var onboardService: OnboardService

    @PostMapping(value = ["/onboard"])
    fun onboard(@RequestBody user: UserDTO): String {
        onboardService.onboard(user)
        return user.toString()
    }
}
