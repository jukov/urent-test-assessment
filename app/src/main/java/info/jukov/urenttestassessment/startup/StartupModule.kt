package info.jukov.urenttestassessment.startup

import android.content.Context
import android.net.ConnectivityManager
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val startupModule = module {
    single<InternetAvailability> {
        AndroidInternetAvailability(get<Context>().getSystemService(ConnectivityManager::class.java))
    }
    viewModel { StartupViewModel(get()) }
}
