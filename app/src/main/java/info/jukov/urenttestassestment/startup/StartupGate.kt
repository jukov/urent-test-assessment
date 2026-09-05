package info.jukov.urenttestassestment.startup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun StartupRoute(content: @Composable () -> Unit) {
    val viewModel: StartupViewModel = koinViewModel()
    val state by viewModel.collectAsState()
    StartupGate(state, { viewModel.retry() }, content)
}

@Composable
internal fun StartupGate(state: StartupState, onRetry: () -> Unit, content: @Composable () -> Unit) {
    when (state) {
        StartupState.Checking -> Surface(Modifier.fillMaxSize()) { Box(Modifier.fillMaxSize()) }
        StartupState.Offline -> OfflineScreen(onRetry)
        StartupState.Retrying -> OfflineScreen(onRetry, checking = true)
        StartupState.Ready -> content()
    }
}
