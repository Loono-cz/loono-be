package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationPreventionStatusDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.SelfExaminationPreventionStatusDto
import cz.loono.backend.api.dto.SelfExaminationResultDto
import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationTypeDto
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.ExaminationRecord
import cz.loono.backend.db.model.SelfExaminationRecord
import cz.loono.backend.db.model.UserAuxiliary
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class PreventionServiceTest {

    private val examinationRecordRepository: ExaminationRecordRepository = mock()
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository = mock()
    private val accountRepository: AccountRepository = mock()
    private val preventionService =
        PreventionService(examinationRecordRepository, selfExaminationRecordRepository, accountRepository)

    @Test
    fun `get examinations for patient`() {
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
                    type = ExaminationTypeDto.GENERAL_PRACTITIONER,
                    uuid = examsUUIDs[0]!!
                ),
                ExaminationRecord(
                    id = 2,
                    type = ExaminationTypeDto.OPHTHALMOLOGIST,
                    uuid = examsUUIDs[1]!!
                ), // is only planned
                ExaminationRecord(
                    id = 3,
                    plannedDate = lastVisit,
                    type = ExaminationTypeDto.COLONOSCOPY,
                    uuid = examsUUIDs[2]!!
                ), // is not required
                ExaminationRecord(
                    id = 4,
                    type = ExaminationTypeDto.DENTIST,
                    uuid = examsUUIDs[3]!!
                ),
                ExaminationRecord(
                    id = 5,
                    type = ExaminationTypeDto.DENTIST,
                    plannedDate = LocalDateTime.MIN,
                    status = ExaminationStatusDto.CONFIRMED,
                    uuid = examsUUIDs[4]!!
                ),
                ExaminationRecord(
                    id = 6,
                    type = ExaminationTypeDto.DERMATOLOGIST,
                    plannedDate = LocalDateTime.MIN,
                    status = ExaminationStatusDto.CONFIRMED,
                    uuid = examsUUIDs[5]!!
                )
            )
        )

        val result = preventionService.getPreventionStatus(uuid)
        assertEquals(
            /* expected = */ listOf(
                ExaminationPreventionStatusDto(
                    uuid = examsUUIDs[0].toString(),
                    examinationType = ExaminationTypeDto.GENERAL_PRACTITIONER,
                    intervalYears = 2,
                    firstExam = true,
                    priority = 1,
                    state = ExaminationStatusDto.NEW,
                    count = 0,
                    plannedDate = now
                ),
                ExaminationPreventionStatusDto(
                    uuid = examsUUIDs[5].toString(),
                    examinationType = ExaminationTypeDto.DERMATOLOGIST,
                    intervalYears = 1,
                    plannedDate = LocalDateTime.MIN,
                    lastConfirmedDate = LocalDateTime.MIN,
                    firstExam = true,
                    priority = 6,
                    state = ExaminationStatusDto.CONFIRMED,
                    count = 1
                ),
                ExaminationPreventionStatusDto(
                    uuid = examsUUIDs[3].toString(),
                    examinationType = ExaminationTypeDto.DENTIST,
                    intervalYears = 1,
                    plannedDate = null,
                    firstExam = true,
                    priority = 8,
                    state = ExaminationStatusDto.NEW,
                    count = 1,
                    lastConfirmedDate = LocalDateTime.MIN
                ),
                ExaminationPreventionStatusDto(
                    uuid = examsUUIDs[1].toString(),
                    examinationType = ExaminationTypeDto.OPHTHALMOLOGIST,
                    intervalYears = 4,
                    plannedDate = null,
                    firstExam = true,
                    priority = 9,
                    state = ExaminationStatusDto.NEW,
                    count = 0
                ),
            ),
            /* actual = */ result.examinations
        )
    }

    @Test
    fun `get self-examinations for patient`() {
        val uuid = UUID.randomUUID().toString()
        val examsUUIDs: MutableMap<Int, String> = mutableMapOf()
        repeat(5) {
            examsUUIDs[it] = UUID.randomUUID().toString()
        }
        val age: Long = 45
        val now = LocalDate.now()
        val account = Account(
            userAuxiliary = UserAuxiliary(
                sex = "FEMALE",
                birthdate = LocalDate.now().minusYears(age)
            )
        )

        whenever(accountRepository.findByUid(uuid)).thenReturn(account)
        whenever(selfExaminationRecordRepository.findAllByAccount(account)).thenReturn(
            setOf(
                SelfExaminationRecord(
                    id = 3,
                    dueDate = now.minusMonths(1L),
                    type = SelfExaminationTypeDto.BREAST,
                    status = SelfExaminationStatusDto.MISSED,
                    result = null,
                    uuid = examsUUIDs[2]!!
                ),
                SelfExaminationRecord(
                    id = 4,
                    dueDate = now.minusMonths(2L),
                    type = SelfExaminationTypeDto.BREAST,
                    status = SelfExaminationStatusDto.COMPLETED,
                    result = SelfExaminationResultDto.FINDING,
                    uuid = examsUUIDs[3]!!
                ),
                SelfExaminationRecord(
                    id = 5,
                    dueDate = now.minusMonths(3L),
                    type = SelfExaminationTypeDto.BREAST,
                    status = SelfExaminationStatusDto.MISSED,
                    result = null,
                    uuid = examsUUIDs[4]!!
                ),
                SelfExaminationRecord(
                    id = 2,
                    dueDate = now,
                    type = SelfExaminationTypeDto.BREAST,
                    status = SelfExaminationStatusDto.COMPLETED,
                    result = SelfExaminationResultDto.OK,
                    uuid = examsUUIDs[1]!!
                ),
                SelfExaminationRecord(
                    id = 1,
                    dueDate = now.plusMonths(1L),
                    type = SelfExaminationTypeDto.BREAST,
                    status = SelfExaminationStatusDto.PLANNED,
                    result = null,
                    uuid = examsUUIDs[0]!!
                )
            )
        )

        val result = preventionService.getPreventionStatus(uuid)
        assertEquals(
            /* expected = */ listOf(
                SelfExaminationPreventionStatusDto(
                    lastExamUuid = examsUUIDs[0],
                    type = SelfExaminationTypeDto.BREAST,
                    plannedDate = now.plusMonths(1L),
                    history = listOf(
                        SelfExaminationStatusDto.MISSED,
                        SelfExaminationStatusDto.COMPLETED,
                        SelfExaminationStatusDto.MISSED,
                        SelfExaminationStatusDto.COMPLETED,
                        SelfExaminationStatusDto.PLANNED
                    )
                ),
            ),
            /* actual = */ result.selfexaminations
        )
    }

    @Test
    fun `get first empty suitable self-examinations`() {
        val uuid = UUID.randomUUID().toString()
        val age: Long = 45
        val account = Account(
            userAuxiliary = UserAuxiliary(
                sex = "MALE",
                birthdate = LocalDate.now().minusYears(age)
            )
        )

        whenever(accountRepository.findByUid(uuid)).thenReturn(account)
        whenever(selfExaminationRecordRepository.findAllByAccount(account)).thenReturn(emptySet())

        val result = preventionService.getPreventionStatus(uuid)
        assertEquals(
            /* expected = */ listOf(
                SelfExaminationPreventionStatusDto(
                    lastExamUuid = null,
                    type = SelfExaminationTypeDto.TESTICULAR,
                    plannedDate = null,
                    history = emptyList()
                ),
            ),
            /* actual = */ result.selfexaminations
        )
    }
}
