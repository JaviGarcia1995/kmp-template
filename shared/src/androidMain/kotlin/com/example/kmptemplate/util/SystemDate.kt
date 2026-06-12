@file:Suppress("DEPRECATION")

package com.example.kmptemplate.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal actual fun platformCurrentDate(): LocalDate {
    val millis = System.currentTimeMillis()
    val instant = Instant.fromEpochMilliseconds(millis)
    val zone = TimeZone.currentSystemDefault()
    return instant.toLocalDateTime(zone).date
}

@OptIn(ExperimentalTime::class)
internal actual fun platformCurrentDateTime(): LocalDateTime {
    val millis = System.currentTimeMillis()
    val instant = Instant.fromEpochMilliseconds(millis)
    val zone = TimeZone.currentSystemDefault()
    return instant.toLocalDateTime(zone)
}
