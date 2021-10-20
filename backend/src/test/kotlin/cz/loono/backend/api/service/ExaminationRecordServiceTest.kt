package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationTypeEnumDto
import cz.loono.backend.createAccount
import cz.loono.backend.db.model.ExaminationRecord
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDate

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
internal class ExaminationRecordServiceTest {

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var recordRepository: ExaminationRecordRepository

    @Test
    fun `getOrCreateRecords with missing account`() {
        val service = ExaminationRecordService(accountRepository, recordRepository)

        val ex = assertThrows<IllegalStateException> {
            service.getOrCreateRecords("uid")
        }

        assertEquals("Tried to load Examination Records for uid: uid but no such account exists.", ex.message)
    }

    @Test
    fun getOrCreateRecords() {
        val account = createAccount()
        accountRepository.save(account)
        val account2 = account.let {
            val records = listOf(
                ExaminationRecord(
                    type = ExaminationTypeEnumDto.DENTIST.name,
                    lastVisit = LocalDate.of(2000, 1, 1),
                    account = it
                ),
                ExaminationRecord(
                    type = "OBSOLETE_TYPE",
                    lastVisit = LocalDate.of(2000, 1, 1),
                    account = it
                ),
            )
            it.copy(examinationRecords = records)
        }
        accountRepository.save(account2)
        val service = ExaminationRecordService(accountRepository, recordRepository)

        val records = service.getOrCreateRecords(account.uid)

        // This test is powerful and checks the following contracts:
        // 1. Missing examinations are inserted
        // 2. Existing examinations are not modified
        // 3. Obsolete examinations are removed
        assertEquals(ExaminationTypeEnumDto.values().size, records.size)
        assertTrue {
            records.map { it.type }.containsAll(ExaminationTypeEnumDto.values().map { it.name })
        }
        assertEquals(
            LocalDate.of(2000, 1, 1),
            records.first { it.type == ExaminationTypeEnumDto.DENTIST.name }.lastVisit
        )

        // Also directly check the record table
        val persistedRecords = recordRepository.findAllByAccount(account)
        assertEquals(ExaminationTypeEnumDto.values().size, persistedRecords.size)
        assertTrue {
            persistedRecords.map { it.type }.containsAll(ExaminationTypeEnumDto.values().map { it.name })
        }
    }

    @Test
    fun `completeExamination with missing account throws`() {
        val service = ExaminationRecordService(accountRepository, recordRepository)

        val ex = assertThrows<IllegalStateException> {
            service.completeExamination(
                "uid",
                ExaminationTypeEnumDto.DENTIST.name,
                LocalDate.of(2020, 1, 1)
            )
        }

        assertEquals("Tried to complete an examination for uid: uid but no such account exists.", ex.message)
    }

    @Test
    fun `completeExamination with future date throws`() {
        accountRepository.save(createAccount())
        val service = ExaminationRecordService(accountRepository, recordRepository)

        val ex = assertThrows<IllegalArgumentException> {
            service.completeExamination(
                "uid",
                ExaminationTypeEnumDto.DENTIST.name,
                LocalDate.of(3000, 1, 1)
            )
        }

        assertEquals("Examination Completion must not be in the future.", ex.message)
    }

    @Test
    fun `completeExamination with empty record fills lastVisit`() {
        accountRepository.save(createAccount())
        val service = ExaminationRecordService(accountRepository, recordRepository)
        // Sanity precondition check!
        assertNull(
            accountRepository.findByUid("uid")!!.examinationRecords
                .firstOrNull { it.type == ExaminationTypeEnumDto.MAMMOGRAM.name }
        )

        val updatedRecord = service.completeExamination(
            "uid",
            ExaminationTypeEnumDto.MAMMOGRAM.name,
            LocalDate.of(2000, 1, 1)
        ).first { it.type == ExaminationTypeEnumDto.MAMMOGRAM.name }

        val persistedUpdatedRecord = accountRepository.findByUid("uid")!!.examinationRecords
            .first { it.type == ExaminationTypeEnumDto.MAMMOGRAM.name }

        assertEquals(LocalDate.of(2000, 1, 1), updatedRecord.lastVisit)
        assertEquals(LocalDate.of(2000, 1, 1), persistedUpdatedRecord.lastVisit)
    }

    @Test
    fun `completeExamination with date before lastVisit does not update lastVisit`() {
        saveAccountWithDentist(LocalDate.of(2000, 1, 1))
        val service = ExaminationRecordService(accountRepository, recordRepository)

        val updatedRecord = service.completeExamination(
            "uid",
            ExaminationTypeEnumDto.DENTIST.name,
            LocalDate.of(1999, 1, 1)
        ).first { it.type == ExaminationTypeEnumDto.DENTIST.name }

        val persistedUpdatedRecord = accountRepository.findByUid("uid")!!.examinationRecords
            .first { it.type == ExaminationTypeEnumDto.DENTIST.name }

        assertEquals(LocalDate.of(2000, 1, 1), updatedRecord.lastVisit)
        assertEquals(LocalDate.of(2000, 1, 1), persistedUpdatedRecord.lastVisit)
    }

    @Test
    fun `completeExamination with date after lastVisit updates lastVisit`() {
        saveAccountWithDentist()
        val service = ExaminationRecordService(accountRepository, recordRepository)

        val updatedRecord = service.completeExamination(
            "uid",
            ExaminationTypeEnumDto.DENTIST.name,
            LocalDate.of(2000, 1, 1)
        ).first { it.type == ExaminationTypeEnumDto.DENTIST.name }

        val persistedUpdatedRecord = accountRepository.findByUid("uid")!!.examinationRecords
            .first { it.type == ExaminationTypeEnumDto.DENTIST.name }

        assertEquals(LocalDate.of(2000, 1, 1), updatedRecord.lastVisit)
        assertEquals(LocalDate.of(2000, 1, 1), persistedUpdatedRecord.lastVisit)
    }

    @Test
    fun `completeExamination with null date sets the current date`() {
        saveAccountWithDentist()
        val service = ExaminationRecordService(accountRepository, recordRepository)

        val updatedRecord = service.completeExamination(
            "uid",
            ExaminationTypeEnumDto.DENTIST.name,
            null, // <-- This is under test
        ).first { it.type == ExaminationTypeEnumDto.DENTIST.name }

        val persistedUpdatedRecord = accountRepository.findByUid("uid")!!.examinationRecords
            .first { it.type == ExaminationTypeEnumDto.DENTIST.name }

        val expectedDate = LocalDate.now().withDayOfMonth(1)
        assertEquals(expectedDate, updatedRecord.lastVisit)
        assertEquals(expectedDate, persistedUpdatedRecord.lastVisit)
    }

    private fun saveAccountWithDentist(lastVisit: LocalDate = LocalDate.of(1999, 1, 1)) {
        val account = createAccount()
        accountRepository.save(account)
        val account2 = account.let {
            val records = listOf(
                ExaminationRecord(
                    type = ExaminationTypeEnumDto.DENTIST.name,
                    lastVisit = lastVisit,
                    account = it
                )
            )
            it.copy(examinationRecords = records)
        }
        accountRepository.save(account2)
    }
}
