package cz.loono.backend.api

import cz.loono.backend.api.dto.ExaminationDTO
import cz.loono.backend.api.dto.ExaminationTypeDTO
import cz.loono.backend.api.dto.LastVisitDTO
import cz.loono.backend.api.dto.OnboardDTO
import cz.loono.backend.api.dto.SexDTO
import cz.loono.backend.api.dto.UserDTO

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
                    lastVisitInterval = LastVisitDTO.values()[(i - 1) % 3],
                    lastVisitYear = 1956,
                    lastVisitMonth = 8,
                    examinationType = ExaminationTypeDTO.values()[(i - 1) % 5]
                )
            )
        }
        return list
    }

    fun createListOfExaminationsWithoutDate(count: Int): List<ExaminationDTO> {
        val list = mutableListOf<ExaminationDTO>()
        for (i in 1..count) {
            list.add(
                ExaminationDTO(
                    lastVisitInterval = LastVisitDTO.values()[(i - 1) % 3],
                    examinationType = ExaminationTypeDTO.values()[(i - 1) % 5]
                )
            )
        }
        return list
    }

    fun createUserDTO(): UserDTO {
        return UserDTO(
            uid = "userId",
            birthdateMonth = 3,
            birthdateYear = 1982,
            sex = SexDTO.MALE,
            email = "primary@test.com",
            notificationEmail = "notify@test.com",
            salutation = "Shrek"
        )
    }

    fun createMinimalUserDTO(): UserDTO {
        return UserDTO(
            uid = "userId",
            birthdateMonth = 12,
            birthdateYear = 2002,
            sex = SexDTO.MALE,
            email = "primary@test.com",
            salutation = "Shrek"
        )
    }
}
