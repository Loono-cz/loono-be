package cz.loono.backend.api.dto

data class UserDTO(
    val uid: String,
    val salutation: String,
    val email: String,
    val notificationEmail: String? = null,
    val sex: SexDTO,
    val birthdateMonth: Int,
    val birthdateYear: Int,
)
