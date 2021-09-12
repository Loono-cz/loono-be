package cz.loono.backend.data.model

import org.hibernate.Hibernate
import java.time.LocalDate
import java.util.Objects
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "\"account\"")
data class Account(
    @Id
    @Column(nullable = false, columnDefinition = "TEXT")
    val uid: String = "",

    @Embedded
    val userAuxiliary: UserAuxiliary = UserAuxiliary(),

    @Embedded
    val settings: Settings = Settings(),

    @Column(nullable = false)
    val points: Int = 0,

    @OneToMany(orphanRemoval = true, cascade = [CascadeType.ALL], mappedBy = "account")
    @Column(nullable = false, updatable = true, insertable = true)
    val examinationRecords: List<ExaminationRecord> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Account

        @Suppress("SENSELESS_COMPARISON")
        return uid != null && uid == other.uid
    }

    override fun hashCode(): Int = 0

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(uid = $uid , userAuxiliary = $userAuxiliary , settings = $settings , points = $points )" // ktlint-disable max-line-length
    }
}

@Embeddable
data class Settings(
    @Column(nullable = false)
    val leaderboardAnonymizationOptIn: Boolean = true,

    @Column(nullable = false)
    val appointmentReminderEmailsOptIn: Boolean = true,

    @Column(nullable = false)
    val newsletterOptIn: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Settings

        return leaderboardAnonymizationOptIn == other.leaderboardAnonymizationOptIn &&
            appointmentReminderEmailsOptIn == other.appointmentReminderEmailsOptIn &&
            newsletterOptIn == other.newsletterOptIn
    }

    override fun hashCode(): Int =
        Objects.hash(leaderboardAnonymizationOptIn, appointmentReminderEmailsOptIn, newsletterOptIn)

    override fun toString(): String {
        return this::class.simpleName + "(leaderboardAnonymizationOptIn = $leaderboardAnonymizationOptIn , appointmentReminderEmailsOptIn = $appointmentReminderEmailsOptIn , newsletterOptIn = $newsletterOptIn )" // ktlint-disable max-line-length
    }
}

@Embeddable
data class UserAuxiliary(
    @Column(nullable = true, columnDefinition = "TEXT")
    val preferredEmail: String? = null,

    @Column(nullable = true, columnDefinition = "TEXT")
    val sex: String? = null,

    @Column(nullable = true)
    val birthdate: LocalDate? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as UserAuxiliary

        return preferredEmail == other.preferredEmail &&
            sex == other.sex &&
            birthdate == other.birthdate
    }

    override fun hashCode(): Int = Objects.hash(preferredEmail, sex, birthdate)

    override fun toString(): String {
        return this::class.simpleName + "(preferredEmail = $preferredEmail , sex = $sex , birthdate = $birthdate )"
    }
}
