package cz.loono.backend.schedule

import cz.loono.backend.api.dto.ExaminationCategoryTypeDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.service.AccountService
import cz.loono.backend.api.service.PreventionService
import cz.loono.backend.api.service.PushNotificationService
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.CronLog
import cz.loono.backend.db.repository.CronLogRepository
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

@Component
class PreventionReminderTask(
    private val accountService: AccountService,
    private val preventionService: PreventionService,
    private val notificationService: PushNotificationService,
    private val cronLogRepository: CronLogRepository
) : DailySchedulerTask {

    override fun run() {
        try {
            val today = LocalDate.now()
            accountService.paginateOverAccounts { accounts ->
                val selectedAccounts = accounts.filter {
                    filterAccounts(it, today)
                }
                val notificationAccounts = mutableSetOf<Account>()
                selectedAccounts.forEach { account ->
                    val status = preventionService.getPreventionStatus(account.uid)
                    status.examinations.forEach examsLoop@{ exam ->
                        when (exam.examinationCategoryType) {
                            ExaminationCategoryTypeDto.MANDATORY -> {
                                when (exam.state) {
                                    ExaminationStatusDto.NEW -> {
                                        if (exam.uuid == null) {
                                            notificationAccounts.add(account)
                                            return@examsLoop
                                        }
                                    }
                                    ExaminationStatusDto.UNKNOWN -> {
                                        notificationAccounts.add(account)
                                        return@examsLoop
                                    }
                                    ExaminationStatusDto.CONFIRMED -> {
                                        val period = Period.between(exam.plannedDate?.toLocalDate(), today)
                                        if (period.years >= exam.intervalYears) {
                                            notificationAccounts.add(account)
                                            return@examsLoop
                                        }
                                    }
                                    else -> {
                                        return@examsLoop
                                    }
                                }
                            }
                            ExaminationCategoryTypeDto.CUSTOM -> {
                                when (exam.state) {
                                    ExaminationStatusDto.CONFIRMED -> {
                                        exam.customInterval?.let { customInterval ->
                                            val period = Period.between(exam.plannedDate?.toLocalDate(), today)
                                            if (period.months >= customInterval) {
                                                notificationAccounts.add(account)
                                                return@examsLoop
                                            }
                                        }
                                    }
                                    else -> {
                                        return@examsLoop
                                    }
                                }
                            }
                            else -> {
                                return@examsLoop
                            }
                        }
                    }
                }
                if (notificationAccounts.isNotEmpty()) {
                    notificationService.sendPreventionNotification(notificationAccounts)
                }
            }
            cronLogRepository.save(
                CronLog(
                    functionName = "PreventionReminderTask",
                    status = "PASSED",
                    message = null,
                    createdAt = LocalDate.now().toString()
                )
            )
        } catch (e: Exception) {
            cronLogRepository.save(
                CronLog(
                    functionName = "PreventionReminderTask",
                    status = "ERROR",
                    message = "$e",
                    createdAt = LocalDate.now().toString()
                )
            )
        }
    }

    private fun filterAccounts(account: Account, today: LocalDate): Boolean {
        if (account.created == today) {
            return false
        }

        val monthsPeriod = ChronoUnit.MONTHS.between(account.created.withDayOfMonth(1), today.withDayOfMonth(1))
        val lastDayOfCurrentMonth = today.withDayOfMonth(today.month.length(today.isLeapYear))
        if (monthsPeriod.toInt() % 3 == 0) {
            if (account.created.dayOfMonth - today.dayOfMonth == 0) {
                return true
            }
            if (lastDayOfCurrentMonth == today && account.created.dayOfMonth > today.dayOfMonth)
                return true
        }

        return false
    }
}
