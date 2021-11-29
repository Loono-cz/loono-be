package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationTypeEnumDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.db.model.ExaminationRecord
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class PreventionService(
    private val examinationRecordRepository: ExaminationRecordRepository,
    private val accountRepository: AccountRepository
) {

    fun getPreventionStatus(accountUuid: UUID): List<PreventionStatus> {
        val account = accountRepository.findByUid(accountUuid.toString()) ?: throw LoonoBackendException(
            HttpStatus.NOT_FOUND, "Account not found"
        )

        val sex = account.userAuxiliary.sex ?: throw LoonoBackendException(
            HttpStatus.UNPROCESSABLE_ENTITY, "sex not known"
        )

        val birthDate = account.userAuxiliary.birthdate ?: throw LoonoBackendException(
            HttpStatus.UNPROCESSABLE_ENTITY, "birthdate not known"
        )
        val age = ChronoUnit.YEARS.between(birthDate, LocalDate.now()).toInt()

        val examinationRequests = ExaminationIntervalProvider.findExaminationRequests(
            Patient(age, SexDto.valueOf(sex))
        )

        val examinationTypesToRecords: Map<String, List<ExaminationRecord>> = examinationRecordRepository.findAllByAccount(account)
            .filter { it.lastVisit != null }
            .groupBy { it.type }
            .mapNotNull { entry -> entry.key to entry.value }
            .toMap()

        return examinationRequests.map { examinationInterval ->
            val examsOfType = examinationTypesToRecords[examinationInterval.examinationType.name]
            val lastExamDate = examsOfType
                ?.mapNotNull { it.lastVisit }
                ?.maxOrNull()

            // TODO  we need planned exam - rework exams table to have planned date and executed date
            PreventionStatus(
                examinationInterval.examinationType,
                examinationInterval.intervalYears,
                lastExamDate,
            )
        }
    }
}

data class PreventionStatus(
    val examinationType: ExaminationTypeEnumDto,
    val intervalYears: Int,
    val lastExamDate: LocalDate?
)
