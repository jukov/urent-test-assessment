package info.jukov.urenttestassestment

import android.app.Application
import info.jukov.urenttestassestment.core.logging.Logging
import info.jukov.urenttestassestment.core.network.networkModule
import info.jukov.urenttestassestment.feature.cities.presentation.citiesModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class URentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Logging.initialize(isDebug = BuildConfig.DEBUG)
        startKoin {
            androidContext(this@URentApplication)
            modules(networkModule, citiesModule)
        }
    }
}
