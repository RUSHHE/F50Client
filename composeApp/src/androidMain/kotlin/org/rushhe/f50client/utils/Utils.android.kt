package org.rushhe.f50client.utils

import android.os.Build
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

actual fun LocalDateTime.format(format: String): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // 使用新 API（java.time 或 kotlinx-datetime）
        DateTimeFormatter.ofPattern(format).format(this.toJavaLocalDateTime())
    } else {
        // 使用老 API（java.util.Date 和 SimpleDateFormat）
        val date = Date(
            this.year - 1900, // 年份需要减去 1900
            this.monthNumber - 1, // 月份需要减去 1
            this.dayOfMonth,
            this.hour,
            this.minute,
            this.second
        )
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        formatter.format(date)
    }
}

actual fun String.formatSMSDate(format: String): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return this.parseSMSDateToInstant()
            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            .format(format)
    } else {
        // 定义原始格式："yy,MM,dd,HH,mm,ss, Z"
        val inputFormat = SimpleDateFormat("yy,MM,dd,HH,mm,ss,Z", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("GMT+0800") // 设定正确的时区

        val outputFormat = SimpleDateFormat(format, Locale.getDefault())

        val date = inputFormat.parse(this)
        return date?.let { outputFormat.format(it) }
            ?: throw IllegalArgumentException("Invalid date format")
    }
}