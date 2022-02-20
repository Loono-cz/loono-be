package cz.loono.backend.api.service

import cz.loono.backend.api.dto.AccountDto
import cz.loono.backend.api.dto.AccountOnboardingDto
import cz.loono.backend.api.dto.AccountUpdateDto
import cz.loono.backend.api.dto.ExaminationRecordDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.createAccount
import cz.loono.backend.db.repository.AccountRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class AccountServiceTest(
    private val repo: AccountRepository,
    private val examinationRecordService: ExaminationRecordService
) {

    private val firebaseAuthService: FirebaseAuthService = mock()

    @Test
    fun `onboard account`() {
        val uid = UUID.randomUUID().toString()
        val account = createAccount(uid = uid)
        val service = AccountService(repo, firebaseAuthService, examinationRecordService)

        val accountDto = service.onboardAccount(
            uid,
            account = AccountOnboardingDto(
                nickname = account.nickname,
                sex = SexDto.valueOf(account.sex),
                preferredEmail = account.preferredEmail,
                birthdate = account.birthdate,
                examinations = emptyList()
            )
        )

        assert(
            accountDto == AccountDto(
                uid = account.uid,
                nickname = account.nickname,
                sex = SexDto.valueOf(account.sex),
                prefferedEmail = account.preferredEmail,
                birthdate = account.birthdate,
                points = 0,
                badges = emptyList(),
                newsletterOptIn = false,
                appointmentReminderEmailsOptIn = true,
                leaderboardAnonymizationOptIn = true,
                profileImageUrl = null
            )
        )
    }

    @Test
    fun `onboard existed account`() {
        val uid = UUID.randomUUID().toString()
        val account = createAccount(uid = uid)
        repo.save(account)
        val service = AccountService(repo, firebaseAuthService, examinationRecordService)

        assertThrows<LoonoBackendException> {
            service.onboardAccount(
                uid,
                account = AccountOnboardingDto(
                    nickname = account.nickname,
                    sex = SexDto.valueOf(account.sex),
                    preferredEmail = account.preferredEmail,
                    birthdate = account.birthdate,
                    examinations = emptyList()
                )
            )
        }
    }

    @Test
    fun `onboard account with different types of exams`() {
        val uid = UUID.randomUUID().toString()
        val account = createAccount(uid = uid, sex = SexDto.FEMALE.name)
        val service = AccountService(repo, firebaseAuthService, examinationRecordService)

        val accountDto = service.onboardAccount(
            uid,
            account = AccountOnboardingDto(
                nickname = account.nickname,
                sex = SexDto.valueOf(account.sex),
                preferredEmail = account.preferredEmail,
                birthdate = account.birthdate,
                examinations = listOf(
                    ExaminationRecordDto(
                        date = LocalDateTime.now().minusYears(1),
                        type = ExaminationTypeDto.GENERAL_PRACTITIONER,
                        status = ExaminationStatusDto.CONFIRMED,
                        firstExam = true
                    ),
                    ExaminationRecordDto(
                        date = LocalDateTime.now(),
                        type = ExaminationTypeDto.DENTIST,
                        status = ExaminationStatusDto.UNKNOWN,
                        firstExam = true
                    ),
                    ExaminationRecordDto(
                        type = ExaminationTypeDto.GYNECOLOGIST,
                        status = ExaminationStatusDto.UNKNOWN,
                        firstExam = true
                    ),
                    ExaminationRecordDto(
                        date = LocalDateTime.now().minusYears(1),
                        type = ExaminationTypeDto.COLONOSCOPY,
                        status = ExaminationStatusDto.CONFIRMED,
                        firstExam = true
                    )
                )
            )
        )

        val exams = repo.findByUid(account.uid)!!.examinationRecords
        assert(
            accountDto == AccountDto(
                uid = account.uid,
                nickname = account.nickname,
                sex = SexDto.valueOf(account.sex),
                prefferedEmail = account.preferredEmail,
                birthdate = account.birthdate,
                points = 0,
                badges = emptyList(),
                newsletterOptIn = false,
                appointmentReminderEmailsOptIn = true,
                leaderboardAnonymizationOptIn = true,
                profileImageUrl = null
            )
        )
        assert(exams.size == 3)
        assert(exams.find { it.type == ExaminationTypeDto.GENERAL_PRACTITIONER } != null)
        assert(exams.find { it.type == ExaminationTypeDto.GYNECOLOGIST } != null)
        assert(exams.find { it.type == ExaminationTypeDto.DENTIST } != null)
    }

    @Test
    fun `update account without values`() {
        val uid = UUID.randomUUID().toString()
        val account = createAccount(uid = uid)
        val service = AccountService(repo, firebaseAuthService, examinationRecordService)
        val storedAccount = repo.save(account)

        val accountDto = service.updateAccount(storedAccount.uid, AccountUpdateDto(storedAccount.uid))

        assert(
            accountDto == AccountDto(
                uid = storedAccount.uid,
                nickname = account.nickname,
                sex = SexDto.valueOf(account.sex),
                prefferedEmail = account.preferredEmail,
                birthdate = account.birthdate,
                points = 0,
                badges = emptyList(),
                newsletterOptIn = true,
                appointmentReminderEmailsOptIn = false,
                leaderboardAnonymizationOptIn = false,
                profileImageUrl = null
            )
        )
    }

    @Test
    fun `update account single change`() {
        val uid = UUID.randomUUID().toString()
        val account = createAccount(uid = uid)
        val service = AccountService(repo, firebaseAuthService, examinationRecordService)
        val storedAccount = repo.save(account)

        val accountDto =
            service.updateAccount(storedAccount.uid, AccountUpdateDto(storedAccount.uid, nickname = "Boss"))

        assert(
            accountDto == AccountDto(
                uid = storedAccount.uid,
                nickname = "Boss",
                sex = SexDto.valueOf(account.sex),
                prefferedEmail = account.preferredEmail,
                birthdate = account.birthdate,
                points = 0,
                badges = emptyList(),
                newsletterOptIn = true,
                appointmentReminderEmailsOptIn = false,
                leaderboardAnonymizationOptIn = false,
                profileImageUrl = null
            )
        )
    }

    @Test
    fun `update account settings`() {
        val uid = UUID.randomUUID().toString()
        val account = createAccount(uid = uid)
        val service = AccountService(repo, firebaseAuthService, examinationRecordService)
        repo.save(account)

        val accountDto = service.updateAccount(
            uid,
            AccountUpdateDto(
                uid,
                leaderboardAnonymizationOptIn = false,
                appointmentReminderEmailsOptIn = false,
                newsletterOptIn = true
            )
        )

        assert(
            accountDto == AccountDto(
                uid = account.uid,
                nickname = account.nickname,
                sex = SexDto.valueOf(account.sex),
                prefferedEmail = account.preferredEmail,
                birthdate = account.birthdate,
                points = 0,
                badges = emptyList(),
                newsletterOptIn = true,
                appointmentReminderEmailsOptIn = false,
                leaderboardAnonymizationOptIn = false,
                profileImageUrl = null
            )
        )
    }

    @Test
    fun `update account all values`() {
        val uid = UUID.randomUUID().toString()
        val account = createAccount(uid = uid)
        val service = AccountService(repo, firebaseAuthService, examinationRecordService)
        repo.save(account)

        val accountDto = service.updateAccount(
            uid,
            AccountUpdateDto(
                uid,
                nickname = "Boss",
                prefferedEmail = "email",
                profileImageUrl = "image",
                leaderboardAnonymizationOptIn = false,
                appointmentReminderEmailsOptIn = false,
                newsletterOptIn = true
            )
        )

        assert(
            accountDto == AccountDto(
                uid = account.uid,
                nickname = "Boss",
                sex = SexDto.valueOf(account.sex),
                prefferedEmail = "email",
                birthdate = account.birthdate,
                points = 0,
                badges = emptyList(),
                newsletterOptIn = true,
                appointmentReminderEmailsOptIn = false,
                leaderboardAnonymizationOptIn = false,
                profileImageUrl = "image"
            )
        )
    }

    @Test
    fun `update account remove image`() {
        val account = createAccount(uid = "102", profileImageUrl = "image")
        val service = AccountService(repo, firebaseAuthService, examinationRecordService)
        val storedAccount = repo.save(account)

        val accountDto = service.updateAccount(
            storedAccount.uid,
            AccountUpdateDto(
                storedAccount.uid,
                profileImageUrl = null
            )
        )

        assert(
            accountDto == AccountDto(
                uid = storedAccount.uid,
                nickname = account.nickname,
                sex = SexDto.valueOf(account.sex),
                prefferedEmail = account.preferredEmail,
                birthdate = account.birthdate,
                points = 0,
                badges = emptyList(),
                newsletterOptIn = true,
                appointmentReminderEmailsOptIn = false,
                leaderboardAnonymizationOptIn = false,
                profileImageUrl = null
            )
        )
    }

    @Test
    fun `delete existing account`() {
        // Arrange
        val service = AccountService(repo, firebaseAuthService, examinationRecordService)
        val existingAccount = createAccount("toDelete")
        repo.save(existingAccount)

        // Act
        service.deleteAccount(uid = existingAccount.uid)

        // Assert
        assert(repo.findByUid(existingAccount.uid) == null)
    }
}
