package cz.loono.backend.db.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.envers.Audited
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@IdClass(HealthcareProviderId::class)
@Table(name = "healthcare_provider")
@Audited
data class HealthcareProvider(

    @Id
    @Column(name = "location_id", nullable = false)
    val locationId: Long = 0,

    @Id
    @Column(name = "institution_id", nullable = false)
    val institutionId: Long = 0,

    @Column(nullable = false, columnDefinition = "TEXT")
    val code: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val title: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val institutionType: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val city: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val postalCode: String = "",

    @Column(columnDefinition = "TEXT")
    val street: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    val houseNumber: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val region: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val regionCode: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val district: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val districtCode: String = "",

    @Column(columnDefinition = "TEXT")
    val administrativeDistrict: String? = null,

    @Column(columnDefinition = "TEXT")
    val phoneNumber: String? = null,

    @Column(columnDefinition = "TEXT")
    val fax: String? = null,

    @Column(columnDefinition = "TEXT")
    val email: String? = null,

    @Column(columnDefinition = "TEXT")
    val website: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    val ico: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val personTypeCode: String = "",

    @Column(columnDefinition = "TEXT")
    val lawyerFormCode: String? = null,

    @Column(columnDefinition = "TEXT")
    val layerForm: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    val personType: String = "",

    @Column(columnDefinition = "TEXT")
    val hqRegion: String? = null,

    @Column(columnDefinition = "TEXT")
    val hqRegionCode: String? = null,

    @Column(columnDefinition = "TEXT")
    val hqDistrict: String? = null,

    @Column(columnDefinition = "TEXT")
    val hqDistrictCode: String? = null,

    @Column(columnDefinition = "TEXT")
    val hqCity: String? = null,

    @Column(columnDefinition = "TEXT")
    val hqPostalCode: String? = null,

    @Column(columnDefinition = "TEXT")
    val hqStreet: String? = null,

    @Column(columnDefinition = "TEXT")
    val hqHouseNumber: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    val specialization: String? = null,

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinTable(
        name = "healthcare_provider_category",
        joinColumns = [
            JoinColumn(name = "location_id", referencedColumnName = "location_id"),
            JoinColumn(name = "institution_id", referencedColumnName = "institution_id")
        ],
        inverseJoinColumns = [JoinColumn(name = "id", referencedColumnName = "id")]
    )
    val category: Set<HealthcareCategory> = mutableSetOf(),

    @Column(columnDefinition = "TEXT")
    val careForm: String? = null,

    @Column(columnDefinition = "TEXT")
    val careType: String? = null,

    @Column(columnDefinition = "TEXT")
    val substitute: String? = null,

    @Column
    val lat: Double? = null,

    @Column
    val lng: Double? = null,

    // Data correction fields
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinTable(
        name = "corrected_healthcare_provider_category",
        joinColumns = [
            JoinColumn(name = "location_id", referencedColumnName = "location_id"),
            JoinColumn(name = "institution_id", referencedColumnName = "institution_id")
        ],
        inverseJoinColumns = [JoinColumn(name = "id", referencedColumnName = "id")]
    )
    val correctedCategory: Set<HealthcareCategory> = mutableSetOf(),

    @Column(columnDefinition = "TEXT")
    val correctedPhoneNumber: String? = null,

    @Column(columnDefinition = "TEXT")
    val correctedWebsite: String? = null,

    @Column
    val correctedLat: Double? = null,

    @Column
    val correctedLng: Double? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HealthcareProvider

        if (locationId != other.locationId) return false
        if (institutionId != other.institutionId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = locationId.hashCode()
        result = 31 * result + institutionId.hashCode()
        return result
    }

    override fun toString(): String =
        "$locationId,$institutionId,$code,$title,$institutionType,$city,$postalCode,$street,$houseNumber,$region," +
            "$regionCode,$district,$districtCode,$administrativeDistrict,$phoneNumber,$fax,$email,$website,$ico," +
            "$personTypeCode,$lawyerFormCode,$layerForm,$personType,$hqRegion,$hqRegionCode,$hqDistrict,$hqDistrictCode," +
            "$hqCity,$hqPostalCode,$hqStreet,$hqHouseNumber,$specialization,$category,$careForm,$careType,$substitute," +
            "$lat,$lng"
}
