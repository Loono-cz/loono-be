package cz.loono.backend.api.service

import cz.loono.backend.db.repository.AccountRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TestEndpointService(
    private val accountRepository: AccountRepository,
    private val preventionService: PreventionService,
    private val notificationService: PushNotificationService
) {

    fun getTestEndpoint(accountId: String): String {
        val accounts = accountRepository.findByUid(accountId)
        val today = LocalDate.now()
        var response = "${accounts?.uid}"

        accounts?.let { account ->
            response = "$response account found "
            val statuses = preventionService.getPreventionStatus(account.uid).selfexaminations
            response = "$response statuses ${statuses.size} "
            val todayNotifications = statuses.filter { it.plannedDate == today }
            val firstNotifications = statuses.filter { account.created.dayOfMonth == today.dayOfMonth && it.plannedDate == null }
            response = "$response today ${todayNotifications.size}, first ${firstNotifications.size} "

            if (todayNotifications.isNotEmpty()) {
                notificationService.sendSelfExamNotification(setOf(account))
                response = "$response normal notifacion + "
            }
            if (firstNotifications.isNotEmpty()) {
                notificationService.sendFirstSelfExamNotification(setOf(account))
                response = "$response first notifacion + "
            }
        }
        return response
    }
}
