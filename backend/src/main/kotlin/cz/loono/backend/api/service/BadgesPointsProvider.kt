package cz.loono.backend.api.service

import cz.loono.backend.api.dto.BadgeTypeDto
import cz.loono.backend.api.dto.ExaminationTypeEnumDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.exception.LoonoBackendException
import org.springframework.http.HttpStatus

object BadgesPointsProvider {
    fun getBadgesAndPoints(examType: ExaminationTypeEnumDto, sex: SexDto) =
        when {
            examType == ExaminationTypeEnumDto.OPHTHALMOLOGIST -> BadgeTypeDto.GLASSES to 100
            examType == ExaminationTypeEnumDto.GENERAL_PRACTITIONER -> BadgeTypeDto.COAT to 200
            examType == ExaminationTypeEnumDto.DERMATOLOGIST -> BadgeTypeDto.GLOVES to 200
            examType == ExaminationTypeEnumDto.DENTIST -> BadgeTypeDto.HEADBAND to 300
            examType == ExaminationTypeEnumDto.COLONOSCOPY -> BadgeTypeDto.SHOES to 1000

            examType == ExaminationTypeEnumDto.ULTRASOUND_BREAST && sex == SexDto.FEMALE -> BadgeTypeDto.TOP to 100
            examType == ExaminationTypeEnumDto.GYNECOLOGIST && sex == SexDto.FEMALE -> BadgeTypeDto.BELT to 200
            examType == ExaminationTypeEnumDto.MAMMOGRAM && sex == SexDto.FEMALE -> BadgeTypeDto.TOP to 500

            examType == ExaminationTypeEnumDto.UROLOGIST && sex == SexDto.MALE -> BadgeTypeDto.BELT to 300

            else -> throw LoonoBackendException(
                status = HttpStatus.BAD_REQUEST, errorMessage = "Unsupported examination type $examType"
            )
        }
}
