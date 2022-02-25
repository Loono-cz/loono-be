package cz.loono.backend.api.service

import cz.loono.backend.api.dto.BadgeTypeDto
import cz.loono.backend.api.dto.ExaminationRecordDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationCompletionInformationDto
import cz.loono.backend.api.dto.SelfExaminationFindingResponseDto
import cz.loono.backend.api.dto.SelfExaminationResultDto
import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationTypeDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.Badge
import cz.loono.backend.db.model.ExaminationRecord
import cz.loono.backend.db.model.SelfExaminationRecord
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import cz.loono.backend.extensions.toLocalDateTime
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ExaminationRecordService(
    private val accountRepository: AccountRepository,
    private val examinationRecordRepository: ExaminationRecordRepository,
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository,
    private val preventionService: PreventionService,
    private val clock: Clock,
) {

    companion object {
        private const val STARTING_LEVEL = 1
        private const val SELF_EXAM_LEVEL_COUNT = 3
        private const val SELF_FINDING_CHECK_INTERVAL = 56L // no. days waiting for finding verification
        private const val INVALID_RESULT_MSG = "Invalid result of self-examination."
    }

    @Synchronized
    @Transactional(rollbackFor = [Exception::class])
    fun confirmExam(examUuid: String, accountUuid: String): ExaminationRecordDto =
        changeState(examUuid, accountUuid, ExaminationStatusDto.CONFIRMED)

    fun confirmSelfExam(
        type: SelfExaminationTypeDto,
        result: SelfExaminationResultDto,
        accountUuid: String
    ): SelfExaminationCompletionInformationDto {
        val account = prerequisitesValidation(accountUuid, type)
        var count = 1
        val selfExams = selfExaminationRecordRepository.findAllByAccountAndTypeOrderByDueDateDesc(account, type)
        if (selfExams.isEmpty()) {
            when (result) {
                SelfExaminationResultDto.OK -> {
                    val firstRecord = selfExaminationRecordRepository.save(
                        SelfExaminationRecord(
                            type = type,
                            dueDate = LocalDate.now(),
                            account = account,
                            result = result,
                            status = SelfExaminationStatusDto.COMPLETED
                        )
                    )
                    saveNewSelfExam(firstRecord)
                }
                SelfExaminationResultDto.FINDING -> {
                    val today = LocalDate.now()
                    selfExaminationRecordRepository.save(
                        SelfExaminationRecord(
                            type = type,
                            dueDate = today,
                            account = account,
                            result = result,
                            status = SelfExaminationStatusDto.WAITING_FOR_CHECKUP,
                            waitingTo = today.plusDays(SELF_FINDING_CHECK_INTERVAL)
                        )
                    )
                }
                else -> {
                    throw LoonoBackendException(
                        HttpStatus.BAD_REQUEST,
                        "400",
                        INVALID_RESULT_MSG
                    )
                }
            }
        } else {
            val plannedExam = selfExams.first { it.status == SelfExaminationStatusDto.PLANNED }
            validateSelfExamConfirmation(plannedExam.dueDate)
            when (result) {
                SelfExaminationResultDto.OK -> {
                    completeSelfExamAsOK(plannedExam)
                    count--
                }
                SelfExaminationResultDto.FINDING -> {
                    selfExaminationRecordRepository.save(
                        plannedExam.copy(
                            result = result,
                            status = SelfExaminationStatusDto.WAITING_FOR_CHECKUP,
                            waitingTo = plannedExam.dueDate!!.plusDays(SELF_FINDING_CHECK_INTERVAL)
                        )
                    )
                }
                else -> {
                    throw LoonoBackendException(
                        HttpStatus.BAD_REQUEST,
                        "400",
                        INVALID_RESULT_MSG
                    )
                }
            }
        }
        val reward = BadgesPointsProvider.getBadgesAndPoints(type, SexDto.valueOf(account.sex))
            ?: throw LoonoBackendException(HttpStatus.BAD_REQUEST)
        selfExams.forEach exams@{
            when (it.status) {
                SelfExaminationStatusDto.COMPLETED -> count++
                SelfExaminationStatusDto.MISSED -> return@exams
                else -> {
                    // Do nothing
                }
            }
        }
        val updatedAccount = if (count == STARTING_LEVEL || count % SELF_EXAM_LEVEL_COUNT == 0) {
            updateWithBadgeAndPoints(reward, account)
        } else {
            account.copy(points = account.points + reward.second)
        }
        accountRepository.save(updatedAccount)
        val badgeLevel = updatedAccount.badges.find { it.type == BadgeTypeDto.SHIELD.toString() }?.level
            ?: throw LoonoBackendException(HttpStatus.BAD_REQUEST)
        return SelfExaminationCompletionInformationDto(
            points = reward.second,
            allPoints = updatedAccount.points,
            badgeType = reward.first,
            badgeLevel = badgeLevel,
            streak = count
        )
    }

    private fun completeSelfExamAsOK(exam: SelfExaminationRecord) {
        selfExaminationRecordRepository.save(
            exam.copy(
                result = SelfExaminationResultDto.OK,
                status = SelfExaminationStatusDto.COMPLETED
            )
        )
        saveNewSelfExam(exam)
    }

    private fun prerequisitesValidation(accountUuid: String, type: SelfExaminationTypeDto): Account {
        val account = accountRepository.findByUid(accountUuid) ?: throw LoonoBackendException(
            HttpStatus.NOT_FOUND,
            "404",
            "The account not found."
        )
        if (!preventionService.validateSexPrerequisites(type, account.sex)) {
            throw LoonoBackendException(
                HttpStatus.BAD_REQUEST,
                "400",
                "This type of examination cannot applied for the account."
            )
        }
        return account
    }

    private fun validateSelfExamConfirmation(dueDate: LocalDate?) {
        if (dueDate == null) {
            throw LoonoBackendException(HttpStatus.BAD_REQUEST)
        }
        val now = LocalDate.now()
        val threeDaysBefore = dueDate.minusDays(3)
        val threeDaysAfter = dueDate.plusDays(3)
        if (!(now.isAfter(threeDaysBefore) && now.isBefore(threeDaysAfter))) {
            throw LoonoBackendException(
                HttpStatus.BAD_REQUEST,
                "400",
                "The self-examination cannot be completed."
            )
        }
    }

    private fun saveNewSelfExam(previousExam: SelfExaminationRecord) {
        val dueDate = previousExam.dueDate ?: LocalDate.now()
        selfExaminationRecordRepository.save(
            SelfExaminationRecord(
                type = previousExam.type,
                dueDate = dueDate.plusMonths(1),
                account = previousExam.account,
                result = null,
                status = SelfExaminationStatusDto.PLANNED
            )
        )
    }

    fun processFindingResult(
        type: SelfExaminationTypeDto,
        result: SelfExaminationResultDto,
        uid: String
    ): SelfExaminationFindingResponseDto {
        val account = prerequisitesValidation(uid, type)
        val examWaitingForResult =
            selfExaminationRecordRepository.findAllByAccountAndTypeOrderByDueDateDesc(account, type)
                .first { it.status == SelfExaminationStatusDto.WAITING_FOR_RESULT }
        when (result) {
            SelfExaminationResultDto.OK -> {
                completeSelfExamAsOK(examWaitingForResult)
                return SelfExaminationFindingResponseDto(
                    message = "Result completed as OK."
                )
            }
            SelfExaminationResultDto.NOT_OK -> {
                selfExaminationRecordRepository.save(
                    examWaitingForResult.copy(
                        result = result,
                        status = SelfExaminationStatusDto.COMPLETED
                    )
                )
                // TODO turn off notification related to the self-exams
                return SelfExaminationFindingResponseDto(
                    message = "The examination marked as NOT OK. Notifications are turned off."
                )
            }
            else -> {
                throw LoonoBackendException(
                    HttpStatus.BAD_REQUEST,
                    "400",
                    "Invalid result of self-examination."
                )
            }
        }
    }

    @Synchronized
    @Transactional(rollbackFor = [Exception::class])
    fun cancelExam(examUuid: String, accountUuid: String): ExaminationRecordDto =
        changeState(examUuid, accountUuid, ExaminationStatusDto.CANCELED)

    fun createOrUpdateExam(examinationRecordDto: ExaminationRecordDto, accountUuid: String): ExaminationRecordDto {
        validateAccountPrerequisites(examinationRecordDto, accountUuid)
        val record = validateUpdateAttempt(examinationRecordDto, accountUuid)
        addRewardIfEligible(examinationRecordDto, accountUuid)
        return examinationRecordRepository.save(
            ExaminationRecord(
                id = record.id,
                uuid = record.uuid,
                type = examinationRecordDto.type,
                plannedDate = examinationRecordDto.date,
                account = findAccount(accountUuid),
                firstExam = examinationRecordDto.firstExam ?: true,
                status = examinationRecordDto.status ?: ExaminationStatusDto.NEW
            )
        ).toExaminationRecordDto()
    }

    private fun validateAccountPrerequisites(record: ExaminationRecordDto, accountUuid: String) {
        val account = accountRepository.findByUid(accountUuid) ?: throw LoonoBackendException(
            HttpStatus.NOT_FOUND,
            "404",
            "The account not found."
        )
        val plannedExam = examinationRecordRepository.findAllByAccount(account)
            .filter { it.type == record.type && it.status == ExaminationStatusDto.NEW }
        if (plannedExam.isNotEmpty() && record.uuid == null) {
            throw LoonoBackendException(
                HttpStatus.CONFLICT,
                "409",
                "The examination of this type already exists."
            )
        }
        val intervals = preventionService.getExaminationRequests(account).filter { it.examinationType == record.type }
        intervals.ifEmpty {
            throw LoonoBackendException(
                HttpStatus.BAD_REQUEST,
                "400",
                "The account doesn't have rights to create this type of examinations."
            )
        }
    }

    private fun validateUpdateAttempt(
        examinationRecordDto: ExaminationRecordDto,
        accountUuid: String
    ): ExaminationRecord =
        if (examinationRecordDto.uuid != null) {
            examinationRecordRepository.findByUuid(examinationRecordDto.uuid)
                ?: throw LoonoBackendException(
                    HttpStatus.NOT_FOUND,
                    "404",
                    "The given examination identifier not found."
                )
        } else {
            ExaminationRecord(account = findAccount(accountUuid))
        }

    private fun findAccount(uuid: String): Account =
        accountRepository.findByUid(uuid) ?: throw LoonoBackendException(
            HttpStatus.NOT_FOUND, "Account not found"
        )

    private fun changeState(
        examUuid: String,
        accountUuid: String,
        state: ExaminationStatusDto
    ): ExaminationRecordDto {
        val account = findAccount(accountUuid)

        val exam = examinationRecordRepository.findByUuidAndAccount(examUuid, account)
        exam.status = state
        val badgeToPoints =
            BadgesPointsProvider.getBadgesAndPoints(exam.type, SexDto.valueOf(account.sex))
        val updatedAccount = updateWithBadgeAndPoints(badgeToPoints, account)
        accountRepository.save(updatedAccount)

        return examinationRecordRepository.save(exam).toExaminationRecordDto()
    }

    private fun updateWithBadgeAndPoints(badgeToPoints: Pair<BadgeTypeDto, Int>, account: Account): Account {
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
        } ?: account.badges.plus(
            Badge(badgeType, account.id, STARTING_LEVEL, account, clock.instant().toLocalDateTime())
        )

        return account.copy(badges = badgesToCopy, points = account.points + points)
    }

    private fun addRewardIfEligible(examinationRecordDto: ExaminationRecordDto, accountUuid: String) {
        if (isEligibleForReward(examinationRecordDto)) {
            // Null validation done before this function called, thus using double-bang operator
            val acc = accountRepository.findByUid(accountUuid)!!
            val reward = BadgesPointsProvider.getBadgesAndPoints(examinationRecordDto.type, SexDto.valueOf(acc.sex))
            val updatedAccount = updateWithBadgeAndPoints(reward, acc)
            accountRepository.save(updatedAccount)
        }
    }

    private fun isEligibleForReward(examinationRecordDto: ExaminationRecordDto) =
        (examinationRecordDto.status in setOf(ExaminationStatusDto.CONFIRMED, ExaminationStatusDto.UNKNOWN)) &&
            examinationRecordDto.date?.plusYears(2)?.isAfter(LocalDateTime.now()) ?: false

    fun ExaminationRecord.toExaminationRecordDto(): ExaminationRecordDto =
        ExaminationRecordDto(
            uuid = uuid,
            type = type,
            date = plannedDate,
            firstExam = firstExam,
            status = status
        )
}
