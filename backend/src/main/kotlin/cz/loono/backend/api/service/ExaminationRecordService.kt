package cz.loono.backend.api.service

import cz.loono.backend.api.dto.BadgeTypeDto
import cz.loono.backend.api.dto.ExaminationActionTypeDto
import cz.loono.backend.api.dto.ExaminationCategoryTypeDto
import cz.loono.backend.api.dto.ExaminationRecordDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationCompletionInformationDto
import cz.loono.backend.api.dto.SelfExaminationFindingResponseDto
import cz.loono.backend.api.dto.SelfExaminationResultDto
import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationTypeDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.data.constants.Constants
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.Badge
import cz.loono.backend.db.model.ExaminationRecord
import cz.loono.backend.db.model.SelfExaminationRecord
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import cz.loono.backend.extensions.atUTCOffset
import cz.loono.backend.extensions.toLocalDateTime
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.temporal.ChronoUnit
import java.util.Objects

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

    @Synchronized
    @Transactional(rollbackFor = [Exception::class])
    fun deleteExam(examUuid: String, accountUuid: String) {
        try {
            examinationRecordRepository.deleteByUuid(examUuid)
//            if (exam != null) {
//                examinationRecordRepository.delete(exam)
//                exam.uuid?.let {
//                    val test = examinationRecordRepository.findByUuid(it)
//                    if (test != null) {
//                        throw LoonoBackendException(
//                            HttpStatus.NOT_FOUND, "Delete failed - STILL FOUND}"
//                        )
//                    }
//                }
//            } else {
//                throw LoonoBackendException(
//                    HttpStatus.NOT_FOUND, "Delete failed - EXAM NOT FOUND $examUuid and $accountUuid and $exam"
//                )
//            }
        } catch (e: Exception) {
            throw LoonoBackendException(
                HttpStatus.NOT_FOUND, "Delete failed catch - $e"
            )
        }
    }

    fun confirmSelfExam(
        type: SelfExaminationTypeDto,
        result: SelfExaminationResultDto,
        accountUuid: String
    ): SelfExaminationCompletionInformationDto {
        val account = prerequisitesValidation(accountUuid, type)
        var count = 1
        val selfExams = selfExaminationRecordRepository.findAllByAccountAndTypeOrderByDueDateDesc(account, type)
        if (selfExams.isEmpty()) {
            when (result.result) {
                SelfExaminationResultDto.Result.OK -> {
                    val firstRecord = selfExaminationRecordRepository.save(
                        SelfExaminationRecord(
                            type = type,
                            dueDate = LocalDate.now(),
                            account = account,
                            result = SelfExaminationResultDto.Result.OK,
                            status = SelfExaminationStatusDto.COMPLETED
                        )
                    )
                    saveNewSelfExam(firstRecord)
                }
                SelfExaminationResultDto.Result.FINDING -> {
                    val today = LocalDate.now()
                    selfExaminationRecordRepository.save(
                        SelfExaminationRecord(
                            type = type,
                            dueDate = today,
                            account = account,
                            result = SelfExaminationResultDto.Result.FINDING,
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
            when (result.result) {
                SelfExaminationResultDto.Result.OK -> {
                    completeSelfExamAsOK(plannedExam)
                    count--
                }
                SelfExaminationResultDto.Result.FINDING -> {
                    selfExaminationRecordRepository.save(
                        plannedExam.copy(
                            result = SelfExaminationResultDto.Result.FINDING,
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
        val reward = BadgesPointsProvider.getSelfExaminationBadgesAndPoints(type, account.getSexAsEnum())
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
        val badgeLevel = updatedAccount.badges.find {
            it.type == BadgeTypeDto.SHIELD.toString() || it.type == BadgeTypeDto.PAULDRONS.toString()
        }?.level
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
                result = SelfExaminationResultDto.Result.OK,
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
        var examWaitingForResult =
            selfExaminationRecordRepository.findAllByAccountAndTypeOrderByDueDateDesc(account, type)
                .filter { it.status == SelfExaminationStatusDto.WAITING_FOR_RESULT }
        examWaitingForResult.ifEmpty {
            examWaitingForResult =
                selfExaminationRecordRepository.findAllByAccountAndTypeOrderByDueDateDesc(account, type)
                    .filter { it.status == SelfExaminationStatusDto.WAITING_FOR_CHECKUP }
        }
        examWaitingForResult.ifEmpty {
            throw throw LoonoBackendException(
                HttpStatus.CONFLICT,
                "400",
                "Result cannot be processed."
            )
        }
        when (result.result) {
            SelfExaminationResultDto.Result.OK -> {
                completeSelfExamAsOK(examWaitingForResult.first())
                return SelfExaminationFindingResponseDto(
                    message = "Result completed as OK."
                )
            }
            SelfExaminationResultDto.Result.NOT_OK -> {
                selfExaminationRecordRepository.save(
                    examWaitingForResult.first().copy(
                        result = SelfExaminationResultDto.Result.NOT_OK,
                        status = SelfExaminationStatusDto.COMPLETED
                    )
                )
                accountRepository.save(account.copy(notify = false))
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
        val account = findAccount(accountUuid)
        checkCustomExamsAmount(account)
        val record = validateUpdateAttempt(examinationRecordDto, accountUuid)
        validateDateInterval(examinationRecordDto, account)
        addRewardIfEligible(examinationRecordDto, account, examinationRecordDto.status)
        return examinationRecordRepository.save(
            ExaminationRecord(
                id = record.id,
                uuid = record.uuid,
                type = examinationRecordDto.type,
                plannedDate = examinationRecordDto.plannedDate?.toLocalDateTime(),
                account = account,
                firstExam = examinationRecordDto.firstExam ?: true,
                status = examinationRecordDto.status ?: ExaminationStatusDto.NEW,
                note = examinationRecordDto.note,
                customInterval = examinationRecordDto.customInterval,
                periodicExam = examinationRecordDto.periodicExam,
                examinationCategoryType = examinationRecordDto.examinationCategoryType ?: ExaminationCategoryTypeDto.MANDATORY,
                examinationActionType = examinationRecordDto.examinationActionType ?: ExaminationActionTypeDto.EXAMINATION
            )
        ).toExaminationRecordDto()
    }

    private fun validateDateInterval(
        record: ExaminationRecordDto,
        account: Account
    ) =
        record.plannedDate?.let planned@{
            record.firstExam?.let { isFirstExam ->
                if (isFirstExam) {
                    return@planned
                }
                val today = clock.instant().toLocalDateTime()
                val plannedLocalDateTime = it.toLocalDateTime()
                if (plannedLocalDateTime.isBefore(today)) {
                    throw LoonoBackendException(
                        HttpStatus.BAD_REQUEST,
                        "400",
                        "Cannot plan an exam in past."
                    )
                }
                if (record.examinationCategoryType == ExaminationCategoryTypeDto.MANDATORY) {
                    if (plannedDateInAcceptedInterval(plannedLocalDateTime, account, record)) {
                        throw LoonoBackendException(
                            HttpStatus.UNPROCESSABLE_ENTITY,
                            "422",
                            "The time has NOT passed."
                        )
                    }
                }
            }
        }

    private fun plannedDateInAcceptedInterval(
        date: LocalDateTime,
        account: Account,
        record: ExaminationRecordDto
    ): Boolean {
        val interval =
            preventionService.getExaminationRequests(account).first { it.examinationType == record.type }
        val lastConfirmed = examinationRecordRepository.findAllByAccountOrderByPlannedDateDesc(account)
            .filter {
                it.type == record.type &&
                    (
                        it.status == ExaminationStatusDto.CONFIRMED ||
                            (it.status == ExaminationStatusDto.UNKNOWN && it.plannedDate != null)
                        )
            }
        lastConfirmed.ifEmpty { return false }
        val intervalInMonths = (interval.intervalYears.toLong() * 12) - 2
        return date.isBefore(lastConfirmed.first().plannedDate!!.plusMonths(intervalInMonths))
    }

    private fun checkCustomExamsAmount(account: Account) {
        val customExamsSize = examinationRecordRepository.findAllByAccount(account)
            .filter { it.examinationCategoryType == ExaminationCategoryTypeDto.CUSTOM && it.status == ExaminationStatusDto.NEW }.size
        if (customExamsSize >= Constants.MAXIMUM_CUSTOM_EXAMS) {
            throw LoonoBackendException(
                HttpStatus.TOO_MANY_REQUESTS,
                "404",
                "Maximum amount of custom exam is exceeded."
            )
        }
    }
    private fun validateAccountPrerequisites(record: ExaminationRecordDto, accountUuid: String) {
        val account = accountRepository.findByUid(accountUuid) ?: throw LoonoBackendException(
            HttpStatus.NOT_FOUND,
            "404",
            "The account not found."
        )

        if (record.examinationCategoryType == ExaminationCategoryTypeDto.MANDATORY) {
            val plannedExam = examinationRecordRepository.findAllByAccount(account)
                .filter { it.type == record.type && it.status == ExaminationStatusDto.NEW }
            if (plannedExam.isNotEmpty() && record.uuid == null) {
                throw LoonoBackendException(
                    HttpStatus.CONFLICT,
                    "409",
                    "The examination of this type already exists."
                )
            }

            val intervals =
                preventionService.getExaminationRequests(account).filter { it.examinationType == record.type }
            intervals.ifEmpty {
                throw LoonoBackendException(
                    HttpStatus.BAD_REQUEST,
                    "400",
                    "The account doesn't have rights to create this type of examinations."
                )
            }
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
        addRewardIfEligible(exam.toExaminationRecordDto(), account, state)
        exam.status = state

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

    private fun addRewardIfEligible(
        examinationRecordDto: ExaminationRecordDto,
        acc: Account,
        newState: ExaminationStatusDto?
    ) {
        if (examinationRecordDto.examinationCategoryType == ExaminationCategoryTypeDto.MANDATORY) {
            val recordBeforeUpdate = examinationRecordDto.uuid?.let { examinationRecordRepository.findByUuid(it) }
            val isFirstExam = recordBeforeUpdate?.firstExam ?: true
            val isStatusChangedToExpectedStates = recordBeforeUpdate?.status != newState && (newState in setOf(ExaminationStatusDto.CONFIRMED, ExaminationStatusDto.UNKNOWN))

            if (isEligibleForReward(
                    isFirstExam,
                    isStatusChangedToExpectedStates,
                    examinationRecordDto.plannedDate?.toLocalDateTime(),
                    newState
                )
            ) {
                val reward =
                    BadgesPointsProvider.getGeneralBadgesAndPoints(examinationRecordDto.type, acc.getSexAsEnum())
                val updatedAccount = updateWithBadgeAndPoints(reward, acc)
                accountRepository.save(updatedAccount)
            }
        }
    }

    private fun isEligibleForReward(
        isFirstExam: Boolean,
        isStatusCorrect: Boolean,
        plannedDate: LocalDateTime?,
        newState: ExaminationStatusDto?
    ) = now().let { now ->
        when {
            newState == ExaminationStatusDto.UNKNOWN && Objects.isNull(plannedDate) -> false
            newState == ExaminationStatusDto.UNKNOWN && plannedDate?.dayOfMonth == now.dayOfMonth && plannedDate.year == now.year -> true
            isFirstExam && isStatusCorrect && isPlannedDateWithinExpectedRange(plannedDate) -> true
            isStatusCorrect && isPlannedDateWithinExpectedRange(plannedDate) -> true
            else -> false
        }
    }

    private fun isPlannedDateWithinExpectedRange(plannedDate: LocalDateTime?) = plannedDate?.let {
        now().let { now ->
            plannedDate.isBefore(now) && plannedDate.let { ChronoUnit.YEARS.between(now, plannedDate) < 2 }
        }
    } ?: true

    fun ExaminationRecord.toExaminationRecordDto(): ExaminationRecordDto =
        ExaminationRecordDto(
            uuid = uuid,
            type = type,
            plannedDate = plannedDate?.atUTCOffset(),
            firstExam = firstExam,
            note = note,
            periodicExam = periodicExam,
            customInterval = customInterval,
            examinationCategoryType = examinationCategoryType,
            status = status,
            examinationActionType = examinationActionType
        )
}
