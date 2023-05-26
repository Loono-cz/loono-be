package cz.loono.backend.api.smartemailng

import com.google.gson.annotations.SerializedName

data class AddUserEmailModel(
    @SerializedName("settings") var settings: EmailSettingsModel,
    @SerializedName("data") var data: List<EmailContactInfoModel>
)

data class EmailSettingsModel(
    @SerializedName("update") var update: Boolean,
    @SerializedName("skip_invalid_emails") var skipInvalidEmails: Boolean,
    @SerializedName("double_opt_in_settings" ) var doubleOptInSettings  : DoubleOptInSettings? = DoubleOptInSettings()
)

data class EmailContactInfoModel(
    @SerializedName("emailaddress") var emailAddress: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("contactlists") var contactLists: List<EmailContactListModel>
)

data class EmailContactListModel(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("status") var status: String = "confirmed"
)

data class DoubleOptInSettings (
    @SerializedName("campaign"       ) var campaign      : Campaign?      = Campaign(),
    @SerializedName("send_to_mode"   ) var sendToMode    : String?        = null,
    @SerializedName("silence_period" ) var silencePeriod : SilencePeriod? = SilencePeriod()
)

data class SenderCredentials (
    @SerializedName("from"        ) var from       : String? = null,
    @SerializedName("reply_to"    ) var replyTo    : String? = null,
    @SerializedName("sender_name" ) var senderName : String? = null
)

data class Campaign (
    @SerializedName("email_id"                        ) var emailId                     : Int?               = null,
    @SerializedName("sender_credentials"              ) var senderCredentials           : SenderCredentials? = SenderCredentials(),
    @SerializedName("confirmation_thank_you_page_url" ) var confirmationThankYouPageUrl : String?            = null
)

data class SilencePeriod (
    @SerializedName("unit"  ) var unit  : String? = null,
    @SerializedName("value" ) var value : Int?    = null
)