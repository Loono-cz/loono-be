package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationTypeEnumDto
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
import java.util.*

class PreventionServiceTest {

    private val examinationRecordRepository: ExaminationRecordRepository = mock()
    private val accountRepository: AccountRepository = mock()
    private val preventionService = PreventionService(examinationRecordRepository, accountRepository)

    @Test
    fun `get prevention for patient`() {
        val uuid = UUID.randomUUID()
        val age: Long = 45
        val lastVisit = LocalDate.now().minusYears(1)
        val account = Account(
            userAuxiliary = UserAuxiliary(
                sex = "MALE",
                birthdate = LocalDate.now().minusYears(age)
            )
        )

        whenever(accountRepository.findByUid(uuid.toString())).thenReturn(account)
        whenever(examinationRecordRepository.findAllByAccount(account)).thenReturn(
            setOf(
                ExaminationRecord(lastVisit = lastVisit, type = ExaminationTypeEnumDto.GENERAL_PRACTITIONER.name),
                ExaminationRecord(lastVisit = null, type = ExaminationTypeEnumDto.OPHTHALMOLOGIST.name), // is only planned
                ExaminationRecord(lastVisit = lastVisit, type = ExaminationTypeEnumDto.COLONOSCOPY.name) // is not required
            )
        )

        val result = preventionService.getPreventionStatus(uuid)
        assertEquals(
            listOf(
                PreventionStatus(ExaminationTypeEnumDto.GENERAL_PRACTITIONER, 2, lastVisit),
                PreventionStatus(ExaminationTypeEnumDto.DERMATOLOGIST, 1, null),
                PreventionStatus(ExaminationTypeEnumDto.DENTIST, 1, null),
                PreventionStatus(ExaminationTypeEnumDto.OPHTHALMOLOGIST, 4, null),
            ),
            result
        )
    }
}
