package cz.loono.backend.api.service

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
            Patient(age, SexDto.valueOf(account.sex))
        )
    }

    fun getPreventionStatus(accountUuid: String): PreventionStatusDto {

        val account = accountRepository.findByUid(accountUuid) ?: throw LoonoBackendException(
            HttpStatus.NOT_FOUND, "Account not found"
        )

        val examinationRequests = getExaminationRequests(account)

        val examinationTypesToRecords: Map<ExaminationTypeDto, List<ExaminationRecord>> =
            examinationRecordRepository.findAllByAccountOrderByPlannedDateDesc(account)
                .groupBy(ExaminationRecord::type)
                .mapNotNull { entry -> entry.key to entry.value }
                .toMap()

        val examinations = prepareExaminationStatuses(
            examinationRequests,
            examinationTypesToRecords,
            account
        )

        val selfExamsList = prepareSelfExaminationsStatuses(account)
        return PreventionStatusDto(examinations = examinations, selfexaminations = selfExamsList)
    }

    private fun prepareExaminationStatuses(
        examinationRequests: List<ExaminationInterval>,
        examinationTypesToRecords: Map<ExaminationTypeDto, List<ExaminationRecord>>,
        account: Account
    ): List<ExaminationPreventionStatusDto> = examinationRequests.map { examinationInterval ->
        val examsOfType = examinationTypesToRecords[examinationInterval.examinationType]
        val sortedExamsOfType = examsOfType
            ?.filter { it ->
                it.plannedDate != null ||
                    it.status != ExaminationStatusDto.CONFIRMED ||
                    it.status != ExaminationStatusDto.CANCELED
            }
            ?.sortedBy(ExaminationRecord::plannedDate) ?: listOf(ExaminationRecord(account = account))

        val confirmedExamsOfCurrentType = examsOfType?.filter {
            it.status == ExaminationStatusDto.CONFIRMED ||
                (it.status == ExaminationStatusDto.UNKNOWN && it.plannedDate != null)
        }
        // 1) Filter all the confirmed records
        // 2) Map all non-nullable lastExamination records
        // 3) Find the largest or return null if the list is empty
        val lastConfirmedDate = confirmedExamsOfCurrentType?.mapNotNull(ExaminationRecord::plannedDate)?.maxOrNull()
        val totalCountOfConfirmedExams = confirmedExamsOfCurrentType?.size ?: 0
        val rewards =
            BadgesPointsProvider.getBadgesAndPoints(examinationInterval.examinationType, SexDto.valueOf(account.sex))

        ExaminationPreventionStatusDto(
            uuid = sortedExamsOfType[0].uuid,
            examinationType = examinationInterval.examinationType,
            intervalYears = examinationInterval.intervalYears,
            plannedDate = sortedExamsOfType[0].plannedDate,
            firstExam = sortedExamsOfType[0].firstExam,
            priority = examinationInterval.priority,
            state = sortedExamsOfType[0].status,
            count = totalCountOfConfirmedExams,
            lastConfirmedDate = lastConfirmedDate,
            points = rewards.second,
            badge = rewards.first
        )
    }

    private fun prepareSelfExaminationsStatuses(account: Account): List<SelfExaminationPreventionStatusDto> {
        val result = mutableListOf<SelfExaminationPreventionStatusDto>()
        val selfExams = selfExaminationRecordRepository.findAllByAccount(account)
        SelfExaminationTypeDto.values().forEach { type ->
            val filteredExams =
                selfExams.filter { exam -> exam.type == type && exam.result != SelfExaminationResultDto.Result.NOT_OK }
            val rewards = BadgesPointsProvider.getBadgesAndPoints(type, SexDto.valueOf(account.sex))
            when {
                filteredExams.isNotEmpty() && rewards != null -> {
                    val activeExam =
                        filteredExams.first { exam ->
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
        }
}
