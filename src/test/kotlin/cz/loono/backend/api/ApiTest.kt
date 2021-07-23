package cz.loono.backend.api

import cz.loono.backend.api.dto.ExaminationDTO
import cz.loono.backend.api.dto.MedicalTypeDTO
import cz.loono.backend.api.dto.OnboardDTO
import cz.loono.backend.api.dto.SexDTO
import cz.loono.backend.api.dto.UserDTO
import java.time.LocalDate

abstract class ApiTest {

    fun createOnboardDTO(
        userDTO: UserDTO = createUserDTO(),
        examinations: List<ExaminationDTO> = emptyList()
    ): OnboardDTO {
        return OnboardDTO(userDTO, examinations)
    }

    fun createListOfExaminations(count: Int): List<ExaminationDTO> {
        val list = mutableListOf<ExaminationDTO>()
        for (i in 1..count) {
            list.add(
                ExaminationDTO(
                    date = LocalDate.EPOCH,
                    medicalType = MedicalTypeDTO.values()[(i - 1) % 5]
                )
            )
        }
        return list
    }

    fun createUserDTO(): UserDTO {
        return UserDTO(
            uid = "userId",
            birthdate = LocalDate.EPOCH,
            sex = SexDTO.MALE,
            email = "primary@test.com",
            notificationEmail = "notify@test.com",
            salutation = "Shrek"
        )
    }

    fun createMinimalUserDTO(): UserDTO {
        return UserDTO(
            uid = "userId",
            birthdate = LocalDate.EPOCH,
            sex = SexDTO.MALE,
            email = "primary@test.com",
            salutation = "Shrek"
        )
    }
}
