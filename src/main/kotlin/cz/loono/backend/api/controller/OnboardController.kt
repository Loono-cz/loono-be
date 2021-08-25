package cz.loono.backend.api.controller

import cz.loono.backend.api.Attributes
import cz.loono.backend.api.dto.OnboardDTO
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.service.OnboardService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class OnboardController {

    @Autowired
    private lateinit var onboardService: OnboardService

    @PostMapping(value = ["/onboard"])
    fun onboard(
        @RequestBody onboard: OnboardDTO,
        @RequestAttribute(name = Attributes.ATTR_UID) uid: String,
    ) {
        if (uid != onboard.user.uid) {
            throw LoonoBackendException(HttpStatus.FORBIDDEN, null, null)
        }

        if (onboardService.userUidExists(onboard.user.uid)) {
           throw AccountAlreadyExistsException()
        }

        onboardService.onboard(onboard)
    }

    companion object {
        const val ACCOUNT_ALREADY_EXISTS_CODE = "ACCOUNT_ALREADY_EXISTS"
        const val ACCOUNT_ALREADY_EXISTS_MSG = "Onboard can only be performed once per account."
    }
}

class AccountAlreadyExistsException : LoonoBackendException(
    HttpStatus.FORBIDDEN,
    OnboardController.ACCOUNT_ALREADY_EXISTS_CODE,
    OnboardController.ACCOUNT_ALREADY_EXISTS_MSG
)
