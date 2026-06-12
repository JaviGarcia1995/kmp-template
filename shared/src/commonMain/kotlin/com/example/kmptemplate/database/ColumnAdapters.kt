@file:Suppress("DEPRECATION")

package com.example.kmptemplate.database

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@OptIn(kotlin.time.ExperimentalTime::class)
object LocalDateTimeAdapter : ColumnAdapter<LocalDateTime, Long> {
    override fun decode(databaseValue: Long): LocalDateTime =
        Instant.fromEpochMilliseconds(databaseValue).toLocalDateTime(TimeZone.UTC)

    override fun encode(value: LocalDateTime): Long =
        value.toInstant(TimeZone.UTC).toEpochMilliseconds()
}
