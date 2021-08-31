package cz.loono.backend.api.controller

import cz.loono.backend.api.dto.ErrorDTO
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Fallback error controller when something or somebody calls [HttpServletResponse.sendError] instead of throwing.
 */
@RestController
class ErrorController : ErrorController {

    private val logger = LoggerFactory.getLogger(javaClass)

    @RequestMapping("/error", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleError(request: HttpServletRequest): ErrorDTO {
        logger.warn("Error should have been handled by throwing LoonoException. Message: ${request.getAttribute(RequestDispatcher.ERROR_MESSAGE)}")

        return ErrorDTO(code = null, message = null)
    }
}
