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

    fun storeFeedback(userUid: String, userFeedbackDto: UserFeedbackDto) {
        val account = accountRepository.findByUid(userUid) ?: throw LoonoBackendException(
            status = HttpStatus.NOT_FOUND,
            errorCode = "404",
            errorMessage = "The account not found."
        )
        userFeedbackRepository.save(
            UserFeedback(
                evaluation = userFeedbackDto.evaluation,
                message = userFeedbackDto.message,
                account = account
            )
        )
    }
}
