package cz.loono.backend.db.model

import org.hibernate.envers.Audited
import java.time.LocalDate
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "account")
@Audited
data class Account(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, columnDefinition = "TEXT", unique = true)
    val uid: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val nickname: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val preferredEmail: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val sex: String,

    @Column(nullable = false)
    val birthdate: LocalDate,

    @Column(columnDefinition = "TEXT")
    val profileImageUrl: String? = null,

    @Column(nullable = false)
    val leaderboardAnonymizationOptIn: Boolean = true,

    @Column(nullable = false)
    val appointmentReminderEmailsOptIn: Boolean = true,

    @Column(nullable = false)
    val newsletterOptIn: Boolean = false,

    @Column(nullable = false)
    val points: Int = 0,

    @OneToMany(orphanRemoval = false, cascade = [CascadeType.ALL], mappedBy = "account", fetch = FetchType.EAGER)
    @Column(nullable = false, updatable = true, insertable = true)
    val examinationRecords: List<ExaminationRecord> = mutableListOf(),

    @OneToMany(orphanRemoval = false, cascade = [CascadeType.ALL], mappedBy = "account", fetch = FetchType.EAGER)
    @Column(nullable = true, updatable = true, insertable = true)
    val badges: Set<Badge> = mutableSetOf(),

    @Column(nullable = false)
    val created: LocalDate = LocalDate.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
        "Account(id=$id, uid='$uid', nickname=$nickname, preferredEmail=$preferredEmail, sex='$sex', " +
            "birthdate=$birthdate, profileImageUrl=$profileImageUrl, " +
            "leaderboardAnonymizationOptIn=$leaderboardAnonymizationOptIn, " +
            "appointmentReminderEmailsOptIn=$appointmentReminderEmailsOptIn, " +
            "newsletterOptIn=$newsletterOptIn, points=$points, examinationRecords=$examinationRecords)"
}
// psql -h database.internal.loono.ceskodigital.net -p 5432 -U loono -d loonodevelopment -f data_dump.sql\
