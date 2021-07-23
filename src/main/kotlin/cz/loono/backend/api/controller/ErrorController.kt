package cz.loono.backend.api.controller

import cz.loono.backend.api.dto.ErrorDTO
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest

@Controller
class ErrorController : ErrorController {

    @RequestMapping("/error", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun handleError(request: HttpServletRequest): ErrorDTO {
        return ErrorDTO(
            status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE).toString(),
            message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE).toString()
        )
    }
}
