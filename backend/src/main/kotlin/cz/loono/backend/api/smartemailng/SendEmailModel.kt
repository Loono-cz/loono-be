package cz.loono.backend.api.smartemailng

import com.google.gson.annotations.SerializedName

data class SendEmailModel(
    @SerializedName("sender_credentials") var senderCredentials: EmailSenderCredentials,
    @SerializedName("email_id") var emailId: Int,
    @SerializedName("tag") var tag: String,
    @SerializedName("tasks") var tasks: List<EmailTasks>
)

data class EmailSenderCredentials(
    @SerializedName("from") var from: String,
    @SerializedName("reply_to") var replyTo: String,
    @SerializedName("sender_name") var senderName: String,
)

data class EmailTasks(
    @SerializedName("recipient") var recipient: EmailRecipient,
    @SerializedName("replace") var replace: List<EmailReplace>
)

data class EmailRecipient(
    @SerializedName("emailaddress") val emailAddress: String
)

data class EmailReplace(
    @SerializedName("key") val key: String,
    @SerializedName("content") val content: String,
)
