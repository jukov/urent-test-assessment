package info.jukov.urenttestassestment

import android.app.Application
import info.jukov.urenttestassestment.core.logging.Logging
import info.jukov.urenttestassestment.core.network.networkModule
import info.jukov.urenttestassestment.feature.cities.di.citiesModule
import info.jukov.urenttestassestment.feature.map.di.mapModule
import info.jukov.urenttestassestment.startup.startupModule
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
