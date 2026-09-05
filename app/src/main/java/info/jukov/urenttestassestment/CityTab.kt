package info.jukov.urenttestassestment

import androidx.annotation.StringRes

internal enum class CityTab(val destination: Any, @param:StringRes val labelRes: Int) {
    List(CityListDestination, R.string.tab_cities),
    Map(CityMapDestination, R.string.tab_map)
}
