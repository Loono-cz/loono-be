package cz.loono.backend.db.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "consultancy_log")
data class ConsultancyLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(columnDefinition = "TEXT")
    val accountUid: String? = null,

    @Column(columnDefinition = "TEXT")
    val tag: String? = null,

    @Column(columnDefinition = "TEXT")
    val message: String? = null,

    @Column(columnDefinition = "TEXT")
    val createdAt: String? = null,

    @Column
    val passed: Boolean = true,

    @Column(columnDefinition = "TEXT")
    val caughtException: String? = null
)
