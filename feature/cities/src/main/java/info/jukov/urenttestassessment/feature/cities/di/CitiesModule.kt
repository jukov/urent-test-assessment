package info.jukov.urenttestassessment.feature.cities.di

import info.jukov.urenttestassessment.core.ui.city.CountryNameFormatter
import info.jukov.urenttestassessment.feature.cities.data.CitiesApi
import info.jukov.urenttestassessment.feature.cities.data.DefaultCitiesRepository
import info.jukov.urenttestassessment.feature.cities.domain.CitiesRepository
import info.jukov.urenttestassessment.feature.cities.presentation.CitiesViewModel
import java.util.Locale
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

public val citiesModule = module {
    single { CitiesApi(get()) }
    single<CitiesRepository> { DefaultCitiesRepository(get()) }
    factory { CountryNameFormatter(Locale.getDefault()) }
    viewModel { CitiesViewModel(get(), get(), get()) }
}
