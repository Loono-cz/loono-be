package cz.loono.backend.db.model

import org.hibernate.Hibernate
import org.hibernate.envers.Audited
import java.time.LocalDate
import java.util.Objects
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "\"account\"")
@Audited
data class Account(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(nullable = false, columnDefinition = "TEXT", unique = true)
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
    val newsletterOptIn: Boolean = false
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
        return this::class.simpleName + "(leaderboardAnonymizationOptIn = $leaderboardAnonymizationOptIn , appointmentReminderEmailsOptIn = $appointmentReminderEmailsOptIn , newsletterOptIn = $newsletterOptIn)" // ktlint-disable max-line-length
    }
}

@Embeddable
data class UserAuxiliary(

    @Column(nullable = true, columnDefinition = "TEXT")
    val nickname: String? = null,

    @Column(nullable = true, columnDefinition = "TEXT")
    val preferredEmail: String? = null,

    @Column(nullable = true, columnDefinition = "TEXT")
    val sex: String? = null,

    @Column(nullable = true)
    val birthdate: LocalDate? = null,

    @Column(columnDefinition = "TEXT")
    val profileImageUrl: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserAuxiliary

        if (nickname != other.nickname) return false
        if (preferredEmail != other.preferredEmail) return false
        if (sex != other.sex) return false
        if (birthdate != other.birthdate) return false
        if (profileImageUrl != other.profileImageUrl) return false

        return true
    }

    override fun hashCode(): Int =
        Objects.hash(nickname, preferredEmail, sex, birthdate, profileImageUrl)

    override fun toString(): String {
        return this::class.simpleName + "(nickname=$nickname, preferredEmail=$preferredEmail, sex=$sex, birthdate=$birthdate, profileImageUrl=$profileImageUrl)"
    }
}
