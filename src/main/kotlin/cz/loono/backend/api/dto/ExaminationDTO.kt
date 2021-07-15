package cz.loono.backend.api.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.Date

@ApiModel(value = "examination", description = "Undergone examination data")
data class ExaminationDTO(

    @ApiModelProperty(notes = "date of undergone medical examination")
    val date: Date,

    @ApiModelProperty(notes = "medical type or department")
    val medicalType: MedicalTypeDTO
)
