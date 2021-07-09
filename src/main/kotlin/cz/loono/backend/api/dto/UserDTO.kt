package cz.loono.backend.api.dto

import java.util.Date

data class UserDTO(
    val salutation: String,
    val email: String,
    val notificationEmail: String = "",
    val sex: Char,
    val birthDate: Date
)
