package cz.loono.backend.api.controller

import cz.loono.backend.api.dto.OnboardDTO
import cz.loono.backend.api.service.FirebaseAuthService
import cz.loono.backend.api.service.OnboardService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.MimeTypeUtils
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@Api(
    tags = ["Onboarding"],
    description = "The user onboarding creates an account and store basic info about user."
)
@RestController
class OnboardController {

    @Autowired
    private lateinit var onboardService: OnboardService

    @Autowired
    private lateinit var firebaseAuthService: FirebaseAuthService

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
        onboard: OnboardDTO,
        @Parameter(
            description = "Bearer token",
            allowEmptyValue = true
        )
        @RequestHeader(name = "Authorization")
        token: String = "",
        response: HttpServletResponse
    ) {

        // Temporary code for Hack day purposes
        if (onboard.user.email.contains("error")) {
            throw Exception("Hack day error thrown!")
        }

        var verifiedUser = false
        if (token.isNotEmpty()) {
            verifiedUser = firebaseAuthService.verifyUser(onboard.user, token)
        }

        if (!verifiedUser && token.isNotEmpty()) {
            response.status = 403
        }

//        onboardService.onboard(user)
    }
}
