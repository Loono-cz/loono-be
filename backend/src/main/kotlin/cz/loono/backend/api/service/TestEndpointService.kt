package cz.loono.backend.api.service

import cz.loono.backend.db.model.Account
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
            response.plus(" account found ")
            val statuses = preventionService.getPreventionStatus(account.uid).selfexaminations

            val todayNotifications = statuses.filter { it.plannedDate == today }
            val firstNotifications = statuses.filter { account.created.dayOfMonth == today.dayOfMonth && it.plannedDate == null }
            response.plus(" today ${todayNotifications.size}, first ${firstNotifications.size} ")

            if (todayNotifications.isNotEmpty()) {
                notificationService.sendSelfExamNotification(setOf<Account>(account))
                response.plus(" normal notifacion + ")
            }
            if (firstNotifications.isNotEmpty()) {
                notificationService.sendFirstSelfExamNotification(setOf<Account>(account))
                response.plus(" first notifacion + ")
            }
        }
        return response
    }
}
