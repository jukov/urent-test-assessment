package info.jukov.urenttestassessment.feature.map.di

import info.jukov.urenttestassessment.feature.map.data.DefaultMapRepository
import info.jukov.urenttestassessment.feature.map.data.MapApi
import info.jukov.urenttestassessment.feature.map.domain.MapRepository
import info.jukov.urenttestassessment.feature.map.presentation.MapViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

public val mapModule = module {
    single { MapApi(get()) }
    single<MapRepository> { DefaultMapRepository(get()) }
    viewModel { MapViewModel(get(), get()) }
}
