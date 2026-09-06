package info.jukov.urenttestassestment

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import info.jukov.urenttestassestment.core.ui.theme.URentTheme
import info.jukov.urenttestassestment.feature.cities.R as CitiesR
import info.jukov.urenttestassestment.feature.cities.domain.CitiesError
import info.jukov.urenttestassestment.feature.cities.domain.CitiesResult
import info.jukov.urenttestassestment.feature.cities.presentation.CitiesRoute
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class CitiesListTest {
    // Queue Orbit callbacks for the Compose clock instead of resuming composition on its worker.
    @get:Rule
    val compose = createComposeRule(effectContext = StandardTestDispatcher())
    private val fixture = CitiesTestFixture()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun showList() {
        compose.runOnUiThread { fixture.create() }
        compose.setContent { URentTheme { CitiesRoute({}, fixture.viewModel) } }
        awaitRequests(1)
    }

    @After
    fun clearViewModel() = compose.runOnUiThread { fixture.close() }

    @Test
    fun inputSearchesAndDisplaysCitiesThenEmptyResults() {
        fixture.succeed(0)
        awaitDisplayed("Moscow & York, Russia")
        compose.onNode(hasSetTextAction()).performTextInput("Atlantis")
        compose.onNode(hasSetTextAction()).assertTextContains("Atlantis")
        awaitRequests(2)
        assertEquals("Atlantis", fixture.requests[1].query)
        assertEquals(1, fixture.requests[1].page)
        fixture.succeed(1, emptyList())
        awaitDisplayed(context.getString(CitiesR.string.cities_empty))
        compose.onNodeWithText("Moscow & York, Russia").assertDoesNotExist()
    }

    @Test
    fun initialErrorRetryLoadsCities() {
        fixture.requests[0].response.complete(CitiesResult.Failure(CitiesError.Network))
        awaitDisplayed(context.getString(CitiesR.string.cities_error_network))
        compose.onNodeWithText(context.getString(CitiesR.string.cities_retry)).performClick()
        awaitRequests(2)
        assertEquals(1, fixture.requests[1].page)
        fixture.succeed(1)
        awaitDisplayed("Moscow & York, Russia")
        compose.onNodeWithText(context.getString(CitiesR.string.cities_retry)).assertDoesNotExist()
    }

    @Test
    fun scrollingLoadsNextPageAndFooterRetryPreservesCities() {
        val cities = (1L..20L).map { CitiesTestFixture.city.copy(id = it, name = "City $it") }
        fixture.succeed(0, cities, total = 40)
        awaitDisplayed("City 1, Russia")
        compose.runOnIdle { assertEquals(1, fixture.requests.size) }
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(19)
        awaitRequests(2)
        assertEquals(2, fixture.requests[1].page)
        awaitDisplayed("City 20, Russia")
        // Repeated end-of-list events while loading must not issue another request.
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(18)
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(20)
        compose.runOnIdle { assertEquals(2, fixture.requests.size) }
        fixture.requests[1].response.complete(CitiesResult.Failure(CitiesError.Server))
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(20)
        awaitDisplayed(context.getString(CitiesR.string.cities_error_generic))
        compose.onNodeWithText(context.getString(CitiesR.string.cities_retry)).performClick()
        awaitRequests(3)
        assertEquals(2, fixture.requests[2].page)
        fixture.succeed(2, listOf(CitiesTestFixture.city.copy(id = 21, name = "City 21")), total = 21)
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(20)
        awaitDisplayed("City 21, Russia")
        compose.onNodeWithText(context.getString(CitiesR.string.cities_retry)).assertDoesNotExist()
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        awaitDisplayed("City 1, Russia")
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(20)
        compose.runOnIdle { assertEquals(3, fixture.requests.size) }
    }

    // Repository completion and scroll requests do not guarantee the expected row is rendered yet.
    private fun awaitDisplayed(text: String) {
        compose.waitUntil(5_000) { compose.onNodeWithText(text).isDisplayed() }
    }

    private fun awaitRequests(count: Int) = compose.waitUntil(5_000) { fixture.requests.size == count }
}
