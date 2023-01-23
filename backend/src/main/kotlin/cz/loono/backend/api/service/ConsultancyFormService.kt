package cz.loono.backend.api.service

import com.google.gson.Gson
import cz.loono.backend.api.dto.ConsultancyFormContentDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.smartemailng.AddUserEmailModel
import cz.loono.backend.api.smartemailng.EmailContactInfoModel
import cz.loono.backend.api.smartemailng.EmailContactListModel
import cz.loono.backend.api.smartemailng.EmailInterceptor
import cz.loono.backend.api.smartemailng.EmailRecipient
import cz.loono.backend.api.smartemailng.EmailReplace
import cz.loono.backend.api.smartemailng.EmailSenderCredentials
import cz.loono.backend.api.smartemailng.EmailSettingsModel
import cz.loono.backend.api.smartemailng.EmailTasks
import cz.loono.backend.api.smartemailng.SendEmailModel
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.repository.AccountRepository
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class ConsultancyFormService(private val accountRepository: AccountRepository) {
    // .addInterceptor(EmailInterceptor("stepan.drozdek@cgi.com", "t5n8lc97atwg3qno129nsvsgnju0fp4mg2ejsd2x"))
    //  .addInterceptor(EmailInterceptor("poradna@loono.cz", "pceabbaif4utnwjefhb1galhg638qrys8u2w622o"))
    val gson = Gson()
    val client = OkHttpClient().newBuilder().addInterceptor(EmailInterceptor("poradna@loono.cz", "pceabbaif4utnwjefhb1galhg638qrys8u2w622o")).build()

    fun testApi() {
        val request = Request.Builder()
            .url("https://app.smartemailing.cz/api/v3/ping")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
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

        client.newCall(request).enqueue(object : Callback {
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
    }

    fun addContactToContactList() {
        val emailContactListModel = listOf(EmailContactListModel(id = 73))
        val emailContactInfoModelList = mutableListOf<EmailContactInfoModel>()
        val allAccounts = accountRepository.findAll()
        val allNewsletterAccounts = allAccounts.filter { it.newsletterOptIn }

        if (allNewsletterAccounts.isNotEmpty()) {
            try {
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
            } catch (e: Exception) {
                throw LoonoBackendException(
                    status = HttpStatus.SERVICE_UNAVAILABLE,
                    errorMessage = e.toString(),
                    errorCode = e.localizedMessage
                )
            }
        }
    }

    fun sendEmailQuestion(accountUuid: String, content: ConsultancyFormContentDto) {
        val user = accountRepository.findByUid(accountUuid)
        user?.let { userAccount ->
            content.message?.let { message ->
                sendEmailToUser(message, userAccount)
                content.tag?.let { tag ->
                    sendEmailToDoctor(message, tag, userAccount)
                }
            }
        }
    }
    fun sendEmailToUser(text: String, userAccount: Account) {
        val email = SendEmailModel(
            senderCredentials = EmailSenderCredentials(
                senderName = "Poradna Preventivka",
                from = "poradna@loono.cz",
                replyTo = "poradna@loono.cz"
            ),
            emailId = 314,
            tag = "BE_PORADNA_USER",
            tasks = listOf(
                EmailTasks(
                    recipient = EmailRecipient(emailAddress = userAccount.preferredEmail),
                    replace = listOf(
                        EmailReplace(key = "user_name", content = userAccount.nickname),
                        EmailReplace(key = "email_body_text", content = text)
                    )
                )
            )
        )

        val request = Request.Builder()
            .url("https://app.smartemailing.cz/api/v3/send/custom-emails-bulk")
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(email).toRequestBody())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
                throw LoonoBackendException(HttpStatus.SERVICE_UNAVAILABLE)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val res = response
                    val resBody = response.body
                    println(response.body)
                } else {
                    println(response.body)
                }
            }
        })
    }

    fun sendEmailToDoctor(text: String, tag: String, userAccount: Account) {
        val email = SendEmailModel(
            senderCredentials = EmailSenderCredentials(
                senderName = "Poradna Preventivka",
                from = "poradna@loono.cz",
                replyTo = "poradna@loono.cz"
            ),
            emailId = 317,
            tag = "BE_PORADNA_DOCTOR",
            tasks = listOf(
                EmailTasks(
                    recipient = EmailRecipient(emailAddress = "poradna@loono.cz"),
                    replace = listOf(
                        EmailReplace(key = "question_tag", content = tag),
                        EmailReplace(key = "user_name", content = userAccount.nickname),
                        EmailReplace(key = "user_age", content = "${userAccount.birthdate}"),
                        EmailReplace(key = "user_gender", content = userAccount.sex),
                        EmailReplace(key = "user_email", content = userAccount.preferredEmail),
                        EmailReplace(key = "email_body_text", content = text)
                    )
                )
            )
        )

        val request = Request.Builder()
            .url("https://app.smartemailing.cz/api/v3/send/custom-emails-bulk")
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(email).toRequestBody())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
                throw LoonoBackendException(HttpStatus.SERVICE_UNAVAILABLE)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
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
