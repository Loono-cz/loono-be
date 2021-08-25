package cz.loono.backend.api.controller

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest

class ErrorControllerTest {

    @Test
    fun testErrorHandling() {
        val httpServletRequest = mock<HttpServletRequest>()
        whenever((httpServletRequest.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))).thenReturn("404")
        whenever((httpServletRequest.getAttribute(RequestDispatcher.ERROR_MESSAGE))).thenReturn("Not found.")

        val errorController = ErrorController()
        val response = errorController.handleError(httpServletRequest)

        assertNull(response.code)
        assertNull(response.message)
    }
}
