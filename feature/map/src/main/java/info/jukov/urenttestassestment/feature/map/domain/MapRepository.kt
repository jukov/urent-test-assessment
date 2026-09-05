package info.jukov.urenttestassestment.feature.map.domain

import info.jukov.urenttestassestment.core.model.City

public interface MapRepository {
    public suspend fun cities(viewport: MapViewport): MapResult
}

public sealed interface MapResult {
    public data class Success(val cities: List<City>) : MapResult
    public data object Failure : MapResult
}
