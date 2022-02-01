package cz.loono.backend

import cz.loono.backend.api.BasicUser
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.Settings
import cz.loono.backend.db.model.UserAuxiliary
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
        nickname = "Zilvar z chudobince",
        preferredEmail = "preferredZilvar@example.com",
        sex = SexDto.MALE.name,
        birthdate = LocalDate.of(2000, 1, 1),
        profileImageUrl = "https://example.com"
    ),
    settings = Settings(
        leaderboardAnonymizationOptIn = false,
        appointmentReminderEmailsOptIn = false,
        newsletterOptIn = true
    ),
    points = 1000
)
