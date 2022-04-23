package cz.loono.backend.db.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "notification_log")
data class NotificationLog(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(columnDefinition = "TEXT")
    val name: String? = null,

    @Column(columnDefinition = "TEXT")
    val heading: String? = null,

    @Column(columnDefinition = "TEXT")
    val content: String? = null,

    @Column(columnDefinition = "TEXT")
    val includeExternalUserIds: String? = null,

    @Column(columnDefinition = "TEXT")
    val scheduleTimeOfDay: String? = null,

    @Column(columnDefinition = "TEXT")
    val delayedOption: String? = null,

    @Column(columnDefinition = "TEXT")
    val largeImage: String? = null,

    @Column(columnDefinition = "TEXT")
    val iosAttachments: String? = null,
)
