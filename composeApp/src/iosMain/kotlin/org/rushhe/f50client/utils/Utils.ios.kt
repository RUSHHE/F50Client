package org.rushhe.f50client.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateFormatter

actual fun LocalDateTime.format(format: String): String {
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = format

    return dateFormatter.stringFromDate(
        NSCalendar.currentCalendar.dateFromComponents(this.toNSDateComponents())
            ?: throw IllegalArgumentException("Could not convert kotlin date to NSDate $this")
    )
}

actual fun String.formatSMSDate(format: String): String {
    return this.parseSMSDateToInstant()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .format(format)
}