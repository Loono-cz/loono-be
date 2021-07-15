package cz.loono.backend.api.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "onboard", description = "All necessary data for the Onboarding")
data class OnboardDTO(

    @ApiModelProperty(position = 0)
    val user: UserDTO,

    @ApiModelProperty(position = 1)
    val examinations: List<ExaminationDTO> = emptyList()
)
