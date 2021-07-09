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
        return User(
            email = user.email,
            salutation = user.salutation,
            notificationEmail = user.notificationEmail,
            sex = user.sex,
            birthDate = user.birthDate
        )
    }
}
