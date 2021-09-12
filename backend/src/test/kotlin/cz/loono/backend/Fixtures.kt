package cz.loono.backend

import cz.loono.backend.api.BasicUser
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.data.model.Account
import cz.loono.backend.data.model.Settings
import cz.loono.backend.data.model.UserAuxiliary
import java.net.URL
import java.time.LocalDate

internal fun createBasicUser() = BasicUser(
    "uid",
    "zilvar@example.com",
    "Zilvar z chudobince",
    URL("https://example.com")
)

internal fun createAccount(uid: String = "uid") = Account(
    uid = uid,
    userAuxiliary = UserAuxiliary(
        "preferredZilvar@example.com",
        SexDto.MALE.name,
        LocalDate.of(2000, 1, 1)
    ),
    settings = Settings(
        leaderboardAnonymizationOptIn = false,
        appointmentReminderEmailsOptIn = false,
        newsletterOptIn = true
    ),
    points = 1000
)
