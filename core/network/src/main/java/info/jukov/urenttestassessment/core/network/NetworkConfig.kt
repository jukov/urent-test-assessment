package info.jukov.urenttestassessment.core.network

public data class NetworkConfig(
    val baseUrl: String = DEFAULT_BASE_URL,
    val connectTimeoutMillis: Long = DEFAULT_CONNECT_TIMEOUT_MILLIS,
    val requestTimeoutMillis: Long = DEFAULT_REQUEST_TIMEOUT_MILLIS,
    val socketTimeoutMillis: Long = DEFAULT_SOCKET_TIMEOUT_MILLIS
) {
    public companion object {
        public const val DEFAULT_BASE_URL: String = "http://dev-dep.tools.urent.tech:8080/"
        public const val DEFAULT_CONNECT_TIMEOUT_MILLIS: Long = 10_000L
        public const val DEFAULT_REQUEST_TIMEOUT_MILLIS: Long = 30_000L
        public const val DEFAULT_SOCKET_TIMEOUT_MILLIS: Long = 30_000L
    }
}
