package cz.loono.backend.api.email

import com.google.gson.annotations.SerializedName

data class AddUserEmailModel(
    @SerializedName("settings") var settings: EmailSettingsModel,
    @SerializedName("data") var data: List<EmailContactInfoModel>
)

data class EmailSettingsModel(
    @SerializedName("update") var update: Boolean,
    @SerializedName("skip_invalid_emails") var skipInvalidEmails: Boolean
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
