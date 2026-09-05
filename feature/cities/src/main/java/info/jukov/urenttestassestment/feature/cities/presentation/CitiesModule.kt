package info.jukov.urenttestassestment.feature.cities.presentation

import info.jukov.urenttestassestment.feature.cities.data.CitiesApi
import info.jukov.urenttestassestment.feature.cities.data.DefaultCitiesRepository
import info.jukov.urenttestassestment.feature.cities.domain.CitiesRepository
import java.util.Locale
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

public val citiesModule = module {
    single { CitiesApi(get()) }
    single<CitiesRepository> { DefaultCitiesRepository(get()) }
    factory { CountryNameFormatter(Locale.getDefault()) }
    viewModel { CitiesViewModel(get(), get(), get()) }
}
