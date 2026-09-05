package info.jukov.urenttestassestment.feature.cities.domain

import info.jukov.urenttestassestment.core.model.City
import info.jukov.urenttestassestment.core.pagination.Page

public interface CitiesRepository {
    public suspend fun cities(query: String?, page: Int, limit: Int): CitiesResult<Page<City>>
}

public sealed interface CitiesResult<out T> {
    public data class Success<T>(val value: T) : CitiesResult<T>
    public data class Failure(val error: CitiesError) : CitiesResult<Nothing>
}

public sealed interface CitiesError {
    public data object Network : CitiesError
    public data object Server : CitiesError
    public data object InvalidResponse : CitiesError
    public data object Unknown : CitiesError
}
