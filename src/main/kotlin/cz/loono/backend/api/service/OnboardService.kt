package cz.loono.backend.api.service

import cz.loono.backend.api.dto.UserDTO
import cz.loono.backend.data.model.User
import cz.loono.backend.data.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class OnboardService {

    @Autowired
    private lateinit var userRepository: UserRepository

    fun onboard(user: UserDTO) {
        userRepository.save(toUser(user))
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
            sex = user.sex.id,
            birthdate = user.birthdate
        )
    }
}
