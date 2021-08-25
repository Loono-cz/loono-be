package cz.loono.backend.api

import cz.loono.backend.api.dto.ErrorDTO
import cz.loono.backend.api.exception.LoonoBackendException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/*
  We need to extend ResponseEntityExceptionHandler in order to handle the internal Spring exceptions with the overridden
  `handleExceptionInternal`.

  We need to handle Exception::class in our ExceptionHandler in order to catch all application errors.
 */
@RestControllerAdvice
class GlobalErrorControllerAdvice : ResponseEntityExceptionHandler() {

    private val errorLogger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(Exception::class)
    fun handleApplicationException(ex: Exception): ResponseEntity<Any> {
        val status: HttpStatus
        val responseBody: ErrorDTO

        when (ex) {
            is LoonoBackendException -> {
                status = ex.status
                responseBody = ErrorDTO(ex.errorCode, ex.errorMessage)
            }
            else -> {
                errorLogger.error("handleApplicationException unexpected error: " + ex.message, ex)
                status = HttpStatus.INTERNAL_SERVER_ERROR
                responseBody = ErrorDTO(code = null, message = null)
            }
        }

        // We return a ResponseEntity, because we need to be able to set the response status code.
        // If we returned just the ErrorDTO alone, it would have status 200.
        // Keep this method's return type as ResponseEntity.
        return ResponseEntity
            .status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseBody)
    }

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        errorLogger.error("handleExceptionInternal: " + ex.message, ex)
        val response = ErrorDTO(code = null, message = null)

        return super.handleExceptionInternal(ex, response, headers, status, request)
    }

}
