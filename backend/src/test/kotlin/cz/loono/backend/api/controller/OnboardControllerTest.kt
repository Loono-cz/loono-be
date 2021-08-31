package cz.loono.backend.api.controller

import cz.loono.backend.api.ApiTest
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.service.OnboardService
import cz.loono.backend.data.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

class OnboardControllerTest : ApiTest() {

    @InjectMocks
    private lateinit var onboardController: OnboardController

    @Mock
    private lateinit var onboardService: OnboardService

    @Mock
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun initMocks() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `different authenticated uid from dto uid returns 403`() {
        val onboardDto = createOnboardDTO()

        val error = assertThrows<LoonoBackendException> {
            onboardController.onboard(onboardDto, onboardDto.user.uid + "randomuid")
        }

        assertEquals(HttpStatus.FORBIDDEN, error.status)
        assertNull(error.errorCode)
        assertNull(error.errorMessage)

        verify(onboardService, times(0)).onboard(any())
    }

    @Test
    fun completeOnboard() {
        whenever(userRepository.existsByUid(any())).thenReturn(false)
        val onboardDto = createOnboardDTO()

        onboardController.onboard(onboardDto, onboardDto.user.uid)

        verify(onboardService, times(1)).onboard(any())
    }

    @Test
    fun `existing uid returns 403`() {
        whenever(userRepository.existsByUid(any())).thenReturn(true)
        val onboardDto = createOnboardDTO()

        val error = assertThrows<AccountAlreadyExistsException> {
            onboardController.onboard(onboardDto, onboardDto.user.uid)
        }

        assertEquals(HttpStatus.FORBIDDEN, error.status)
        assertEquals(OnboardController.ACCOUNT_ALREADY_EXISTS_CODE, error.errorCode)
        assertEquals(OnboardController.ACCOUNT_ALREADY_EXISTS_MSG, error.errorMessage)

        verify(onboardService, times(0)).onboard(any())
    }
}
