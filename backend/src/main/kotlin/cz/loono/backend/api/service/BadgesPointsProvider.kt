package cz.loono.backend.api.service

import cz.loono.backend.api.dto.BadgeTypeDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.SelfExaminationTypeDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.exception.LoonoBackendException
import org.springframework.http.HttpStatus

object BadgesPointsProvider {
    fun getGeneralBadgesAndPoints(examType: ExaminationTypeDto, sex: SexDto) =
        when {
            examType == ExaminationTypeDto.OPHTHALMOLOGIST -> BadgeTypeDto.GLASSES to 100
            examType == ExaminationTypeDto.GENERAL_PRACTITIONER -> BadgeTypeDto.COAT to 200
            examType == ExaminationTypeDto.DERMATOLOGIST -> BadgeTypeDto.GLOVES to 200
            examType == ExaminationTypeDto.DENTIST -> BadgeTypeDto.HEADBAND to 300
            examType == ExaminationTypeDto.COLONOSCOPY -> BadgeTypeDto.SHOES to 1000

            examType == ExaminationTypeDto.ULTRASOUND_BREAST && sex == SexDto.FEMALE -> BadgeTypeDto.TOP to 100
            examType == ExaminationTypeDto.GYNECOLOGIST && sex == SexDto.FEMALE -> BadgeTypeDto.BELT to 200
            examType == ExaminationTypeDto.MAMMOGRAM && sex == SexDto.FEMALE -> BadgeTypeDto.TOP to 500

            examType == ExaminationTypeDto.UROLOGIST && sex == SexDto.MALE -> BadgeTypeDto.BELT to 300

            else -> throw LoonoBackendException(
                status = HttpStatus.BAD_REQUEST, errorMessage = "Unsupported examination type $examType"
            )
        }

    fun getSelfExaminationBadgesAndPoints(selfExamType: SelfExaminationTypeDto, sex: SexDto): Pair<BadgeTypeDto, Int>? =
        when {
            selfExamType == SelfExaminationTypeDto.BREAST && sex == SexDto.FEMALE -> BadgeTypeDto.SHIELD to 50
            selfExamType == SelfExaminationTypeDto.TESTICULAR && sex == SexDto.MALE -> BadgeTypeDto.SHIELD to 50
            else -> null
        }

    fun getSelfExaminationType(badgeTypeDto: BadgeTypeDto, sex: SexDto) =
        badgeTypeDto.takeIf {
            badgeTypeDto == BadgeTypeDto.SHIELD
        }?.let {
            if (sex == SexDto.FEMALE) SelfExaminationTypeDto.BREAST else SelfExaminationTypeDto.TESTICULAR
        }

    val GENERAL_BADGES_TO_EXAMS = mapOf(
        BadgeTypeDto.GLASSES to ExaminationTypeDto.OPHTHALMOLOGIST,
        BadgeTypeDto.COAT to ExaminationTypeDto.GENERAL_PRACTITIONER,
        BadgeTypeDto.GLOVES to ExaminationTypeDto.DERMATOLOGIST,
        BadgeTypeDto.HEADBAND to ExaminationTypeDto.DENTIST,
        BadgeTypeDto.SHOES to ExaminationTypeDto.COLONOSCOPY,
        BadgeTypeDto.TOP to ExaminationTypeDto.MAMMOGRAM,
        BadgeTypeDto.BELT to ExaminationTypeDto.UROLOGIST,
    )
}
