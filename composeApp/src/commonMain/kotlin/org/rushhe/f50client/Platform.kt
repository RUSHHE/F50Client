package org.rushhe.f50client

interface Platform {
    val name: String
    val type: Pair<PlatformType, Double>
}

@Suppress("EnumEntryName")
enum class PlatformType {
    Android, iOS
}

expect fun getPlatform(): Platform