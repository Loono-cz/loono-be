package cz.loono.backend.data.model

import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "\"user\"")
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(nullable = false)
    val salutation: String = "",

    @Column(nullable = false)
    val email: String = "",

    @Column(nullable = false)
    val notificationEmail: String = "",

    @Column(nullable = false)
    val sex: Char = '?',

    @Column(nullable = false)
    val birthDate: Date = Date()
)
