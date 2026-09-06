package info.jukov.urenttestassessment.core.logging

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class AppLoggerTest {
    private val recordingTree = RecordingTree()

    @Before
    fun setUp() {
        Timber.uprootAll()
        Timber.plant(recordingTree)
    }

    @After
    fun tearDown() {
        Timber.uprootAll()
        Logging.initialize(isDebug = false)
    }

    @Test
    fun `logger forwards level tag throwable and formatted message to Timber`() {
        val failure = IllegalStateException("failure")

        AppLogger.withTag("Cities").warn(failure, "Page %d failed", 2)

        assertEquals(1, recordingTree.entries.size)
        val entry = recordingTree.entries.single()
        assertEquals("Cities", entry.tag)
        assertTrue(entry.message.startsWith("Page 2 failed"))
        assertTrue(entry.message.contains("IllegalStateException: failure"))
        assertEquals(failure, entry.throwable)
        assertEquals(WARN_PRIORITY, entry.priority)
    }

    @Test
    fun `release initialization leaves Timber without trees`() {
        Logging.initialize(isDebug = true)
        assertTrue(Logging.isDetailedLoggingEnabled)
        assertEquals(1, Timber.treeCount)

        Logging.initialize(isDebug = false)

        assertFalse(Logging.isDetailedLoggingEnabled)
        assertEquals(0, Timber.treeCount)
    }

    private class RecordingTree : Timber.Tree() {
        val entries = mutableListOf<Entry>()

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            entries += Entry(priority, tag, message, t)
        }
    }

    private data class Entry(val priority: Int, val tag: String?, val message: String, val throwable: Throwable?)

    private companion object {
        const val WARN_PRIORITY = 5
    }
}
