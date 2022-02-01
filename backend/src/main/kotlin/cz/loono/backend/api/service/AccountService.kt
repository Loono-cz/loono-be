package cz.loono.backend.api.service

import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.Settings
import cz.loono.backend.db.model.UserAuxiliary
import cz.loono.backend.db.repository.AccountRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val firebaseAuthService: FirebaseAuthService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(rollbackFor = [Exception::class])
    fun ensureAccountExists(uid: String) {
        if (!accountRepository.existsByUid(uid)) {
            accountRepository.save(Account(uid = uid))
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun deleteAccount(uid: String) {
        val deletedCount = accountRepository.deleteAccountByUid(uid)
        if (deletedCount == 0L) {
            throw LoonoBackendException(
                status = HttpStatus.NOT_FOUND,
                errorCode = "404",
                errorMessage = "The account not found."
            )
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateSettings(uid: String, settings: Settings): Account {
        val account = accountRepository.findByUid(uid)
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
        val account = accountRepository.findByUid(uid)
        if (account == null) {
            logger.error(
                "Tried to update User Auxiliary for uid: $uid but no such account exists. " +
                    "The account should have been created by the interceptor."
            )
            throw IllegalStateException("Tried to update User Auxiliary for uid: $uid but no such account exists.")
        }

        firebaseAuthService.updateUser(uid, aux)
        return accountRepository.save(account.copy(userAuxiliary = aux))
    }
}
