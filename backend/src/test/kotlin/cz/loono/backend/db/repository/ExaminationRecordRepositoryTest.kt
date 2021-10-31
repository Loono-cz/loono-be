package cz.loono.backend.db.repository

import cz.loono.backend.api.dto.ExaminationTypeEnumDto
import cz.loono.backend.createAccount
import cz.loono.backend.db.model.ExaminationRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDate

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class ExaminationRecordRepositoryTest {

    @Autowired
    private lateinit var recordRepo: ExaminationRecordRepository
    @Autowired
    private lateinit var accountRepo: AccountRepository

    @Test
    fun `findAllByAccount ignores other accounts`() {
        var account1 = createAccount("uid1")
        account1 = accountRepo.save(account1)
        account1 = account1.let {
            val records = listOf(
                ExaminationRecord(
                    type = ExaminationTypeEnumDto.DENTIST.name,
                    lastVisit = LocalDate.of(1999, 1, 1),
                    account = it
                )
            )
            it.copy(examinationRecords = records)
        }
        account1 = accountRepo.save(account1)

        var account2 = createAccount("uid2")
        account2 = accountRepo.save(account2)
        account2 = account2.let {
            val records = listOf(
                ExaminationRecord(
                    type = ExaminationTypeEnumDto.MAMMOGRAM.name,
                    lastVisit = LocalDate.of(2000, 1, 1),
                    account = it
                )
            )
            it.copy(examinationRecords = records)
        }
        accountRepo.save(account2)

        val allRecords = recordRepo.findAll()
        assertEquals(2, allRecords.count())

        val a1Records = recordRepo.findAllByAccount(account1)
        assertEquals(1, a1Records.size)
        assertEquals(ExaminationTypeEnumDto.DENTIST.name, a1Records.first().type)
    }
}
