package info.jukov.urenttestassestment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.test.platform.app.InstrumentationRegistry
import info.jukov.urenttestassestment.core.network.NetworkConfig
import info.jukov.urenttestassestment.feature.cities.domain.CitiesRepository
import info.jukov.urenttestassestment.feature.cities.presentation.CitiesViewModel
import info.jukov.urenttestassestment.feature.map.domain.MapRepository
import info.jukov.urenttestassestment.feature.map.presentation.MapViewModel
import info.jukov.urenttestassestment.startup.InternetAvailability
import info.jukov.urenttestassestment.startup.StartupViewModel
import io.ktor.client.HttpClient
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

class AppGraphTest {
    @Test
    fun applicationGraphResolvesRepositoriesAndAllViewModels() {
        // Inspect the graph started by URentApplication, including its actual module list.
        val koin = GlobalContext.get()
        assertNotNull(koin.get<NetworkConfig>())
        assertSame(koin.get<HttpClient>(), koin.get<HttpClient>())
        assertSame(koin.get<CitiesRepository>(), koin.get<CitiesRepository>())
        assertSame(koin.get<MapRepository>(), koin.get<MapRepository>())
        assertNotNull(koin.get<InternetAvailability>())
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val store = ViewModelStore()
            try {
                val provider = ViewModelProvider.create(
                    store,
                    viewModelFactory {
                        initializer { koin.get<CitiesViewModel> { parametersOf(SavedStateHandle()) } }
                        initializer { koin.get<MapViewModel> { parametersOf(SavedStateHandle()) } }
                        initializer { koin.get<StartupViewModel>() }
                    }
                )
                assertNotNull(provider[CitiesViewModel::class])
                assertNotNull(provider[MapViewModel::class])
                assertNotNull(provider[StartupViewModel::class])
            } finally {
                // Release ViewModels and cancel their startup work before finishing the test.
                store.clear()
            }
        }
    }
}
