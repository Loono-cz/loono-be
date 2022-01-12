package cz.loono.backend.api

import com.google.gson.annotations.SerializedName

data class PushNotification(
    @SerializedName("app_id")
    val appId: String,
    val name: MultipleLangString,
    val headings: MultipleLangString,
    val contents: MultipleLangString,
    @SerializedName("include_player_ids")
    val includePlayerIds: List<String>
)

data class MultipleLangString(
    val cs: String,
    val en: String
)
