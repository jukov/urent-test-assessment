package info.jukov.urenttestassestment

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class SystemBarsTest {
    @Test
    fun `system bars use transparent scrim`() {
        assertEquals(Color.TRANSPARENT, SYSTEM_BAR_SCRIM)
    }
}
