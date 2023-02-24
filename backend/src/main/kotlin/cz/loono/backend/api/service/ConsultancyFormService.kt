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
import cz.loono.backend.db.model.ConsultancyLog
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.db.repository.ConsultancyLogRepository
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.io.IOException
import java.time.LocalDateTime

@Service
class ConsultancyFormService(
    private val accountRepository: AccountRepository,
    private val consultancyLogRepository: ConsultancyLogRepository
) {
    val gson = Gson()
    val client = OkHttpClient().newBuilder().addInterceptor(
        EmailInterceptor(
            EmailInterceptor.SMARTEMAILING_USER,
            EmailInterceptor.SMARTEMAILING_PSW
        )
    ).build()
    fun addContactToContactList() {
        val emailContactListModel = listOf(EmailContactListModel(id = 89, status = "confirmed"))
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
            } catch (e: Exception) {
                consultancyLogRepository.save(
                    ConsultancyLog(
                        accountUid = "USER_IMPORT",
                        message = "$e",
                        tag = "${e.cause}",
                        passed = false,
                        caughtException = "${e.message}",
                        createdAt = LocalDateTime.now().toString()
                    )
                )
                throw LoonoBackendException(
                    status = HttpStatus.SERVICE_UNAVAILABLE,
                    errorMessage = e.toString(),
                    errorCode = e.localizedMessage
                )
            }
        }
    }

    fun sendEmailQuestion(accountUuid: String, content: ConsultancyFormContentDto) {
        try {
            val user = accountRepository.findByUid(accountUuid)
            user?.let { userAccount ->
                content.message?.let { message ->
                    content.tag?.let { tag ->
                        sendEmailToUser(message, tag, userAccount)
                        sendEmailToDoctor(message, tag, userAccount)
                        consultancyLogRepository.save(
                            ConsultancyLog(
                                accountUid = accountUuid,
                                message = message,
                                tag = tag,
                                passed = true,
                                caughtException = null,
                                createdAt = LocalDateTime.now().toString()
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            throw LoonoBackendException(
                status = HttpStatus.SERVICE_UNAVAILABLE,
                errorMessage = e.toString(),
                errorCode = e.localizedMessage
            )
        }
    }

    fun sendEmailToUser(message: String, tag: String, userAccount: Account) {
        try {
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
                            EmailReplace(key = "email_body_text", content = message)
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
                    consultancyLogRepository.save(
                        ConsultancyLog(
                            accountUid = userAccount.uid,
                            message = message,
                            tag = tag,
                            passed = false,
                            caughtException = e.toString(),
                            createdAt = LocalDateTime.now().toString()
                        )
                    )
                    throw LoonoBackendException(
                        status = HttpStatus.SERVICE_UNAVAILABLE,
                        errorMessage = e.toString(),
                        errorCode = e.localizedMessage
                    )
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        consultancyLogRepository.save(
                            ConsultancyLog(
                                accountUid = userAccount.uid,
                                message = message,
                                tag = tag,
                                passed = false,
                                caughtException = response.body.toString(),
                                createdAt = LocalDateTime.now().toString()
                            )
                        )
                        throw LoonoBackendException(
                            status = HttpStatus.SERVICE_UNAVAILABLE,
                            errorMessage = response.body.toString(),
                            errorCode = response.code.toString()
                        )
                    }
                }
            })
        } catch (e: Exception) {
            consultancyLogRepository.save(
                ConsultancyLog(
                    accountUid = userAccount.uid,
                    message = message,
                    tag = tag,
                    passed = false,
                    caughtException = e.toString(),
                    createdAt = LocalDateTime.now().toString()
                )
            )
            throw LoonoBackendException(
                status = HttpStatus.SERVICE_UNAVAILABLE,
                errorMessage = e.toString(),
                errorCode = e.localizedMessage
            )
        }
    }

    fun sendEmailToDoctor(message: String, tag: String, userAccount: Account) {
        try {
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
                            EmailReplace(key = "email_body_text", content = message)
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
                    consultancyLogRepository.save(
                        ConsultancyLog(
                            accountUid = userAccount.uid,
                            message = message,
                            tag = tag,
                            passed = false,
                            caughtException = e.toString(),
                            createdAt = LocalDateTime.now().toString()
                        )
                    )
                    throw LoonoBackendException(
                        status = HttpStatus.SERVICE_UNAVAILABLE,
                        errorMessage = e.toString(),
                        errorCode = e.localizedMessage
                    )
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        consultancyLogRepository.save(
                            ConsultancyLog(
                                accountUid = userAccount.uid,
                                message = message,
                                tag = tag,
                                passed = false,
                                caughtException = response.body.toString(),
                                createdAt = LocalDateTime.now().toString()
                            )
                        )
                        throw LoonoBackendException(
                            status = HttpStatus.SERVICE_UNAVAILABLE,
                            errorMessage = response.body.toString(),
                            errorCode = response.code.toString()
                        )
                    }
                }
            })
        } catch (e: Exception) {
            consultancyLogRepository.save(
                ConsultancyLog(
                    accountUid = userAccount.uid,
                    message = message,
                    tag = tag,
                    passed = false,
                    caughtException = e.toString(),
                    createdAt = LocalDateTime.now().toString()
                )
            )
            throw LoonoBackendException(
                status = HttpStatus.SERVICE_UNAVAILABLE,
                errorMessage = e.toString(),
                errorCode = e.localizedMessage
            )
        }
    }
}
