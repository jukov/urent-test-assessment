package info.jukov.urenttestassessment.core.network

public sealed interface NetworkResult<out T> {
    public data class Success<T>(val value: T) : NetworkResult<T>

    public data class Failure(val error: NetworkError) : NetworkResult<Nothing>
}

public sealed interface NetworkError {
    public data class Http(val statusCode: Int) : NetworkError

    public data object Timeout : NetworkError

    public data object Connection : NetworkError

    public data object Serialization : NetworkError

    public data class Unknown(val cause: Throwable) : NetworkError
}
