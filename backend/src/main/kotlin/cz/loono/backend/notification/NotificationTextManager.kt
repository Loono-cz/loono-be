package cz.loono.backend.notification

import cz.loono.backend.api.dto.BadgeTypeDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.exception.LoonoBackendException
import org.springframework.http.HttpStatus
import java.io.FileInputStream
import java.util.Properties

class NotificationTextManager {

    companion object {
        private val propertiesFile = this::class.java.getResource("/texts/notification.properties").file
        private val properties = Properties()
    }

    init {
        FileInputStream(propertiesFile).use {
            properties.load(it)
        }
    }

    fun getText(propertyName: String): String = properties[propertyName].toString()

    fun getText(propertyName: String, type: ExaminationTypeDto): String =
        replaceType(getText(propertyName), type)

    fun getText(propertyName: String, interval: Int): String =
        replaceInterval(getText(propertyName), interval)

    fun getText(propertyName: String, sex: SexDto): String =
        replaceSelfExam(getText(propertyName), sex)

    fun getText(propertyName: String, interval: Int, badge: BadgeTypeDto): String =
        replaceBadge(getText(propertyName, interval), badge)

    private fun replaceType(text: String, type: ExaminationTypeDto): String =
        text.replace("_TYPE_", getText("type.${type.name.lowercase()}"))

    private fun replaceBadge(text: String, badge: BadgeTypeDto): String =
        text.replace("_BADGE_", getText("badge.${badge.name.lowercase()}"))

    private fun replaceSelfExam(text: String, sex: SexDto): String =
        text.replace("_SELF1_", getText("self.${sex.name.lowercase()}.1"))
            .replace("_SELF2_", getText("self.${sex.name.lowercase()}.2"))

    private fun replaceInterval(text: String, interval: Int): String =
        when (interval) {
            1 -> text.replace("_INTERVAL_", "$interval ${getText("years.1")}")
            2, 3, 4 -> text.replace("_INTERVAL_", "$interval ${getText("years.2")}")
            in 5..100 -> text.replace("_INTERVAL_", "$interval ${getText("years.5")}")
            else -> throw LoonoBackendException(HttpStatus.BAD_REQUEST)
        }
}
