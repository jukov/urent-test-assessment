package info.jukov.urenttestassessment.feature.map.data

import info.jukov.urenttestassessment.core.model.City
import info.jukov.urenttestassessment.core.network.NetworkResult
import info.jukov.urenttestassessment.feature.map.domain.MapRepository
import info.jukov.urenttestassessment.feature.map.domain.MapResult
import info.jukov.urenttestassessment.feature.map.domain.MapViewport

internal class DefaultMapRepository(private val api: MapApi) : MapRepository {
    override suspend fun cities(viewport: MapViewport): MapResult = when (val result = api.cities(viewport)) {
        is NetworkResult.Success -> MapResult.Success(
            result.value.items.map { City(it.id, it.name, it.country, it.pop, it.lat, it.lon) }.distinctBy(City::id)
        )

        is NetworkResult.Failure -> MapResult.Failure
    }
}
