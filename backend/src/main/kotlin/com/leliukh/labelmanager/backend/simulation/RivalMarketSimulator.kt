package com.leliukh.labelmanager.backend.simulation

import com.leliukh.labelmanager.backend.SimulationConfig
import com.leliukh.labelmanager.backend.data.BackendStore
import com.leliukh.labelmanager.backend.data.StoreWriter
import com.leliukh.labelmanager.backend.domain.models.Listing
import com.leliukh.labelmanager.backend.domain.models.Label
import com.leliukh.labelmanager.backend.domain.services.DealExecutor
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Rival labels trading on the market while users are away.
 *
 * Time is simulated in fixed ticks. Every tick a rival label may list an artist, rival labels may buy
 * each other's listings, and each user listing that has been on the market for a while may catch a
 * rival's interest: the closer the asking price is to the market value, the likelier a sale. Rivals
 * never pay more than their budget, and every deal goes through the same [DealExecutor] as a
 * user's purchase.
 *
 * The simulation is replayed lazily: [advanceTo] runs every tick that passed since the last call,
 * at most [SimulationConfig.maxCatchUpTicks] of them. With a seeded [Random] and a fixed clock the
 * outcome is fully reproducible.
 *
 * @param store persistent state
 * @param clock source of the current time
 * @param random source of randomness
 * @param executor applies the market rules
 * @param seeder creates the rival labels on the first run
 * @param config tick length, catch-up limit and interest delay
 */
class RivalMarketSimulator(
    private val store: BackendStore,
    private val clock: Clock,
    private val random: Random,
    private val executor: DealExecutor,
    private val seeder: RivalLabelsSeeder,
    private val config: SimulationConfig,
) {

    /** Runs the simulation up to the current moment. */
    suspend fun catchUp() {
        advanceTo(clock.now())
    }

    /**
     * Seeds the rival labels on the first call, then replays every tick up to [now].
     *
     * @param now the moment to simulate up to
     */
    suspend fun advanceTo(now: Instant) {
        if (!config.isEnabled) return
        val last = store.read { simulatedUntil() }
        if (last != null && now - last < config.tick) return
        store.write {
            val from = simulatedUntil()
            if (from == null) {
                seeder.seed(this, now)
                return@write
            }
            val elapsedTicks = ((now - from) / config.tick).toLong()
            if (elapsedTicks <= 0) return@write
            val ticks = elapsedTicks.coerceAtMost(config.maxCatchUpTicks.toLong())
            val start = if (elapsedTicks > ticks) now - config.tick * ticks.toInt() else from
            (1..ticks).forEach { index -> tick(start + config.tick * index.toInt()) }
            setSimulatedUntil(if (elapsedTicks > ticks) now else from + config.tick * ticks.toInt())
        }
    }

    private fun StoreWriter.tick(at: Instant) {
        val rivals = allLabels().filter { !it.isUserOwned }
        if (rivals.size < 2) return
        withdrawStaleListings(at)
        if (random.nextDouble() < LIST_CHANCE) listRivalArtist(rivals, at)
        if (random.nextDouble() < RIVAL_DEAL_CHANCE) tradeBetweenRivals(rivals, at)
        considerUserListings(rivals, at)
    }

    private fun StoreWriter.withdrawStaleListings(at: Instant) {
        allListings()
            .filter {
                at - it.listedAt > STALE_AFTER && findLabel(it.sellerLabelId)?.isUserOwned == false
            }
            .forEach { with(executor) { withdraw(it, at) } }
    }

    private fun StoreWriter.listRivalArtist(rivals: List<Label>, at: Instant) {
        val openBySeller = allListings().groupingBy { it.sellerLabelId }.eachCount()
        val seller =
            rivals
                .filter { (openBySeller[it.id] ?: 0) < MAX_OPEN_LISTINGS_PER_RIVAL }
                .filter { artistsOfLabel(it.id).size > MIN_ROSTER_TO_SELL }
                .randomOrNull(random) ?: return
        val artist =
            artistsOfLabel(seller.id)
                .filter { findListingOfArtist(it.id) == null }
                .randomOrNull(random) ?: return
        with(executor) { list(artist, askingPrice(artist, random), at) }
    }

    private fun StoreWriter.tradeBetweenRivals(rivals: List<Label>, at: Instant) {
        val rivalIds = rivals.map { it.id }.toSet()
        val listing =
            allListings()
                .filter { it.sellerLabelId in rivalIds && at - it.listedAt >= config.interestDelay }
                .randomOrNull(random) ?: return
        buyForRival(listing, rivals, at)
    }

    private fun StoreWriter.considerUserListings(rivals: List<Label>, at: Instant) {
        allListings()
            .filter { at - it.listedAt >= config.interestDelay }
            .filter { findLabel(it.sellerLabelId)?.isUserOwned == true }
            .forEach { listing ->
                val artist = findArtist(listing.artistId) ?: return@forEach
                val ratio = listing.askingPrice.toDouble() / artist.marketValue
                if (random.nextDouble() < config.userListingChance * attractiveness(ratio)) {
                    buyForRival(listing, rivals, at)
                }
            }
    }

    private fun StoreWriter.buyForRival(listing: Listing, rivals: List<Label>, at: Instant) {
        val buyer =
            rivals
                .mapNotNull { findLabel(it.id) }
                .filter { it.id != listing.sellerLabelId }
                .filter { it.budget >= listing.askingPrice }
                .filter { artistsOfLabel(it.id).size < MAX_ROSTER_TO_BUY }
                .randomOrNull(random) ?: return
        with(executor) { purchase(listing, buyer.id, at) }
    }

    private fun attractiveness(priceToValue: Double): Double =
        when {
            priceToValue <= 1.0 -> 1.0

            priceToValue <= 1.25 -> 0.6

            priceToValue <= 1.6 -> 0.25

            priceToValue <= 2.0 -> 0.08

            else -> 0.0
        }

    private companion object {
        const val LIST_CHANCE = 0.45
        const val RIVAL_DEAL_CHANCE = 0.3
        const val MAX_OPEN_LISTINGS_PER_RIVAL = 3
        const val MIN_ROSTER_TO_SELL = 12
        const val MAX_ROSTER_TO_BUY = 22
        val STALE_AFTER = 3.days + 30.minutes
    }
}
