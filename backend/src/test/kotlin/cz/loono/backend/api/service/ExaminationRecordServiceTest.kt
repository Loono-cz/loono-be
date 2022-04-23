package cz.loono.backend.api.service

import cz.loono.backend.api.dto.BadgeTypeDto
import cz.loono.backend.api.dto.ExaminationRecordDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.SelfExaminationResultDto
import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationTypeDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.createAccount
import cz.loono.backend.db.model.ExaminationRecord
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest(properties = ["spring.profiles.active=test"])
@Transactional
class ExaminationRecordServiceTest(
    private val accountRepository: AccountRepository,
    private val examinationRecordRepository: ExaminationRecordRepository,
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository,
    private val examinationRecordService: ExaminationRecordService
) {

    @Test
    fun `changing state for a non-existing user`() {
        assertThrows<LoonoBackendException>("Account not found") {
            examinationRecordService.createOrUpdateExam(
                ExaminationRecordDto(
                    uuid = "1",
                    type = ExaminationTypeDto.GENERAL_PRACTITIONER,
                    status = ExaminationStatusDto.NEW
                ),
                "1"
            )
        }
    }

    @Test
    fun `try to create another new exam of the same type`() {
        val account = accountRepository.save(
            createAccount(
                uid = "101",
                sex = SexDto.MALE.value,
                birthday = LocalDate.of(1990, 9, 9)
            )
        )
        val exam = ExaminationRecordDto(
            type = ExaminationTypeDto.GENERAL_PRACTITIONER
        )
        examinationRecordRepository.save(ExaminationRecord(type = exam.type, account = account))
        val secondExam = ExaminationRecordDto(
            type = ExaminationTypeDto.GENERAL_PRACTITIONER,
            status = ExaminationStatusDto.NEW,
            firstExam = false,
            plannedDate = LocalDateTime.MAX
        )

        assertThrows<LoonoBackendException> {
            examinationRecordService.createOrUpdateExam(secondExam, "101")
        }
    }

    @Test
    fun `changing state of a non-existing exam`() {
        accountRepository.save(createAccount(uid = "101"))
        val exam = ExaminationRecordDto(
            uuid = "1",
            type = ExaminationTypeDto.GENERAL_PRACTITIONER,
            status = ExaminationStatusDto.NEW,
            firstExam = false,
            plannedDate = LocalDateTime.MIN
        )

        assertThrows<LoonoBackendException>("The given examination identifier not found.") {
            examinationRecordService.createOrUpdateExam(
                exam,
                "101"
            )
        }
    }

    @Test
    fun `Should add badges and points upon account creation`() {
        val uid = "101"
        accountRepository.save(
            createAccount(
                uid = uid,
                sex = SexDto.MALE.value,
                birthday = LocalDate.of(1990, 9, 9)
            )
        )

        val exam = ExaminationRecordDto(
            firstExam = true,
            type = ExaminationTypeDto.GENERAL_PRACTITIONER,
            plannedDate = LocalDateTime.now().minusDays(1)
        )

        examinationRecordService.createOrUpdateExam(exam, uid)
        val actual = accountRepository.findByUid(uid)

        assertThat(actual?.points).isEqualTo(200)
        assertThat(actual?.badges).hasSize(1)
        assertThat(actual?.badges?.first()?.type).isEqualTo("COAT")
    }

    @Test
    fun `Create correct future exam`() {
        val uid = "101"
        accountRepository.save(
            createAccount(
                uid = uid,
                sex = SexDto.MALE.value,
                birthday = LocalDate.of(1990, 9, 9)
            )
        )
        val exam = ExaminationRecordDto(
            type = ExaminationTypeDto.GENERAL_PRACTITIONER,
            plannedDate = LocalDateTime.now().plusYears(2)
        )

        examinationRecordService.createOrUpdateExam(exam, uid)
        val actual = accountRepository.findByUid(uid)

        assertThat(actual?.points).isEqualTo(0)
        assertThat(actual?.badges).isEmpty()
    }

    @Test
    fun `Create a new exam in past`() {
        val uid = "101"
        val account = accountRepository.save(
            createAccount(
                uid = uid,
                sex = SexDto.MALE.value,
                birthday = LocalDate.of(1990, 9, 9)
            )
        )

        examinationRecordRepository.save(
            ExaminationRecord(
                type = ExaminationTypeDto.GENERAL_PRACTITIONER,
                status = ExaminationStatusDto.CONFIRMED,
                plannedDate = LocalDateTime.now().minusYears(1),
                account = account
            )
        )
        val exam = ExaminationRecordDto(
            type = ExaminationTypeDto.GENERAL_PRACTITIONER,
            plannedDate = LocalDateTime.now().minusDays(1)
        )

        assertThrows<LoonoBackendException> {
            examinationRecordService.createOrUpdateExam(exam, uid)
        }
    }

    @Test
    fun `valid changing of state`() {
        val account = accountRepository.save(
            createAccount(
                uid = "101",
                sex = SexDto.MALE.value,
                birthday = LocalDate.of(1990, 9, 9)
            )
        )

        val exam = ExaminationRecordDto(
            type = ExaminationTypeDto.GENERAL_PRACTITIONER
        )
        val storedExam = examinationRecordRepository.save(ExaminationRecord(type = exam.type, account = account))
        val changedExam = ExaminationRecordDto(
            uuid = storedExam.uuid,
            type = ExaminationTypeDto.GENERAL_PRACTITIONER,
            status = ExaminationStatusDto.NEW,
            firstExam = false,
            plannedDate = LocalDateTime.MAX
        )

        val result = examinationRecordService.createOrUpdateExam(changedExam, "101")

        assert(result.status == changedExam.status)
        assert(result.firstExam == changedExam.firstExam)
        assert(result.plannedDate == changedExam.plannedDate)
    }

    @Test
    fun `try to create exam with non-suitable sex`() {
        accountRepository.save(
            createAccount(
                uid = "101",
                sex = SexDto.MALE.value,
                birthday = LocalDate.of(1990, 9, 9)
            )
        )

        val examRecord = ExaminationRecordDto(
            type = ExaminationTypeDto.GYNECOLOGIST,
            plannedDate = LocalDateTime.MAX
        )

        assertThrows<LoonoBackendException>("The account doesn't have rights to create this type of examinations.") {
            examinationRecordService.createOrUpdateExam(examRecord, "101")
        }
    }

    @Test
    fun `confirm exam`() {
        val account = accountRepository.save(createAccount(uid = "101"))

        val exam = ExaminationRecordDto(
            type = ExaminationTypeDto.GENERAL_PRACTITIONER,
            status = ExaminationStatusDto.NEW
        )
        val storedExam = examinationRecordRepository.save(ExaminationRecord(type = exam.type, account = account))

        val result = examinationRecordService.confirmExam(storedExam.uuid!!, "101")

        assert(result.status == ExaminationStatusDto.CONFIRMED)
    }

    @Test
    fun `cancel exam`() {
        val account = accountRepository.save(createAccount(uid = "101"))

        val exam = ExaminationRecordDto(
            type = ExaminationTypeDto.GENERAL_PRACTITIONER,
            status = ExaminationStatusDto.NEW
        )
        val storedExam = examinationRecordRepository.save(ExaminationRecord(type = exam.type, account = account))

        val result = examinationRecordService.cancelExam(storedExam.uuid!!, "101")

        assert(result.status == ExaminationStatusDto.CANCELED)
    }

    @Test
    fun `complete first self-exam of incorrect type`() {
        val account =
            accountRepository.save(createAccount(uid = "101", sex = SexDto.MALE.name))

        assertThrows<LoonoBackendException>("This type of examination cannot applied for the account.") {
            examinationRecordService.confirmSelfExam(
                SelfExaminationTypeDto.BREAST,
                SelfExaminationResultDto(result = SelfExaminationResultDto.Result.OK),
                account.uid
            )
        }
    }

    @Test
    fun `complete first self-exam`() {
        val account =
            accountRepository.save(createAccount(uid = "101", sex = SexDto.FEMALE.name))

        val result = examinationRecordService.confirmSelfExam(
            SelfExaminationTypeDto.BREAST,
            SelfExaminationResultDto(result = SelfExaminationResultDto.Result.OK),
            account.uid
        )

        assert(result.points == 50)
        assert(result.allPoints == 50)
        assert(result.streak == 1)
        assert(result.badgeLevel == 1)
        assert(result.badgeType == BadgeTypeDto.SHIELD)
        assert(
            selfExaminationRecordRepository.findAllByAccountAndTypeOrderByDueDateDesc(
                account,
                SelfExaminationTypeDto.BREAST
            ).first().waitingTo == null
        )
    }

    @Test
    fun `complete first self-exam with finding`() {
        val account =
            accountRepository.save(createAccount(uid = "101", sex = SexDto.FEMALE.name))

        val result = examinationRecordService.confirmSelfExam(
            SelfExaminationTypeDto.BREAST,
            SelfExaminationResultDto(result = SelfExaminationResultDto.Result.FINDING),
            account.uid
        )

        assert(result.points == 50)
        assert(result.allPoints == 50)
        assert(result.streak == 1)
        assert(result.badgeLevel == 1)
        assert(result.badgeType == BadgeTypeDto.SHIELD)
        assert(
            selfExaminationRecordRepository.findAllByAccountAndTypeOrderByDueDateDesc(
                account,
                SelfExaminationTypeDto.BREAST
            ).first().waitingTo == LocalDate.now().plusDays(56)
        )
    }

    @Test
    fun `complete second self-exam`() {
        val account =
            accountRepository.save(
                createAccount(
                    uid = "101", sex = SexDto.MALE.name, points = 150
                )
            )

        examinationRecordService.confirmSelfExam(
            SelfExaminationTypeDto.TESTICULAR,
            SelfExaminationResultDto(result = SelfExaminationResultDto.Result.OK),
            account.uid
        )
        val exam = selfExaminationRecordRepository.findAllByStatus(SelfExaminationStatusDto.PLANNED).first()
        selfExaminationRecordRepository.save(exam.copy(dueDate = LocalDate.now()))

        val result = examinationRecordService.confirmSelfExam(
            SelfExaminationTypeDto.TESTICULAR,
            SelfExaminationResultDto(result = SelfExaminationResultDto.Result.OK),
            account.uid
        )

        assert(result.points == 50)
        assert(result.allPoints == 250)
        assert(result.streak == 2)
        assert(result.badgeLevel == 1)
        assert(result.badgeType == BadgeTypeDto.SHIELD)
        assert(
            selfExaminationRecordRepository.findAllByAccountAndTypeOrderByDueDateDesc(
                account,
                SelfExaminationTypeDto.TESTICULAR
            ).first().waitingTo == null
        )
    }

    @Test
    fun `complete second self-exam with finding`() {
        val account =
            accountRepository.save(
                createAccount(
                    uid = "101", sex = SexDto.MALE.name, points = 150
                )
            )

        examinationRecordService.confirmSelfExam(
            SelfExaminationTypeDto.TESTICULAR,
            SelfExaminationResultDto(result = SelfExaminationResultDto.Result.OK),
            account.uid
        )
        val exam = selfExaminationRecordRepository.findAllByStatus(SelfExaminationStatusDto.PLANNED).first()
        selfExaminationRecordRepository.save(exam.copy(dueDate = LocalDate.now()))

        val result = examinationRecordService.confirmSelfExam(
            SelfExaminationTypeDto.TESTICULAR,
            SelfExaminationResultDto(result = SelfExaminationResultDto.Result.FINDING),
            account.uid
        )

        assert(result.points == 50)
        assert(result.allPoints == 250)
        assert(result.streak == 2)
        assert(result.badgeLevel == 1)
        assert(result.badgeType == BadgeTypeDto.SHIELD)
        assert(
            selfExaminationRecordRepository.findAllByAccountAndTypeOrderByDueDateDesc(
                account,
                SelfExaminationTypeDto.TESTICULAR
            )[1].waitingTo == LocalDate.now().plusDays(56)
        )
    }

    @Test
    fun `complete 6th self-exam`() {
        val account =
            accountRepository.save(
                createAccount(
                    uid = "101", sex = SexDto.MALE.name, points = 150
                )
            )

        repeat(5) {
            examinationRecordService.confirmSelfExam(
                SelfExaminationTypeDto.TESTICULAR,
                SelfExaminationResultDto(result = SelfExaminationResultDto.Result.OK),
                account.uid
            )
            val exam = selfExaminationRecordRepository.findAllByStatus(SelfExaminationStatusDto.PLANNED).first()
            selfExaminationRecordRepository.save(exam.copy(dueDate = LocalDate.now()))
        }

        val result = examinationRecordService.confirmSelfExam(
            SelfExaminationTypeDto.TESTICULAR,
            SelfExaminationResultDto(result = SelfExaminationResultDto.Result.OK),
            account.uid
        )

        assert(result.points == 50)
        assert(result.allPoints == 450)
        assert(result.streak == 6)
        assert(result.badgeLevel == 3)
        assert(result.badgeType == BadgeTypeDto.SHIELD)
    }

    @Test
    fun `Should not add another badge when cancelling`() {
        accountRepository.save(createAccount(uid = "101", sex = SexDto.MALE.name, points = 150))
        val exam = ExaminationRecordDto(
            type = ExaminationTypeDto.GENERAL_PRACTITIONER,
            plannedDate = LocalDateTime.now().minusYears(2),
            firstExam = true
        )

        val examRecord = examinationRecordService.createOrUpdateExam(exam, "101")
        examinationRecordService.cancelExam(examRecord.uuid!!, "101")

        assertThat(accountRepository.findByUid("101")?.badges?.first()?.level).isEqualTo(1)
    }

    @Test
    fun `Should not add another badge when updating`() {
        accountRepository.save(createAccount(uid = "101", sex = SexDto.MALE.name, points = 150))
        val exam = ExaminationRecordDto(
            type = ExaminationTypeDto.DERMATOLOGIST,
            plannedDate = LocalDateTime.now().minusYears(3),
            firstExam = true
        )

        val examRecord = examinationRecordService.createOrUpdateExam(exam, "101")
        examinationRecordService.createOrUpdateExam(examRecord.copy(plannedDate = examRecord.plannedDate?.plusYears(1)), "101")

        assertThat(accountRepository.findByUid("101")?.badges?.first()?.level).isEqualTo(1)
    }
}
