package cz.loono.backend.api.service

import com.google.gson.Gson
import cz.loono.backend.api.dto.BadgeTypeDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.NotificationLog
import cz.loono.backend.db.repository.NotificationLogRepository
import cz.loono.backend.notification.NotificationDefinition
import cz.loono.backend.notification.NotificationResponse
import cz.loono.backend.notification.PushNotification
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PushNotificationService(
    private val notificationLogRepository: NotificationLogRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        if (ONESIGNAL_API_KEY.isEmpty()) {
            logger.error("ONESIGNAL_API_KEY ENV variable is not set.")
        }
        if (ONESIGNAL_APP_ID.isEmpty()) {
            logger.error("ONESIGNAL_APP_ID ENV variable is not set.")
        }
    }

    fun sendPreventionNotification(accounts: Set<Account>): String =
        sendPushNotification(NotificationDefinition.getPreventionNotification(accounts))

    fun sendCompletionNotification(
        accounts: Set<Account>,
        time: String,
        examinationTypeDto: ExaminationTypeDto
    ): String =
        sendPushNotification(NotificationDefinition.getCompletionNotification(accounts, time, examinationTypeDto))

    fun sendNewExamMonthAheadNotificationToOrder(
        accounts: Set<Account>,
        examinationTypeDto: ExaminationTypeDto,
        interval: Int,
        badgeTypeDto: BadgeTypeDto
    ): String = sendPushNotification(
        NotificationDefinition.getOrderNewExamMonthAheadNotification(
            accounts,
            examinationTypeDto,
            interval,
            badgeTypeDto
        )
    )

    fun sendNewExam2MonthsAheadNotificationToOrder(
        accounts: Set<Account>,
        examinationTypeDto: ExaminationTypeDto,
        interval: Int
    ): String = sendPushNotification(
        NotificationDefinition.getOrderNewExam2MonthsAheadNotification(
            accounts,
            examinationTypeDto,
            interval
        )
    )

    fun sendComingVisitNotification(
        accounts: Set<Account>,
        examinationTypeDto: ExaminationTypeDto,
        time: String
    ): String = sendPushNotification(
        NotificationDefinition.getComingVisitNotification(
            accounts,
            examinationTypeDto,
            time
        )
    )

    fun sendFirstSelfExamNotification(accounts: Set<Account>): String =
        sendPushNotification(NotificationDefinition.getFirstSelfExamNotification(accounts))

    fun sendSelfExamNotification(accounts: Set<Account>): String =
        sendPushNotification(NotificationDefinition.getSelfExamNotification(accounts))

    // TODO - remove after testing
    fun sendFirstSelfExamNotificationTestEndpoint(accounts: Set<Account>): String =
        sendPushNotification(NotificationDefinition.getFirstSelfExamNotificationTestEndpoint(accounts))
    fun sendSelfExamNotificationTestEndpoint(accounts: Set<Account>): String =
        sendPushNotification(NotificationDefinition.getSelfExamNotificationTestEndpoint(accounts))

    fun sendSelfExamIssueResultNotification(accounts: Set<Account>): String =
        sendPushNotification(NotificationDefinition.getSelfExamIssueResultNotification(accounts))

    private fun sendPushNotification(notification: PushNotification): String {
        val body = Gson().toJson(notification).toRequestBody()
        val request = Request.Builder()
            .addAuthenticationHeader()
            .addContentTypeHeader()
            .url(composeUrl("notifications"))
            .post(body)
            .build()

        notificationLogRepository.save(
            NotificationLog(
                name = notification.name,
                heading = notification.headings.cs,
                content = notification.contents.cs,
                includeExternalUserIds = notification.includeExternalUserIds.toString(),
                scheduleTimeOfDay = notification.scheduleTimeOfDay,
                delayedOption = notification.delayedOption,
                largeImage = notification.largeImage,
                iosAttachments = notification.iosAttachments.toString()
            )
        )

        val call: Call = OkHttpClient().newCall(request)
        return Gson().fromJson(call.execute().body!!.string(), NotificationResponse::class.java).id
    }

    private fun Request.Builder.addAuthenticationHeader(): Request.Builder =
        this.addHeader("Authorization", "Basic $ONESIGNAL_API_KEY")

    private fun Request.Builder.addContentTypeHeader(): Request.Builder =
        this.addHeader("Content-Type", "application/json; charset=utf-8")

    private fun composeUrl(endpoint: String): String = "$ONESIGNAL_API_URL/$API_VERSION/$endpoint"

    companion object {
        val ONESIGNAL_API_KEY: String = System.getenv().getOrDefault("ONESIGNAL_API_KEY", "")
        val ONESIGNAL_APP_ID: String = System.getenv().getOrDefault("ONESIGNAL_APP_ID", "")
        const val ONESIGNAL_API_URL = "https://onesignal.com/api"
        const val API_VERSION = "v1"
    }
}
