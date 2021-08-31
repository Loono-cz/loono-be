package cz.loono.backend.api.dto

data class ExaminationDTO(
    val lastVisitInterval: LastVisitDTO,
    val lastVisitMonth: Int? = null,
    val lastVisitYear: Int? = null,
    val examinationType: ExaminationTypeDTO
)
