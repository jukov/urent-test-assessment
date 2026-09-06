package info.jukov.urenttestassessment.feature.map.presentation

import android.graphics.PointF
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import org.maplibre.android.maps.MapView

// Compose's synthetic pointers have zero pressure. MapLibre filters those out, so native
// gesture tests supply realistic pressure and run on Main through the actual MapView.
internal fun MapView.panAcrossMap() {
    val start = SystemClock.uptimeMillis()
    touch(start, 0, MotionEvent.ACTION_DOWN, PointF(width * 0.7f, height * 0.5f))
    for (step in 1..10) {
        touch(start, step * 20L, MotionEvent.ACTION_MOVE, PointF(width * (0.7f - step * 0.04f), height * 0.5f))
    }
    touch(start, 220, MotionEvent.ACTION_UP, PointF(width * 0.3f, height * 0.5f))
}

internal fun MapView.tapWithTwoFingers() {
    val start = SystemClock.uptimeMillis()
    val left = PointF(width * 0.4f, height * 0.5f)
    val right = PointF(width * 0.6f, height * 0.5f)
    touch(start, 0, MotionEvent.ACTION_DOWN, left)
    touch(start, 20, MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), left, right)
    touch(start, 60, MotionEvent.ACTION_POINTER_UP or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), left, right)
    touch(start, 80, MotionEvent.ACTION_UP, left)
}

private fun MapView.touch(start: Long, elapsed: Long, action: Int, vararg points: PointF) {
    val properties = Array(points.size) { index ->
        MotionEvent.PointerProperties().apply {
            id = index
            toolType = MotionEvent.TOOL_TYPE_FINGER
        }
    }
    val coordinates = Array(points.size) { index ->
        MotionEvent.PointerCoords().apply {
            x = points[index].x
            y = points[index].y
            pressure = 1f
            size = 1f
        }
    }
    val event = MotionEvent.obtain(
        start, start + elapsed, action, points.size, properties, coordinates,
        0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0
    )
    try {
        dispatchTouchEvent(event)
    } finally {
        event.recycle()
    }
}
