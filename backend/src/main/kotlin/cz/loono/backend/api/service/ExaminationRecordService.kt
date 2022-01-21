package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationRecordDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.ExaminationRecord
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExaminationRecordService @Autowired constructor(
    private val accountRepository: AccountRepository,
    private val examinationRecordRepository: ExaminationRecordRepository,
) {

    @Synchronized
    @Transactional(rollbackFor = [Exception::class])
    fun confirmExam(examUuid: String, accoutUid: String): ExaminationRecordDto {
        return changeState(examUuid, accoutUid, ExaminationStatusDto.CONFIRMED)
    }

    @Synchronized
    @Transactional(rollbackFor = [Exception::class])
    fun cancelExam(examUuid: String, accountUid: String): ExaminationRecordDto {
        return changeState(examUuid, accountUid, ExaminationStatusDto.CANCELED)
    }

    fun createOrUpdateExam(examinationRecordDto: ExaminationRecordDto, uid: String): ExaminationRecordDto {
        val record = validateUpdateAttempt(examinationRecordDto)
        return examinationRecordRepository.save(
            ExaminationRecord(
                id = record.id,
                uuid = record.uuid,
                type = examinationRecordDto.type,
                plannedDate = examinationRecordDto.date,
                account = findAccount(uid),
                firstExam = examinationRecordDto.firstExam ?: true,
                status = examinationRecordDto.status ?: ExaminationStatusDto.NEW
            )
        ).toExaminationRecordDto()
    }

    private fun validateUpdateAttempt(examinationRecordDto: ExaminationRecordDto): ExaminationRecord =
        if (examinationRecordDto.uuid != null) {
            examinationRecordRepository.findByUuid(examinationRecordDto.uuid)
                ?: throw LoonoBackendException(
                    HttpStatus.NOT_FOUND,
                    "404",
                    "The given examination identifier not found."
                )
        } else {
            ExaminationRecord()
        }

    private fun findAccount(uid: String): Account =
        accountRepository.findByUid(uid) ?: throw LoonoBackendException(
            HttpStatus.NOT_FOUND, "Account not found"
        )

    private fun changeState(examUuid: String, accountUid: String, state: ExaminationStatusDto): ExaminationRecordDto {
        val account = findAccount(accountUid)

        val exam = examinationRecordRepository.findByUuidAndAccount(examUuid, account)
        exam.status = state

        return examinationRecordRepository.save(exam).toExaminationRecordDto()
    }

    fun ExaminationRecord.toExaminationRecordDto(): ExaminationRecordDto =
        ExaminationRecordDto(
            uuid = uuid,
            type = type,
            date = plannedDate,
            firstExam = firstExam,
            status = status
        )
}
