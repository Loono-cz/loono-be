package cz.loono.backend.api.service

import com.google.gson.Gson
import cz.loono.backend.api.dto.HealthcareCategoryTypeDto
import cz.loono.backend.api.dto.HealthcareProviderDetailDto
import cz.loono.backend.api.dto.HealthcareProviderDetailListDto
import cz.loono.backend.api.dto.HealthcareProviderIdDto
import cz.loono.backend.api.dto.HealthcareProviderIdListDto
import cz.loono.backend.api.dto.SimpleHealthcareProviderDto
import cz.loono.backend.api.dto.UpdateStatusMessageDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.data.HealthcareCSVParser
import cz.loono.backend.data.constants.CategoryValues
import cz.loono.backend.data.constants.Constants.CORRECTED_DATA_URL
import cz.loono.backend.data.constants.Constants.OPEN_DATA_URL
import cz.loono.backend.db.model.HealthcareProvider
import cz.loono.backend.db.model.HealthcareProviderId
import cz.loono.backend.db.model.ServerProperties
import cz.loono.backend.db.repository.HealthcareProviderRepository
import cz.loono.backend.db.repository.ServerPropertiesRepository
import cz.loono.backend.extensions.trimProviderImport
import cz.loono.backend.extensions.trimProviderNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.net.ConnectException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.exists

@Service
class HealthcareProvidersService(
    private val healthcareProviderRepository: HealthcareProviderRepository,
    private val serverPropertiesRepository: ServerPropertiesRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var scopeUpdateCorrectedData: Job? = null
    private val batchSize = 500
    private var updating = false
    private var zipFilePath = Path.of("init")
    var lastUpdate = LocalDate.MIN!!

    private val removedCategories = listOf(
        CategoryValues.PHARMACOLOGY.value,
        CategoryValues.NURSE.value,
        CategoryValues.ANESTHESIOLOGY_ARO.value,
        CategoryValues.RADIOLOGY.value,
        CategoryValues.SPEECH_THERAPY.value,
        CategoryValues.BIOCHEMISTRY.value,
        CategoryValues.PALLIATIVE_MEDICINE.value,
        CategoryValues.MEDICAL_LABORATORY_TECHNICIAN.value,
        CategoryValues.ONCOLOGY.value,
        CategoryValues.MICROBIOLOGY.value,
        CategoryValues.PHONIATRICS.value,
        CategoryValues.GERIATRICS.value,
        CategoryValues.GENETICS.value,
        CategoryValues.PATHOLOGY.value,
        CategoryValues.OCCUPATIONAL_MEDICINE.value,
        CategoryValues.HYGIENE.value,
        CategoryValues.NEONATAL.value,
        CategoryValues.INFECTIOUS_MEDICINE.value,
        CategoryValues.LDN.value,
        CategoryValues.SPECIALIST.value,
        CategoryValues.PSYCHOSOMATICS.value,
        CategoryValues.FORENSIC_MEDICINE.value,
        CategoryValues.PARAMEDIC.value,
        CategoryValues.AERO_MEDICINE.value,
        CategoryValues.SOCIAL_WORKER.value,
        CategoryValues.BEHAVIORAL_ANALYST.value,
        CategoryValues.PUBLIC_HEALTHCARE.value,
        CategoryValues.RADIOLOGICAL_PHYSICIST.value,
        CategoryValues.BIOMEDICAL_TECHNICIAN.value
    )

    // Scheduled is commented as Loono does not want automatic update
    // @Scheduled(cron = "\${scheduler.cron.data-update}") // each the 2nd day of month at 2AM
    @Synchronized
    fun updateData(): UpdateStatusMessageDto {
        updating = true
        val input: InputStream
        try {
            input = URL(OPEN_DATA_URL).openStream()
        } catch (e: ConnectException) {
            updating = false
            throw LoonoBackendException(
                HttpStatus.SERVICE_UNAVAILABLE,
                errorCode = HttpStatus.SERVICE_UNAVAILABLE.value().toString(),
                errorMessage = "New open data are not available."
            )
        }
        val providers = HealthcareCSVParser().parse(input)
        if (providers.isNotEmpty()) {
            updating = true
            try {
                saveProviders(providers)
                setLastUpdate()
                prepareAllProviders()
            } finally {
                updating = false
            }
        } else {
            updating = false
            throw LoonoBackendException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                errorCode = HttpStatus.UNPROCESSABLE_ENTITY.value().toString(),
                errorMessage = "Data update failed."
            )
        }
        logger.info("Update finished.")
        return UpdateStatusMessageDto("Data successfully updated.")
    }
    @Synchronized
    fun searchUpdatedProviders(): UpdateStatusMessageDto {
        if (scopeUpdateCorrectedData == null) {
            scopeUpdateCorrectedData = CoroutineScope(Dispatchers.IO).launch {
                try {
                    val url = URL(CORRECTED_DATA_URL)
                    val inputStream = withContext(Dispatchers.IO) {
                        url.openStream()
                    }

                    var skip = true
                    val providersToUpdate = mutableListOf<HealthcareProvider>()
                    val xlWb = XSSFWorkbook(inputStream)
                    val xlWsProviders = xlWb.getSheetAt(0)

                    xlWsProviders.forEach { row ->
                        if (skip) {
                            skip = false
                        } else {
                            val provider = HealthcareProvider(
                                locationId = row.getCell(0).toString().toDouble().toLong(),
                                institutionId = row.getCell(1).toString().toDouble().toLong(),
                                title = row.getCell(2).toString(),
                                institutionType = row.getCell(3).toString(),
                                city = row.getCell(4).toString(),
                                postalCode = row.getCell(5).toString().trimProviderNumber(),
                                street = row.getCell(6)?.toString().trimProviderImport(),
                                houseNumber = row.getCell(7).toString(),
                                region = row.getCell(8).toString(),
                                district = row.getCell(9).toString(),
                                correctedPhoneNumber = (row.getCell(10) as XSSFCell).toString().trimProviderNumber(),
                                email = row.getCell(11)?.toString().trimProviderImport(),
                                correctedWebsite = row.getCell(12)?.toString().trimProviderImport(),
                                ico = (row.getCell(13) as XSSFCell).toString().trimProviderNumber(),
                                hqCity = row.getCell(14)?.toString().trimProviderImport(),
                                hqDistrict = row.getCell(15)?.toString().trimProviderImport(),
                                hqHouseNumber = row.getCell(16)?.toString().trimProviderImport(),
                                hqPostalCode = row.getCell(17)?.toString().trimProviderImport()?.trimProviderNumber(),
                                hqRegion = row.getCell(18)?.toString().trimProviderImport(),
                                hqStreet = row.getCell(19)?.toString().trimProviderImport(),
                                specialization = row.getCell(20)?.toString().trimProviderImport(),
                                careForm = row.getCell(21)?.toString().trimProviderImport(),
                                correctedLat = row.getCell(22)?.toString().trimProviderImport()?.toDouble(),
                                correctedLng = row.getCell(23)?.toString().trimProviderImport()?.toDouble(),
                                categories = Gson().toJson(setCategoriesValueToEnum(row.getCell(24).toString()))
                            )
                            providersToUpdate.add(provider)
                        }
                    }
                    skip = true

                    providersToUpdate.forEach {
                        val findProvider = healthcareProviderRepository.findById(
                            HealthcareProviderId(
                                locationId = it.locationId,
                                institutionId = it.institutionId
                            )
                        )
                        if (findProvider.isEmpty) {
                            healthcareProviderRepository.save(it)
                        } else {
                            healthcareProviderRepository.updateProvider(
                                title = it.title,
                                institutionType = it.institutionType,
                                city = it.city,
                                postalCode = it.postalCode,
                                street = it.street,
                                houseNumber = it.houseNumber,
                                region = it.region,
                                district = it.district,
                                correctedPhoneNumber = it.correctedPhoneNumber,
                                email = it.email,
                                correctedWebsite = it.correctedWebsite,
                                ico = it.ico,
                                hqCity = it.hqCity,
                                hqDistrict = it.hqDistrict,
                                hqHouseNumber = it.hqHouseNumber,
                                hqPostalCode = it.hqPostalCode,
                                hqRegion = it.hqRegion,
                                hqStreet = it.hqStreet,
                                specialization = it.specialization,
                                careForm = it.careForm,
                                correctedLat = it.correctedLat,
                                correctedLng = it.correctedLng,
                                categories = it.categories,
                                locationId = it.locationId,
                                institutionId = it.institutionId,
                                lastUpdated = LocalDateTime.now()
                            )
                        }
                    }
                    setLastUpdate()
                    prepareAllProviders()
                } catch (e: Exception) {
                    throw LoonoBackendException(HttpStatus.SERVICE_UNAVAILABLE, e.message, e.localizedMessage)
                }
            }
            return UpdateStatusMessageDto("Corrected data update is running in coroutine.")
        } else {
            if (scopeUpdateCorrectedData?.isActive == true) {
                return UpdateStatusMessageDto("Corrected data update is running active.")
            }
            if (scopeUpdateCorrectedData?.isCompleted == true) {
                scopeUpdateCorrectedData = null
                return UpdateStatusMessageDto("Corrected data update is completed.")
            }
            return UpdateStatusMessageDto("Corrected data update is in unknown state.")
        }
    }

    @Synchronized
    fun saveProviders(providers: List<HealthcareProvider>) {
        if (providers.isEmpty()) {
            return
        }
        val cycles = providers.size.div(batchSize)
        val rest = providers.size % batchSize - 1
        for (i in 0..cycles) {
            val start = i * batchSize
            var end = start + 499
            if (i == cycles) {
                end = start + rest
            }
            saveProvidersBatch(providers.subList(start, end))
        }
    }

    @Synchronized
    @Transactional
    fun saveProvidersBatch(providersSublist: List<HealthcareProvider>) {
        healthcareProviderRepository.saveAll(providersSublist)
    }

    @Synchronized
    @Transactional(rollbackFor = [Exception::class])
    fun setLastUpdate() {
        val serverProperties = serverPropertiesRepository.findAll()
        lastUpdate = LocalDate.now()
        if (serverProperties.isEmpty()) {
            serverPropertiesRepository.save(ServerProperties())
            return
        }
        val firstProperties = serverProperties.first()
        firstProperties.lastUpdate = lastUpdate
        serverPropertiesRepository.save(firstProperties)
    }

    @Synchronized
    fun prepareAllProviders() {
        val count = storedProvidersCount()
        val providers = LinkedHashSet<SimpleHealthcareProviderDto>(count)

        val cycles = count.div(batchSize)
        for (i in 0..cycles) {
            providers.addAll(findPage(i))
        }

//        val allFilteredProviders = healthcareProviderRepository.findAll()
//            .filter { (it.lat != null && it.lng != null) || (it.correctedLat != null && it.correctedLng != null) }
//            .toSet()
//            .map { it.simplify() }
//            .filter { it.category.isNotEmpty() }
//
//        providers.addAll(allFilteredProviders)
        zipProviders(providers)
    }

    @Synchronized
    @Transactional(readOnly = true)
    fun storedProvidersCount(): Int = healthcareProviderRepository.count().toInt()

    @Synchronized
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    fun findPage(page: Int): List<SimpleHealthcareProviderDto> =
        healthcareProviderRepository.findAll(PageRequest.of(page, batchSize))
            .filter { (it.lat != null && it.lng != null) || (it.correctedLat != null && it.correctedLng != null) }
            .toSet()
            .map { it.simplify() }
            .filter { it.category.isNotEmpty() }

    @Synchronized
    private fun zipProviders(providers: LinkedHashSet<SimpleHealthcareProviderDto>) {
        if (!zipFilePath.endsWith("init") && zipFilePath.exists()) {
            Files.delete(zipFilePath)
        }
        val filePath = Path.of("providers-$lastUpdate.zip")
        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(File(filePath.toUri())))).use { zip ->
                OutputStreamWriter(zip).use { writer ->
                    zip.putNextEntry(ZipEntry("providers.json"))
                    Gson().toJson(providers, writer)
                }
            }
        } catch (ioe: IOException) {
            throw LoonoBackendException(
                status = HttpStatus.UNPROCESSABLE_ENTITY,
                errorCode = "422",
                errorMessage = "The file cannot be downloaded."
            )
        }
        zipFilePath = filePath
    }

    fun getAllSimpleData(): Path {
        if (updating) {
            throw throw LoonoBackendException(
                status = HttpStatus.UNPROCESSABLE_ENTITY,
                errorCode = "422",
                errorMessage = "Server is updating data."
            )
        }
        return zipFilePath
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    fun getHealthcareProviderDetail(healthcareProviderId: HealthcareProviderIdDto): HealthcareProviderDetailDto {
        val provider = healthcareProviderRepository.findByIdOrNull(
            HealthcareProviderId(
                locationId = healthcareProviderId.locationId,
                institutionId = healthcareProviderId.institutionId
            )
        )
        return provider?.getDetails() ?: throw LoonoBackendException(
            status = HttpStatus.NOT_FOUND,
            errorCode = "404",
            errorMessage = "The healthcare provider with this ID not found."
        )
    }

    fun getMultipleHealthcareProviderDetails(providerIdsList: HealthcareProviderIdListDto): HealthcareProviderDetailListDto =
        HealthcareProviderDetailListDto(
            healthcareProvidersDetails = providerIdsList.providersIds?.map {
                getHealthcareProviderDetail(it)
            } ?: throw LoonoBackendException(
                status = HttpStatus.UNPROCESSABLE_ENTITY,
                errorCode = "422",
                errorMessage = "Incorrect request."
            )
        )

    fun setCategoriesValueToEnum(value: String): List<String> {
        val categories = mutableListOf<String>()
        if (value.contains(",")) {
            val findFullText = CategoryValues.values().firstOrNull { it.value == value }
            if (findFullText == null) {
                if (value.contains("Anesteziologie, ARO, intenzivní péče"))
                    categories.add(HealthcareCategoryTypeDto.ANESTHESIOLOGY_ARO.value)
                if (value.contains("Angiologie, cévní"))
                    categories.add(HealthcareCategoryTypeDto.ANGIOLOGY.value)
                if (value.contains("Endokrinologie, hormony"))
                    categories.add(HealthcareCategoryTypeDto.ENDOCRINOLOGY.value)
                if (value.contains("Dermatovenerologie, kožní"))
                    categories.add(HealthcareCategoryTypeDto.DERMATOVENEROLOGY.value)
                if (value.contains("Geriatrie, medicína stáří, senioři"))
                    categories.add(HealthcareCategoryTypeDto.GERIATRICS.value)
                if (value.contains("Hematologie, krevní"))
                    categories.add(HealthcareCategoryTypeDto.HEMATOLOGY.value)
                if (value.contains("Interna, vnitřní lékařství"))
                    categories.add(HealthcareCategoryTypeDto.INTERNAL_MEDICINE.value)
                if (value.contains("Nefrologie, ledviny"))
                    categories.add(HealthcareCategoryTypeDto.NEPHROLOGY.value)
                if (value.contains("Pneumologie, plicní"))
                    categories.add(HealthcareCategoryTypeDto.PNEUMOLOGY.value)
                if (value.contains("Výživa, nutriční"))
                    categories.add(HealthcareCategoryTypeDto.NUTRITION.value)

                val categoriesValues = value.split(", ")
                categoriesValues.forEach { categoryValue ->
                    CategoryValues.values().firstOrNull { it.value == categoryValue }?.let { it1 ->
                        categories.add(it1.name)
                    }
                }
            } else {
                categories.add(findFullText.name)
            }
        } else {
            CategoryValues.values().firstOrNull { it.value == value }?.let { it1 ->
                categories.add(it1.name)
            }
        }
        return categories
    }

    fun castCategoriesJsonToCorrectList(json: String?): List<String> {
        val resultList = mutableListOf<String>()
        if (!json.isNullOrEmpty()) {
            val listOfEnum = Gson().fromJson(json, Array<String>::class.java).asList()
            listOfEnum.forEach {
                resultList.add(CategoryValues.valueOf(it).value)
            }
        }
        return resultList
    }
    @Suppress("UNCHECKED_CAST")
    fun HealthcareProvider.simplify(): SimpleHealthcareProviderDto =
        SimpleHealthcareProviderDto(
            locationId = locationId,
            institutionId = institutionId,
            title = title,
            street = street,
            houseNumber = houseNumber,
            city = city,
            postalCode = postalCode,
            category = castCategoriesJsonToCorrectList(categories),
            specialization = specialization,
            lat = correctedLat ?: lat ?: 0.0,
            lng = correctedLng ?: lng ?: 0.0
        )

    // TODO tady se vybira zda corrected data nebo puvodni
    @Suppress("UNCHECKED_CAST")
    fun HealthcareProvider.getDetails(): HealthcareProviderDetailDto =
        HealthcareProviderDetailDto(
            locationId = locationId,
            institutionId = institutionId,
            title = title,
            institutionType = institutionType,
            street = street,
            houseNumber = houseNumber,
            city = city,
            postalCode = postalCode,
            phoneNumber = correctedPhoneNumber ?: phoneNumber,
            fax = fax,
            email = email,
            website = correctedWebsite ?: website,
            ico = ico,
            category = castCategoriesJsonToCorrectList(categories),
            specialization = specialization,
            careForm = careForm,
            careType = careType,
            substitute = substitute,
            lat = correctedLat ?: lat ?: 0.0,
            lng = correctedLng ?: lng ?: 0.0
        )
}
