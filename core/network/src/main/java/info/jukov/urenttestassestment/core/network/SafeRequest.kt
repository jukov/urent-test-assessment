package info.jukov.urenttestassestment.core.network

import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.ContentConvertException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

@Suppress("SwallowedException", "TooGenericExceptionCaught")
public suspend inline fun <reified T> safeRequest(request: suspend () -> HttpResponse): NetworkResult<T> = try {
    NetworkResult.Success(request().body())
} catch (exception: CancellationException) {
    throw exception
} catch (exception: HttpRequestTimeoutException) {
    NetworkResult.Failure(NetworkError.Timeout)
} catch (exception: ConnectTimeoutException) {
    NetworkResult.Failure(NetworkError.Timeout)
} catch (exception: SocketTimeoutException) {
    NetworkResult.Failure(NetworkError.Timeout)
} catch (exception: ResponseException) {
    NetworkResult.Failure(NetworkError.Http(exception.response.status.value))
} catch (exception: ContentConvertException) {
    NetworkResult.Failure(NetworkError.Serialization)
} catch (exception: SerializationException) {
    NetworkResult.Failure(NetworkError.Serialization)
} catch (exception: IOException) {
    NetworkResult.Failure(NetworkError.Connection)
} catch (exception: Exception) {
    NetworkResult.Failure(NetworkError.Unknown(exception))
}
