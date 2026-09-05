package info.jukov.urenttestassestment.core.ui.city

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class CityDetailsTest {
    @Test
    fun `details format country and population for the active locale`() {
        val city = CityDetails(1, "Москва", "RU", 12_655_000)
        assertEquals(CityDetailsUi("Москва", "Russia", "12,655,000"), city.toUi(Locale.ENGLISH))
        assertEquals(CityDetailsUi("Москва", "Россия", "12\u00a0655\u00a0000"), city.toUi(Locale.forLanguageTag("ru")))
    }

    @Test
    fun `zero population and unknown country preserve API data`() {
        assertEquals(CityDetailsUi("Town", "ZZ", "0"), CityDetails(2, "Town", "ZZ", 0).toUi(Locale.ENGLISH))
    }
}
