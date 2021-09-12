package cz.loono.backend.api.service

import cz.loono.backend.data.model.Account
import cz.loono.backend.data.model.Settings
import cz.loono.backend.data.model.UserAuxiliary
import cz.loono.backend.data.repository.AccountRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.Exception

@Service
class AccountService @Autowired constructor(
    private val accountRepository: AccountRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(rollbackFor = [Exception::class])
    fun ensureAccountExists(uid: String) {
        if (!accountRepository.existsByUid(uid)) {
            accountRepository.save(Account(uid = uid))
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateSettings(uid: String, settings: Settings): Account {
        val account = accountRepository.findByIdOrNull(uid)
        if (account == null) {
            logger.error(
                "Tried to update Account Settings for uid: $uid but no such account exists. " +
                    "The account should have been created by the interceptor."
            )
            throw IllegalStateException("Tried to update Account Settings for uid: $uid but no such account exists.")
        }

        return accountRepository.save(account.copy(settings = settings))
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateUserAuxiliary(uid: String, aux: UserAuxiliary): Account {
        val account = accountRepository.findByIdOrNull(uid)
        if (account == null) {
            logger.error(
                "Tried to update User Auxiliary for uid: $uid but no such account exists. " +
                    "The account should have been created by the interceptor."
            )
            throw IllegalStateException("Tried to update User Auxiliary for uid: $uid but no such account exists.")
        }

        return accountRepository.save(account.copy(userAuxiliary = aux))
    }
}
