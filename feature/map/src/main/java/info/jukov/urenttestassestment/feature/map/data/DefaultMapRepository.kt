package info.jukov.urenttestassestment.feature.map.data

import info.jukov.urenttestassestment.core.model.City
import info.jukov.urenttestassestment.core.network.NetworkResult
import info.jukov.urenttestassestment.feature.map.domain.MapRepository
import info.jukov.urenttestassestment.feature.map.domain.MapResult
import info.jukov.urenttestassestment.feature.map.domain.MapViewport

internal class DefaultMapRepository(private val api: MapApi) : MapRepository {
    override suspend fun cities(viewport: MapViewport): MapResult = when (val result = api.cities(viewport)) {
        is NetworkResult.Success -> MapResult.Success(
            result.value.items.map { City(it.id, it.name, it.country, it.pop, it.lat, it.lon) }.distinctBy(City::id)
        )

        is NetworkResult.Failure -> MapResult.Failure
    }
}
