package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationTypeDto
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
                    validInterval.intervalYears,
                    preventionRule.priority
                )
            } else null // no prevention required for patients age and sex
        }

    private val rules = listOf(
        PreventionRule(
            examinationType = ExaminationTypeDto.GENERAL_PRACTITIONER,
            intervalsMale = listOf(AgeInterval(fromAge = 19, intervalYears = 2)),
            intervalsFemale = listOf(AgeInterval(fromAge = 19, intervalYears = 2)),
            priority = 1
        ),
        PreventionRule(
            examinationType = ExaminationTypeDto.MAMMOGRAM,
            intervalsMale = listOf(),
            intervalsFemale = listOf(AgeInterval(fromAge = 45, intervalYears = 2)),
            priority = 2
        ),
        PreventionRule(
            examinationType = ExaminationTypeDto.GYNECOLOGIST,
            intervalsMale = listOf(),
            intervalsFemale = listOf(AgeInterval(fromAge = 15, intervalYears = 1)),
            priority = 3
        ),
        PreventionRule(
            examinationType = ExaminationTypeDto.COLONOSCOPY,
            intervalsMale = listOf(AgeInterval(fromAge = 50, intervalYears = 10)),
            intervalsFemale = listOf(AgeInterval(fromAge = 50, intervalYears = 10)),
            priority = 4
        ),
        PreventionRule(
            examinationType = ExaminationTypeDto.UROLOGIST,
            intervalsMale = listOf(AgeInterval(fromAge = 50, intervalYears = 1)),
            intervalsFemale = listOf(),
            priority = 5
        ),
        PreventionRule(
            examinationType = ExaminationTypeDto.DERMATOLOGIST,
            intervalsMale = listOf(AgeInterval(fromAge = 19, intervalYears = 1)),
            intervalsFemale = listOf(AgeInterval(fromAge = 19, intervalYears = 1)),
            priority = 6
        ),
        PreventionRule(
            examinationType = ExaminationTypeDto.ULTRASOUND_BREAST,
            intervalsMale = listOf(),
            intervalsFemale = listOf(AgeInterval(fromAge = 19, toAge = 44, intervalYears = 2)),
            priority = 7
        ),
        PreventionRule(
            examinationType = ExaminationTypeDto.DENTIST,
            intervalsMale = listOf(
                AgeInterval(fromAge = 19, intervalYears = 1)
            ),
            intervalsFemale = listOf(
                AgeInterval(fromAge = 19, intervalYears = 1)
            ),
            priority = 8
        ),
        PreventionRule(
            examinationType = ExaminationTypeDto.OPHTHALMOLOGIST,
            intervalsMale = listOf(
                AgeInterval(fromAge = 19, toAge = 44, intervalYears = 2),
                AgeInterval(fromAge = 45, toAge = 61, intervalYears = 4),
                AgeInterval(fromAge = 62, intervalYears = 2)
            ),
            intervalsFemale = listOf(
                AgeInterval(fromAge = 19, toAge = 44, intervalYears = 2),
                AgeInterval(fromAge = 45, toAge = 61, intervalYears = 4),
                AgeInterval(fromAge = 62, intervalYears = 2)
            ),
            priority = 9
        )
    )
}

data class ExaminationInterval(
    val examinationType: ExaminationTypeDto,
    val intervalYears: Int,
    val priority: Int
)

data class Patient(
    val age: Int,
    val sex: SexDto,
)

data class PreventionRule(
    val examinationType: ExaminationTypeDto,
    val intervalsMale: List<AgeInterval>,
    val intervalsFemale: List<AgeInterval>,
    val priority: Int
)

data class AgeInterval(
    val fromAge: Int,
    val toAge: Int? = null,
    val intervalYears: Int
)
