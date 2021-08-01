package cz.loono.backend.data.model

import java.time.LocalDate
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

    @Column(nullable = false, columnDefinition = "TEXT", unique = true)
    val uid: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val salutation: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val email: String = "",

    @Column(nullable = true, columnDefinition = "TEXT")
    val notificationEmail: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    val sex: String = "",

    @Column(nullable = false)
    val birthdate: LocalDate = LocalDate.EPOCH,

    @OneToMany(mappedBy = "user")
    val examinations: Set<Examination> = emptySet()
)
