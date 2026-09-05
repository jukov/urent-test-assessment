package info.jukov.urenttestassestment

import android.app.Application
import info.jukov.urenttestassestment.core.logging.Logging

class URentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Logging.initialize(isDebug = BuildConfig.DEBUG)
    }
}
