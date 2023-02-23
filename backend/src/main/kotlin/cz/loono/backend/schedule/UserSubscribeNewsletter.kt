package cz.loono.backend.schedule

import com.google.gson.Gson
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.smartemailng.AddUserEmailModel
import cz.loono.backend.api.smartemailng.EmailContactInfoModel
import cz.loono.backend.api.smartemailng.EmailContactListModel
import cz.loono.backend.api.smartemailng.EmailInterceptor
import cz.loono.backend.api.smartemailng.EmailSettingsModel
import cz.loono.backend.db.model.CronLog
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.CronLogRepository
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.LocalDate

@Component
class UserSubscribeNewsletter(
    private val accountRepository: AccountRepository,
    private val cronLogRepository: CronLogRepository
) : DailySchedulerTask {
    override fun run() {
        val gson = Gson()
        val client = OkHttpClient().newBuilder().addInterceptor(EmailInterceptor(
            EmailInterceptor.SMARTEMAILING_USER,
            EmailInterceptor.SMARTEMAILING_PSW
        )).build()
        val emailContactInfoModelList = mutableListOf<EmailContactInfoModel>()

        try {
            val now = LocalDate.now()
            val emailContactListModel = listOf(EmailContactListModel(id = 89))
            val allAccounts = accountRepository.findAll()
            val allNewsletterAccounts = allAccounts.filter { it.newsletterOptIn && it.created == now.minusDays(1) }

            if (allNewsletterAccounts.isNotEmpty()) {
                allNewsletterAccounts.forEach { account ->
                    if (emailContactInfoModelList.size % 400 == 0) {
                        val emailBody = AddUserEmailModel(
                            settings = EmailSettingsModel(update = true, skipInvalidEmails = true),
                            data = emailContactInfoModelList
                        )

                        val request = Request.Builder()
                            .url("https://app.smartemailing.cz/api/v3/import")
                            .addHeader("Content-Type", "application/json")
                            .post(gson.toJson(emailBody).toRequestBody())
                            .build()

                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                println(e)
                                throw LoonoBackendException(HttpStatus.SERVICE_UNAVAILABLE)
                            }

                            override fun onResponse(call: Call, response: Response) {
                                if (response.isSuccessful) {
                                    println(response.body)
                                } else {
                                    println(response.body)
                                }
                            }
                        })
                        emailContactInfoModelList.clear()
                    }

                    emailContactInfoModelList.add(
                        EmailContactInfoModel(
                            emailAddress = account.preferredEmail,
                            name = account.nickname,
                            contactLists = emailContactListModel
                        )
                    )
                }
                val emailBody = AddUserEmailModel(
                    settings = EmailSettingsModel(update = true, skipInvalidEmails = true),
                    data = emailContactInfoModelList
                )

                val request = Request.Builder()
                    .url("https://app.smartemailing.cz/api/v3/import")
                    .addHeader("Content-Type", "application/json")
                    .post(gson.toJson(emailBody).toRequestBody())
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        println(e)
                        throw LoonoBackendException(HttpStatus.SERVICE_UNAVAILABLE)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            println(response.body)
                        } else {
                            println(response.body)
                        }
                    }
                })
                emailContactInfoModelList.clear()
            }
            cronLogRepository.save(
                CronLog(
                    functionName = "UserSubscribeNewsletter",
                    status = "PASSED",
                    message = null,
                    createdAt = LocalDate.now().toString()
                )
            )
        } catch (e: Exception) {
            cronLogRepository.save(
                CronLog(
                    functionName = "UserSubscribeNewsletter",
                    status = "ERROR",
                    message = "$e",
                    createdAt = LocalDate.now().toString()
                )
            )
        }
    }
}
