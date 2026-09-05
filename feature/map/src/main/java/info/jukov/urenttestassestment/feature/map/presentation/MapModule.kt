package info.jukov.urenttestassestment.feature.map.presentation

import info.jukov.urenttestassestment.feature.map.data.DefaultMapRepository
import info.jukov.urenttestassestment.feature.map.data.MapApi
import info.jukov.urenttestassestment.feature.map.domain.MapRepository
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

public val mapModule = module {
    single { MapApi(get()) }
    single<MapRepository> { DefaultMapRepository(get()) }
    viewModel { MapViewModel(get(), get()) }
}
