package cz.loono.backend.api.controller

import cz.loono.backend.api.dto.UserDTO
import cz.loono.backend.api.service.OnboardService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.MimeTypeUtils
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Api(
    tags = ["Onboarding"],
    description = "The user onboarding creates an account and store basic info about user."
)
@RestController
class OnboardController {

    @Autowired
    private lateinit var onboardService: OnboardService

    @ApiOperation(
        value = "Onboards a new user",
        notes = "A creation of a new account with all information needed for the onboarding.",
        consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
        produces = MimeTypeUtils.APPLICATION_JSON_VALUE
    )
    @PostMapping(value = ["/onboard"])
    fun onboard(
        @Parameter(
            description = "All collected information to the account creation.",
            allowEmptyValue = false,
            required = true
        )
        @RequestBody
        user: UserDTO
    ): String {
        onboardService.onboard(user)
        return user.email + " onboarded."
    }
}
