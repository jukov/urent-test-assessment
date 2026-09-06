package info.jukov.urenttestassessment.feature.map.domain

import info.jukov.urenttestassessment.core.model.City

public interface MapRepository {
    public suspend fun cities(viewport: MapViewport): MapResult
}

public sealed interface MapResult {
    public data class Success(val cities: List<City>) : MapResult
    public data object Failure : MapResult
}
