package cz.loono.backend.data

import cz.loono.backend.data.constants.CategoryValues
import cz.loono.backend.data.constants.PersonType
import cz.loono.backend.db.model.HealthcareCategory
import cz.loono.backend.db.model.HealthcareProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HealthcareProviderBuilderTest {

    @Test
    fun `empty column list`() {
        val healthcareProviderBuilder = HealthcareProviderBuilder(emptyList())

        assertThrows<IllegalArgumentException> {
            healthcareProviderBuilder.build()
        }
    }

    @Test
    fun `just two columns in list`() {
        val healthcareProviderBuilder = HealthcareProviderBuilder(listOf("1", "2"))

        val healthcareProvider = healthcareProviderBuilder.build()

        assert(healthcareProvider.title == "")
        assert(healthcareProvider.category.isEmpty())
    }

    @Test
    fun `replacing substituted strings`() {
        val healthcareProviderBuilder = HealthcareProviderBuilder(listOf("1", "2", "_Q_quot_Q_", "_Q_a_COMMA_ b_Q_c"))

        val healthcareProvider = healthcareProviderBuilder.build()

        assert(healthcareProvider.code == "\"quot\"")
        assert(healthcareProvider.title == "\"a, b\"c")
    }

    @Test
    fun `building result`() {
        val healthcareProviderBuilder = HealthcareProviderBuilder(getAllColumns())

        val healthcareProvider = healthcareProviderBuilder.build()

        assert(
            healthcareProvider == HealthcareProvider(
                locationId = 239519,
                institutionId = 161084,
                code = "03685080000000",
                title = "Mgr. DOMINIKA MACHTOVÁ",
                institutionType = "Samostatné zařízení fyzioterapeuta",
                city = "Husinec",
                postalCode = "38421",
                street = "Prokopovo náměstí",
                houseNumber = "1",
                region = "Jihočeský kraj",
                regionCode = "CZ031",
                district = "Prachatice",
                districtCode = "CZ0315",
                administrativeDistrict = "adm",
                phoneNumber = "tel",
                fax = "fax",
                email = "mail",
                website = "web",
                ico = "03685080",
                personTypeCode = "1",
                hqRegionCode = "CZ031",
                hqDistrictCode = "CZ0315",
                hqCity = "Husinec",
                hqPostalCode = "38421",
                hqStreet = "Prokopovo náměstí",
                hqHouseNumber = "1",
                specialization = "Fyzioterapeut",
                careForm = "ambulantní péče",
                careType = "careType",
                substitute = "sub",
                lat = "49.053069339386",
                lng = "13.986452702303",
            )
        )
    }

    @Test
    fun `provider with lawyer form`() {
        val healthcareProviderBuilder = HealthcareProviderBuilder(getAllColumns())

        healthcareProviderBuilder.withLawyerForm()
        val healthcareProvider = healthcareProviderBuilder.build()

        assert(healthcareProvider.personType == PersonType.LEGAL_PERSON.value)
        assert(healthcareProvider.layerForm == "Společnost s ručením omezeným")
        assert(healthcareProvider.lawyerFormCode == "112")
    }

    @Test
    fun `provider with hq location names`() {
        val healthcareProviderBuilder = HealthcareProviderBuilder(getAllColumns())

        healthcareProviderBuilder.withHQDistrictAndRegionName()
        val healthcareProvider = healthcareProviderBuilder.build()

        assert(healthcareProvider.hqDistrict == "Prachatice")
        assert(healthcareProvider.hqRegion == "Jihočeský kraj")
    }

    @Test
    fun `provider categories happy case`() {
        val healthcareProviderBuilder = HealthcareProviderBuilder(getAllColumns())

        healthcareProviderBuilder.withCategories()
        val healthcareProvider = healthcareProviderBuilder.build()

        assert(healthcareProvider.category.isNotEmpty())
        assert(
            healthcareProvider.category == setOf(
                HealthcareCategory(
                    value = CategoryValues.PHYSIOTHERAPY.value,
                    healthcareProviders = emptySet()
                )
            )
        )
    }

    @Test
    fun `provider categories with empty specialization`() {
        val columns = getAllColumns().toMutableList()
        columns[27] = ""
        val healthcareProviderBuilder = HealthcareProviderBuilder(columns)

        healthcareProviderBuilder.withCategories()
        val healthcareProvider = healthcareProviderBuilder.build()

        assert(healthcareProvider.category.isEmpty())
    }

    @Test
    fun `provider categories with multiple specialization`() {
        val columns = getAllColumns().toMutableList()
        columns[27] = "rehabilitační a fyzikální medicína, Nutriční terapeut, Fyzioterapeut"
        val healthcareProviderBuilder = HealthcareProviderBuilder(columns)

        healthcareProviderBuilder.withCategories()
        val healthcareProvider = healthcareProviderBuilder.build()

        assert(healthcareProvider.category.isNotEmpty())
        assert(
            healthcareProvider.category == setOf(
                HealthcareCategory(
                    value = CategoryValues.PHYSIOTHERAPY.value
                ),
                HealthcareCategory(
                    value = CategoryValues.REHABILITATION.value
                ),
                HealthcareCategory(
                    value = CategoryValues.NUTRITION.value
                )
            )
        )
    }

    private fun getAllColumns(): List<String> {
        return listOf(
            "239519",
            "161084",
            "03685080000000",
            "Mgr. DOMINIKA MACHTOVÁ",
            "Samostatné zařízení fyzioterapeuta",
            "Husinec",
            "38421",
            "Prokopovo náměstí",
            "1",
            "Jihočeský kraj",
            "CZ031",
            "Prachatice",
            "CZ0315",
            "adm",
            "tel",
            "fax",
            "mail",
            "web",
            "03685080",
            "2",
            "112",
            "CZ031",
            "CZ0315",
            "Husinec",
            "38421",
            "Prokopovo náměstí",
            "1",
            "Fyzioterapeut",
            "ambulantní péče",
            "careType",
            "sub",
            "49.053069339386",
            "13.986452702303"
        )
    }
}
