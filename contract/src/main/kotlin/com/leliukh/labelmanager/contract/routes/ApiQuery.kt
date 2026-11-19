package com.leliukh.labelmanager.contract.routes

/**
 * Query parameters accepted by the list endpoints.
 *
 * [ApiRoutes.LISTINGS] takes every filter; [ApiRoutes.ACTIVITY] takes only [CURSOR] and
 * [LIMIT]. Text filters are case-insensitive substring matches, country filters are
 * ISO 3166-1 alpha-2 codes, prices are whole US dollars, [GENRES] is a comma-separated
 * list of [com.leliukh.labelmanager.contract.dto.artist.GenreDto] names.
 */
public object ApiQuery {

    public const val ARTIST_NAME: String = "artistName"
    public const val LABEL_NAME: String = "labelName"
    public const val ARTIST_COUNTRY: String = "artistCountry"
    public const val LABEL_COUNTRY: String = "labelCountry"
    public const val MIN_PRICE: String = "minPrice"
    public const val MAX_PRICE: String = "maxPrice"
    public const val GENRES: String = "genres"
    public const val SORT: String = "sort"
    public const val CURSOR: String = "cursor"
    public const val LIMIT: String = "limit"

    public const val DEFAULT_LIMIT: Int = 20
    public const val MAX_LIMIT: Int = 50
}
