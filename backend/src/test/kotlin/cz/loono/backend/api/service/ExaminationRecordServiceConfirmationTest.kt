package cz.loono.backend.api.service

import cz.loono.backend.api.dto.SelfExaminationResultDto
import cz.loono.backend.api.dto.SelfExaminationTypeDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.createAccount
import cz.loono.backend.db.model.SelfExaminationRecord
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.LocalDate

class ExaminationRecordServiceConfirmationTest {

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
    fun `happy case`() {
        val account = createAccount(sex = SexDto.FEMALE.name)
        whenever(accountRepository.findByUid("101")).thenReturn(account)
        whenever(
            selfExaminationRecordRepository.findAllByAccountAndTypeOrderByDueDateDesc(
                account,
                SelfExaminationTypeDto.BREAST
            )
        ).thenReturn(listOf(SelfExaminationRecord(dueDate = LocalDate.now(), account = account)))

        assertDoesNotThrow("Happy case") {
            examinationRecordService.confirmSelfExam(SelfExaminationTypeDto.BREAST, SelfExaminationResultDto.OK, "101")
        }
    }

    @Test
    fun `not-suitable sex`() {
        val account = createAccount(sex = SexDto.MALE.name)
        whenever(accountRepository.findByUid("101")).thenReturn(account)
        whenever(
            selfExaminationRecordRepository.findAllByAccountAndTypeOrderByDueDateDesc(
                account,
                SelfExaminationTypeDto.BREAST
            )
        ).thenReturn(listOf(SelfExaminationRecord(account = account)))

        assertThrows<LoonoBackendException> {
            examinationRecordService.confirmSelfExam(SelfExaminationTypeDto.BREAST, SelfExaminationResultDto.OK, "101")
        }
    }

    @Test
    fun `too early`() {
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
        ).thenReturn(listOf(SelfExaminationRecord(dueDate = LocalDate.now().minusDays(3), account = account)))

        assertThrows<LoonoBackendException> {
            examinationRecordService.confirmSelfExam(SelfExaminationTypeDto.BREAST, SelfExaminationResultDto.OK, "101")
        }
    }

    @Test
    fun `too late`() {
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
        ).thenReturn(listOf(SelfExaminationRecord(dueDate = LocalDate.now().plusDays(3), account = account)))

        assertThrows<LoonoBackendException> {
            examinationRecordService.confirmSelfExam(SelfExaminationTypeDto.BREAST, SelfExaminationResultDto.OK, "101")
        }
    }
}
