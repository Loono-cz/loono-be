package cz.loono.backend.data

import cz.loono.backend.data.constants.CategoryValues
import org.junit.jupiter.api.Test

class SpecializationMapperTest {

    private val specializationMapper = SpecializationMapper()

    @Test
    fun `simple case with single category`() {
        val category = specializationMapper.defineCategory("Diabetologie")
        assert(category == listOf(CategoryValues.DIABETOLOGY.name))
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
            category == listOf(
                CategoryValues.GYNECOLOGY.name,
                CategoryValues.ONCOLOGY.name
            )
        )
    }

    @Test
    fun `multi-specialization`() {
        val category =
            specializationMapper.defineCategory("rehabilitační a fyzikální medicína, Nutriční terapeut, Fyzioterapeut")

        assert(
            category == listOf(
                CategoryValues.REHABILITATION.name,
                CategoryValues.NUTRITION.name,
                CategoryValues.PHYSIOTHERAPY.name
            )
        )
    }
}
