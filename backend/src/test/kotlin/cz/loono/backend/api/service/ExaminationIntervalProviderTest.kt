package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.SexDto
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ExaminationIntervalProviderTest {
    @Test
    fun `female 19`() {
        Assertions.assertEquals(
            listOf(
                ExaminationInterval(ExaminationTypeDto.GENERAL_PRACTITIONER, 2, 1),
                ExaminationInterval(ExaminationTypeDto.GYNECOLOGIST, 1, 3),
                ExaminationInterval(ExaminationTypeDto.DERMATOLOGIST, 1, 6),
                ExaminationInterval(ExaminationTypeDto.ULTRASOUND_BREAST, 2, 7),
                ExaminationInterval(ExaminationTypeDto.DENTIST, 1, 8),
                ExaminationInterval(ExaminationTypeDto.OPHTHALMOLOGIST, 2, 9),
            ),
            ExaminationIntervalProvider.findExaminationRequests(Patient(19, SexDto.FEMALE)),
        )
    }

    @Test
    fun `female 50`() {
        Assertions.assertEquals(
            listOf(
                ExaminationInterval(ExaminationTypeDto.GENERAL_PRACTITIONER, 2, 1),
                ExaminationInterval(ExaminationTypeDto.MAMMOGRAM, 2, 2),
                ExaminationInterval(ExaminationTypeDto.GYNECOLOGIST, 1, 3),
                ExaminationInterval(ExaminationTypeDto.COLONOSCOPY, 10, 4),
                ExaminationInterval(ExaminationTypeDto.DERMATOLOGIST, 1, 6),
                ExaminationInterval(ExaminationTypeDto.DENTIST, 1, 8),
                ExaminationInterval(ExaminationTypeDto.OPHTHALMOLOGIST, 4, 9),
            ),
            ExaminationIntervalProvider.findExaminationRequests(Patient(50, SexDto.FEMALE)),
        )
    }

    @Test
    fun `male 19`() {
        Assertions.assertEquals(
            listOf(
                ExaminationInterval(ExaminationTypeDto.GENERAL_PRACTITIONER, 2, 1),
                ExaminationInterval(ExaminationTypeDto.DERMATOLOGIST, 1, 6),
                ExaminationInterval(ExaminationTypeDto.DENTIST, 1, 8),
                ExaminationInterval(ExaminationTypeDto.OPHTHALMOLOGIST, 2, 9),
            ),
            ExaminationIntervalProvider.findExaminationRequests(Patient(19, SexDto.MALE)),
        )
    }

    @Test
    fun `male 70`() {
        Assertions.assertEquals(
            listOf(
                ExaminationInterval(ExaminationTypeDto.GENERAL_PRACTITIONER, 2, 1),
                ExaminationInterval(ExaminationTypeDto.COLONOSCOPY, 10, 4),
                ExaminationInterval(ExaminationTypeDto.UROLOGIST, 1, 5),
                ExaminationInterval(ExaminationTypeDto.DERMATOLOGIST, 1, 6),
                ExaminationInterval(ExaminationTypeDto.DENTIST, 1, 8),
                ExaminationInterval(ExaminationTypeDto.OPHTHALMOLOGIST, 2, 9),
            ),
            ExaminationIntervalProvider.findExaminationRequests(Patient(70, SexDto.MALE)),
        )
    }
}
