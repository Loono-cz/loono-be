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
        if (record.indexOf(COLUMN_START_QUOTE) > -1) {
            var toSearch = record
            while (toSearch.indexOf(COLUMN_START_QUOTE) > -1) {
                val substr = toSearch.substring(
                    toSearch.indexOf(COLUMN_START_QUOTE) + 1,
                    toSearch.indexOf(COLUMN_END_QUOTE) + 1
                )
                var replacement = substr.substring(1, substr.length - 1)
                replacement = replacement.replace(",", "_COMMA_")
                toSearch = toSearch.substring(toSearch.indexOf(COLUMN_END_QUOTE) + 1)
                record = record.replace(substr, replacement)
            }
        }
        return record.split(',')
    }

    private fun substituteQuotes(record: String): String {
        var result = record.replace("$COLUMN_START_QUOTE$QUOTE_CHAR", "$COLUMN_START_QUOTE$QUOTE_CHAR_SUB")
        result = result.replace("$QUOTE_CHAR$COLUMN_END_QUOTE", "$QUOTE_CHAR_SUB$COLUMN_END_QUOTE")
        return result.replace(QUOTE_CHAR, QUOTE_CHAR_SUB)
    }

    private fun verifyColumns(columns: List<String>) {
        if (Constants.healthcareProvidersCSVHeader != columns) {
            logger.warn("The structure of the file has changed.")
        }
    }

    companion object {
        private const val QUOTE_CHAR = "\"\""
        private const val QUOTE_CHAR_SUB = "_Q_"
        private const val COLUMN_START_QUOTE = ",\""
        private const val COLUMN_END_QUOTE = "\","
    }
}
