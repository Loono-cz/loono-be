package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationTypeEnumDto
import cz.loono.backend.api.dto.SexDto

/**
 * This object in fact wraps the provided excel with code, so it's a statical set of rules
 * that return collection of required Examinations and their requested intervals.
 */
object ExaminationIntervalProvider {

    fun findExaminationRequests(patient: Patient): List<ExaminationInterval> =
        rules.mapNotNull { preventionRule ->
            val intervals = when (patient.sex) {
                SexDto.MALE -> preventionRule.intervalsMale
                SexDto.FEMALE -> preventionRule.intervalsFemale
            }

            val validInterval = intervals.find { ageInterval ->
                ageInterval.fromAge <= patient.age &&
                    (ageInterval.toAge == null || ageInterval.toAge >= patient.age)
            }

            if (validInterval != null) {
                ExaminationInterval(
                    preventionRule.examinationType,
                    validInterval.intervalYears
                )
            } else null // no prevention required for patients age and sex
        }

    private val rules = listOf(
        PreventionRule(
            examinationType = ExaminationTypeEnumDto.GENERAL_PRACTITIONER,
            intervalsMale = listOf(AgeInterval(fromAge = 19, intervalYears = 2)),
            intervalsFemale = listOf(AgeInterval(fromAge = 19, intervalYears = 2))
        ),
        PreventionRule(
            examinationType = ExaminationTypeEnumDto.MAMMOGRAM,
            intervalsMale = listOf(),
            intervalsFemale = listOf(AgeInterval(fromAge = 45, intervalYears = 2))
        ),
        PreventionRule(
            examinationType = ExaminationTypeEnumDto.GYNECOLOGIST,
            intervalsMale = listOf(),
            intervalsFemale = listOf(AgeInterval(fromAge = 15, intervalYears = 1))
        ),
        PreventionRule(
            examinationType = ExaminationTypeEnumDto.COLONOSCOPY,
            intervalsMale = listOf(AgeInterval(fromAge = 50, intervalYears = 10)),
            intervalsFemale = listOf(AgeInterval(fromAge = 50, intervalYears = 10))
        ),
        PreventionRule(
            examinationType = ExaminationTypeEnumDto.UROLOGIST,
            intervalsMale = listOf(AgeInterval(fromAge = 50, intervalYears = 1)),
            intervalsFemale = listOf()
        ),
        PreventionRule(
            examinationType = ExaminationTypeEnumDto.DERMATOLOGIST,
            intervalsMale = listOf(AgeInterval(fromAge = 19, intervalYears = 1)),
            intervalsFemale = listOf(AgeInterval(fromAge = 19, intervalYears = 1))
        ),
        PreventionRule(
            examinationType = ExaminationTypeEnumDto.ULTRASOUND_BREAST,
            intervalsMale = listOf(),
            intervalsFemale = listOf(AgeInterval(fromAge = 19, toAge = 44, intervalYears = 2))
        ),
        PreventionRule(
            examinationType = ExaminationTypeEnumDto.DENTIST,
            intervalsMale = listOf(
                AgeInterval(fromAge = 19, intervalYears = 1)
            ),
            intervalsFemale = listOf(
                AgeInterval(fromAge = 19, intervalYears = 1)
            )
        ),
        PreventionRule(
            examinationType = ExaminationTypeEnumDto.OPHTHALMOLOGIST,
            intervalsMale = listOf(
                AgeInterval(fromAge = 19, toAge = 44, intervalYears = 2),
                AgeInterval(fromAge = 45, toAge = 61, intervalYears = 4),
                AgeInterval(fromAge = 62, intervalYears = 2)
            ),
            intervalsFemale = listOf(
                AgeInterval(fromAge = 19, toAge = 44, intervalYears = 2),
                AgeInterval(fromAge = 45, toAge = 61, intervalYears = 4),
                AgeInterval(fromAge = 62, intervalYears = 2)
            )
        )
    )
}

data class ExaminationInterval(
    val examinationType: ExaminationTypeEnumDto,
    val intervalYears: Int,
)

data class Patient(
    val age: Int,
    val sex: SexDto,
)

data class PreventionRule(
    val examinationType: ExaminationTypeEnumDto,
    val intervalsMale: List<AgeInterval>,
    val intervalsFemale: List<AgeInterval>
)

data class AgeInterval(
    val fromAge: Int,
    val toAge: Int? = null,
    val intervalYears: Int
)
