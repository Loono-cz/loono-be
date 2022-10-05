package cz.loono.backend.api.service

import cz.loono.backend.api.dto.ExaminationCategoryTypeDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ExaminationRecordRepository
import cz.loono.backend.db.repository.SelfExaminationRecordRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

@Service
class TestEndpointService(
    private val accountRepository: AccountRepository,
    private val preventionService: PreventionService,
    private val notificationService: PushNotificationService,
    private val examinationRecordRepository: ExaminationRecordRepository,
    private val selfExaminationRecordRepository: SelfExaminationRecordRepository
) {

    fun getTestEndpoint(accountId: String): String {
        val accounts = accountRepository.findByUid(accountId)
        val today = LocalDate.now()
        var response = "${accounts?.uid}"
        val time = LocalDateTime.now().plusHours(2).plusMinutes(2).format(DateTimeFormatter.ofPattern("HH:mm"))
        response = "$response local time: $time \n"
        accounts?.let { account ->
            try {
                response = "$response account found \n "
                val statuses = selfExaminationRecordRepository.findAllByAccount(account)
                response = "$response self exam statuses ${statuses.size} \n"
                val todayNotifications = statuses.filter { it.dueDate == today && it.status == SelfExaminationStatusDto.PLANNED && it.result == null }
                val firstNotifications = statuses.filter { account.created.dayOfMonth == today.dayOfMonth && it.dueDate == null }
                response = "$response today ${todayNotifications.size}, first ${firstNotifications.size} \n"

                if (todayNotifications.isNotEmpty()) {
                    notificationService.sendSelfExamNotificationTestEndpoint(setOf(account))
                    response = "$response normal notifacion - $todayNotifications \n "
                }
                if (firstNotifications.isNotEmpty()) {
                    notificationService.sendFirstSelfExamNotificationTestEndpoint(setOf(account))
                    response = "$response first notifacion - $firstNotifications \n "
                }
                if (statuses.isEmpty() && account.created.dayOfMonth == today.dayOfMonth) {
                    notificationService.sendFirstSelfExamNotificationTestEndpoint(setOf(account))
                    response = "$response first notifacion on empty list \n"
                }
                selfExaminationRecordRepository.findAllByStatus(SelfExaminationStatusDto.WAITING_FOR_CHECKUP).forEach {
                    if (it.account == account) {
                        response = "$response WAITING FOR CHECKUP $it \n"
                        if (it.waitingTo == LocalDate.now()) {
                            selfExaminationRecordRepository.save(
                                it.copy(
                                    status = SelfExaminationStatusDto.WAITING_FOR_RESULT,
                                    waitingTo = null
                                )
                            )
                            response = "$response WAITING FOR CHECKUP SEND \n"
                            notificationService.sendSelfExamIssueResultNotificationTestEndpoint(setOf(account))
                        }
                    }
                }
            } catch (e: Exception) {
                throw LoonoBackendException(
                    HttpStatus.CONFLICT, "test self exam fail - ${e.localizedMessage}"
                )
            }
        }

        try {
            response = "$response + \n CUSTOM EXAMS CONFIRMATION"
            val now = LocalDateTime.now()
            val plannedExams = examinationRecordRepository.findAllByStatus(status = ExaminationStatusDto.NEW)
            val customExams = plannedExams.filter { it.examinationCategoryType == ExaminationCategoryTypeDto.CUSTOM }
            val customExamNonPeriodic = customExams.filter { it.periodicExam == false }
            response = "$response + customExamNonPeriodic ${customExamNonPeriodic.size}"
            customExamNonPeriodic.forEach { record ->
                response = "$response + record ${record.id} - ${record.plannedDate} \n"
                record.plannedDate?.let { plannedDate ->
                    if (now.isAfter(plannedDate)) {
                        response = "$response + CHANGE record ${record.id} - ${record.plannedDate} \n"
                        examinationRecordRepository.save(record.copy(status = ExaminationStatusDto.CONFIRMED))
                    }
                }
            }
        } catch (e: Exception) {
            throw LoonoBackendException(
                HttpStatus.CONFLICT, "test confirmation failed - ${e.localizedMessage}"
            )
        }

        try {
            response = "$response \n CUSTOM EXAMS 2 MONTHS NOTIFICATION"
            accounts?.let { account ->
                val examStatuses = preventionService.getPreventionStatus(account.uid).examinations
                val mandatoryExams = examStatuses.filter { it.examinationCategoryType == ExaminationCategoryTypeDto.MANDATORY }
                val customExams = examStatuses.filter { it.examinationCategoryType == ExaminationCategoryTypeDto.CUSTOM }
                mandatoryExams.forEach { status ->
                    status.lastConfirmedDate?.let {
                        response = "$response \n M \n $status"
                        val period = Period.between(status.lastConfirmedDate.toLocalDate(), today)
                        val passedMonths = period.years * 12 + period.months
                        response = "$response \n passedMonths = $passedMonths , status*12-2 = ${(status.intervalYears * 12) - 2} , periodDays = ${period.days} "
                        response = "$response \n ${passedMonths == (status.intervalYears * 12) - 2 && period.days == 0} "
                        if (passedMonths == (status.intervalYears * 12) - 2 && period.days == 0) {
                            response = "$response \n SEND NOTIFICATION 2 MONTH ${status.uuid} - ${status.lastConfirmedDate} \n "
                            notificationService.sendNewExam2MonthsAheadNotificationToOrderTestEndpoint(
                                setOf(account),
                                status.examinationType,
                                status.intervalYears,
                                status.examinationCategoryType
                            )
                        }
                    }
                }

                customExams.forEach { status ->
                    response = "$response \n custom record $status"
                    status.lastConfirmedDate?.let {
                        response = "$response \n HAS LAST DATE"
                        if (status.periodicExam == true) {
                            response = "$response \n IS PERIODIC"
                            val period = Period.between(status.lastConfirmedDate.toLocalDate(), today)
                            response = "$response \n PERIOD IS Y-${period.years} M-${period.months} D-${period.days}"
                            response = "$response \n ${period.months == status.customInterval?.minus(2) && period.days == 0} "
                            if (period.months == (status.customInterval?.minus(2)) && period.days == 0) {
                                response = "$response \n SEND ${setOf(account) } \n ${status.examinationType} \n ${status.intervalYears}"
                                notificationService.sendNewExam2MonthsAheadNotificationToOrderTestEndpoint(
                                    setOf(account),
                                    status.examinationType,
                                    status.intervalYears,
                                    status.examinationCategoryType
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw LoonoBackendException(
                HttpStatus.CONFLICT, "test custom notification failed - ${e.localizedMessage}"
            )
        }

        return response
    }
}
