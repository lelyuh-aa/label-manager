package com.leliukh.labelmanager.core.formatting.presentation

import java.text.Normalizer

private val DIACRITICS = Regex("\\p{Mn}+")

/**
 * The text as search compares it: lower case and without accents, so "theo" finds "Théo"
 * and "cote" finds "Côte d’Ivoire".
 */
fun String.normalizedForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD).replace(DIACRITICS, "").lowercase()
