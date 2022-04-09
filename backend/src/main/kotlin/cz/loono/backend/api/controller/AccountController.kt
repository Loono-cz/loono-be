package cz.loono.backend.api.controller

import cz.loono.backend.api.Attributes
import cz.loono.backend.api.BasicUser
import cz.loono.backend.api.dto.AccountDto
import cz.loono.backend.api.dto.AccountOnboardingDto
import cz.loono.backend.api.dto.AccountUpdateDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.service.AccountService
import cz.loono.backend.db.repository.AccountRepository
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

@RestController
@RequestMapping("/account", produces = [MediaType.APPLICATION_JSON_VALUE])
class AccountController(
    private val accountService: AccountService,
    private val accountRepository: AccountRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/onboard")
    fun onboardAccount(
        @RequestAttribute(name = Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser,

        @RequestBody
        account: AccountOnboardingDto
    ): AccountDto = accountService.onboardAccount(basicUser.uid, account)

    @GetMapping
    fun getAccount(
        @RequestAttribute(name = Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser
    ): AccountDto {
        val account = accountRepository.findByUid(basicUser.uid)
        if (account == null) {
            logger.error(
                "Tried to load account with uid: ${basicUser.uid} but no such account exists."
            )
            throw LoonoBackendException(HttpStatus.NOT_FOUND)
        }
        return accountService.transformToAccountDTO(account)
    }

    @PostMapping
    fun updateAccount(
        @RequestAttribute(name = Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser,

        @RequestBody
        accountUpdate: AccountUpdateDto
    ): AccountDto {
        val account = accountRepository.findByUid(basicUser.uid)
        if (account == null) {
            logger.error(
                "Tried to load account with uid: ${basicUser.uid} but no such account exists."
            )
            throw LoonoBackendException(HttpStatus.NOT_FOUND)
        }
        return accountService.updateAccount(basicUser.uid, accountUpdate)
    }

    @DeleteMapping
    fun deleteAccount(
        @RequestAttribute(name = Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser
    ) = accountService.deleteAccount(basicUser.uid)

    @GetMapping("/login")
    fun login(
        @RequestAttribute(name = Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser
    ) = accountService.login(basicUser.uid)

    @GetMapping("/logout")
    fun logout(
        @RequestAttribute(name = Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser
    ) = accountService.logout(basicUser.uid)
}
