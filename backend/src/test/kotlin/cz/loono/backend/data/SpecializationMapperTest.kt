package cz.loono.backend.data

import cz.loono.backend.data.constants.CategoryValues
import cz.loono.backend.db.model.HealthcareCategory
import org.junit.jupiter.api.Test

class SpecializationMapperTest {

    private val specializationMapper = SpecializationMapper()

    @Test
    fun `simple case with single category`() {
        val category = specializationMapper.defineCategory("Diabetologie")

        assert(category == setOf(HealthcareCategory(value = CategoryValues.DIABETOLOGY.value)))
    }

    @Test
    fun `no specialization`() {
        val category = specializationMapper.defineCategory("")

        assert(category.isEmpty())
    }

    @Test
    fun `unknown category`() {
        val category = specializationMapper.defineCategory("unknown")

        assert(category.isEmpty())
    }

    @Test
    fun `specialization with more categories`() {
        val category = specializationMapper.defineCategory("Onkogynekologie")

        assert(
            category == setOf(
                HealthcareCategory(value = CategoryValues.ONCOLOGY.value),
                HealthcareCategory(value = CategoryValues.GYNECOLOGY.value)
            )
        )
    }

    @Test
    fun `multi-specialization`() {
        val category =
            specializationMapper.defineCategory("rehabilitační a fyzikální medicína, Nutriční terapeut, Fyzioterapeut")

        assert(
            category == setOf(
                HealthcareCategory(value = CategoryValues.REHABILITATION.value),
                HealthcareCategory(value = CategoryValues.NUTRITION.value),
                HealthcareCategory(value = CategoryValues.PHYSIOTHERAPY.value)
            )
        )
    }
}
