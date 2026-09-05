package info.jukov.urenttestassestment.core.ui.city

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import info.jukov.urenttestassestment.core.ui.R
import java.net.URLEncoder
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.compose.collectSideEffect
import org.orbitmvi.orbit.viewmodel.container

public class CitySearchViewModel :
    ViewModel(),
    ContainerHost<Unit, String> {
    override val container = container<Unit, String>(Unit)

    public fun search(name: String) = intent { postSideEffect(name) }
}

@Composable
public fun rememberCitySearchAction(viewModel: CitySearchViewModel = viewModel()): (String) -> Unit {
    val context = LocalContext.current
    viewModel.collectSideEffect { name ->
        if (!openCitySearch(context, name)) {
            Toast.makeText(context, R.string.city_browser_unavailable, Toast.LENGTH_LONG).show()
        }
    }
    return { viewModel.search(it) }
}

internal fun citySearchUrl(name: String): String = "https://www.google.com/search?q=${URLEncoder.encode(name, "UTF-8")}"

public fun openCitySearch(context: Context, name: String): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, citySearchUrl(name).toUri())
        .addCategory(Intent.CATEGORY_BROWSABLE)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return try {
        if (intent.resolveActivity(context.packageManager) == null) return false
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}
