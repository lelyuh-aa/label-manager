package com.leliukh.labelmanager.contract.routes

/**
 * Paths of every endpoint of the Label Manager REST API, version 1.
 *
 * Templates use `{name}` placeholders; the matching builder functions fill them in.
 * All endpoints except the `auth` ones and [COUNTRIES] require an
 * `Authorization: Bearer <access token>` header.
 */
public object ApiRoutes {

    public const val PREFIX: String = "/api/v1"

    public const val REGISTER: String = "$PREFIX/auth/register"
    public const val LOGIN: String = "$PREFIX/auth/login"
    public const val REFRESH: String = "$PREFIX/auth/refresh"
    public const val LOGOUT: String = "$PREFIX/auth/logout"

    public const val ME: String = "$PREFIX/me"
    public const val LABEL: String = "$PREFIX/label"
    public const val ARTIST: String = "$PREFIX/artists/{artistId}"

    public const val LISTINGS: String = "$PREFIX/market/listings"
    public const val LISTING: String = "$PREFIX/market/listings/{listingId}"
    public const val LISTING_PURCHASE: String = "$PREFIX/market/listings/{listingId}/purchase"

    public const val ACTIVITY: String = "$PREFIX/activity"
    public const val ACTIVITY_READ: String = "$PREFIX/activity/read"
    public const val ACTIVITY_EVENT_READ: String = "$PREFIX/activity/{eventId}/read"

    public const val COUNTRIES: String = "$PREFIX/countries"

    public const val ARTIST_ID: String = "artistId"
    public const val LISTING_ID: String = "listingId"
    public const val EVENT_ID: String = "eventId"

    /**
     * @param artistId id of the artist
     */
    public fun artist(artistId: String): String = ARTIST.replace("{$ARTIST_ID}", artistId)

    /**
     * @param listingId id of the marketing
     */
    public fun listing(listingId: String): String =
        LISTING.replace("{$LISTING_ID}", listingId)

    /**
     * @param listingId id of the marketing to buy
     */
    public fun listingPurchase(listingId: String): String =
        LISTING_PURCHASE.replace("{$LISTING_ID}", listingId)

    /**
     * @param eventId id of the activity event to mark read
     */
    public fun activityEventRead(eventId: String): String =
        ACTIVITY_EVENT_READ.replace("{$EVENT_ID}", eventId)
}
