package cz.loono.backend.api.service

import cz.loono.backend.api.dto.AccountDto
import cz.loono.backend.api.dto.AccountOnboardingDto
import cz.loono.backend.api.dto.AccountUpdateDto
import cz.loono.backend.api.dto.BadgeDto
import cz.loono.backend.api.dto.BadgeTypeDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.repository.AccountRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.Period

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val firebaseAuthService: FirebaseAuthService,
    private val examinationRecordService: ExaminationRecordService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(rollbackFor = [Exception::class])
    fun onboardAccount(uuid: String, account: AccountOnboardingDto): AccountDto {
        if (accountRepository.existsByUid(uuid)) {
            throw LoonoBackendException(HttpStatus.BAD_REQUEST, "400", "Account already exists.")
        }
        validateAgeRequirement(account)
        val storedAccount = accountRepository.save(
            Account(
                uid = uuid,
                nickname = account.nickname,
                sex = account.sex.name,
                birthdate = account.birthdate,
                preferredEmail = account.preferredEmail
            )
        )
        val acceptedExams = account.examinations.filter {
            it.type == ExaminationTypeDto.GENERAL_PRACTITIONER ||
                it.type == ExaminationTypeDto.DENTIST ||
                (it.type == ExaminationTypeDto.GYNECOLOGIST && account.sex == SexDto.FEMALE)
        }
        acceptedExams.forEach {
            examinationRecordService.createOrUpdateExam(it, storedAccount.uid)
        }
        return transformToAccountDTO(
            accountRepository.findByUid(storedAccount.uid) ?: throw LoonoBackendException(
                HttpStatus.NOT_FOUND,
                "404",
                "Account not found."
            )
        )
    }

    private fun validateAgeRequirement(account: AccountOnboardingDto) {
        val today = LocalDate.now()
        val period = Period.between(account.birthdate, today)
        if (period.years < 18) {
            throw LoonoBackendException(
                HttpStatus.BAD_REQUEST,
                "400",
                "The user is not 18 and above years old."
            )
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun deleteAccount(uid: String) {
        val account = accountRepository.findByUid(uid) ?: throw LoonoBackendException(
            status = HttpStatus.NOT_FOUND,
            errorCode = "404",
            errorMessage = "The account not found."
        )
        accountRepository.delete(account)
        firebaseAuthService.deleteAccount(uid)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateAccount(uuid: String, accountUpdate: AccountUpdateDto): AccountDto {
        val account = accountRepository.findByUid(uuid)
        if (account == null) {
            logger.error(
                "Tried to update Account Settings for uid: $uuid but no such account exists."
            )
            throw IllegalStateException("Tried to update Account Settings for uid: $uuid but no such account exists.")
        }
        var updatedAccount: Account = account
        accountUpdate.nickname?.let {
            updatedAccount = accountRepository.save(updatedAccount.copy(nickname = accountUpdate.nickname))
        }
        accountUpdate.prefferedEmail?.let {
            updatedAccount = accountRepository.save(updatedAccount.copy(preferredEmail = accountUpdate.prefferedEmail))
        }
        accountUpdate.leaderboardAnonymizationOptIn?.let {
            updatedAccount =
                accountRepository.save(updatedAccount.copy(leaderboardAnonymizationOptIn = accountUpdate.leaderboardAnonymizationOptIn))
        }
        accountUpdate.appointmentReminderEmailsOptIn?.let {
            updatedAccount =
                accountRepository.save(updatedAccount.copy(appointmentReminderEmailsOptIn = accountUpdate.appointmentReminderEmailsOptIn))
        }
        accountUpdate.newsletterOptIn?.let {
            updatedAccount =
                accountRepository.save(updatedAccount.copy(newsletterOptIn = accountUpdate.newsletterOptIn))
        }
        if (updatedAccount.profileImageUrl != accountUpdate.profileImageUrl) {
            updatedAccount =
                accountRepository.save(updatedAccount.copy(profileImageUrl = accountUpdate.profileImageUrl))
        }
        return transformToAccountDTO(updatedAccount)
    }

    fun transformToAccountDTO(account: Account): AccountDto =
        AccountDto(
            uid = account.uid,
            nickname = account.nickname,
            sex = SexDto.valueOf(account.sex),
            birthdate = account.birthdate,
            profileImageUrl = account.profileImageUrl,
            points = account.points,
            prefferedEmail = account.preferredEmail,
            appointmentReminderEmailsOptIn = account.appointmentReminderEmailsOptIn,
            leaderboardAnonymizationOptIn = account.leaderboardAnonymizationOptIn,
            newsletterOptIn = account.newsletterOptIn,
            badges = account.badges
                .map { BadgeDto(type = BadgeTypeDto.valueOf(it.type), level = it.level) }
                .sortedBy(BadgeDto::type)
        )
}
