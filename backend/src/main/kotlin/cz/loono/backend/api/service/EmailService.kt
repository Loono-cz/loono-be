package cz.loono.backend.api.service

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import cz.loono.backend.api.email.*
import cz.loono.backend.api.exception.LoonoBackendException
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class EmailService {
    // .addInterceptor(EmailInterceptor("stepan.drozdek@cgi.com", "t5n8lc97atwg3qno129nsvsgnju0fp4mg2ejsd2x"))
    val gson = Gson()
    val client = OkHttpClient()
                    .newBuilder()
                        .addInterceptor(EmailInterceptor("stepan.drozdek@cgi.com", "pceabbaif4utnwjefhb1galhg638qrys8u2w622o"))
                        .build()

    fun testApi(){
        val request = Request.Builder()
            .url("https://app.smartemailing.cz/api/v3/ping")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                println(e)
                throw LoonoBackendException(HttpStatus.SERVICE_UNAVAILABLE)
            }

            override fun onResponse(call: Call, response: Response) {
                println(response.body)
            }
        })
    }

    fun testLogin() {
        val request = Request.Builder()
            .url("https://app.smartemailing.cz/api/v3/check-credentials")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                println(e)
                throw LoonoBackendException(HttpStatus.SERVICE_UNAVAILABLE)
            }

            override fun onResponse(call: Call, response: Response) {
                println(response.body)
            }
        })
    }

    fun getContactList() {
        val request = Request.Builder()
            .url("https://app.smartemailing.cz/api/v3/contactlists/73")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                println(e)
                throw LoonoBackendException(HttpStatus.SERVICE_UNAVAILABLE)
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful){
                    println(response.body)
                } else {
                    println(response.body)
                }
            }
        })
    }

    fun addContactToContactList(){
        val emailContactListModel = listOf(EmailContactListModel(id = 73))

        val emailBody = AddUserEmailModel(
            settings = EmailSettingsModel(update = true, skipInvalidEmails = true),
            data = listOf(
                EmailContactInfoModel(
                    emailAddress = "testAddEmail@test.com",
                    name = "Test Testovaci",
                    contactLists = emailContactListModel)
            )
        )

        val request = Request.Builder()
            .url("https://app.smartemailing.cz/api/v3/import")
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(emailBody).toRequestBody())
            .build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                println(e)
                throw LoonoBackendException(HttpStatus.SERVICE_UNAVAILABLE)
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful){
                    val res = response
                    val resBody = response.body
                    println(response.body)
                } else {
                    println(response.body)
                }
            }
        })
    }
}