package info.jukov.urenttestassestment.core.network

import info.jukov.urenttestassestment.core.logging.AppLogger
import info.jukov.urenttestassestment.core.logging.Logger
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

public fun createHttpClient(config: NetworkConfig = NetworkConfig(), debugLogging: Boolean = false): HttpClient =
    HttpClient(OkHttp) { configureNetwork(config, debugLogging, AppLogger) }

public fun createHttpClient(
    engine: HttpClientEngine,
    config: NetworkConfig = NetworkConfig(),
    debugLogging: Boolean = false
): HttpClient = createHttpClient(engine, config, debugLogging, AppLogger)

internal fun createHttpClient(
    engine: HttpClientEngine,
    config: NetworkConfig,
    debugLogging: Boolean,
    logger: Logger
): HttpClient = HttpClient(engine) {
    configureNetwork(config, debugLogging, logger)
}

private fun HttpClientConfig<*>.configureNetwork(config: NetworkConfig, debugLogging: Boolean, logger: Logger) {
    expectSuccess = true

    defaultRequest {
        url(config.baseUrl)
        accept(ContentType.Application.Json)
    }
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }
    install(HttpTimeout) {
        connectTimeoutMillis = config.connectTimeoutMillis
        requestTimeoutMillis = config.requestTimeoutMillis
        socketTimeoutMillis = config.socketTimeoutMillis
    }
    if (debugLogging) {
        install(Logging) {
            this.logger = KtorLoggerAdapter(logger.withTag(NETWORK_LOG_TAG))
            level = LogLevel.ALL
        }
    }
}

private class KtorLoggerAdapter(private val logger: Logger) : io.ktor.client.plugins.logging.Logger {
    override fun log(message: String) {
        logger.debug(message)
    }
}

private const val NETWORK_LOG_TAG = "Ktor"
