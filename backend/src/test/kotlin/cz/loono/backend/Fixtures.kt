package cz.loono.backend

import cz.loono.backend.api.BasicUser
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.db.model.Account
import java.net.URL
import java.time.LocalDate

internal fun createBasicUser(uid: String = "uid") = BasicUser(
    uid,
    "zilvar@example.com",
    "Zilvar z chudobince",
    URL("https://example.com")
)

internal fun createAccount(
    uid: String = "uid",
    sex: String = SexDto.MALE.name,
    birthday: LocalDate = LocalDate.of(2000, 1, 1),
    points: Int = 0,
    nickname: String = "Zilvar z chudobince",
    profileImageUrl: String = "https://example.com"
) = Account(
    uid = uid,
    nickname = nickname,
    preferredEmail = "preferredZilvar@example.com",
    sex = sex,
    birthdate = birthday,
    profileImageUrl = profileImageUrl,
    leaderboardAnonymizationOptIn = false,
    appointmentReminderEmailsOptIn = false,
    newsletterOptIn = true,
    points = points
)
