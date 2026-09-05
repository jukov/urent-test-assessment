package info.jukov.urenttestassestment.core.logging

import timber.log.Timber

/** Configures the process-wide logging policy from the application composition root. */
object Logging {
    private val debugTree = Timber.DebugTree()

    @Volatile
    var isDetailedLoggingEnabled: Boolean = false
        private set

    @Synchronized
    fun initialize(isDebug: Boolean) {
        Timber.uprootAll()
        isDetailedLoggingEnabled = isDebug
        if (isDebug) {
            Timber.plant(debugTree)
        }
    }
}
