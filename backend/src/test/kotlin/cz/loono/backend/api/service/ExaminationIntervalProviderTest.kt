package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationTypeEnumDto
import cz.loono.backend.api.dto.SexDto
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ExaminationIntervalProviderTest {

    @Test
    fun `female 19`() {
        Assertions.assertEquals(
            listOf(
                ExaminationInterval(ExaminationTypeEnumDto.GENERAL_PRACTITIONER, 2),
                ExaminationInterval(ExaminationTypeEnumDto.GYNECOLOGIST, 1),
                ExaminationInterval(ExaminationTypeEnumDto.DERMATOLOGIST, 1),
                ExaminationInterval(ExaminationTypeEnumDto.ULTRASOUND_BREAST, 2),
                ExaminationInterval(ExaminationTypeEnumDto.DENTIST, 1),
                ExaminationInterval(ExaminationTypeEnumDto.OPHTHALMOLOGIST, 2),
            ),
            ExaminationIntervalProvider.findExaminationRequests(Patient(19, SexDto.FEMALE)),
        )
    }

    @Test
    fun `female 50`() {
        Assertions.assertEquals(
            listOf(
                ExaminationInterval(ExaminationTypeEnumDto.GENERAL_PRACTITIONER, 2),
                ExaminationInterval(ExaminationTypeEnumDto.MAMMOGRAM, 2),
                ExaminationInterval(ExaminationTypeEnumDto.GYNECOLOGIST, 1),
                ExaminationInterval(ExaminationTypeEnumDto.COLONOSCOPY, 10),
                ExaminationInterval(ExaminationTypeEnumDto.DERMATOLOGIST, 1),
                ExaminationInterval(ExaminationTypeEnumDto.DENTIST, 1),
                ExaminationInterval(ExaminationTypeEnumDto.OPHTHALMOLOGIST, 4),
            ),
            ExaminationIntervalProvider.findExaminationRequests(Patient(50, SexDto.FEMALE)),
        )
    }

    @Test
    fun `male 19`() {
        Assertions.assertEquals(
            listOf(
                ExaminationInterval(ExaminationTypeEnumDto.GENERAL_PRACTITIONER, 2),
                ExaminationInterval(ExaminationTypeEnumDto.DERMATOLOGIST, 1),
                ExaminationInterval(ExaminationTypeEnumDto.DENTIST, 1),
                ExaminationInterval(ExaminationTypeEnumDto.OPHTHALMOLOGIST, 2),
            ),
            ExaminationIntervalProvider.findExaminationRequests(Patient(19, SexDto.MALE)),
        )
    }

    @Test
    fun `male 70`() {
        Assertions.assertEquals(
            listOf(
                ExaminationInterval(ExaminationTypeEnumDto.GENERAL_PRACTITIONER, 2),
                ExaminationInterval(ExaminationTypeEnumDto.COLONOSCOPY, 10),
                ExaminationInterval(ExaminationTypeEnumDto.UROLOGIST, 1),
                ExaminationInterval(ExaminationTypeEnumDto.DERMATOLOGIST, 1),
                ExaminationInterval(ExaminationTypeEnumDto.DENTIST, 1),
                ExaminationInterval(ExaminationTypeEnumDto.OPHTHALMOLOGIST, 2),
            ),
            ExaminationIntervalProvider.findExaminationRequests(Patient(70, SexDto.MALE)),
        )
    }
}
