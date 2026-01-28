package com.example.todolist.data.formatter

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DateFormatter @Inject constructor() {
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        .withZone(ZoneId.systemDefault())

    fun format(millis: Long): String {
        return formatter.format(Instant.ofEpochMilli(millis))
    }

    fun formatOrNull(millis: Long?): String? {
        return millis?.let { format(it) }
    }
}
