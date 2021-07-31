package cz.loono.backend.data.model

import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "\"examination\"")
data class Examination(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(nullable = false, columnDefinition = "TEXT")
    val lastVisit: String = "",

    @Column(nullable = true)
    val date: LocalDate? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    val examinationType: String = "",

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User()
)
