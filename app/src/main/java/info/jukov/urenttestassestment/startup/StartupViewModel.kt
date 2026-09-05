package info.jukov.urenttestassestment.startup

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container

internal enum class StartupState { Checking, Offline, Retrying, Ready }

/** Admission lasts for this Activity ViewModel, including configuration changes, but not process death. */
internal class StartupViewModel(private val internet: InternetAvailability) :
    ViewModel(),
    ContainerHost<StartupState, Nothing> {
    override val container = container<StartupState, Nothing>(StartupState.Checking) {
        checkInternet()
    }

    fun retry() = intent {
        if (state != StartupState.Offline) return@intent
        reduce { StartupState.Retrying }
        checkInternet()
    }

    private suspend fun Syntax<StartupState, Nothing>.checkInternet() {
        val available = internet.isAvailable()
        reduce { if (available) StartupState.Ready else StartupState.Offline }
    }
}
