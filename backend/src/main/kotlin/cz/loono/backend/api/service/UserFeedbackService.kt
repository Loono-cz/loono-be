package cz.loono.backend.api.service

import cz.loono.backend.api.dto.UserFeedbackDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.db.model.UserFeedback
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.UserFeedbackRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class UserFeedbackService(
    private val userFeedbackRepository: UserFeedbackRepository,
    private val accountRepository: AccountRepository
) {

    fun storeFeedback(userFeedbackDto: UserFeedbackDto) {
        userFeedbackDto.message?.let {
            validateMessageContent(userFeedbackDto.message)
        }
        val account = userFeedbackDto.uid?.let {
            accountRepository.findByUid(userFeedbackDto.uid) ?: throw LoonoBackendException(
                status = HttpStatus.NOT_FOUND,
                errorCode = "404"
            )
        }
        userFeedbackRepository.save(
            UserFeedback(
                evaluation = userFeedbackDto.evaluation,
                message = userFeedbackDto.message,
                account = account
            )
        )
    }

    private fun validateMessageContent(message: String) {
        if (message.length > 500) {
            throw LoonoBackendException(
                status = HttpStatus.BAD_REQUEST,
                errorCode = "400",
                errorMessage = "The message is too long."
            )
        }
    }
}
