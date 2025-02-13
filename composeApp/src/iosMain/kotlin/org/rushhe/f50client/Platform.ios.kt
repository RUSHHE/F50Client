package org.rushhe.f50client

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val type: Pair<PlatformType, Double> = Pair(PlatformType.iOS, UIDevice.currentDevice.systemVersion().toDouble())
}

actual fun getPlatform(): Platform = IOSPlatform()