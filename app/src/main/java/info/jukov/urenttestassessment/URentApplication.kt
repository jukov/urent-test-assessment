package info.jukov.urenttestassessment

import android.app.Application
import info.jukov.urenttestassessment.core.logging.Logging
import info.jukov.urenttestassessment.core.network.networkModule
import info.jukov.urenttestassessment.feature.cities.di.citiesModule
import info.jukov.urenttestassessment.feature.map.di.mapModule
import info.jukov.urenttestassessment.startup.startupModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class URentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupLogging()
        setupDi()
    }

    private fun setupLogging() {
        Logging.initialize(isDebug = BuildConfig.DEBUG)
    }

    private fun setupDi() {
        startKoin {
            androidContext(this@URentApplication)
            modules(networkModule, citiesModule, mapModule, startupModule)
        }
    }
}
