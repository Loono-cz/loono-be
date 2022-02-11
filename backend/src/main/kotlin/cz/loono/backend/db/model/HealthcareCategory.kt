package cz.loono.backend.db.model

import org.hibernate.envers.Audited
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "healthcare_category")
@Audited
data class HealthcareCategory(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(nullable = false, columnDefinition = "TEXT")
    val value: String = "",

    @ManyToMany(mappedBy = "category")
    val healthcareProviders: Set<HealthcareProvider> = mutableSetOf()
)
