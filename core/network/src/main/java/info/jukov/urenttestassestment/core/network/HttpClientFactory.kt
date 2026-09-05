package info.jukov.urenttestassestment.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

public fun createHttpClient(config: NetworkConfig = NetworkConfig(), debugLogging: Boolean = false): HttpClient =
    createHttpClient(Android.create(), config, debugLogging)

public fun createHttpClient(
    engine: HttpClientEngine,
    config: NetworkConfig = NetworkConfig(),
    debugLogging: Boolean = false,
    logger: Logger = Logger.DEFAULT
): HttpClient = HttpClient(engine) {
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
            this.logger = RedactingLogger(logger)
            level = LogLevel.HEADERS
            sanitizeHeader { header -> isSensitiveHeader(header) }
        }
    }
}

private fun isSensitiveHeader(header: String): Boolean = header.lowercase() in SENSITIVE_HEADERS

private val SENSITIVE_HEADERS = setOf(
    HttpHeaders.Authorization.lowercase(),
    HttpHeaders.Cookie.lowercase(),
    HttpHeaders.ProxyAuthorization.lowercase(),
    HttpHeaders.SetCookie.lowercase(),
    "api-key",
    "x-api-key"
)

private class RedactingLogger(private val delegate: Logger) : Logger {
    override fun log(message: String) {
        delegate.log(
            URL_WITH_QUERY.replace(message) { result ->
                result.groupValues[1] + "?<redacted>"
            }
        )
    }

    private companion object {
        val URL_WITH_QUERY: Regex = """(https?://[^?\s]+)\?[^\s]+""".toRegex()
    }
}
