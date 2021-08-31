package cz.loono.backend.api.service

import cz.loono.backend.api.ApiTest
import cz.loono.backend.api.dto.ExaminationTypeDTO
import cz.loono.backend.api.dto.LastVisitDTO
import cz.loono.backend.api.dto.OnboardDTO
import cz.loono.backend.data.model.Examination
import cz.loono.backend.data.model.User
import cz.loono.backend.data.repository.ExaminationRepository
import cz.loono.backend.data.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.time.LocalDate

class OnboardServiceTest : ApiTest() {

    @InjectMocks
    private lateinit var onboardService: OnboardService

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var examinationRepository: ExaminationRepository

    @BeforeEach
    fun initMocks() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testFullTransformation() {

        val userDto = createUserDTO()
        val examinations = createListOfExaminations(1)
        val onboardDTO = createOnboardDTO(userDto, examinations)
        val userCaptor = argumentCaptor<User>()
        val examsCaptor = argumentCaptor<List<Examination>>()

        onboardService.onboard(onboardDTO)
        verify(userRepository, times(1)).save(userCaptor.capture())
        verify(examinationRepository, times(1)).saveAll(examsCaptor.capture())

        val user = userCaptor.firstValue
        val exams = examsCaptor.firstValue
        assert(exams.size == 1)

        val exam = exams[0]
        assert(
            exam == Examination(
                user = user,
                examinationType = ExaminationTypeDTO.GENERAL_PRACTITIONER.name,
                lastVisit = LastVisitDTO.LAST_YEAR.name,
                date = LocalDate.of(1956, 8, 1)
            )
        )

        assert(
            user == User(
                uid = userDto.uid,
                birthdate = LocalDate.of(userDto.birthdateYear, userDto.birthdateMonth, 1),
                sex = userDto.sex.name,
                email = userDto.email,
                notificationEmail = userDto.notificationEmail,
                salutation = userDto.salutation
            )
        )
    }

    @Test
    fun testTransformationWithoutNotificationEmail() {

        val userDto = createMinimalUserDTO()
        val captor = argumentCaptor<User>()

        onboardService.onboard(OnboardDTO(userDto))
        verify(userRepository, times(1)).save(captor.capture())

        val user = captor.firstValue
        assert(
            user == User(
                uid = userDto.uid,
                birthdate = LocalDate.of(userDto.birthdateYear, userDto.birthdateMonth, 1),
                sex = userDto.sex.name,
                email = userDto.email,
                notificationEmail = userDto.email,
                salutation = userDto.salutation
            )
        )
    }

    @Test
    fun testTransformationWithoutExaminations() {

        val userDto = createMinimalUserDTO()
        val examsCaptor = argumentCaptor<List<Examination>>()

        onboardService.onboard(OnboardDTO(userDto))
        verify(examinationRepository, times(1)).saveAll(examsCaptor.capture())

        val exams = examsCaptor.firstValue
        assert(exams.isEmpty())
    }

    @Test
    fun testTransformationExaminationWithoutDate() {

        val userDto = createUserDTO()
        val examsList = createListOfExaminationsWithoutDate(1)
        val examsCaptor = argumentCaptor<List<Examination>>()
        val userCaptor = argumentCaptor<User>()

        onboardService.onboard(OnboardDTO(userDto, examsList))
        verify(userRepository, times(1)).save(userCaptor.capture())
        verify(examinationRepository, times(1)).saveAll(examsCaptor.capture())

        val user = userCaptor.firstValue
        val exams = examsCaptor.firstValue
        assert(exams.size == 1)

        val exam = exams[0]
        assert(
            exam == Examination(
                user = user,
                examinationType = ExaminationTypeDTO.GENERAL_PRACTITIONER.name,
                lastVisit = LastVisitDTO.LAST_YEAR.name,
                date = null
            )
        )
    }
}
