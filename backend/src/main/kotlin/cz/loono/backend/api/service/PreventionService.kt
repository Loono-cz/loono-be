package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationCategoryTypeDto
import cz.loono.backend.api.dto.ExaminationPreventionStatusDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.PreventionStatusDto
import cz.loono.backend.api.dto.SelfExaminationPreventionStatusDto
import cz.loono.backend.api.dto.SelfExaminationResultDto
import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationTypeDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.ExaminationRecord
import cz.loono.backend.db.model.SelfExaminationRecord
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import cz.loono.backend.extensions.atUTCOffset
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class PreventionService(
    private val examinationRecordRepository: ExaminationRecordRepository,
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository,
    private val accountRepository: AccountRepository
) {

    fun getExaminationRequests(account: Account): List<ExaminationInterval> {

        val age = ChronoUnit.YEARS.between(account.birthdate, LocalDate.now()).toInt()

        return ExaminationIntervalProvider.findExaminationRequests(
            Patient(age, account.getSexAsEnum())
        )
    }

    fun getPreventionStatus(accountUuid: String): PreventionStatusDto {

        val account = accountRepository.findByUid(accountUuid) ?: throw LoonoBackendException(
            HttpStatus.NOT_FOUND, "Account not found"
        )
        try {
            val joinedExaminations: List<ExaminationPreventionStatusDto>

            val examinationRequests = getExaminationRequests(account)

            val examinationByAccountAndPlannedDate = examinationRecordRepository.findAllByAccountOrderByPlannedDateDesc(account)
            val filteredExaminationByAccountAndPlannedDate = examinationByAccountAndPlannedDate.filter { it.examinationCategoryType != ExaminationCategoryTypeDto.CUSTOM }
            val examinationTypesToRecords: Map<ExaminationTypeDto, List<ExaminationRecord>> =
                filteredExaminationByAccountAndPlannedDate
                    .groupBy(ExaminationRecord::type)
                    .mapNotNull { entry -> entry.key to entry.value }
                    .toMap()

            val examinations = prepareExaminationStatuses(
                examinationRequests,
                examinationTypesToRecords,
                account
            )
            try {
                val filteredExaminations = examinations.filter { it.examinationCategoryType == ExaminationCategoryTypeDto.MANDATORY || it.examinationCategoryType == null }

                val plannedExam = examinationRecordRepository.findAllByAccount(account)

                val customExams = plannedExam.filter { it.examinationCategoryType == ExaminationCategoryTypeDto.CUSTOM }
                val customExamsNotOnceConfirmed = customExams.filter { it.periodicExam == true || (it.periodicExam == false && it.status == ExaminationStatusDto.NEW) }
                val listOfPassedCustomExams = customExamsNotOnceConfirmed.filter { (it.status == ExaminationStatusDto.CONFIRMED || it.status == ExaminationStatusDto.UNKNOWN) }
                val customExaminations = prepareCustomStatuses(customExamsNotOnceConfirmed, listOfPassedCustomExams)

                joinedExaminations = customExaminations + filteredExaminations
            } catch (e: Exception) {
                throw LoonoBackendException(
                    HttpStatus.CONFLICT, "Custom and mandatory join failed - ${e.localizedMessage}"
                )
            }

            val selfExamsList = prepareSelfExaminationsStatuses(account)
            return PreventionStatusDto(examinations = joinedExaminations, selfexaminations = selfExamsList)
        } catch (e: Exception) {
            throw LoonoBackendException(
                HttpStatus.CONFLICT, "EXAMS GET failed - ${e.localizedMessage}"
            )
        }
    }

    private fun prepareExaminationStatuses(
        examinationRequests: List<ExaminationInterval>,
        examinationTypesToRecords: Map<ExaminationTypeDto, List<ExaminationRecord>>,
        account: Account
    ): List<ExaminationPreventionStatusDto> = examinationRequests.map { examinationInterval ->
        val examsOfType = examinationTypesToRecords[examinationInterval.examinationType]
        val examsOfTypeNotNull = examsOfType?.filter { it.plannedDate != null }
        val examsOfTypeNotCanceled = examsOfTypeNotNull?.filter { it.status != ExaminationStatusDto.CANCELED }
        var sortedExamsOfType = examsOfTypeNotCanceled?.sortedByDescending(ExaminationRecord::plannedDate)
        if (sortedExamsOfType.isNullOrEmpty()) {
            sortedExamsOfType = listOf(
                ExaminationRecord(
                    account = account,
                    type = examinationInterval.examinationType,
                    uuid = null,
                    firstExam = false,
                    note = null,
                    status = ExaminationStatusDto.NEW
                )
            )
        }

        val confirmedExamsOfCurrentType = examsOfType?.filter {
            it.status == ExaminationStatusDto.CONFIRMED || (it.status == ExaminationStatusDto.UNKNOWN && it.plannedDate != null)
        } ?: emptyList()

        // 1) Filter all the confirmed records
        // 2) Map all non-nullable lastExamination records
        // 3) Find the largest or return null if the list is empty
        val lastConfirmedDate = confirmedExamsOfCurrentType.mapNotNull(ExaminationRecord::plannedDate).maxOrNull()
        val totalCountOfConfirmedExams = confirmedExamsOfCurrentType.size

        val rewards =
            BadgesPointsProvider.getGeneralBadgesAndPoints(examinationInterval.examinationType, account.getSexAsEnum())

        ExaminationPreventionStatusDto(
            uuid = sortedExamsOfType[0].uuid,
            examinationType = examinationInterval.examinationType,
            intervalYears = examinationInterval.intervalYears,
            plannedDate = sortedExamsOfType[0].plannedDate?.atUTCOffset(),
            firstExam = sortedExamsOfType[0].firstExam,
            priority = examinationInterval.priority,
            state = sortedExamsOfType[0].status,
            count = totalCountOfConfirmedExams,
            lastConfirmedDate = lastConfirmedDate?.atUTCOffset(),
            points = rewards.second,
            badge = rewards.first,
            examinationCategoryType = sortedExamsOfType[0].examinationCategoryType,
            periodicExam = sortedExamsOfType[0].periodicExam,
            customInterval = sortedExamsOfType[0].customInterval,
            examinationActionType = sortedExamsOfType[0].examinationActionType,
            note = sortedExamsOfType[0].note,
            createdAt = sortedExamsOfType[0].createdAt
        )
    }

    private fun prepareCustomStatuses(plannedExam: List<ExaminationRecord>, pastExams: List<ExaminationRecord>): List<ExaminationPreventionStatusDto> {
        try {
            return plannedExam.map { customExam ->
                val lastExam = pastExams.filter { it.type == customExam.type && it.plannedDate != null }
                val countOfExamType = pastExams.filter { it.type == customExam.type && it.plannedDate != null }.size
                ExaminationPreventionStatusDto(
                    uuid = customExam.uuid,
                    examinationType = customExam.type,
                    intervalYears = customExam.customInterval ?: 0,
                    plannedDate = customExam.plannedDate?.atUTCOffset(),
                    firstExam = customExam.firstExam,
                    priority = 0,
                    state = customExam.status,
                    count = countOfExamType,
                    lastConfirmedDate = lastExam.mapNotNull(ExaminationRecord::plannedDate).maxOrNull()?.atUTCOffset(),
                    points = if (customExam.periodicExam == true) { 50 } else { 0 },
                    badge = null,
                    examinationCategoryType = customExam.examinationCategoryType,
                    periodicExam = customExam.periodicExam,
                    customInterval = customExam.customInterval,
                    examinationActionType = customExam.examinationActionType,
                    note = customExam.note,
                    createdAt = customExam.createdAt
                )
            }
        } catch (e: Exception) {
            throw LoonoBackendException(
                HttpStatus.CONFLICT, "Custom assert failed - ${e.localizedMessage}"
            )
        }
    }

    private fun prepareSelfExaminationsStatuses(account: Account): List<SelfExaminationPreventionStatusDto> {
        val result = mutableListOf<SelfExaminationPreventionStatusDto>()
        SelfExaminationTypeDto.values().forEach { type ->
            val filteredExams = selfExaminationRecordRepository.findAllByAccountAndTypeOrderByDueDateAsc(account, type)
            val rewards = BadgesPointsProvider.getSelfExaminationBadgesAndPoints(type, account.getSexAsEnum())
            when {
                filteredExams.isNotEmpty() && rewards != null -> {
                    if (filteredExams.last().result != SelfExaminationResultDto.Result.NOT_OK) {
                        val activeExam =
                            filteredExams.last { exam ->
                                exam.status == SelfExaminationStatusDto.PLANNED ||
                                    exam.result == SelfExaminationResultDto.Result.FINDING
                            }
                        result.add(
                            SelfExaminationPreventionStatusDto(
                                lastExamUuid = activeExam.uuid,
                                plannedDate = activeExam.dueDate,
                                type = type,
                                history = filteredExams.map(SelfExaminationRecord::status),
                                points = rewards.second,
                                badge = rewards.first
                            )
                        )
                    }
                }
                rewards != null -> {
                    result.add(
                        SelfExaminationPreventionStatusDto(
                            type = type,
                            history = emptyList(),
                            points = rewards.second,
                            badge = rewards.first
                        )
                    )
                }
            }
        }
        return result
    }

    fun validateSexPrerequisites(type: SelfExaminationTypeDto, sex: String): Boolean =
        when (type) {
            SelfExaminationTypeDto.BREAST -> sex == SexDto.FEMALE.name
            SelfExaminationTypeDto.TESTICULAR -> sex == SexDto.MALE.name
            SelfExaminationTypeDto.SKIN -> true
        }
}
