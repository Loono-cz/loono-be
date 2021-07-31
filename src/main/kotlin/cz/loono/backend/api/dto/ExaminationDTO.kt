package cz.loono.backend.api.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "examination", description = "Undergone examination data")
data class ExaminationDTO(

    @ApiModelProperty(notes = "last visit interval")
    val lastVisitInterval: LastVisitDTO,

    @ApiModelProperty(notes = "last visit month")
    val lastVisitMonth: Int,

    @ApiModelProperty(notes = "last visit yaer")
    val lastVisitYear: Int,

    @ApiModelProperty(notes = "examination type")
    val examinationType: ExaminationTypeDTO
)
