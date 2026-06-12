package com.example.kmptemplate.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

internal expect fun platformCurrentDate(): LocalDate
internal expect fun platformCurrentDateTime(): LocalDateTime

