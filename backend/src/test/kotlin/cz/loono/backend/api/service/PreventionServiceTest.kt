package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.ExaminationTypeEnumDto
import cz.loono.backend.api.dto.PreventionStatusDto
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.ExaminationRecord
import cz.loono.backend.db.model.UserAuxiliary
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class PreventionServiceTest {

    private val examinationRecordRepository: ExaminationRecordRepository = mock()
    private val accountRepository: AccountRepository = mock()
    private val preventionService = PreventionService(examinationRecordRepository, accountRepository)

    @Test
    fun `get prevention for patient`() {
        val uuid = UUID.randomUUID().toString()
        val examsUUIDs: MutableMap<Int, String> = mutableMapOf()
        repeat(6) {
            examsUUIDs[it] = UUID.randomUUID().toString()
        }
        val age: Long = 45
        val now = LocalDateTime.now()
        val lastVisit = now.minusYears(1)
        val account = Account(
            userAuxiliary = UserAuxiliary(
                sex = "MALE",
                birthdate = LocalDate.now().minusYears(age)
            )
        )

        whenever(accountRepository.findByUid(uuid)).thenReturn(account)
        whenever(examinationRecordRepository.findAllByAccountOrderByPlannedDateDesc(account)).thenReturn(
            setOf(
                ExaminationRecord(
                    id = 1,
                    plannedDate = now,
                    type = ExaminationTypeEnumDto.GENERAL_PRACTITIONER,
                    uuid = examsUUIDs[0]!!
                ),
                ExaminationRecord(
                    id = 2,
                    type = ExaminationTypeEnumDto.OPHTHALMOLOGIST,
                    uuid = examsUUIDs[1]!!
                ), // is only planned
                ExaminationRecord(
                    id = 3,
                    plannedDate = lastVisit,
                    type = ExaminationTypeEnumDto.COLONOSCOPY,
                    uuid = examsUUIDs[2]!!
                ), // is not required
                ExaminationRecord(
                    id = 4,
                    type = ExaminationTypeEnumDto.DENTIST,
                    uuid = examsUUIDs[3]!!
                ),
                ExaminationRecord(
                    id = 5,
                    type = ExaminationTypeEnumDto.DENTIST,
                    plannedDate = LocalDateTime.MIN,
                    status = ExaminationStatusDto.CONFIRMED,
                    uuid = examsUUIDs[4]!!
                ),
                ExaminationRecord(
                    id = 6,
                    type = ExaminationTypeEnumDto.DERMATOLOGIST,
                    plannedDate = LocalDateTime.MIN,
                    status = ExaminationStatusDto.CONFIRMED,
                    uuid = examsUUIDs[5]!!
                )
            )
        )

        val result = preventionService.getPreventionStatus(uuid)
        assertEquals(
            /* expected = */ listOf(
                PreventionStatusDto(
                    uuid = examsUUIDs[0],
                    examinationType = ExaminationTypeEnumDto.GENERAL_PRACTITIONER,
                    intervalYears = 2,
                    firstExam = true,
                    priority = 1,
                    state = ExaminationStatusDto.NEW,
                    count = 0,
                    plannedDate = now
                ),
                PreventionStatusDto(
                    uuid = examsUUIDs[5],
                    examinationType = ExaminationTypeEnumDto.DERMATOLOGIST,
                    intervalYears = 1,
                    plannedDate = LocalDateTime.MIN,
                    lastConfirmedDate = LocalDateTime.MIN,
                    firstExam = true,
                    priority = 6,
                    state = ExaminationStatusDto.CONFIRMED,
                    count = 1
                ),
                PreventionStatusDto(
                    uuid = examsUUIDs[3],
                    examinationType = ExaminationTypeEnumDto.DENTIST,
                    intervalYears = 1,
                    plannedDate = null,
                    firstExam = true,
                    priority = 8,
                    state = ExaminationStatusDto.NEW,
                    count = 1,
                    lastConfirmedDate = LocalDateTime.MIN
                ),
                PreventionStatusDto(
                    uuid = examsUUIDs[1],
                    examinationType = ExaminationTypeEnumDto.OPHTHALMOLOGIST,
                    intervalYears = 4,
                    plannedDate = null,
                    firstExam = true,
                    priority = 9,
                    state = ExaminationStatusDto.NEW,
                    count = 0
                ),
            ),
            /* actual = */ result
        )
    }
}
