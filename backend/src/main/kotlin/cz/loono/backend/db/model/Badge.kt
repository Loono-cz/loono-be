package cz.loono.backend.db.model

import cz.loono.backend.api.dto.BadgeTypeDto
import org.hibernate.envers.Audited
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "badge")
@Audited
@IdClass(BadgeCompositeKey::class)
data class Badge(
    @Id
    val type: String,
    @Id
    @Column(name = "account_id")
    val accountId: Long,
    val level: Int,
    @ManyToOne(optional = false)
    @JoinColumns(JoinColumn(name = "account_id", insertable = false, updatable = false))
    val account: Account,
    @Column(nullable = false)
    val lastUpdateOn: LocalDateTime = LocalDateTime.now()
) {
    fun getBadgeAsEnum() = BadgeTypeDto.valueOf(this.type)
}

/**
 * This class is needed to allow a composite key on the Badge class
 * I.e. type and accountId should be unique combination
 */
@Embeddable
data class BadgeCompositeKey(
    val type: String,
    val accountId: Long,
) : Serializable
