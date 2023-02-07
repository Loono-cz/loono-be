package cz.loono.backend.api.service

import com.google.gson.Gson
import cz.loono.backend.api.dto.HealthcareProviderDetailDto
import cz.loono.backend.api.dto.HealthcareProviderDetailListDto
import cz.loono.backend.api.dto.HealthcareProviderIdDto
import cz.loono.backend.api.dto.HealthcareProviderIdListDto
import cz.loono.backend.api.dto.SimpleHealthcareProviderDto
import cz.loono.backend.api.dto.UpdateStatusMessageDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.data.HealthcareCSVParser
import cz.loono.backend.data.constants.CategoryValues
import cz.loono.backend.data.constants.Constants.OPEN_DATA_URL
import cz.loono.backend.db.model.HealthcareCategory
import cz.loono.backend.db.model.HealthcareProvider
import cz.loono.backend.db.model.HealthcareProviderId
import cz.loono.backend.db.model.ServerProperties
import cz.loono.backend.db.repository.HealthcareCategoryRepository
import cz.loono.backend.db.repository.HealthcareProviderRepository
import cz.loono.backend.db.repository.ServerPropertiesRepository
import cz.loono.backend.extensions.trimProviderImport
import cz.loono.backend.extensions.trimProviderNumber
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.net.ConnectException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.exists

@Service
class HealthcareProvidersService(
    private val healthcareProviderRepository: HealthcareProviderRepository,
    private val healthcareCategoryRepository: HealthcareCategoryRepository,
    private val serverPropertiesRepository: ServerPropertiesRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

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

    @Scheduled(cron = "\${scheduler.cron.data-update}") // each the 2nd day of month at 2AM
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
                // findDifferenceProviders(providers)
                searchUpdatedProviders()
                saveCategories()
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
    fun searchUpdatedProviders() {
        var skip = true
        val providersToUpdate = mutableListOf<HealthcareProvider>()
        val categoryToUpdate = mutableListOf<HealthcareProvider>()
        val inputStream = this::class.java.getResourceAsStream("/static/notification/missing_data_-_healthcare_providers.xlsx")
        val xlWb = XSSFWorkbook(inputStream)
        val xlWsProviders = xlWb.getSheetAt(2)
        val xlWsCategories = xlWb.getSheetAt(1)

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
                    correctedPhoneNumber = (row.getCell(10) as XSSFCell).rawValue?.toString()?.trimProviderNumber(),
                    email = row.getCell(11)?.toString().trimProviderImport(),
                    correctedWebsite = row.getCell(12)?.toString().trimProviderImport(),
                    ico = (row.getCell(13) as XSSFCell).rawValue.toString().trimProviderNumber(),
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
                    correctedCategory = setOf(HealthcareCategory(value = row.getCell(24).toString()))
                )
                providersToUpdate.add(provider)
            }
        }

        skip = true

        xlWsCategories.forEach { row ->
            if (skip) {
                skip = false
            } else {
                println(row.rowNum)
                val provider = HealthcareProvider(
                    locationId = row.getCell(0).toString().toDouble().toLong(),
                    institutionId = row.getCell(1).toString().toDouble().toLong(),
                    code = (row.getCell(2) as XSSFCell).rawValue.toString().trimProviderNumber(),
                    title = row.getCell(3).toString(),
                    institutionType = row.getCell(4).toString(),
                    city = row.getCell(5).toString(),
                    postalCode = row.getCell(6).toString().trimProviderNumber(),
                    street = row.getCell(7)?.toString().trimProviderImport(),
                    houseNumber = row.getCell(8).toString(),
                    region = row.getCell(9).toString(),
                    regionCode = row.getCell(10).toString(),
                    district = row.getCell(11).toString(),
                    districtCode = row.getCell(12).toString(),
                    administrativeDistrict = row.getCell(13).toString(),
                    correctedPhoneNumber = (row.getCell(14) as XSSFCell).rawValue?.toString().trimProviderImport()?.trimProviderNumber(),
                    fax = (row.getCell(15) as XSSFCell).rawValue?.toString().trimProviderImport()?.trimProviderNumber(),
                    email = row.getCell(16)?.toString().trimProviderImport(),
                    correctedWebsite = row.getCell(17)?.toString().trimProviderImport(),
                    ico = (row.getCell(18) as XSSFCell).rawValue.toString().trimProviderNumber(),
                    personTypeCode = row.getCell(19).toString().trimProviderNumber(),
                    lawyerFormCode = row.getCell(20).toString().trimProviderNumber(),
                    hqRegionCode = row.getCell(21)?.toString().trimProviderImport(),
                    hqDistrictCode = row.getCell(22)?.toString().trimProviderImport(),
                    hqDistrict = row.getCell(23)?.toString().trimProviderImport(),
                    hqPostalCode = row.getCell(24)?.toString().trimProviderImport()?.trimProviderNumber(),
                    hqStreet = row.getCell(25)?.toString().trimProviderImport(),
                    hqHouseNumber = row.getCell(26)?.toString().trimProviderImport(),
                    specialization = row.getCell(3)?.toString().trimProviderImport(),
                    // TODO specialization is missing, and collumn 27 is unclear
                    careForm = row.getCell(28)?.toString().trimProviderImport(),
                    careType = row.getCell(29)?.toString().trimProviderImport(),
                    substitute = row.getCell(30)?.toString().trimProviderImport(),
                    correctedLat = row.getCell(31)?.toString().trimProviderImport()?.toDouble(),
                    correctedLng = row.getCell(32)?.toString().trimProviderImport()?.toDouble()
                )
                categoryToUpdate.add(provider)
            }
        }

        providersToUpdate.forEach {
            healthcareProviderRepository.save(it)
        }
        categoryToUpdate.forEach {
            healthcareProviderRepository.save(it)
        }
    }
    @Synchronized
    fun findDifferenceProviders(providers: List<HealthcareProvider>) {
        if (providers.isEmpty()) {
            return
        }
        val foundList = mutableListOf<HealthcareProvider?>()
        val notfoundList = mutableListOf<String>()
        val csvFile = File("/Users/emtech36/Documents/GitHub/loono-be/backend/src/main/resources/static/notification/data-1675259453877.csv").inputStream()
        csvFile.use { inputStream ->
            val reader = BufferedReader(inputStream.reader(Charsets.UTF_8))
            reader.forEachLine { line ->
                val lineParts = line.split(',')
                val partInstId = lineParts[0].replace("\"", "")
                val partLocID = lineParts[1].replace("\"", "")

                val find = providers.find { it.institutionId == partInstId.toLong() && it.locationId == partLocID.toLong() }
                if (find != null) {
                    foundList.add(find)
                } else {
                    notfoundList.add(line)
                }
            }
        }
        val fs = foundList.size
        val nfs = notfoundList.size

        val fos = FileOutputStream("/Users/emtech36/Documents/GitHub/loono-be/backend/src/main/resources/static/notification/data-missing.csv")
        val osw = OutputStreamWriter(fos, StandardCharsets.UTF_8)
        val writer = BufferedWriter(osw)
        notfoundList.forEach {
            writer.write(it)
            writer.newLine()
        }
        writer.flush()
        println("found size  $fs and ntfs = $nfs")
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
    @Transactional
    fun saveCategories() {
        val categoryValues = CategoryValues.values().map { HealthcareCategory(value = it.value) }
        healthcareCategoryRepository.saveAll(categoryValues)
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
            category = category.map(HealthcareCategory::value)
                .ifEmpty { correctedCategory.map(HealthcareCategory::value) }
                .filterNot(removedCategories::contains),
            specialization = specialization,
            lat = lat ?: correctedLat!!,
            lng = lng ?: correctedLng!!
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
            phoneNumber = phoneNumber ?: correctedPhoneNumber,
            fax = fax,
            email = email,
            website = website ?: correctedWebsite,
            ico = ico,
            category = category.map(HealthcareCategory::value)
                .ifEmpty { correctedCategory.map(HealthcareCategory::value) }
                .filterNot(removedCategories::contains),
            specialization = specialization,
            careForm = careForm,
            careType = careType,
            substitute = substitute,
            lat = lat ?: correctedLat!!,
            lng = lng ?: correctedLng!!
        )
}
