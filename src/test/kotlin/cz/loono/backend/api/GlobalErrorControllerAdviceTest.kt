package cz.loono.backend.api

import cz.loono.backend.api.dto.ErrorDTO
import cz.loono.backend.api.exception.LoonoBackendException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.servlet.handler.DispatcherServletWebRequest

internal class GlobalErrorControllerAdviceTest {

    @Test
    fun `LoonoBackendException fills in code and message`() {
        val errorCode = "EXPECTED_ERROR_CODE"
        val errorMessage = "Expected error message."
        val ex = LoonoBackendException(HttpStatus.VARIANT_ALSO_NEGOTIATES, errorCode, errorMessage)
        val handler = GlobalErrorControllerAdvice()

        val response = handler.handleApplicationException(ex)

        assertEquals(ex.status, response.statusCode)
        assertEquals(ErrorDTO(errorCode, errorMessage), response.body)
    }

    @Test
    fun `general exception returns null values and 500`() {
        val errorMessage = "Expected error message."
        val ex = Exception(errorMessage)
        val handler = GlobalErrorControllerAdvice()

        val response = handler.handleApplicationException(ex)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(ErrorDTO(code = null, message = null), response.body)
    }

    @Test
    fun `internal exception returns null values`() {
        val request = DispatcherServletWebRequest(MockHttpServletRequest())
        val handler = GlobalErrorControllerAdvice()

        val response = handler.handleException(HttpRequestMethodNotSupportedException("POST"), request)

        assertNotNull(response)
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response!!.statusCode)
        assertEquals(ErrorDTO(code = null, message = null), response.body)
    }
}
