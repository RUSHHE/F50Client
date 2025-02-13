package org.rushhe.f50client.utils

import androidx.annotation.RequiresApi
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.datetime.format.parse
import kotlinx.datetime.toLocalDateTime

/**
 * 将字符串yy,MM,dd,HH,mm,ss,±HHMM解析为Instant
 * @return Instant
 */
@RequiresApi(26)
fun String.parseSMSDateToInstant(): Instant {
    // 动态计算本世纪的年份
    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    val centuryYear = currentYear / 100 * 100 // 获取当前世纪首年
    // 定义输入格式
    val inputFormat = DateTimeComponents.Format {
        yearTwoDigits(centuryYear)
        char(',')
        monthNumber()
        char(',')
        dayOfMonth()
        char(',')
        hour()
        char(',')
        minute()
        char(',')
        second()
        char(',')
        offset(UtcOffset.Formats.FOUR_DIGITS)
    }

    // 解析字符串为 DateTimeComponents
    val components = DateTimeComponents.parse(this@parseSMSDateToInstant, inputFormat)

    return components.toInstantUsingOffset()
}

expect fun String.formatSMSDate(format: String): String

expect fun LocalDateTime.format(format: String): String