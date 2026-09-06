package info.jukov.urenttestassessment.feature.map.domain

import kotlin.math.asin
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

public data class Coordinate(val latitude: Double, val longitude: Double)

public data class MapViewport(val center: Coordinate, val radius: Long) {
    public companion object {
        public fun fromCorners(center: Coordinate, corners: List<Coordinate>): MapViewport {
            require(corners.isNotEmpty())
            return MapViewport(center, ceil(corners.maxOf { distance(center, it) }).toLong().coerceAtLeast(1))
        }
    }
}

private fun distance(from: Coordinate, to: Coordinate): Double {
    val latitude = Math.toRadians(to.latitude - from.latitude)
    val longitude = Math.toRadians(to.longitude - from.longitude)
    val a = sin(latitude / 2) * sin(latitude / 2) +
        cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) *
        sin(longitude / 2) * sin(longitude / 2)
    return 2 * EARTH_RADIUS_METERS * asin(sqrt(a.coerceIn(0.0, 1.0)))
}

private const val EARTH_RADIUS_METERS = 6_371_000.0
