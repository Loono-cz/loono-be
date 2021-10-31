package cz.loono.backend.db.model

import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "\"server_properties\"")
data class ServerProperties(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(nullable = false, columnDefinition = "TEXT")
    val superUserName: String = "loonoAdmin",

    @Column(nullable = false, columnDefinition = "TEXT")
    // The password is known by Loono administrators. Just ask.
    val superUserPassword: String = "\$2a\$10\$hx6i9opda20rbC81fJqUj.3mE.xZDB5OV5fApv9WlyEnkFNbFZUh2",

    @Column(nullable = false)
    var lastUpdate: LocalDate = LocalDate.now()
)
