package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationPreventionStatusDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.PreventionStatusDto
import cz.loono.backend.api.dto.SelfExaminationPreventionStatusDto
import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationTypeDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.ExaminationRecord
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

        val birthDate = account.userAuxiliary.birthdate ?: throw LoonoBackendException(
            HttpStatus.UNPROCESSABLE_ENTITY, "birthdate not known"
        )
        val age = ChronoUnit.YEARS.between(birthDate, LocalDate.now()).toInt()

        return ExaminationIntervalProvider.findExaminationRequests(
            Patient(age, SexDto.valueOf(account.userAuxiliary.sex))
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

        val examinations = prepareExaminationStatuses(examinationRequests, examinationTypesToRecords)

        val selfExamsList = prepareSelfExaminationsStatuses(account)
        return PreventionStatusDto(examinations = examinations, selfexaminations = selfExamsList)
    }

    private fun prepareExaminationStatuses(
        examinationRequests: List<ExaminationInterval>,
        examinationTypesToRecords: Map<ExaminationTypeDto, List<ExaminationRecord>>
    ): List<ExaminationPreventionStatusDto> = examinationRequests.map { examinationInterval ->
        val examsOfType = examinationTypesToRecords[examinationInterval.examinationType]
        val sortedExamsOfType = examsOfType
            ?.filter { it ->
                it.plannedDate != null ||
                    it.status != ExaminationStatusDto.CONFIRMED ||
                    it.status != ExaminationStatusDto.CANCELED
            }
            ?.sortedBy(ExaminationRecord::plannedDate) ?: listOf(ExaminationRecord())

        val confirmedExamsOfCurrentType = examsOfType?.filter { it.status == ExaminationStatusDto.CONFIRMED }
        // 1) Filter all the confirmed records
        // 2) Map all non-nullable lastExamination records
        // 3) Find the largest or return null if the list is empty
        val lastConfirmedDate = confirmedExamsOfCurrentType?.mapNotNull(ExaminationRecord::plannedDate)?.maxOrNull()
        val totalCountOfConfirmedExams = confirmedExamsOfCurrentType?.size ?: 0

        ExaminationPreventionStatusDto(
            uuid = sortedExamsOfType[0].uuid,
            examinationType = examinationInterval.examinationType,
            intervalYears = examinationInterval.intervalYears,
            plannedDate = sortedExamsOfType[0].plannedDate,
            firstExam = sortedExamsOfType[0].firstExam,
            priority = examinationInterval.priority,
            state = sortedExamsOfType[0].status,
            count = totalCountOfConfirmedExams,
            lastConfirmedDate = lastConfirmedDate
        )
    }

    private fun prepareSelfExaminationsStatuses(account: Account): List<SelfExaminationPreventionStatusDto> {
        val result = mutableListOf<SelfExaminationPreventionStatusDto>()
        val selfExams = selfExaminationRecordRepository.findAllByAccount(account)
        SelfExaminationTypeDto.values().forEach { type ->
            val filteredExams = selfExams.filter { exam -> exam.type == type }
            val suitableSelfExam = validateSexPrerequisites(type, account.userAuxiliary.sex)
            if (filteredExams.isNotEmpty() && suitableSelfExam) {
                val plannedExam = filteredExams.filter { exam -> exam.status == SelfExaminationStatusDto.PLANNED }[0]
                result.add(
                    SelfExaminationPreventionStatusDto(
                        lastExamUuid = plannedExam.uuid,
                        plannedDate = plannedExam.dueDate,
                        type = type,
                        history = filteredExams.map(SelfExaminationRecord::status)
                    )
                )
            } else if (suitableSelfExam) {
                result.add(
                    SelfExaminationPreventionStatusDto(
                        type = type,
                        history = emptyList()
                    )
                )
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
