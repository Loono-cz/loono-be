package cz.loono.backend.api.controller

import cz.loono.backend.api.ApiTest
import cz.loono.backend.api.service.FirebaseAuthService
import cz.loono.backend.api.service.OnboardService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import javax.servlet.http.HttpServletResponse

class OnboardControllerTest : ApiTest() {

    @InjectMocks
    private lateinit var onboardController: OnboardController

    @Mock
    private lateinit var firebaseAuthService: FirebaseAuthService

    @Mock
    private lateinit var onboardService: OnboardService

    @BeforeEach
    fun initMocks() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun onboardRejected403() {

        val httpServletResponse = mock<HttpServletResponse>()
        whenever(firebaseAuthService.verifyUser(any(), any())).thenReturn(false)

        onboardController.onboard(createOnboardDTO(), "token", httpServletResponse)

        verify(httpServletResponse, times(1)).sendError(403)
        verify(onboardService, times(0)).onboard(any())
    }

    @Test
    fun completeOnboard() {

        val httpServletResponse = mock<HttpServletResponse>()
        whenever(firebaseAuthService.verifyUser(any(), any())).thenReturn(true)
        whenever(onboardService.userUidExists(any())).thenReturn(false)

        onboardController.onboard(createOnboardDTO(), "token", httpServletResponse)

        verify(onboardService, times(1)).onboard(any())
    }

    @Test
    fun uuidExists() {

        val httpServletResponse = mock<HttpServletResponse>()
        whenever(firebaseAuthService.verifyUser(any(), any())).thenReturn(true)
        whenever(onboardService.userUidExists(any())).thenReturn(true)

        onboardController.onboard(createOnboardDTO(), "token", httpServletResponse)

        verify(httpServletResponse, times(1)).sendError(400, "The user already exists.")
        verify(onboardService, times(0)).onboard(any())
    }
}
