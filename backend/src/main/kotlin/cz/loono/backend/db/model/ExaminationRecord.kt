package cz.loono.backend.db.model

import org.hibernate.Hibernate
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "\"examination_record\"")
data class ExaminationRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    val type: String = "",

    @Column(nullable = true)
    val lastVisit: LocalDate? = null,

    @ManyToOne(optional = false)
    val account: Account = Account(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as ExaminationRecord

        @Suppress("SENSELESS_COMPARISON")
        return id != null && id == other.id
    }

    override fun hashCode(): Int = 0

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , type = $type , lastVisit = $lastVisit , accountId = ${account.uid} )" // ktlint-disable max-line-ength
    }
}
