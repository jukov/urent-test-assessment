package info.jukov.urenttestassestment.core.ui.city

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import androidx.core.net.toUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CitySearchTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @Test
    fun `browser receives an HTTPS search with encoded city name`() {
        val name = "Санкт-Петербург & York + #1?"
        registerBrowser(name)
        assertTrue(openCitySearch(context, name))
        val launched = shadowOf(RuntimeEnvironment.getApplication()).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, launched.action)
        assertTrue(launched.hasCategory(Intent.CATEGORY_BROWSABLE))
        assertEquals("https", launched.data?.scheme)
        assertEquals("www.google.com", launched.data?.host)
        assertEquals("/search", launched.data?.path)
        assertEquals(name, launched.data?.getQueryParameter("q"))
        assertEquals(setOf("q"), launched.data?.queryParameterNames)
    }

    @Test
    fun `missing browser returns failure without launching an activity`() {
        assertFalse(openCitySearch(context, "Moscow"))
        assertNull(shadowOf(RuntimeEnvironment.getApplication()).nextStartedActivity)
    }

    @Test
    fun `browser disappearing or refusing launch returns failure`() {
        registerBrowser("Moscow")
        listOf(ActivityNotFoundException(), SecurityException()).forEach { failure ->
            val failingContext = object : ContextWrapper(context) {
                override fun startActivity(intent: Intent): Unit = throw failure
            }
            assertFalse(openCitySearch(failingContext, "Moscow"))
        }
    }

    private fun registerBrowser(name: String) {
        val intent = Intent(Intent.ACTION_VIEW, citySearchUrl(name).toUri()).addCategory(Intent.CATEGORY_BROWSABLE)
        val handler = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = "test.browser"
                this.name = "BrowserActivity"
                applicationInfo = ApplicationInfo().apply { packageName = "test.browser" }
            }
        }
        shadowOf(context.packageManager).addResolveInfoForIntent(intent, handler)
    }
}
