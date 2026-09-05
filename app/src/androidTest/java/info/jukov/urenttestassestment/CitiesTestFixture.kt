package info.jukov.urenttestassestment

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import info.jukov.urenttestassestment.core.model.City
import info.jukov.urenttestassestment.core.pagination.Page
import info.jukov.urenttestassestment.core.ui.city.CityDetails
import info.jukov.urenttestassestment.core.ui.city.CountryNameFormatter
import info.jukov.urenttestassestment.feature.cities.domain.CitiesRepository
import info.jukov.urenttestassestment.feature.cities.domain.CitiesResult
import info.jukov.urenttestassestment.feature.cities.presentation.CitiesRoute
import info.jukov.urenttestassestment.feature.cities.presentation.CitiesViewModel
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CompletableDeferred

internal class CitiesTestFixture {
    data class Request(val query: String?, val page: Int, val limit: Int) {
        val response = CompletableDeferred<CitiesResult<Page<City>>>()
    }

    val requests = CopyOnWriteArrayList<Request>()
    private val store = ViewModelStore()
    lateinit var viewModel: CitiesViewModel

    private val repository = object : CitiesRepository {
        override suspend fun cities(query: String?, page: Int, limit: Int): CitiesResult<Page<City>> {
            val request = Request(query, page, limit)
            requests += request
            return request.response.await()
        }
    }

    fun create() {
        viewModel = ViewModelProvider.create(
            store,
            viewModelFactory {
                initializer { newViewModel(SavedStateHandle()) }
            }
        )[CitiesViewModel::class]
    }

    // Use the NavBackStackEntry's ViewModelStore and SavedStateHandle, as CitiesRoute does in the app.
    @Composable
    fun Route(onOpenCity: (CityDetails) -> Unit) {
        val scoped: CitiesViewModel = viewModel { newViewModel(createSavedStateHandle()) }
        CitiesRoute(onOpenCity, scoped)
    }

    private fun newViewModel(savedState: SavedStateHandle) =
        CitiesViewModel(repository, CountryNameFormatter(Locale.ENGLISH), savedState)

    fun close() = store.clear()

    fun succeed(index: Int, cities: List<City> = listOf(city), total: Long = cities.size.toLong()) {
        val request = requests[index]
        request.response.complete(CitiesResult.Success(Page(cities, request.page, request.limit, total)))
    }

    companion object {
        val city = City(7, "Moscow & York", "RU", 12_655_000, 55.75, 37.61)
    }
}
