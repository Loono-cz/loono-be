package cz.loono.backend.api.dto

data class OnboardDTO(
    val user: UserDTO,
    val examinations: List<ExaminationDTO> = emptyList()
)
