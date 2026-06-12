package com.example.kmptemplate.time

import com.example.kmptemplate.util.platformCurrentDate
import com.example.kmptemplate.util.platformCurrentDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

interface TimeProvider {
    fun currentDate(): LocalDate
    fun currentDateTime(): LocalDateTime
}

class DefaultTimeProvider : TimeProvider {
    override fun currentDate(): LocalDate = platformCurrentDate()
    override fun currentDateTime(): LocalDateTime = platformCurrentDateTime()
}

