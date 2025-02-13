package org.rushhe.f50client.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module
import org.rushhe.f50client.remote.F50ClientApi
import org.rushhe.f50client.repository.F50ClientRepository
import org.rushhe.f50client.repository.F50ClientRepositoryInterface
import org.rushhe.f50client.repository.platformModule
import org.rushhe.f50client.viewmodel.SMSViewModel
import org.rushhe.f50client.viewmodel.SettingsViewModel

val viewModelModule = module {
    singleOf(::SMSViewModel)
    singleOf(::SettingsViewModel)
}

fun initKoin(enableNetworkLogs: Boolean = false, appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(viewModelModule)
        modules(commonModule(enableNetworkLogs = enableNetworkLogs), platformModule())
    }

// called by iOS etc
fun initKoin() = initKoin(enableNetworkLogs = false) {}

fun commonModule(enableNetworkLogs: Boolean) = module {
    singleOf(::createJson)
    single { createHttpClient(get(), get(), enableNetworkLogs = enableNetworkLogs) }
    singleOf(::F50ClientApi)
    singleOf(::F50ClientRepository).bind<F50ClientRepositoryInterface>()

    single { CoroutineScope(Dispatchers.Default + SupervisorJob()) }
}

fun createJson() = Json { isLenient = true; ignoreUnknownKeys = true }


fun createHttpClient(httpClientEngine: HttpClientEngine, json: Json, enableNetworkLogs: Boolean) =
    HttpClient(httpClientEngine) {
        install(ContentNegotiation) {
            json(json)
        }
        if (enableNetworkLogs) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
        }
    }