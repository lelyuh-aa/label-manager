package com.leliukh.labelmanager.core.formatting.presentation

import java.util.Locale

/**
 * The country's name in the device language, e.g. `DE` → "Germany" or "Германия".
 *
 * @param countryCode ISO 3166-1 alpha-2 code
 * @param locale language of the name
 * @return the localised name, or the code itself when the platform does not know it
 */
fun countryDisplayName(countryCode: String, locale: Locale = Locale.getDefault()): String {
    val name = Locale.Builder().setRegion(countryCode).build().getDisplayCountry(locale)
    return name.ifBlank { countryCode }
}
