package cz.loono.backend.db.repository

import cz.loono.backend.db.model.HealthcareProvider
import cz.loono.backend.db.model.HealthcareProviderId
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface HealthcareProviderRepository : PagingAndSortingRepository<HealthcareProvider, HealthcareProviderId> {
    @Transactional
    @Modifying
    @Query(
        "UPDATE HealthcareProvider h set " +
        "h.title = :title, h.institutionType = :institutionType, h.city = :city, " +
        "h.postalCode = :postalCode, h.street = :street, h.houseNumber = :houseNumber, " +
        "h.region = :region, h.district = :district, h.correctedPhoneNumber = :correctedPhoneNumber, " +
        "h.email = :email, h.correctedWebsite = :correctedWebsite, h.ico = :ico, " +
        "h.hqCity = :hqCity, h.hqDistrict = :hqDistrict, h.hqHouseNumber = :hqHouseNumber, " +
        "h.hqPostalCode = :hqPostalCode, h.hqRegion = :hqRegion, h.hqStreet = :hqStreet, " +
        "h.specialization = :specialization, h.careForm = :careForm, h.correctedLat = :correctedLat, " +
        "h.correctedLng = :correctedLng, h.categories = :categories " +
        "where h.locationId = :locationId and h.institutionId = :institutionId "
    )
    fun updateProvider(
        title: String,
        institutionType: String,
        city: String,
        postalCode: String,
        street: String?,
        houseNumber: String,
        region: String,
        district: String,
        correctedPhoneNumber: String?,
        email: String?,
        correctedWebsite: String?,
        ico: String,
        hqCity: String?,
        hqDistrict: String?,
        hqHouseNumber: String?,
        hqPostalCode: String?,
        hqRegion: String?,
        hqStreet: String?,
        specialization: String?,
        careForm: String?,
        correctedLat: Double?,
        correctedLng: Double?,
        categories: String?,
        locationId: Long,
        institutionId: Long
    )
}
