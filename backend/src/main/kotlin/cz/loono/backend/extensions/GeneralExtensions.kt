package cz.loono.backend.extensions

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

// Using `Clock.systemUTC()` here, since UTC used in ClockConfiguration too
fun Instant.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, Clock.systemUTC().zone)

fun LocalDateTime.atUTCOffset() = this.atOffset(ZoneOffset.UTC)
