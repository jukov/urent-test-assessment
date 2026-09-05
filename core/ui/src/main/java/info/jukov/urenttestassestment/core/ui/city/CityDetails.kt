package info.jukov.urenttestassestment.core.ui.city

import java.text.NumberFormat
import java.util.Locale

public data class CityDetails(val id: Long, val name: String, val countryCode: String, val population: Long)

public data class CityDetailsUi(val name: String, val country: String, val population: String)

public fun CityDetails.toUi(locale: Locale): CityDetailsUi = CityDetailsUi(
    name = name,
    country = CountryNameFormatter(locale).format(countryCode),
    population = NumberFormat.getIntegerInstance(locale).format(population)
)
