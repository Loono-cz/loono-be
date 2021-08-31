package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationDTO
import cz.loono.backend.api.dto.OnboardDTO
import cz.loono.backend.api.dto.UserDTO
import cz.loono.backend.data.model.Examination
import cz.loono.backend.data.model.User
import cz.loono.backend.data.repository.ExaminationRepository
import cz.loono.backend.data.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class OnboardService {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var examinationRepository: ExaminationRepository

    @Transactional(rollbackFor = [Exception::class])
    fun onboard(onboard: OnboardDTO) {
        val user = toUser(onboard.user)
        userRepository.save(user)
        examinationRepository.saveAll(toExaminations(user, onboard.examinations))
    }

    private fun toExaminations(user: User, examinations: List<ExaminationDTO>): List<Examination> {
        val result = mutableListOf<Examination>()
        for (exam in examinations) {
            result.add(toExamination(user, exam))
        }
        return result
    }

    private fun toExamination(user: User, examination: ExaminationDTO): Examination {
        var date: LocalDate? = null
        if (examination.lastVisitYear != null && examination.lastVisitMonth != null) {
            date = LocalDate.of(examination.lastVisitYear, examination.lastVisitMonth, 1)
        }
        return Examination(
            date = date,
            examinationType = examination.examinationType.name,
            lastVisit = examination.lastVisitInterval.name,
            user = user
        )
    }

    private fun toUser(user: UserDTO): User {
        var notificationEmail = user.notificationEmail
        if (user.notificationEmail.isNullOrEmpty()) {
            notificationEmail = user.email
        }
        return User(
            uid = user.uid,
            salutation = user.salutation,
            email = user.email,
            notificationEmail = notificationEmail,
            sex = user.sex.name,
            birthdate = LocalDate.of(user.birthdateYear, user.birthdateMonth, 1)
        )
    }
}
