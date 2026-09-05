package info.jukov.urenttestassestment.feature.cities.presentation

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class CountryNameFormatterTest {
    @Test
    fun `formats country in selected locale and supports Kosovo`() {
        assertEquals("Russia", CountryNameFormatter(Locale.ENGLISH).format("RU"))
        assertEquals("Косово", CountryNameFormatter(Locale.forLanguageTag("ru")).format("XK"))
    }

    @Test
    fun `keeps unknown country code`() {
        assertEquals("ZZ", CountryNameFormatter(Locale.ENGLISH).format("ZZ"))
    }
}
