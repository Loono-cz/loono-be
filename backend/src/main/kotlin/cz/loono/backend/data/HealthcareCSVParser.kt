package cz.loono.backend.data

import cz.loono.backend.data.constants.Constants
import cz.loono.backend.db.model.HealthcareProvider
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStream

class HealthcareCSVParser {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * The function should receive a CSV from NRPZS open data.
     * @see <a href="https://opendata.mzcr.cz/data/nrpzs/narodni-registr-poskytovatelu-zdravotnich-sluzeb.csv-metadata.json">Doc</a>
     */
    fun parse(input: InputStream): List<HealthcareProvider> {

        val providers = mutableListOf<HealthcareProvider>()

        input.use { inputStream ->
            val reader = BufferedReader(inputStream.reader(Charsets.UTF_8))
            reader.forEachLine { line ->
                val columns = parseColumns(line)

                if (line.startsWith("MistoPoskytovaniId")) {
                    verifyColumns(columns)
                    return@forEachLine
                }
                if (columns.size != Constants.healthcareProvidersCSVHeader.size) {
                    logger.warn("The line doesn't fit header size and will be skipped.")
                    return@forEachLine
                }

                val healthcareProvider = HealthcareProviderBuilder(columns)
                    .withCategories()
                    .withLawyerForm()
                    .withHQDistrictAndRegionName()
                providers.add(healthcareProvider.build())
            }
        }

        return providers
    }

    private fun parseColumns(line: String): List<String> {
        var record = substituteQuotes(line)
        if (record.indexOf(",\"") > -1) {
            var toSearch = record
            while (toSearch.indexOf(",\"") > -1) {
                val substr = toSearch.substring(toSearch.indexOf(",\"") + 1, toSearch.indexOf("\",") + 1)
                var replacement = substr.substring(1, substr.length - 1)
                replacement = replacement.replace(",", "_COMMA_")
                toSearch = toSearch.substring(toSearch.indexOf("\",") + 1)
                record = record.replace(substr, replacement)
            }
        }
        return record.split(',')
    }

    private fun substituteQuotes(record: String): String {
        var result = record.replace(",\"\"\"", ",\"_Q_")
        result = result.replace("\"\"\",", "_Q_\",")
        return result.replace("\"\"", "_Q_")
    }

    private fun verifyColumns(columns: List<String>) {
        if (Constants.healthcareProvidersCSVHeader != columns) {
            logger.warn("The structure of the file has changed.")
        }
    }
}
