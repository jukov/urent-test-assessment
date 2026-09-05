package info.jukov.urenttestassestment.core.pagination

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaginationStateMachineTest {
    private val machine = PaginationStateMachine<Item, Int>(Item::id)

    @Test
    fun `initial progress transitions to empty failure`() {
        val failure = IllegalStateException("failed")
        machine.fail(failure)
        assertEquals(PaginationState.EmptyFailure(failure), machine.state)
    }

    @Test
    fun `initial progress transitions to empty result`() {
        machine.append(Page(emptyList(), 1, 20, 0))
        assertEquals(PaginationState.Empty, machine.state)
    }

    @Test
    fun `append progress preserves content and blocks concurrent request`() {
        machine.append(Page(listOf(Item(1), Item(2)), 1, 2, 4))
        assertTrue(machine.startAppend())
        assertEquals(PaginationState.ContentProgress(listOf(Item(1), Item(2))), machine.state)
        assertFalse(machine.startAppend())
    }

    @Test
    fun `append failure preserves content and retry returns to progress`() {
        val failure = IllegalStateException("failed")
        machine.append(Page(listOf(Item(1), Item(2)), 1, 2, 4))
        machine.startAppend()
        machine.fail(failure)
        assertEquals(PaginationState.ContentFailure(listOf(Item(1), Item(2)), failure), machine.state)
        assertTrue(machine.startAppend())
        assertEquals(PaginationState.ContentProgress(listOf(Item(1), Item(2))), machine.state)
    }

    @Test
    fun `refresh keeps content visible and replaces it on success`() {
        machine.append(Page(listOf(Item(1), Item(2)), 1, 2, 4))
        assertTrue(machine.startRefresh())
        assertEquals(PaginationState.ContentRefreshing(listOf(Item(1), Item(2))), machine.state)
        machine.append(Page(listOf(Item(3), Item(4)), 1, 2, 4))
        assertEquals(PaginationState.Content(listOf(Item(3), Item(4))), machine.state)
        assertEquals(2, machine.nextPage)
    }

    @Test
    fun `refresh failure restores next append page`() {
        val failure = IllegalStateException("failed")
        machine.append(Page(listOf(Item(1), Item(2)), 1, 2, 4))
        machine.startRefresh()
        machine.fail(failure)

        assertEquals(PaginationState.Content(listOf(Item(1), Item(2))), machine.state)
        assertEquals(2, machine.nextPage)
    }

    @Test
    fun `refresh failure restores end reached state`() {
        val failure = IllegalStateException("failed")
        val items = listOf(Item(1), Item(2))
        machine.append(Page(items, 1, 2, 2))
        assertEquals(PaginationState.ContentEndReached(items), machine.state)

        machine.startRefresh()
        machine.fail(failure)

        assertEquals(PaginationState.ContentEndReached(items), machine.state)
        assertEquals(2, machine.nextPage)
    }

    @Test
    fun `merges pages without duplicate ids and reaches end`() {
        machine.append(Page(listOf(Item(1), Item(2)), 1, 2, 4))
        machine.startAppend()
        machine.append(Page(listOf(Item(2), Item(3)), 2, 2, 4))
        assertEquals(PaginationState.ContentEndReached(listOf(Item(1), Item(2), Item(3))), machine.state)
    }

    @Test
    fun `uses short page fallback when total is invalid`() {
        machine.append(Page(listOf(Item(1)), 1, 20, null))
        assertEquals(PaginationState.ContentEndReached(listOf(Item(1))), machine.state)
    }

    private data class Item(val id: Int)
}
