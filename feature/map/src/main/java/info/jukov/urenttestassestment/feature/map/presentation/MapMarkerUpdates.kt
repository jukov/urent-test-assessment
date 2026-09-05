package info.jukov.urenttestassestment.feature.map.presentation

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Submit immutable state lists on Main. Only the newest result is published on Main;
// comparison, sorting, feature construction and JSON serialization run on the worker.
internal class MapMarkerUpdates(
    scope: CoroutineScope,
    workerDispatcher: CoroutineDispatcher = Dispatchers.Default,
    publish: (String) -> Unit
) {
    private val requests = Channel<List<MapCityUi>>(Channel.CONFLATED)
    private var requested: List<MapCityUi>? = null

    init {
        scope.launch {
            val data = MapMarkerData()
            var published: String? = null
            requests.consumeAsFlow().collectLatest { cities ->
                val json = withContext(workerDispatcher) {
                    // collectLatest waits for cancelled work before starting the next
                    // item, so this mutable snapshot has a single worker at a time.
                    data.update(cities)
                    data.json
                }
                if (cities === requested && json !== published) {
                    publish(json)
                    published = json
                }
            }
        }
    }

    fun submit(cities: List<MapCityUi>) {
        if (cities === requested) return
        requested = cities
        requests.trySend(cities)
    }
}
