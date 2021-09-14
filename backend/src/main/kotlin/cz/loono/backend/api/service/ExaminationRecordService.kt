package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationTypeEnumDto
import cz.loono.backend.data.model.Account
import cz.loono.backend.data.model.ExaminationRecord
import cz.loono.backend.data.repository.AccountRepository
import cz.loono.backend.data.repository.ExaminationRecordRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ExaminationRecordService @Autowired constructor(
    private val accountRepository: AccountRepository,
    private val examinationRecordRepository: ExaminationRecordRepository,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Loads all the examination records for this account `uid`.
     *
     * If a record for an examination type is not found, a default record for that type is inserted.
     *
     * If the database contains records with types that no longer exist, they will be removed from the database.
     *
     * Since missing examination records are inserted and leftover records are purged,
     * this method always returns a record per each existing type.
     */
    @Transactional(rollbackFor = [Exception::class])
    fun getOrCreateRecords(uid: String): List<ExaminationRecord> {
        val account = accountRepository.findByUid(uid)
        if (account == null) {
            logger.error("Account does not exist, it should have been created by the account interceptor.")
            throw IllegalStateException(
                "Tried to load Examination Records for uid: $uid " +
                    "but no such account exists."
            )
        }

        return account.withAllRecords().examinationRecords
    }

    private fun Account.withAllRecords(): Account {
        val currentExamTypes = this.examinationRecords.map { record -> record.type }
        val desiredExamTypes = ExaminationTypeEnumDto.values().map { it.name }

        // Fast path = the current account already contains exactly the required examination records.
        if (currentExamTypes.size == desiredExamTypes.size &&
            currentExamTypes.subtract(desiredExamTypes).isEmpty()
        ) {
            return this
        }

        val newRecords = mutableListOf<ExaminationRecord>()
        newRecords += retainValidRecords(examinationRecords, desiredExamTypes)
        newRecords += createMissingRecords(this, desiredExamTypes)

        deleteObsoleteRecords(examinationRecords, desiredExamTypes)

        return accountRepository.save(copy(examinationRecords = newRecords))
    }

    private fun retainValidRecords(
        currentRecords: List<ExaminationRecord>,
        desiredRecordTypes: List<String>
    ): List<ExaminationRecord> {
        val retainedTypes = desiredRecordTypes intersect currentRecords.map { it.type }

        return currentRecords.filter { record ->
            record.type in retainedTypes
        }
    }

    private fun createMissingRecords(account: Account, desiredRecordTypes: List<String>): List<ExaminationRecord> {
        val missingTypes = desiredRecordTypes subtract account.examinationRecords.map { it.type }

        return missingTypes.map { type ->
            ExaminationRecord(type = type, account = account)
        }
    }

    private fun deleteObsoleteRecords(currentRecords: List<ExaminationRecord>, desiredRecordTypes: List<String>) {
        val obsoleteTypes = currentRecords.map { it.type } subtract desiredRecordTypes
        val removals = currentRecords.filter { record ->
            record.type in obsoleteTypes
        }
        examinationRecordRepository.deleteAll(removals)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun completeExamination(
        uid: String,
        type: String,
        completionDate: LocalDate?
    ): List<ExaminationRecord> {
        @Suppress("NAME_SHADOWING")
        val completionDate = completionDate?.withDayOfMonth(1) ?: LocalDate.now().withDayOfMonth(1)

        if (completionDate.isAfter(LocalDate.now().withDayOfMonth(1))) {
            logger.error("Completion date is in the future, this should have been caught by input validator!")
            throw IllegalArgumentException("Examination Completion must not be in the future.")
        }

        val account = accountRepository.findByUid(uid)
        if (account == null) {
            logger.error("Account does not exist, it should have been created by the account interceptor.")
            throw IllegalStateException("Tried to complete an examination for uid: $uid but no such account exists.")
        }
        val currentRecord = account
            .withAllRecords()
            .examinationRecords
            .first { it.type == type }

        var updatedRecord = currentRecord

        if (currentRecord.lastVisit == null || currentRecord.lastVisit.isBefore(completionDate)) {
            updatedRecord = currentRecord.copy(lastVisit = completionDate)
        }

        // TODO call Gamification Feature to award points if user is eligible

        accountRepository.save(account.withReplacedRecord(updatedRecord))
        return getOrCreateRecords(uid)
    }

    fun Account.withReplacedRecord(newRecord: ExaminationRecord): Account {
        val updatedRecords: List<ExaminationRecord> = examinationRecords
            .toMutableList()
            .apply {
                retainAll { it.type != newRecord.type }
                add(newRecord)
            }

        return copy(examinationRecords = updatedRecords)
    }
}
