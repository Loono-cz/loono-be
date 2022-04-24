package cz.loono.backend.api.v1

import cz.loono.backend.api.Attributes
import cz.loono.backend.api.BasicUser
import cz.loono.backend.api.dto.UserFeedbackDto
import cz.loono.backend.api.service.UserFeedbackService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserFeedbackController(
    private val userFeedbackService: UserFeedbackService
) {

    @GetMapping("v1/feedback")
    fun storeFeedback(
        @RequestAttribute(name = Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser,

        @RequestBody
        feedback: UserFeedbackDto
    ) {
        userFeedbackService.storeFeedback(basicUser.uid, feedback)
    }
}
