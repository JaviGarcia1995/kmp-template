@file:Suppress("DEPRECATION")

package com.example.kmptemplate.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToLong
import kotlin.time.ExperimentalTime
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

@OptIn(ExperimentalTime::class)
internal actual fun platformCurrentDate(): LocalDate {
    val now = NSDate()
    val millis = (now.timeIntervalSince1970() * 1000.0).roundToLong()
    val instant = Instant.fromEpochMilliseconds(millis)
    val zone = TimeZone.currentSystemDefault()
    return instant.toLocalDateTime(zone).date
}

@OptIn(ExperimentalTime::class)
internal actual fun platformCurrentDateTime(): LocalDateTime {
    val now = NSDate()
    val millis = (now.timeIntervalSince1970() * 1000.0).roundToLong()
    val instant = Instant.fromEpochMilliseconds(millis)
    val zone = TimeZone.currentSystemDefault()
    return instant.toLocalDateTime(zone)
}
