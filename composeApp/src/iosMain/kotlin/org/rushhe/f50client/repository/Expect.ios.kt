package org.rushhe.f50client.repository

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.rushhe.f50client.db.F50ClientDatabase
import org.rushhe.f50client.di.F50ClientWrapper
import org.rushhe.f50client.helper.NotificationHelper
import platform.Foundation.NSUserDefaults

actual fun platformModule() = module {
    // 通知帮助类
    singleOf(::NotificationHelper)
    // SQLDelight
    single {
        val driver = NativeSqliteDriver(F50ClientDatabase.Schema, "f50client.db")
        F50ClientWrapper(F50ClientDatabase(driver))
    }
    // Ktor
    single<HttpClientEngine> { Darwin.create() }
    // multiplatform-settings
    single<Settings> {
        val delegate: NSUserDefaults = NSUserDefaults.standardUserDefaults
        NSUserDefaultsSettings(delegate)
    }
}