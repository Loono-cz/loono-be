package cz.loono.backend.api.service

import com.google.gson.Gson
import cz.loono.backend.api.MultipleLangString
import cz.loono.backend.api.PushNotification
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.slf4j.LoggerFactory

class PushNotificationService {

    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        if (ONESIGNAL_API_KEY.isNullOrEmpty()) {
            logger.warn("ONESIGNAL_API_KEY ENV variable is not set.")
        }
    }

    fun sendPushNotification() {
        val body = Gson().toJson(
            PushNotification(
                appId = ONESIGNAL_APP_ID,
                name = MultipleLangString(cs = "Notifikace", en = "Notification name"),
                headings = MultipleLangString(cs = "Titulek", en = "Title"),
                contents = MultipleLangString(cs = "Obsah notifikace.", en = "Notification content."),
                includePlayerIds = listOf("60015e21-b25d-4eda-b677-151c6aed73d5")
            )
        ).toRequestBody()
        val request = Request.Builder()
            .addContentTypeHeader()
            .url(composeUrl("notifications"))
            .post(body)
            .build()

        val call: Call = OkHttpClient().newCall(request)
        val response: Response = call.execute()
        logger.info(response.body!!.string())
    }

    private fun Request.Builder.addAuthenticationHeader(): Request.Builder {
        return this.addHeader("Authorization", "Basic $ONESIGNAL_API_KEY")
    }

    private fun Request.Builder.addContentTypeHeader(): Request.Builder {
        return this.addHeader("Content-Type", "application/json; charset=utf-8")
    }

    private fun composeUrl(endpoint: String): String {
        return "$ONESIGNAL_API_URL/$API_VERSION/$endpoint"
    }

    companion object {
        val ONESIGNAL_API_KEY = System.getenv("ONESIGNAL_API_KEY")
        const val ONESIGNAL_APP_ID = "234d9f26-44c2-4752-b2d3-24bd93059267"
        const val ONESIGNAL_API_URL = "https://onesignal.com/api"
        const val API_VERSION = "v1"
    }
}
