package cz.loono.backend.api.service

import cz.loono.backend.api.dto.AccountDto
import cz.loono.backend.api.dto.AccountOnboardingDto
import cz.loono.backend.api.dto.AccountUpdateDto
import cz.loono.backend.api.dto.BadgeDto
import cz.loono.backend.api.dto.BadgeTypeDto
import cz.loono.backend.api.dto.ExaminationRecordDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.createAccount
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(properties = ["spring.profiles.active=test"])
class AccountServiceTest(
    private val repo: AccountRepository,
    private val examinationRecordService: ExaminationRecordService,
    private val examinationRecordRepository: ExaminationRecordRepository,
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository,
    @Value("\${task.badge-downgrade.page-size}")
    private val pageSize: Int,
) {

    private val firebaseAuthService: FirebaseAuthService = mock()

    @AfterEach
    fun setUp() {
        repo.deleteAll()
        examinationRecordRepository.deleteAll()
        selfExaminationRecordRepository.deleteAll()
    }

    @Test
    fun `onboard account`() {
        val uid = UUID.randomUUID().toString()
        val account = createAccount(uid = uid)
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize,
        )

        val accountDto = service.onboardAccount(
            uid,
            account = AccountOnboardingDto(
                nickname = account.nickname,
                sex = account.getSexAsEnum(),
                preferredEmail = account.preferredEmail,
                birthdate = account.birthdate,
                examinations = emptyList()
            )
        )

        assert(
            accountDto == AccountDto(
                uid = account.uid,
                nickname = account.nickname,
                sex = account.getSexAsEnum(),
                preferredEmail = account.preferredEmail,
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
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize
        )

        assertThrows<LoonoBackendException> {
            service.onboardAccount(
                uid,
                account = AccountOnboardingDto(
                    nickname = account.nickname,
                    sex = account.getSexAsEnum(),
                    preferredEmail = account.preferredEmail,
                    birthdate = account.birthdate,
                    examinations = emptyList()
                )
            )
        }
    }

    @Test
    fun `onboard account with different types of exams and should add badges and points`() {
        val uid = UUID.randomUUID().toString()
        val account = createAccount(uid = uid, sex = SexDto.FEMALE.name)
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize
        )

        val accountDto = service.onboardAccount(
            uid,
            account = AccountOnboardingDto(
                nickname = account.nickname,
                sex = account.getSexAsEnum(),
                preferredEmail = account.preferredEmail,
                birthdate = account.birthdate,
                examinations = listOf(
                    ExaminationRecordDto(
                        plannedDate = LocalDateTime.now().minusYears(1),
                        type = ExaminationTypeDto.GENERAL_PRACTITIONER,
                        status = ExaminationStatusDto.CONFIRMED,
                        firstExam = true
                    ),
                    ExaminationRecordDto(
                        plannedDate = LocalDateTime.now(),
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
                        plannedDate = LocalDateTime.now().minusYears(1),
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
                sex = account.getSexAsEnum(),
                preferredEmail = account.preferredEmail,
                birthdate = account.birthdate,
                points = 500,
                badges = listOf(BadgeDto(BadgeTypeDto.COAT, 1), BadgeDto(BadgeTypeDto.HEADBAND, 1)),
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
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize
        )
        val storedAccount = repo.save(account)

        val accountDto = service.updateAccount(storedAccount.uid, AccountUpdateDto())

        assert(
            accountDto == AccountDto(
                uid = storedAccount.uid,
                nickname = account.nickname,
                sex = account.getSexAsEnum(),
                preferredEmail = account.preferredEmail,
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
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize
        )
        val storedAccount = repo.save(account)

        val accountDto =
            service.updateAccount(storedAccount.uid, AccountUpdateDto(nickname = "Boss"))

        assert(
            accountDto == AccountDto(
                uid = storedAccount.uid,
                nickname = "Boss",
                sex = account.getSexAsEnum(),
                preferredEmail = account.preferredEmail,
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
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize
        )
        repo.save(account)

        val accountDto = service.updateAccount(
            uid,
            AccountUpdateDto(
                leaderboardAnonymizationOptIn = false,
                appointmentReminderEmailsOptIn = false,
                newsletterOptIn = true
            )
        )

        assert(
            accountDto == AccountDto(
                uid = account.uid,
                nickname = account.nickname,
                sex = account.getSexAsEnum(),
                preferredEmail = account.preferredEmail,
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
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize
        )
        repo.save(account)

        val accountDto = service.updateAccount(
            uid,
            AccountUpdateDto(
                nickname = "Boss",
                preferredEmail = "email",
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
                sex = account.getSexAsEnum(),
                preferredEmail = "email",
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
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize
        )
        val storedAccount = repo.save(account)

        val accountDto = service.updateAccount(
            storedAccount.uid,
            AccountUpdateDto(profileImageUrl = null)
        )

        assert(
            accountDto == AccountDto(
                uid = storedAccount.uid,
                nickname = account.nickname,
                sex = account.getSexAsEnum(),
                preferredEmail = account.preferredEmail,
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
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize
        )
        val existingAccount = createAccount("toDelete")
        repo.save(existingAccount)

        // Act
        service.deleteAccount(uid = existingAccount.uid)

        // Assert
        assert(repo.findByUid(existingAccount.uid) == null)
    }

    @Test
    fun `login account`() {
        // Arrange
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize
        )
        val existingAccount = createAccount("uid")
        repo.save(existingAccount)

        // Act
        service.login(uid = existingAccount.uid)

        // Assert
        assert(repo.findByUid(existingAccount.uid)!!.active)
    }

    @Test
    fun `logout account`() {
        // Arrange
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize
        )
        val existingAccount = createAccount("uid")
        repo.save(existingAccount)

        // Act
        service.logout(uid = existingAccount.uid)

        // Assert
        assert(!repo.findByUid(existingAccount.uid)!!.active)
    }

    @Test
    fun `Should add badges and points for exams which are within expected interval`() {
        val uid = UUID.randomUUID().toString()
        val account = createAccount(uid = uid, sex = SexDto.FEMALE.name)
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize
        )

        val actual = service.onboardAccount(
            uid,
            account = AccountOnboardingDto(
                nickname = account.nickname,
                sex = account.getSexAsEnum(),
                preferredEmail = account.preferredEmail,
                birthdate = account.birthdate,
                examinations = listOf(
                    ExaminationRecordDto(
                        plannedDate = LocalDateTime.now().minusYears(2).plusDays(1),
                        type = ExaminationTypeDto.GENERAL_PRACTITIONER,
                        status = ExaminationStatusDto.CONFIRMED,
                        firstExam = true
                    ),
                    ExaminationRecordDto(
                        plannedDate = LocalDateTime.now(),
                        type = ExaminationTypeDto.DENTIST,
                        status = ExaminationStatusDto.UNKNOWN,
                        firstExam = true
                    ),
                )
            )
        )

        val expected = AccountDto(
            uid = account.uid,
            nickname = account.nickname,
            sex = account.getSexAsEnum(),
            preferredEmail = account.preferredEmail,
            birthdate = account.birthdate,
            points = 500,
            badges = listOf(BadgeDto(BadgeTypeDto.COAT, 1), BadgeDto(BadgeTypeDto.HEADBAND, 1)),
            newsletterOptIn = false,
            appointmentReminderEmailsOptIn = true,
            leaderboardAnonymizationOptIn = true,
            profileImageUrl = null
        )
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `The age of the user has to be 18 and more`() {
        val uid = UUID.randomUUID().toString()
        val account = createAccount(uid = uid, birthday = LocalDate.now())
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize
        )

        assertThrows<LoonoBackendException> {
            service.onboardAccount(
                uid,
                account = AccountOnboardingDto(
                    nickname = account.nickname,
                    sex = account.getSexAsEnum(),
                    preferredEmail = account.preferredEmail,
                    birthdate = account.birthdate,
                    examinations = emptyList()
                )
            )
        }
    }

    @Test
    fun `Should add badges and points respecting statuses`() {
        val uid = UUID.randomUUID().toString()
        val account = createAccount(uid = uid, sex = SexDto.FEMALE.name)
        val service = AccountService(
            repo,
            examinationRecordRepository,
            selfExaminationRecordRepository,
            firebaseAuthService,
            examinationRecordService,
            pageSize
        )

        val actual = service.onboardAccount(
            uid,
            account = AccountOnboardingDto(
                nickname = account.nickname,
                sex = account.getSexAsEnum(),
                preferredEmail = account.preferredEmail,
                birthdate = account.birthdate,
                examinations = listOf(
                    ExaminationRecordDto(
                        plannedDate = LocalDateTime.now(),
                        type = ExaminationTypeDto.GENERAL_PRACTITIONER,
                        status = ExaminationStatusDto.CONFIRMED,
                        firstExam = true
                    ),
                    ExaminationRecordDto(
                        plannedDate = LocalDateTime.now(),
                        type = ExaminationTypeDto.DENTIST,
                        status = ExaminationStatusDto.NEW,
                        firstExam = true
                    ),
                )
            )
        )

        val expected = AccountDto(
            uid = account.uid,
            nickname = account.nickname,
            sex = account.getSexAsEnum(),
            preferredEmail = account.preferredEmail,
            birthdate = account.birthdate,
            points = 200,
            badges = listOf(BadgeDto(BadgeTypeDto.COAT, 1)),
            newsletterOptIn = false,
            appointmentReminderEmailsOptIn = true,
            leaderboardAnonymizationOptIn = true,
            profileImageUrl = null
        )
        assertThat(actual).isEqualTo(expected)
    }
}
