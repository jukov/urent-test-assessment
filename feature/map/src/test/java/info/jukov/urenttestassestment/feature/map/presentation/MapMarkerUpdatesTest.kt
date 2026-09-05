package info.jukov.urenttestassestment.feature.map.presentation

import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maplibre.geojson.FeatureCollection

@OptIn(ExperimentalCoroutinesApi::class)
class MapMarkerUpdatesTest {
    private val city = MapCityUi(7, "Moscow", 55.75, 37.62, 10_381_222)

    @Test
    fun `city traversal and preparation run off the caller thread and publish returns to it`() = runTest {
        val caller = Thread.currentThread()
        val ready = CompletableDeferred<String>()
        val cities = object : AbstractList<MapCityUi>() {
            override val size: Int = 2

            override fun get(index: Int): MapCityUi {
                assertNotSame(caller, Thread.currentThread())
                return city.copy(id = index.toLong())
            }
        }
        Executors.newSingleThreadExecutor().asCoroutineDispatcher().use { worker ->
            val updates = MapMarkerUpdates(backgroundScope, worker) {
                assertSame(caller, Thread.currentThread())
                ready.complete(it)
            }
            updates.submit(cities)
            assertEquals(2, FeatureCollection.fromJson(ready.await()).features()!!.size)
        }
    }

    @Test
    fun `queued updates conflate and repeated state does not rebuild or republish`() = runTest {
        val worker = QueuedDispatcher()
        val published = mutableListOf<String>()
        val updates = MapMarkerUpdates(backgroundScope, worker, published::add)
        repeat(100) { updates.submit(listOf(city.copy(id = it.toLong()))) }
        val latest = listOf(city)
        updates.submit(latest)
        runCurrent()
        worker.runAll()
        runCurrent()
        assertEquals(listOf("7"), published.map { FeatureCollection.fromJson(it).features()!!.single().id() })
        repeat(10) { updates.submit(latest) }
        runCurrent()
        assertEquals(0, worker.pending)
        updates.submit(latest.map { it.copy() })
        runCurrent()
        worker.runAll()
        runCurrent()
        assertEquals(1, published.size)
    }

    @Test
    fun `completed old preparation cannot publish after a newer request was submitted`() = runTest {
        val worker = QueuedDispatcher()
        val published = mutableListOf<String>()
        val updates = MapMarkerUpdates(backgroundScope, worker, published::add)
        updates.submit(listOf(city.copy(id = 1)))
        runCurrent()
        worker.runAll() // Old result is queued for delivery on the caller dispatcher.
        updates.submit(listOf(city.copy(id = 2)))
        runCurrent()
        assertTrue(published.isEmpty())
        worker.runAll()
        runCurrent()
        assertEquals(listOf("2"), published.map { FeatureCollection.fromJson(it).features()!!.single().id() })
    }

    @Test
    fun `disposing the controller scope prevents pending results from reaching the map`() = runTest {
        val worker = QueuedDispatcher()
        val scope = CoroutineScope(backgroundScope.coroutineContext + Job(backgroundScope.coroutineContext[Job]))
        val published = mutableListOf<String>()
        val updates = MapMarkerUpdates(scope, worker, published::add)
        updates.submit(listOf(city))
        runCurrent()
        scope.cancel()
        worker.runAll()
        runCurrent()
        assertTrue(published.isEmpty())
    }

    private class QueuedDispatcher : CoroutineDispatcher() {
        private val tasks = ArrayDeque<Runnable>()
        val pending: Int get() = tasks.size

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            tasks.addLast(block)
        }

        fun runAll() {
            while (tasks.isNotEmpty()) tasks.removeFirst().run()
        }
    }
}
