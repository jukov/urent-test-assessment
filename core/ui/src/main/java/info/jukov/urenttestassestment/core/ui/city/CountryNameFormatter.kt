package info.jukov.urenttestassestment.core.ui.city

import java.util.Locale

public class CountryNameFormatter(private val locale: Locale) {
    public fun format(code: String): String {
        val normalized = code.trim().uppercase(Locale.ROOT)
        return when {
            normalized == "XK" -> if (locale.language == "ru") "Косово" else "Kosovo"
            normalized !in Locale.getISOCountries() -> code
            else -> Locale.Builder().setRegion(normalized).build().getDisplayCountry(locale)
        }
    }
}
