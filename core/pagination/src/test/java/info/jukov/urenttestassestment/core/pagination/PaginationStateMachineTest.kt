package info.jukov.urenttestassestment.core.pagination

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaginationStateMachineTest {
    private val machine = PaginationStateMachine<Item, Int>(Item::id)

    @Test
    fun `does not start concurrent requests`() {
        assertTrue(machine.start())
        assertFalse(machine.start())
    }

    @Test
    fun `merges pages without duplicate ids`() {
        machine.start()
        machine.append(Page(listOf(Item(1), Item(2)), 1, 2, 4))
        machine.start()
        machine.append(Page(listOf(Item(2), Item(3)), 2, 2, 4))

        assertEquals(listOf(1, 2, 3), machine.snapshot.items.map(Item::id))
        assertEquals(PaginationStatus.EndReached, machine.snapshot.status)
    }

    @Test
    fun `uses short page fallback when total is invalid`() {
        machine.start()
        machine.append(Page(listOf(Item(1)), 1, 20, null))

        assertEquals(PaginationStatus.EndReached, machine.snapshot.status)
    }

    private data class Item(val id: Int)
}
