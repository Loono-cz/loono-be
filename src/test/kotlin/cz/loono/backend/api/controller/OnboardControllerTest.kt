package cz.loono.backend.api.controller

import cz.loono.backend.api.ApiTest
import cz.loono.backend.api.service.OnboardService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.springframework.mock.web.MockHttpServletResponse

class OnboardControllerTest : ApiTest() {

    @InjectMocks
    private lateinit var onboardController: OnboardController

    @Mock
    private lateinit var onboardService: OnboardService

    @BeforeEach
    fun initMocks() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `different authenticated uid from dto uid returns 403`() {
        val response = MockHttpServletResponse()
        val onboardDto = createOnboardDTO()
        onboardController.onboard(onboardDto, onboardDto.user.uid + "randomuid", response)

        assertTrue { response.isCommitted }
        assertEquals(403, response.status)

        verify(onboardService, times(0)).onboard(any())
    }

    @Test
    fun completeOnboard() {
        whenever(onboardService.userUidExists(any())).thenReturn(false)
        val onboardDto = createOnboardDTO()

        onboardController.onboard(onboardDto, onboardDto.user.uid, MockHttpServletResponse())

        verify(onboardService, times(1)).onboard(any())
    }

    @Test
    fun `existing uid returns 400`() {
        whenever(onboardService.userUidExists(any())).thenReturn(true)
        val response = MockHttpServletResponse()
        val onboardDto = createOnboardDTO()

        onboardController.onboard(onboardDto, onboardDto.user.uid, response)

        assertTrue { response.isCommitted }
        assertEquals(400, response.status)
        assertEquals("The user already exists.", response.errorMessage)

        verify(onboardService, times(0)).onboard(any())
    }
}
