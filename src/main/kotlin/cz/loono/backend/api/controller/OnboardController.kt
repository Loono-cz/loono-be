package cz.loono.backend.api.controller

import cz.loono.backend.api.Attributes
import cz.loono.backend.api.dto.OnboardDTO
import cz.loono.backend.api.service.OnboardService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
class OnboardController {

    @Autowired
    private lateinit var onboardService: OnboardService

    @PostMapping(value = ["/onboard"])
    fun onboard(
        @RequestBody onboard: OnboardDTO,
        @RequestAttribute(name = Attributes.ATTR_UID) uid: String,
        response: HttpServletResponse
    ) {
        if (uid != onboard.user.uid) {
            response.sendError(403)
            return
        }

        if (onboardService.userUidExists(onboard.user.uid)) {
            response.sendError(400, "The user already exists.")
            return
        }

        onboardService.onboard(onboard)
    }
}
