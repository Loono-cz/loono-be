package cz.loono.backend.api.dto

enum class MedicalTypeDTO(val id: Int) {
    GENERAL_PRACTITIONER(0),
    DENTIST(1),
    GYNECOLOGIST(2),
    UROLOGIST(3),
    CARDIOLOGIST(4)
}
