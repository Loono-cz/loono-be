package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationRecordDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.ExaminationTypeEnumDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.Badge
import cz.loono.backend.db.model.ExaminationRecord
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExaminationRecordService(
    private val accountRepository: AccountRepository,
    private val examinationRecordRepository: ExaminationRecordRepository
) {

    companion object {
        private const val STARTING_LEVEL = 1
    }

    @Synchronized
    @Transactional(rollbackFor = [Exception::class])
    fun confirmExam(examUuid: String, accountUuid: String): ExaminationRecordDto =
        changeState(examUuid, accountUuid, ExaminationStatusDto.CONFIRMED)

    @Synchronized
    @Transactional(rollbackFor = [Exception::class])
    fun cancelExam(examUuid: String, accountUuid: String): ExaminationRecordDto =
        changeState(examUuid, accountUuid, ExaminationStatusDto.CANCELED)

    fun createOrUpdateExam(examinationRecordDto: ExaminationRecordDto, uuid: String): ExaminationRecordDto {
        val record = validateUpdateAttempt(examinationRecordDto)
        return examinationRecordRepository.save(
            ExaminationRecord(
                id = record.id,
                uuid = record.uuid,
                type = examinationRecordDto.type,
                plannedDate = examinationRecordDto.date,
                account = findAccount(uuid),
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

    private fun findAccount(uuid: String): Account =
        accountRepository.findByUid(uuid) ?: throw LoonoBackendException(
            HttpStatus.NOT_FOUND, "Account not found"
        )

    private fun changeState(examUuid: String, accountUuid: String, state: ExaminationStatusDto): ExaminationRecordDto {
        val account = findAccount(accountUuid)

        val exam = examinationRecordRepository.findByUuidAndAccount(examUuid, account)
        exam.status = state
        val updatedAccount = updateWithBadgeAndPoints(exam.type, account)
        updatedAccount?.let {
            accountRepository.save(updatedAccount)
        }
        return examinationRecordRepository.save(exam).toExaminationRecordDto()
    }

    private fun updateWithBadgeAndPoints(examType: ExaminationTypeEnumDto, account: Account): Account? =
        account.userAuxiliary.sex?.let { sexString ->
            val badgeToPoints = BadgesPointsProvider.getBadgesAndPoints(examType, SexDto.valueOf(sexString))
            val badgeType = badgeToPoints.first.toString()
            val points = badgeToPoints.second

            // Increment badge level by 1 if badge already exists, add this badge as new one otherwise
            val badgeToIncrement = account.badges.find { it.type.equals(badgeType, ignoreCase = true) }
            val badgesToCopy = badgeToIncrement?.let { toIncrement ->
                // Due to immutability first removing badge, then coping old badge with incremented level
                val badgesWithoutToIncrement = account.badges
                    .minus(toIncrement)
                    .toMutableSet()
                badgesWithoutToIncrement.apply { add(toIncrement.copy(level = toIncrement.level.inc())) }
            } ?: account.badges.plus(Badge(badgeType, account.id, STARTING_LEVEL, account))

            account.copy(badges = badgesToCopy, points = account.points + points)
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
