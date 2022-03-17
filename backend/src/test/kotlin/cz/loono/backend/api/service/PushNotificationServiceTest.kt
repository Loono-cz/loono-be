package cz.loono.backend.api.service

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import cz.loono.backend.api.dto.BadgeTypeDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.createAccount
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.notification.NotificationData
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDate

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class PushNotificationServiceTest(
    private val accountRepository: AccountRepository
) {

    private val pushNotificationService = PushNotificationService()

    companion object {

        private val notifications = mutableListOf<String>()

        @AfterAll
        @JvmStatic
        private fun deleteMessages() {
            notifications.forEach {
                OneSignalTestClient.deleteNotification(it)
            }
        }
    }

    @Test
    fun `prevention notification`() {
        val account = createAccount()

        val notificationId = pushNotificationService.sendPreventionNotification(setOf(account))

        notifications.add(notificationId)
        val storedNotification = OneSignalTestClient.viewNotification(notificationId)
        assertEquals("Hola, hola, prevence volá!", storedNotification.headings.cs)
        assertEquals("Mrkni, na které preventivní prohlídky se objednat.", storedNotification.contents.cs)
        assertEquals(NotificationData(screen = "main"), storedNotification.data)
        assertEquals("timezone", storedNotification.delayed_option)
        assertEquals("8:00AM", storedNotification.delivery_time_of_day)
    }

    @Test
    fun `coming visit notification`() {
        val account = createAccount()

        val notificationId =
            pushNotificationService.sendComingVisitNotification(setOf(account), ExaminationTypeDto.DENTIST, "7:45")

        notifications.add(notificationId)
        val storedNotification = OneSignalTestClient.viewNotification(notificationId)
        assertEquals("Zítra tě čeká prohlídka", storedNotification.headings.cs)
        assertEquals("Za 24 hodin jdeš k zubaři na preventivní prohlídku.", storedNotification.contents.cs)
        assertEquals(
            NotificationData(screen = "checkup", examinationType = ExaminationTypeDto.DENTIST),
            storedNotification.data
        )
        assertEquals("timezone", storedNotification.delayed_option)
        assertEquals("7:45", storedNotification.delivery_time_of_day)
    }

    @Test
    fun `confirmation notification`() {
        val account = createAccount()

        val notificationId =
            pushNotificationService.sendCompletionNotification(setOf(account), "7:45", ExaminationTypeDto.DENTIST)

        notifications.add(notificationId)
        val storedNotification = OneSignalTestClient.viewNotification(notificationId)
        assertEquals("Byl/a jsi na prohlídce?", storedNotification.headings.cs)
        assertEquals("Potvrď preventivní prohlídku v aplikaci a získej odměnu.", storedNotification.contents.cs)
        assertEquals(
            NotificationData(screen = "checkup", examinationType = ExaminationTypeDto.DENTIST),
            storedNotification.data
        )
        assertEquals("timezone", storedNotification.delayed_option)
        assertEquals("7:45", storedNotification.delivery_time_of_day)
    }

    @Test
    fun `new exam a month ahead notification`() {
        val account = createAccount()

        val notificationId =
            pushNotificationService.sendNewExamMonthAheadNotificationToOrder(
                setOf(account),
                ExaminationTypeDto.DENTIST,
                2,
                BadgeTypeDto.GLASSES
            )

        notifications.add(notificationId)
        val storedNotification = OneSignalTestClient.viewNotification(notificationId)
        assertEquals("Nejvyšší čas zajít k zubaři", storedNotification.headings.cs)
        assertEquals(
            "Jsou/je to už 2 roky od poslední prohlídky. Objednej se ještě dnes, ať neztratíš brýle.",
            storedNotification.contents.cs
        )
        assertEquals(
            NotificationData(screen = "checkup", examinationType = ExaminationTypeDto.DENTIST),
            storedNotification.data
        )
        assertEquals("timezone", storedNotification.delayed_option)
        assertEquals("8:00AM", storedNotification.delivery_time_of_day)
    }

    @Test
    fun `new exam 2 months ahead notification`() {
        val account = createAccount()

        val notificationId =
            pushNotificationService.sendNewExam2MonthsAheadNotificationToOrder(
                setOf(account),
                ExaminationTypeDto.DENTIST,
                2
            )

        notifications.add(notificationId)
        val storedNotification = OneSignalTestClient.viewNotification(notificationId)
        assertEquals("Objednej se k zubaři", storedNotification.headings.cs)
        assertEquals(
            "2 roky utekl/y jako voda, je čas se objednat na preventivní prohlídku.",
            storedNotification.contents.cs
        )
        assertEquals(
            NotificationData(screen = "checkup", examinationType = ExaminationTypeDto.DENTIST),
            storedNotification.data
        )
        assertEquals("timezone", storedNotification.delayed_option)
        assertEquals("8:00AM", storedNotification.delivery_time_of_day)
    }

    @Test
    fun `first self-exam notification`() {
        val account = createAccount()

        val notificationId =
            pushNotificationService.sendFirstSelfExamNotification(setOf(account), SexDto.valueOf(account.sex))

        notifications.add(notificationId)
        val storedNotification = OneSignalTestClient.viewNotification(notificationId)
        assertEquals("První samovyšetření čeká", storedNotification.headings.cs)
        assertEquals("Zvládne ho opravu každý a nezabere to ani pět minut.", storedNotification.contents.cs)
        assertEquals(NotificationData(screen = "self"), storedNotification.data)
        assertEquals("timezone", storedNotification.delayed_option)
        assertEquals("6:00PM", storedNotification.delivery_time_of_day)
    }

    @Test
    fun `self-exam notification`() {
        val account = createAccount()

        val notificationId =
            pushNotificationService.sendSelfExamNotification(setOf(account), SexDto.valueOf(account.sex))

        notifications.add(notificationId)
        val storedNotification = OneSignalTestClient.viewNotification(notificationId)
        assertEquals("Je čas na samovyšetření", storedNotification.headings.cs)
        assertEquals("Po měsíci přišel čas si sáhnout na varlata.", storedNotification.contents.cs)
        assertEquals(NotificationData(screen = "self"), storedNotification.data)
        assertEquals("timezone", storedNotification.delayed_option)
        assertEquals("6:00PM", storedNotification.delivery_time_of_day)
    }

    @Test
    fun `self-exam issue result notification`() {
        val account = createAccount()

        val notificationId =
            pushNotificationService.sendSelfExamIssueResultNotification(setOf(account), SexDto.valueOf(account.sex))

        notifications.add(notificationId)
        val storedNotification = OneSignalTestClient.viewNotification(notificationId)
        assertEquals("Máš varlata zdravá?", storedNotification.headings.cs)
        assertEquals(
            "Před časem se ti na varlatech něco nezdálo. Jak dopadla prohlídka u lékaře?",
            storedNotification.contents.cs
        )
        assertEquals(NotificationData(screen = "self"), storedNotification.data)
        assertEquals("timezone", storedNotification.delayed_option)
        assertEquals("6:00PM", storedNotification.delivery_time_of_day)
    }

    private fun createAccount() = accountRepository.save(
        createAccount(
            uid = "tXHYX4ZisxcttkHH5DTROg2yTlv2",
            sex = SexDto.MALE.value,
            birthday = LocalDate.of(1990, 9, 9)
        )
    )
}

class OneSignalTestClient {

    companion object {

        private val ONESIGNAL_API_KEY: String = System.getenv("ONESIGNAL_API_KEY")

        fun viewNotification(id: String): NotificationInfo {
            val request = Request.Builder().addHeader(
                "Authorization",
                "Basic $ONESIGNAL_API_KEY"
            ).url("https://onesignal.com/api/v1/notifications/$id?app_id=234d9f26-44c2-4752-b2d3-24bd93059267").get()
                .build()

            return Gson().fromJson(
                OkHttpClient().newCall(request).execute().body!!.string(),
                NotificationInfo::class.java
            )
        }

        fun deleteNotification(id: String) {
            val request = Request.Builder().addHeader(
                "Authorization",
                "Basic $ONESIGNAL_API_KEY"
            ).url("https://onesignal.com/api/v1/notifications/$id?app_id=234d9f26-44c2-4752-b2d3-24bd93059267")
                .delete()
                .build()

            OkHttpClient().newCall(request).execute()
        }
    }
}

data class NotificationInfo(
    @SerializedName("adm_big_picture") val adm_big_picture: String,
    @SerializedName("adm_group") val adm_group: String,
    @SerializedName("adm_group_message") val adm_groupMessage: AdmGroupMessage,
    @SerializedName("adm_large_icon") val adm_large_icon: String,
    @SerializedName("adm_small_icon") val adm_small_icon: String,
    @SerializedName("adm_sound") val adm_sound: String,
    @SerializedName("spoken_text") val spoken_text: SpokenText,
    @SerializedName("alexa_ssml") val alexa_ssml: String,
    @SerializedName("alexa_display_title") val alexa_display_title: String,
    @SerializedName("amazon_background_data") val amazon_background_data: Boolean,
    @SerializedName("android_accent_color") val android_accent_color: String,
    @SerializedName("android_group") val android_group: String,
    @SerializedName("android_group_message") val android_groupMessage: AndroidGroupMessage,
    @SerializedName("android_led_color") val android_led_color: String,
    @SerializedName("android_sound") val android_sound: String,
    @SerializedName("android_visibility") val android_visibility: Int,
    @SerializedName("app_id") val app_id: String,
    @SerializedName("big_picture") val big_picture: String,
    @SerializedName("buttons") val buttons: String,
    @SerializedName("canceled") val canceled: Boolean,
    @SerializedName("chrome_big_picture") val chrome_big_picture: String,
    @SerializedName("chrome_icon") val chrome_icon: String,
    @SerializedName("chrome_web_icon") val chrome_web_icon: String,
    @SerializedName("chrome_web_image") val chrome_web_image: String,
    @SerializedName("chrome_web_badge") val chrome_web_badge: String,
    @SerializedName("content_available") val content_available: Boolean,
    @SerializedName("name") val name: String,
    @SerializedName("contents") val contents: Contents,
    @SerializedName("converted") val converted: Int,
    @SerializedName("data") val data: NotificationData,
    @SerializedName("delayed_option") val delayed_option: String,
    @SerializedName("delivery_time_of_day") val delivery_time_of_day: String,
    @SerializedName("errored") val errored: Int,
    @SerializedName("excluded_segments") val excluded_segments: List<String>,
    @SerializedName("failed") val failed: Int,
    @SerializedName("firefox_icon") val firefox_icon: String,
    @SerializedName("global_image") val global_image: String,
    @SerializedName("headings") val headings: Headings,
    @SerializedName("id") val id: String,
    @SerializedName("include_player_ids") val include_player_ids: List<String>,
    @SerializedName("include_external_user_ids") val include_external_user_ids: List<String>,
    @SerializedName("included_segments") val included_segments: List<String>,
    @SerializedName("thread_id") val thread_id: String,
    @SerializedName("ios_badgeCount") val ios_badgeCount: Int,
    @SerializedName("ios_badgeType") val ios_badgeType: String,
    @SerializedName("ios_category") val ios_category: String,
    @SerializedName("ios_interruption_level") val ios_interruption_level: String,
    @SerializedName("ios_relevance_score") val ios_relevance_score: Int,
    @SerializedName("ios_sound") val ios_sound: String,
    @SerializedName("apns_alert") val apns_alert: ApnsAlert,
    @SerializedName("isAdm") val isAdm: Boolean,
    @SerializedName("isAndroid") val isAndroid: Boolean,
    @SerializedName("isChrome") val isChrome: Boolean,
    @SerializedName("isChromeWeb") val isChromeWeb: Boolean,
    @SerializedName("isAlexa") val isAlexa: Boolean,
    @SerializedName("isFirefox") val isFirefox: Boolean,
    @SerializedName("isIos") val isIos: Boolean,
    @SerializedName("isSafari") val isSafari: Boolean,
    @SerializedName("isWP") val isWP: Boolean,
    @SerializedName("isWP_WNS") val isWP_WNS: Boolean,
    @SerializedName("isEdge") val isEdge: Boolean,
    @SerializedName("large_icon") val large_icon: String,
    @SerializedName("priority") val priority: Int,
    @SerializedName("queued_at") val queued_at: Int,
    @SerializedName("remaining") val remaining: Int,
    @SerializedName("send_after") val send_after: Int,
    @SerializedName("completed_at") val completed_at: Int,
    @SerializedName("small_icon") val small_icon: String,
    @SerializedName("successful") val successful: Int,
    @SerializedName("received") val received: Int,
    @SerializedName("tags") val tags: String,
    @SerializedName("filters") val filters: String,
    @SerializedName("template_id") val template_id: String,
    @SerializedName("ttl") val ttl: Int,
    @SerializedName("url") val url: String,
    @SerializedName("web_url") val web_url: String,
    @SerializedName("app_url") val app_url: String,
    @SerializedName("web_buttons") val web_buttons: String,
    @SerializedName("web_push_topic") val web_push_topic: String,
    @SerializedName("wp_sound") val wp_sound: String,
    @SerializedName("wp_wns_sound") val wp_wns_sound: String,
    @SerializedName("platform_delivery_stats") val platform_deliveryStats: PlatformDeliveryStats,
    @SerializedName("ios_attachments") val ios_attachments: IosAttachments,
    @SerializedName("throttle_rate_per_minute") val throttle_rate_per_minute: String
)

class SpokenText

class ApnsAlert

data class AdmGroupMessage(
    @SerializedName("en") val en: String,
    @SerializedName("cs") val cs: String
)

data class Android(
    @SerializedName("successful") val successful: Int,
    @SerializedName("errored") val errored: Int,
    @SerializedName("failed") val failed: Int,
    @SerializedName("converted") val converted: Int,
    @SerializedName("received") val received: Int
)

data class AndroidGroupMessage(
    @SerializedName("en") val en: String,
    @SerializedName("cs") val cs: String
)

data class Contents(
    @SerializedName("en") val en: String,
    @SerializedName("cs") val cs: String
)

data class Headings(
    @SerializedName("en") val en: String,
    @SerializedName("cs") val cs: String
)

data class Ios(
    @SerializedName("successful") val successful: Int,
    @SerializedName("errored") val errored: Int,
    @SerializedName("failed") val failed: Int,
    @SerializedName("converted") val converted: Int,
    @SerializedName("received") val received: Int
)

data class IosAttachments(
    @SerializedName("id") val id: String
)

data class PlatformDeliveryStats(
    @SerializedName("android") val android: Android,
    @SerializedName("ios") val ios: Ios
)
