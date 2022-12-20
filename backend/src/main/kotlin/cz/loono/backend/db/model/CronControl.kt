package cz.loono.backend.db.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "cron_control")
data class CronControl(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(columnDefinition = "TEXT")
    val message: String? = null,

    @Column(columnDefinition = "TEXT")
    val createdAt: String? = null,

    @Column(columnDefinition = "TEXT")
    val functionName: String? = null,

    @Column(columnDefinition = "TEXT")
    val status: String? = null
)
