package info.jukov.urenttestassestment.core.pagination

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaginationStateMachineTest {
    private val machine = PaginationStateMachine<Item, Int>(Item::id)

    @Test
    fun `starts on first page in empty progress`() {
        assertEquals(PaginationState.EmptyProgress, machine.state)
        assertEquals(1, machine.nextPage)
    }

    @Test
    fun `initial progress transitions to empty failure`() {
        val failure = IllegalStateException("failed")
        machine.fail(failure)
        assertEquals(PaginationState.EmptyFailure(failure), machine.state)
    }

    @Test
    fun `initial progress transitions to empty result`() {
        machine.append(Page(items = emptyList(), page = 1, limit = 20, total = 0))
        assertEquals(PaginationState.Empty, machine.state)
    }

    @Test
    fun `append progress preserves content and blocks concurrent request`() {
        machine.append(Page(items = listOf(Item(1), Item(2)), page = 1, limit = 2, total = 4))
        assertTrue(machine.startAppend())
        assertEquals(PaginationState.ContentProgress(listOf(Item(1), Item(2))), machine.state)
        assertFalse(machine.startAppend())
    }

    @Test
    fun `append is rejected without loadable content`() {
        assertFalse(machine.startAppend())
        machine.append(Page(items = emptyList(), page = 1, limit = 20, total = 0))
        assertFalse(machine.startAppend())
        machine.startInitial()
        machine.fail(IllegalStateException("failed"))
        assertFalse(machine.startAppend())
    }

    @Test
    fun `append is rejected after reaching end`() {
        machine.append(Page(items = listOf(Item(1)), page = 1, limit = 20, total = 1))
        assertFalse(machine.startAppend())
    }

    @Test
    fun `append failure preserves content and retry returns to progress`() {
        val failure = IllegalStateException("failed")
        machine.append(Page(items = listOf(Item(1), Item(2)), page = 1, limit = 2, total = 4))
        machine.startAppend()
        machine.fail(failure)
        assertEquals(PaginationState.ContentFailure(listOf(Item(1), Item(2)), failure), machine.state)
        assertTrue(machine.startAppend())
        assertEquals(PaginationState.ContentProgress(listOf(Item(1), Item(2))), machine.state)
    }

    @Test
    fun `refresh keeps content visible and replaces it on success`() {
        machine.append(Page(items = listOf(Item(1), Item(2)), page = 1, limit = 2, total = 4))
        assertTrue(machine.startRefresh())
        assertEquals(PaginationState.ContentRefreshing(listOf(Item(1), Item(2))), machine.state)
        machine.append(Page(items = listOf(Item(3), Item(4)), page = 1, limit = 2, total = 4))
        assertEquals(PaginationState.Content(listOf(Item(3), Item(4))), machine.state)
        assertEquals(2, machine.nextPage)
    }

    @Test
    fun `refresh failure restores next append page`() {
        val failure = IllegalStateException("failed")
        machine.append(Page(items = listOf(Item(1), Item(2)), page = 1, limit = 2, total = 4))
        machine.startRefresh()
        machine.fail(failure)

        assertEquals(PaginationState.Content(listOf(Item(1), Item(2))), machine.state)
        assertEquals(2, machine.nextPage)
    }

    @Test
    fun `refresh failure restores end reached state`() {
        val failure = IllegalStateException("failed")
        val items = listOf(Item(1), Item(2))
        machine.append(Page(items = items, page = 1, limit = 2, total = 2))
        assertEquals(PaginationState.ContentEndReached(items), machine.state)

        machine.startRefresh()
        machine.fail(failure)

        assertEquals(PaginationState.ContentEndReached(items), machine.state)
        assertEquals(2, machine.nextPage)
    }

    @Test
    fun `refresh failure restores previous content failure`() {
        val appendFailure = IllegalStateException("append failed")
        val refreshFailure = IllegalStateException("refresh failed")
        val items = listOf(Item(1), Item(2))
        machine.append(Page(items = items, page = 1, limit = 2, total = 4))
        machine.startAppend()
        machine.fail(appendFailure)

        machine.startRefresh()
        machine.fail(refreshFailure)

        assertEquals(PaginationState.ContentFailure(items, appendFailure), machine.state)
        assertEquals(2, machine.nextPage)
    }

    @Test
    fun `refresh cancels append progress and restores stable content on failure`() {
        val items = listOf(Item(1), Item(2))
        machine.append(Page(items = items, page = 1, limit = 2, total = 4))
        machine.startAppend()

        machine.startRefresh()
        machine.fail(IllegalStateException("refresh failed"))

        assertEquals(PaginationState.Content(items), machine.state)
        assertEquals(2, machine.nextPage)
    }

    @Test
    fun `second refresh is rejected while refresh is running`() {
        machine.append(Page(items = listOf(Item(1), Item(2)), page = 1, limit = 2, total = 4))
        assertTrue(machine.startRefresh())
        assertFalse(machine.startRefresh())
    }

    @Test
    fun `merges pages without duplicate ids and reaches end`() {
        machine.append(Page(items = listOf(Item(1), Item(2)), page = 1, limit = 2, total = 4))
        machine.startAppend()
        machine.append(Page(items = listOf(Item(2), Item(3)), page = 2, limit = 2, total = 4))
        assertEquals(PaginationState.ContentEndReached(listOf(Item(1), Item(2), Item(3))), machine.state)
    }

    @Test
    fun `uses short page fallback when total is invalid`() {
        machine.append(Page(items = listOf(Item(1)), page = 1, limit = 20, total = null))
        assertEquals(PaginationState.ContentEndReached(listOf(Item(1))), machine.state)
    }

    @Test
    fun `full page without total keeps pagination available`() {
        val items = listOf(Item(1), Item(2))
        machine.append(Page(items = items, page = 1, limit = 2, total = null))

        assertEquals(PaginationState.Content(items), machine.state)
        assertEquals(2, machine.nextPage)
        assertTrue(machine.startAppend())
    }

    @Test
    fun `empty appended page preserves content and reaches end`() {
        val items = listOf(Item(1), Item(2))
        machine.append(Page(items = items, page = 1, limit = 2, total = 10))
        machine.startAppend()
        machine.append(Page(items = emptyList(), page = 2, limit = 2, total = 10))

        assertEquals(PaginationState.ContentEndReached(items), machine.state)
        assertEquals(3, machine.nextPage)
    }

    @Test
    fun `next page follows highest received page`() {
        machine.append(Page(items = listOf(Item(1), Item(2)), page = 3, limit = 2, total = 10))
        assertEquals(4, machine.nextPage)
    }

    @Test
    fun `start initial resets content and page counter`() {
        machine.append(Page(items = listOf(Item(1), Item(2)), page = 3, limit = 2, total = 10))
        machine.startInitial()

        assertEquals(PaginationState.EmptyProgress, machine.state)
        assertEquals(1, machine.nextPage)
    }

    @Test
    fun `failure outside progress states is ignored`() {
        val items = listOf(Item(1), Item(2))
        machine.append(Page(items = items, page = 1, limit = 2, total = 4))
        machine.fail(IllegalStateException("stale failure"))

        assertEquals(PaginationState.Content(items), machine.state)
        assertEquals(2, machine.nextPage)
    }

    private data class Item(val id: Int)
}
