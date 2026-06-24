package com.renz.golfperformancetracker

import android.app.Application
import com.renz.golfperformancetracker.data.NetworkConfig
import com.renz.golfperformancetracker.data.di.dataModule
import com.renz.golfperformancetracker.ui.di.uiModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import timber.log.Timber

class GolfApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@GolfApp)
            modules(
                module {
                    single {
                        NetworkConfig(
                            apiBaseUrl = BuildConfig.API_BASE_URL,
                            isDebug = BuildConfig.DEBUG,
                        )
                    }
                },
                dataModule,
                uiModule,
            )
        }
    }
}
