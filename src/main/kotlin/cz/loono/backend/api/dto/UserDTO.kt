package cz.loono.backend.api.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDate

@ApiModel(value = "user", description = "Onboarded user data")
data class UserDTO(

    @ApiModelProperty(notes = "unique user identifier")
    val uid: String,

    @ApiModelProperty(notes = "user salutation")
    val salutation: String,

    @ApiModelProperty(notes = "primary email")
    val email: String,

    @ApiModelProperty(
        notes = "notification email, if differs from the primary one which is related to the account",
        allowEmptyValue = true,
        required = false
    )
    val notificationEmail: String? = null,

    val sex: SexDTO,

    @ApiModelProperty(notes = "birthdate of user")
    val birthdate: LocalDate,
)
