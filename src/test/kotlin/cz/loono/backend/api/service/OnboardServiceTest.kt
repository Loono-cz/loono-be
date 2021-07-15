package cz.loono.backend.api.service

import cz.loono.backend.api.dto.SexDTO
import cz.loono.backend.api.dto.UserDTO
import cz.loono.backend.data.model.User
import cz.loono.backend.data.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.Date

class OnboardServiceTest {

    @InjectMocks
    private lateinit var onboardService: OnboardService

    @Mock
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun initMocks() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testFullTransformation() {
        val date = Date()
        val userDto = UserDTO(
            uid = "userId",
            birthdate = date,
            sex = SexDTO.MALE,
            email = "primary@test.com",
            notificationEmail = "notify@test.com",
            salutation = "Shrek"
        )

        val captor: ArgumentCaptor<User> = ArgumentCaptor.forClass(User::class.java)

        onboardService.onboard(userDto)
        verify(userRepository, times(1)).save(captor.capture())

        val user = captor.value
        assert(
            user.equals(
                User(
                    uid = userDto.uid,
                    birthdate = date,
                    sex = userDto.sex.id,
                    email = userDto.email,
                    notificationEmail = userDto.notificationEmail,
                    salutation = userDto.salutation
                )
            )
        )
    }

    @Test
    fun testTransformationWithoutNotificationEmail() {
        val date = Date()
        val userDto = UserDTO(
            uid = "userId",
            birthdate = date,
            sex = SexDTO.MALE,
            email = "primary@test.com",
            salutation = "Shrek"
        )

        val captor: ArgumentCaptor<User> = ArgumentCaptor.forClass(User::class.java)

        onboardService.onboard(userDto)
        verify(userRepository, times(1)).save(captor.capture())

        val user = captor.value
        assert(
            user.equals(
                User(
                    uid = userDto.uid,
                    birthdate = date,
                    sex = userDto.sex.id,
                    email = userDto.email,
                    notificationEmail = userDto.email,
                    salutation = userDto.salutation
                )
            )
        )
    }
}
