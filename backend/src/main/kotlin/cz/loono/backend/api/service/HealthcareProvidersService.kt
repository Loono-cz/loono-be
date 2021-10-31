package cz.loono.backend.api.service

import com.google.gson.Gson
import cz.loono.backend.api.dto.HealthcareProviderDetailsDto
import cz.loono.backend.api.dto.HealthcareProviderIdDto
import cz.loono.backend.api.dto.HealthcareProviderListDto
import cz.loono.backend.api.dto.SimpleHealthcareProviderDto
import cz.loono.backend.api.dto.UpdateStatusMessageDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.data.HealthcareCSVParser
import cz.loono.backend.data.constants.CategoryValues
import cz.loono.backend.data.constants.Constants.OPEN_DATA_URL
import cz.loono.backend.db.model.HealthcareCategory
import cz.loono.backend.db.model.HealthcareProvider
import cz.loono.backend.db.model.ServerProperties
import cz.loono.backend.db.repository.HealthcareCategoryRepository
import cz.loono.backend.db.repository.HealthcareProviderRepository
import cz.loono.backend.db.repository.ServerPropertiesRepository
import io.github.reactivecircus.cache4k.Cache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class HealthcareProvidersService @Autowired constructor(
    private val healthcareProviderRepository: HealthcareProviderRepository,
    private val healthcareCategoryRepository: HealthcareCategoryRepository,
    private val serverPropertiesRepository: ServerPropertiesRepository
) {

    private val providersCache = Cache.Builder().build<HealthcareProviderIdDto, HealthcareProvider>()
    private val fileCache = Cache.Builder().build<String, ByteArray>()

    var lastUpdate = ""

    @Scheduled(cron = "0 0 2 2 * ?") // each the 2nd day of month at 2AM
    @Synchronized
    @Transactional(rollbackFor = [Exception::class])
    fun updateData(): UpdateStatusMessageDto {
        val input = URL(OPEN_DATA_URL).openStream()
        val providers = HealthcareCSVParser().parse(input)
        if (providers.isNotEmpty()) {
            val categoryValues = CategoryValues.values().map { HealthcareCategory(value = it.value) }
            healthcareCategoryRepository.saveAll(categoryValues)
            healthcareProviderRepository.saveAll(providers)
            updateCache()
            setLastUpdate()
        } else {
            throw LoonoBackendException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                errorCode = HttpStatus.UNPROCESSABLE_ENTITY.value().toString(),
                errorMessage = "Data update failed."
            )
        }
        return UpdateStatusMessageDto("Data successfully updated.")
    }

    private fun setLastUpdate() {
        val serverProperties = serverPropertiesRepository.findAll()
        val updateDate = LocalDate.now()
        lastUpdate = "${updateDate.year}-${updateDate.monthValue}"
        if (serverProperties.isEmpty()) {
            serverPropertiesRepository.save(ServerProperties())
            return
        }
        val firstProperties = serverProperties.first()
        firstProperties.lastUpdate = updateDate
        serverPropertiesRepository.save(firstProperties)
    }

    private fun updateCache() {
        providersCache.invalidateAll()
        fileCache.invalidateAll()
        val providers = healthcareProviderRepository.findAll()
        providers.forEach { provider ->
            val id = HealthcareProviderIdDto(locationId = provider.locationId, institutionId = provider.institutionId)
            providersCache.put(id, provider)
        }
        fileCache.put("providers", zipProviders())
    }

    private fun zipProviders(): ByteArray {
        val simplifyProviders = providersCache.asMap().values.map { it.simplify() }
        val list = HealthcareProviderListDto(
            healthcareProviders = simplifyProviders
        )
        val jsonString = Gson().toJson(list)
        val byteArrayOutputStream = ByteArrayOutputStream()
        try {
            ZipOutputStream(byteArrayOutputStream).use { zos ->
                val entry = ZipEntry("providers.json")
                zos.putNextEntry(entry)
                zos.write(jsonString.toByteArray())
                zos.closeEntry()
            }
        } catch (ioe: IOException) {
            throw LoonoBackendException(HttpStatus.INTERNAL_SERVER_ERROR)
        }
        return byteArrayOutputStream.toByteArray()
    }

    fun getAllSimpleData(): ByteArray {
        return fileCache.get("providers")!!
    }

    fun getHealthcareProviderDetail(healthcareProviderId: HealthcareProviderIdDto): HealthcareProviderDetailsDto {
        val provider = providersCache.get(healthcareProviderId) ?: throw LoonoBackendException(
            status = HttpStatus.NOT_FOUND,
            errorCode = "404",
            errorMessage = "The healthcare provider with this ID not found."
        )
        return provider.getDetails()
    }

    fun HealthcareProvider.simplify(): SimpleHealthcareProviderDto {
        return SimpleHealthcareProviderDto(
            locationId = locationId,
            institutionId = institutionId,
            title = title,
            street = street,
            houseNumber = houseNumber,
            city = city,
            postalCode = postalCode,
            category = category.map { it.value },
            specialization = specialization,
            lat = lat,
            lng = lng
        )
    }

    fun HealthcareProvider.getDetails(): HealthcareProviderDetailsDto {
        return HealthcareProviderDetailsDto(
            locationId = locationId,
            institutionId = institutionId,
            title = title,
            institutionType = institutionType,
            street = street,
            houseNumber = houseNumber,
            city = city,
            postalCode = postalCode,
            phoneNumber = phoneNumber,
            fax = fax,
            email = email,
            website = website,
            ico = ico,
            category = category.map { it.value },
            specialization = specialization,
            careForm = careForm,
            careType = careType,
            substitute = substitute,
            lat = lat,
            lng = lng
        )
    }
}
