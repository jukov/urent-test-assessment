package info.jukov.urenttestassestment.startup

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal fun interface InternetAvailability {
    suspend fun isAvailable(): Boolean
}

/** Reads Android's validation result; never probes the API or another host. */
internal class AndroidInternetAvailability(private val connectivity: ConnectivityManager) : InternetAvailability {
    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        val network = connectivity.activeNetwork ?: return@withContext false
        val capabilities = connectivity.getNetworkCapabilities(network) ?: return@withContext false
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
