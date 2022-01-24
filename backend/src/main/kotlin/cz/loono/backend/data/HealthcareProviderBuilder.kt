package cz.loono.backend.data

import cz.loono.backend.data.constants.Constants
import cz.loono.backend.data.constants.District
import cz.loono.backend.data.constants.LawyerForm
import cz.loono.backend.data.constants.Region
import cz.loono.backend.db.model.HealthcareCategory
import cz.loono.backend.db.model.HealthcareProvider
import org.slf4j.LoggerFactory

class HealthcareProviderBuilder(private val columns: List<String>) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private var lawyerFormCode = ""
    private var lawyerFormName = ""
    private var lawyerPersonType = ""
    private var categories = emptySet<HealthcareCategory>()
    private var hqDistrictName = ""
    private var hqRegionName = ""

    fun build(): HealthcareProvider =
        HealthcareProvider(
            locationId = getColumnValue("MistoPoskytovaniId", columns).toLong(),
            institutionId = getColumnValue("ZdravotnickeZarizeniId", columns).toLong(),
            code = getColumnValue("Kod", columns),
            title = getColumnValue("NazevZarizeni", columns),
            institutionType = getColumnValue("DruhZarizeni", columns),
            city = getColumnValue("Obec", columns),
            postalCode = getColumnValue("Psc", columns),
            street = getColumnValue("Ulice", columns),
            houseNumber = getColumnValue("CisloDomovniOrientacni", columns),
            region = getColumnValue("Kraj", columns),
            regionCode = getColumnValue("KrajCode", columns),
            district = getColumnValue("Okres", columns),
            districtCode = getColumnValue("OkresCode", columns),
            administrativeDistrict = getColumnValue("SpravniObvod", columns),
            phoneNumber = getColumnValue("PoskytovatelTelefon", columns),
            fax = getColumnValue("PoskytovatelFax", columns),
            email = getColumnValue("PoskytovatelEmail", columns),
            website = getColumnValue("PoskytovatelWeb", columns),
            ico = getColumnValue("Ico", columns),
            personTypeCode = getColumnValue("TypOsoby", columns),
            personType = lawyerPersonType,
            lawyerFormCode = lawyerFormCode,
            layerForm = lawyerFormName,
            hqRegionCode = getColumnValue("KrajCodeSidlo", columns),
            hqRegion = hqRegionName,
            hqDistrictCode = getColumnValue("OkresCodeSidlo", columns),
            hqDistrict = hqDistrictName,
            hqCity = getColumnValue("ObecSidlo", columns),
            hqPostalCode = getColumnValue("PscSidlo", columns),
            hqStreet = getColumnValue("UliceSidlo", columns),
            hqHouseNumber = getColumnValue("CisloDomovniOrientacniSidlo", columns),
            specialization = getColumnValue("OborPece", columns),
            careForm = getColumnValue("FormaPece", columns),
            careType = getColumnValue("DruhPece", columns),
            substitute = getColumnValue("OdbornyZastupce", columns),
            lat = getColumnValue("Lat", columns).toDoubleOrNull(),
            lng = getColumnValue("Lng", columns).toDoubleOrNull(),
            category = categories
        )

    fun withLawyerForm(): HealthcareProviderBuilder {
        val lawyerFormCodeId = getColumnValue("PravniFormaKod", columns).toIntOrNull()
        if (lawyerFormCodeId != null) {
            lawyerFormCode = lawyerFormCodeId.toString()
            val lawyerForm = LawyerForm.ofCode(lawyerFormCodeId)
            lawyerFormName = lawyerForm.name
            lawyerPersonType = lawyerForm.personType.value
        }
        return this
    }

    fun withCategories(): HealthcareProviderBuilder {
        val specialization = getColumnValue("OborPece", columns)
        categories = SpecializationMapper().defineCategory(specialization)
        return this
    }

    fun withHQDistrictAndRegionName(): HealthcareProviderBuilder {
        if (getColumnValue("OkresCodeSidlo", columns) != "OkresCodeSidlo") {
            hqDistrictName = District.valueOf(getColumnValue("OkresCodeSidlo", columns)).value
            hqRegionName = Region.valueOf(getColumnValue("KrajCodeSidlo", columns)).value
        }
        return this
    }

    private fun getColumnValue(columnName: String, columns: List<String>): String {

        val columnIndex = Constants.healthcareProvidersCSVHeader.indexOf(columnName)
        if (columns.size < 2) {
            throw IllegalArgumentException("Location ID and Institution ID is required.")
        } else if (columnIndex >= columns.size) {
            logger.warn("Column size of the builder doesn't suite the column index.")
            return ""
        }

        var value = columns[columnIndex]
        value = value.replace("_Q_", "\"")
        value = value.replace("_COMMA_", ",")
        return value
    }
}
