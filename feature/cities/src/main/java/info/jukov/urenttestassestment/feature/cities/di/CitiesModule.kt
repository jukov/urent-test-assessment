package info.jukov.urenttestassestment.feature.cities.di

import info.jukov.urenttestassestment.core.ui.city.CountryNameFormatter
import info.jukov.urenttestassestment.feature.cities.data.CitiesApi
import info.jukov.urenttestassestment.feature.cities.data.DefaultCitiesRepository
import info.jukov.urenttestassestment.feature.cities.domain.CitiesRepository
import info.jukov.urenttestassestment.feature.cities.presentation.CitiesViewModel
import java.util.Locale
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

public val citiesModule = module {
    single { CitiesApi(get()) }
    single<CitiesRepository> { DefaultCitiesRepository(get()) }
    factory { CountryNameFormatter(Locale.getDefault()) }
    viewModel { CitiesViewModel(get(), get(), get()) }
}
