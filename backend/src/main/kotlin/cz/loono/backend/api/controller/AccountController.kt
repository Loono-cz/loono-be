package cz.loono.backend.api.controller

import cz.loono.backend.api.Attributes
import cz.loono.backend.api.BasicUser
import cz.loono.backend.api.dto.AccountDto
import cz.loono.backend.api.dto.SettingsDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.dto.UserDto
import cz.loono.backend.api.dto.UserPatchDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.service.AccountService
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.Settings
import cz.loono.backend.db.model.UserAuxiliary
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.let3
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import javax.validation.Valid

@RestController
@RequestMapping("/account", produces = [MediaType.APPLICATION_JSON_VALUE])
class AccountController(
    private val accountService: AccountService,
    private val accountRepository: AccountRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getAccount(
        @RequestAttribute(name = Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser
    ): AccountDto {
        val account = accountRepository.findByUid(basicUser.uid)
        if (account == null) {
            logger.error(
                "Tried to load account with uid: ${basicUser.uid} but no such account exists. " +
                    "The account should have been created by the interceptor."
            )
            throw LoonoBackendException(HttpStatus.NOT_FOUND)
        }

        return assembleAccountDto(basicUser, account)
    }

    @DeleteMapping
    fun deleteAccount(
        @RequestAttribute(name = Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser
    ) = accountService.deleteAccount(basicUser.uid)

    @PostMapping("/user/update")
    fun updateUserAuxiliary(
        @RequestAttribute(name = Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser,

        @RequestBody
        @Valid
        patch: UserPatchDto
    ): AccountDto {
        val aux = UserAuxiliary(
            nickname = patch.nickname,
            preferredEmail = patch.preferredEmail,
            sex = patch.sex?.name,
            birthdate = let3(patch.birthdateYear, patch.birthdateMonth, 1, LocalDate::of),
            profileImageUrl = patch.profileImageUrl
        )
        val updatedAccount = accountService.updateUserAuxiliary(basicUser.uid, aux)

        return assembleAccountDto(basicUser, updatedAccount)
    }

    @PostMapping("/settings/update")
    fun updateSettings(
        @RequestAttribute(name = Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser,
        settings: SettingsDto,
    ): AccountDto {
        val domainSettings = Settings(
            leaderboardAnonymizationOptIn = settings.leaderboardAnonymizationOptIn,
            appointmentReminderEmailsOptIn = settings.appointmentReminderEmailsOptIn,
            newsletterOptIn = settings.newsletterOptIn
        )
        val updatedAccount = accountService.updateSettings(basicUser.uid, domainSettings)

        return assembleAccountDto(basicUser, updatedAccount)
    }

    private fun assembleAccountDto(basicUser: BasicUser, account: Account): AccountDto {
        val userDto = assembleUserDto(basicUser, account.userAuxiliary)
        val settingsDto = SettingsDto(
            leaderboardAnonymizationOptIn = account.settings.leaderboardAnonymizationOptIn,
            appointmentReminderEmailsOptIn = account.settings.appointmentReminderEmailsOptIn,
            newsletterOptIn = account.settings.newsletterOptIn
        )
        return AccountDto(user = userDto, settings = settingsDto, points = account.points)
    }

    private fun assembleUserDto(base: BasicUser, aux: UserAuxiliary): UserDto =
        UserDto(
            uid = base.uid,
            email = base.email,
            nickname = aux.nickname ?: base.name,
            sex = aux.sex?.let(SexDto::valueOf),
            birthdateMonth = aux.birthdate?.monthValue,
            birthdateYear = aux.birthdate?.year,
            preferredEmail = aux.preferredEmail,
            profileImageUrl = aux.profileImageUrl ?: base.photoUrl.toString()
        )
}
