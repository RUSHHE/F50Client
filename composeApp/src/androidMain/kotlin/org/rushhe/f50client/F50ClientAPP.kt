package org.rushhe.f50client

import android.app.Application
import org.rushhe.f50client.di.initKoin
import org.koin.android.ext.koin.androidContext

class F50ClientAPP : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@F50ClientAPP)
        }
    }
}