package info.jukov.urenttestassessment.feature.cities.data

import info.jukov.urenttestassessment.core.model.City
import info.jukov.urenttestassessment.core.network.NetworkError
import info.jukov.urenttestassessment.core.network.NetworkResult
import info.jukov.urenttestassessment.core.pagination.Page
import info.jukov.urenttestassessment.feature.cities.domain.CitiesError
import info.jukov.urenttestassessment.feature.cities.domain.CitiesRepository
import info.jukov.urenttestassessment.feature.cities.domain.CitiesResult

internal class DefaultCitiesRepository(private val api: CitiesApi) : CitiesRepository {
    override suspend fun cities(query: String?, page: Int, limit: Int): CitiesResult<Page<City>> =
        when (val result = api.cities(query, page, limit)) {
            is NetworkResult.Success -> CitiesResult.Success(result.value.toDomain())
            is NetworkResult.Failure -> CitiesResult.Failure(result.error.toDomain())
        }
}

private fun CitiesResponseDto.toDomain() = Page(
    items = items.map { dto ->
        City(dto.id, dto.name, dto.country, dto.pop, dto.lat, dto.lon)
    },
    page = page,
    limit = limit,
    total = total
)

private fun NetworkError.toDomain(): CitiesError = when (this) {
    NetworkError.Connection, NetworkError.Timeout -> CitiesError.Network
    is NetworkError.Http -> CitiesError.Server
    NetworkError.Serialization -> CitiesError.InvalidResponse
    is NetworkError.Unknown -> CitiesError.Unknown
}
