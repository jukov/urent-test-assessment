package info.jukov.urenttestassestment.core.network

import io.ktor.client.HttpClient
import org.koin.dsl.module
import org.koin.dsl.onClose

public val networkModule = createNetworkModule()

internal fun createNetworkModule(
    clientFactory: (NetworkConfig) -> HttpClient = { config ->
        createHttpClient(config = config, debugLogging = BuildConfig.DEBUG)
    }
) = module {
    single { NetworkConfig() }
    single { clientFactory(get()) } onClose { client -> client?.close() }
}
