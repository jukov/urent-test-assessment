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

public enum class CitiesError {
    Network,
    Server,
    InvalidResponse,
    Unknown
}
