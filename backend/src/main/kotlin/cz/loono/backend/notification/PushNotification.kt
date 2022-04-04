package cz.loono.backend.notification

import com.google.gson.annotations.SerializedName
import cz.loono.backend.api.dto.ExaminationTypeDto

data class PushNotification(
    @SerializedName("app_id")
    val appId: String,
    val name: String,
    val headings: MultipleLangString,
    val contents: MultipleLangString,
    @SerializedName("include_external_user_ids")
    val includeExternalUserIds: List<String>,
    @SerializedName("delivery_time_of_day")
    val scheduleTimeOfDay: String,
    @SerializedName("delayed_option")
    val delayedOption: String = "timezone",
    val data: NotificationData,
    // Android large icon
    @SerializedName("large_icon")
    val largeImage: String? = null,
    // Android large icon
    @SerializedName("ios_attachments")
    val iosAttachments: NotificationAttachment? = null,
)

data class NotificationAttachment(
    val image: String
)

data class MultipleLangString(
    val cs: String,
    val en: String
)

data class NotificationData(
    val screen: String,
    val examinationType: ExaminationTypeDto? = null
)
