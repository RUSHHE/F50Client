package org.rushhe.f50client

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val type: Pair<PlatformType, Double> = Pair(PlatformType.Android, Build.VERSION.SDK_INT.toDouble())
}

actual fun getPlatform(): Platform = AndroidPlatform()