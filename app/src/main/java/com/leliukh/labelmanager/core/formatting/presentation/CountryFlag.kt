package com.leliukh.labelmanager.core.formatting.presentation

private const val REGIONAL_INDICATOR_A = 0x1F1E6
private const val COUNTRY_CODE_LENGTH = 2

/**
 * Turns an ISO 3166-1 alpha-2 code into its flag emoji, e.g. `DE` into 🇩🇪.
 *
 * A flag emoji is the pair of regional indicator symbols for the code's two letters, so every
 * country gets a flag without bundling images.
 *
 * @param countryCode two ASCII letters, any case
 * @return the flag, or an empty string for anything that is not two letters
 */
fun countryFlagEmoji(countryCode: String): String {
    val code = countryCode.uppercase()
    if (code.length != COUNTRY_CODE_LENGTH || code.any { it !in 'A'..'Z' }) return ""
    return buildString {
        code.forEach { letter -> appendCodePoint(REGIONAL_INDICATOR_A + (letter - 'A')) }
    }
}
