package info.jukov.urenttestassestment.feature.map.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapViewportTest {
    @Test
    fun `radius covers the farthest corner in meters rounding upwards`() {
        val viewport = MapViewport.fromCorners(
            Coordinate(0.0, 0.0),
            listOf(Coordinate(0.0, 1.0), Coordinate(0.0, -2.0), Coordinate(1.0, 0.0), Coordinate(-1.0, 0.0))
        )
        assertEquals(222390L, viewport.radius)
    }

    @Test
    fun `crossing antimeridian uses shortest great circle distance`() {
        val viewport = MapViewport.fromCorners(Coordinate(0.0, 179.0), listOf(Coordinate(0.0, -179.0)))
        assertEquals(222390L, viewport.radius)
    }

    @Test
    fun `polar viewport and zero extent produce a positive finite radius`() {
        assertEquals(1L, MapViewport.fromCorners(Coordinate(90.0, 0.0), listOf(Coordinate(90.0, 0.0))).radius)
        assertTrue(MapViewport.fromCorners(Coordinate(89.0, 0.0), listOf(Coordinate(89.0, 180.0))).radius > 0)
    }
}
