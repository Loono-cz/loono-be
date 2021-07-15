package cz.loono.backend.data.model

import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "\"user\"")
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(nullable = false)
    val uid: String = "",

    @Column(nullable = false)
    val salutation: String = "",

    @Column(nullable = false)
    val email: String = "",

    @Column(nullable = true)
    val notificationEmail: String? = null,

    @Column(nullable = false)
    val sex: Char = '?',

    @Column(nullable = false)
    val birthdate: Date = Date(),

    @OneToMany(mappedBy = "user")
    val examinations: Set<Examination> = emptySet()
)
