package org.rushhe.f50client.repository

import android.content.Context
import android.content.SharedPreferences
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.rushhe.f50client.db.F50ClientDatabase
import org.rushhe.f50client.di.F50ClientWrapper

actual fun platformModule() = module {
    // SQLDelight
    single {
        val driver =
            AndroidSqliteDriver(F50ClientDatabase.Schema, get(), "f50client.db")

        F50ClientWrapper(F50ClientDatabase(driver))
    }
    // Ktor
    single<HttpClientEngine> { OkHttp.create() }
    // multiplatform-settings
    single<Settings> {
        val delegate: SharedPreferences =
            androidContext().getSharedPreferences("f50client", Context.MODE_PRIVATE)
        SharedPreferencesSettings(delegate)
    }
}