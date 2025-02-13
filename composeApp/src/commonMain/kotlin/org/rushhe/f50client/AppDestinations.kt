package org.rushhe.f50client

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String,
    val route: String,
) {
    HOME("首页", Icons.Default.Home, "首页", "home"),
    SMS("信息", Icons.Default.Sms, "信息", "sms"),
    SETTINGS("设置", Icons.Default.Settings, "设置", "settings"),
}