package cz.loono.backend.api.service

import cz.loono.backend.api.dto.SelfExaminationResultDto
import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationTypeDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.createAccount
import cz.loono.backend.db.model.SelfExaminationRecord
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock

class ExaminationRecordServiceSelfResultTest {

    private val accountRepository: AccountRepository = mock()
    private val examinationRecordRepository: ExaminationRecordRepository = mock()
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository = mock()
    private val preventionService =
        PreventionService(examinationRecordRepository, selfExaminationRecordRepository, accountRepository)
    private val clock = Clock.systemUTC()

    private val examinationRecordService = ExaminationRecordService(
        accountRepository,
        examinationRecordRepository,
        selfExaminationRecordRepository,
        preventionService,
        clock
    )

    @Test
    fun `happy case finding is OK`() {
        val account = createAccount(sex = SexDto.FEMALE.name)
        whenever(accountRepository.findByUid("101")).thenReturn(account)
        whenever(
            selfExaminationRecordRepository.findAllByAccountAndTypeOrderByDueDateDesc(
                account,
                SelfExaminationTypeDto.BREAST
            )
        ).thenReturn(
            listOf(
                SelfExaminationRecord(
                    account = account,
                    status = SelfExaminationStatusDto.WAITING_FOR_RESULT
                )
            )
        )

        val result = examinationRecordService.processFindingResult(
            SelfExaminationTypeDto.BREAST,
            SelfExaminationResultDto(result = SelfExaminationResultDto.Result.OK),
            "101"
        )

        assert(result.message == "Result completed as OK.")
        verify(selfExaminationRecordRepository, times(2)).save(any())
    }

    @Test
    fun `happy case finding is NOT OK`() {
        val account = createAccount(sex = SexDto.FEMALE.name)
        whenever(accountRepository.findByUid("101")).thenReturn(account)
        whenever(
            selfExaminationRecordRepository.findAllByAccountAndTypeOrderByDueDateDesc(
                account,
                SelfExaminationTypeDto.BREAST
            )
        ).thenReturn(
            listOf(
                SelfExaminationRecord(
                    account = account,
                    status = SelfExaminationStatusDto.WAITING_FOR_RESULT
                )
            )
        )

        val result = examinationRecordService.processFindingResult(
            SelfExaminationTypeDto.BREAST,
            SelfExaminationResultDto(result = SelfExaminationResultDto.Result.NOT_OK),
            "101"
        )

        assert(result.message == "The examination marked as NOT OK. Notifications are turned off.")
        verify(selfExaminationRecordRepository, times(1)).save(any())
        verify(accountRepository, times(1)).save(account.copy(notify = false))
    }

    @Test
    fun `missing account`() {
        whenever(accountRepository.findByUid("101")).thenReturn(null)

        assertThrows<LoonoBackendException> {
            examinationRecordService.processFindingResult(
                SelfExaminationTypeDto.BREAST,
                SelfExaminationResultDto(result = SelfExaminationResultDto.Result.NOT_OK),
                "101"
            )
        }
    }

    @Test
    fun `not suitable sex`() {
        val account = createAccount(sex = SexDto.MALE.name)
        whenever(accountRepository.findByUid("101")).thenReturn(account)

        assertThrows<LoonoBackendException> {
            examinationRecordService.processFindingResult(
                SelfExaminationTypeDto.BREAST,
                SelfExaminationResultDto(result = SelfExaminationResultDto.Result.NOT_OK),
                "101"
            )
        }
    }

    @Test
    fun `invalid result`() {
        val account = createAccount(
            uid = "101",
            sex = SexDto.FEMALE.name
        )
        whenever(accountRepository.findByUid("101")).thenReturn(account)
        whenever(
            selfExaminationRecordRepository.findAllByAccountAndTypeOrderByDueDateDesc(
                account,
                SelfExaminationTypeDto.BREAST
            )
        ).thenReturn(listOf(SelfExaminationRecord(dueDate = null, account = account)))

        assertThrows<LoonoBackendException> {
            examinationRecordService.confirmSelfExam(
                SelfExaminationTypeDto.BREAST,
                SelfExaminationResultDto(result = SelfExaminationResultDto.Result.FINDING),
                "101"
            )
        }
    }
}
