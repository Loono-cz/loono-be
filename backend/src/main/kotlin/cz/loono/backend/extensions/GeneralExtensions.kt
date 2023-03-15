package cz.loono.backend.extensions

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

// Using `Clock.systemUTC()` here, since UTC used in ClockConfiguration too
fun Instant.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, Clock.systemUTC().zone)

fun LocalDateTime.atUTCOffset(): OffsetDateTime = this.atOffset(ZoneOffset.UTC)

fun String?.trimProviderImport(): String? {
    return if (this.isNullOrEmpty()) {
        null
    } else {
        this.trim()
    }
}

fun String.trimProviderNumber(): String {
    return if (this.contains('.')) {
        val splited = this.split('.')
        splited[0]
    } else {
        this
    }
}
