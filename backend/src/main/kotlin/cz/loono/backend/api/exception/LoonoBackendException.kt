package cz.loono.backend.api.exception

import cz.loono.backend.api.dto.ErrorDto
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

/**
 * Umbrella type for known error states. All exceptions thrown by our controllers should inherit from this class.
 */
open class LoonoBackendException(
    status: HttpStatus,
    /**
     * Error code for [ErrorDto]
     */
    val errorCode: String? = null,
    /**
     * Error message for [ErrorDto]
     */
    val errorMessage: String? = null,
    cause: Throwable? = null
) : ResponseStatusException(status, errorMessage, cause)
