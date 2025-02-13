package org.rushhe.f50client.viewmodel

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsViewModel : ViewModel(), KoinComponent {
    private val settings: Settings by inject()

    private val _routerAddress = MutableStateFlow(
        settings.getString("router_address", "192.168.0.1")
    )
    val routerAddress: StateFlow<String> = _routerAddress.asStateFlow()

    private val _pollingInterval = MutableStateFlow(
        settings.getInt("polling_interval", 1)
    )
    val pollingInterval: StateFlow<Int> = _pollingInterval.asStateFlow()

    private val _darkMode = MutableStateFlow(
        enumValues<DarkMode>().getOrElse(settings["dark_mode", DarkMode.SYSTEM.ordinal]) { DarkMode.SYSTEM }
    )
    val darkMode: StateFlow<DarkMode> = _darkMode.asStateFlow()

    fun setRouterAddress(address: String) {
        _routerAddress.value = address
        settings.putString("router_address", address)
    }

    fun setPollingInterval(interval: Int) {
        _pollingInterval.value = interval
        settings.putInt("polling_interval", interval)
    }

    fun setDarkMode(mode: DarkMode) {
        _darkMode.value = mode
        settings.putInt("dark_mode", mode.ordinal)
    }
}

enum class DarkMode(val title: String) {
    SYSTEM("跟随系统"),
    OPEN("总是开启"),
    CLOSE("总是关闭")
}