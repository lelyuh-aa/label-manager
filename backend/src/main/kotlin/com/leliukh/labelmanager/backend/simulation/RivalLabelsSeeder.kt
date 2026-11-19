package com.leliukh.labelmanager.backend.simulation

import com.leliukh.labelmanager.backend.SimulationConfig
import com.leliukh.labelmanager.backend.data.StoreWriter
import com.leliukh.labelmanager.backend.domain.models.Artist
import com.leliukh.labelmanager.backend.domain.services.DealExecutor
import com.leliukh.labelmanager.backend.generation.LabelGenerator
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Creates the rival labels on the first start, so a new user finds a lively market: labels with full
 * rosters, some artists with a deal history and open listings of various ages.
 *
 * @param config how many labels, listings and past deals to create
 * @param random source of randomness
 * @param labelGenerator creates the labels
 * @param executor applies the market rules to the past deals and listings
 * @param timeZone time zone that defines "today" for artist ages
 */
class RivalLabelsSeeder(
    private val config: SimulationConfig,
    private val random: Random,
    private val labelGenerator: LabelGenerator,
    private val executor: DealExecutor,
    private val timeZone: TimeZone,
) {

    /**
     * @param writer the open transaction
     * @param now the current moment; past deals and listings are dated before it
     */
    fun seed(writer: StoreWriter, now: Instant) {
        val today = now.toLocalDateTime(timeZone).date
        val takenNames = writer.allLabels().map { it.name }.toMutableSet()
        val rivals =
            List(config.rivalLabelCount) {
                val (label, artists) = labelGenerator.generate(null, takenNames, today)
                takenNames += label.name
                writer.saveLabel(label)
                artists.forEach(writer::saveArtist)
                label
            }
        if (rivals.size < 2) return
        val historyTimes =
            List(config.historicalDeals) {
                    now - random.nextLong(2.days.inWholeHours, 30.days.inWholeHours).hours
                }
                .sorted()
        historyTimes.forEach { listedAt ->
            val seller = rivals.random(random)
            val buyer = rivals.filter { it.id != seller.id }.random(random)
            val artist = writer.artistsOfLabel(seller.id).randomOrNull(random) ?: return@forEach
            val price = askingPrice(artist, random)
            val listing = with(executor) { writer.list(artist, price, listedAt) }
            val soldAt = listedAt + random.nextLong(1, 60).minutes
            runCatching { with(executor) { writer.purchase(listing, buyer.id, soldAt) } }
                .onFailure { writer.deleteListing(listing.id) }
        }
        repeat(config.initialListings) {
            val seller = rivals.random(random)
            val artist =
                writer
                    .artistsOfLabel(seller.id)
                    .filter { writer.findListingOfArtist(it.id) == null }
                    .randomOrNull(random) ?: return@repeat
            val listedAt = now - random.nextLong(5, 3 * 24 * 60).minutes
            with(executor) { writer.list(artist, askingPrice(artist, random), listedAt) }
        }
        writer.setSimulatedUntil(now)
    }
}

/**
 * A rival label's asking price: 95–160 % of the market value, rounded to ten thousand dollars.
 *
 * @param artist the artist to price
 * @param random source of randomness
 * @return the asking price
 */
internal fun askingPrice(artist: Artist, random: Random): Long {
    val raw = artist.marketValue * random.nextDouble(0.95, 1.6)
    return (raw / PRICE_STEP).toLong().coerceAtLeast(1) * PRICE_STEP
}

private const val PRICE_STEP = 10_000L
