package com.leliukh.labelmanager.backend.simulation

import com.leliukh.labelmanager.backend.SimulationConfig
import com.leliukh.labelmanager.backend.domain.models.ActivityType
import com.leliukh.labelmanager.backend.generation.LabelGenerator
import com.leliukh.labelmanager.backend.testing.ServicesFixture
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RivalMarketSimulatorTest {

    @Test
    fun `the first run creates rival labels with a lively market`() = runTest {
        val fixture = ServicesFixture()
        val simulator = fixture.simulator()

        simulator.catchUp()

        val labels = fixture.store.read { allLabels() }
        assertEquals(24, labels.size)
        assertTrue(labels.none { it.isUserOwned })
        assertEquals(24 * 16, fixture.store.read { labels.sumOf { artistsOfLabel(it.id).size } })
        assertTrue(fixture.store.read { allListings() }.size in 20..30)
        assertTrue(fixture.store.read { allListings() }.all { it.listedAt <= fixture.clock.now })
        assertEquals(fixture.clock.now, fixture.store.read { simulatedUntil() })
        val signedArtists =
            fixture.store.read {
                labels.flatMap { artistsOfLabel(it.id) }.flatMap { dealsOfArtist(it.id) }
            }
        assertTrue(signedArtists.size >= 20)
        assertTrue(signedArtists.all { it.valueAfter > it.valueBefore })
    }

    @Test
    fun `seeding happens once`() = runTest {
        val fixture = ServicesFixture()
        val simulator = fixture.simulator()

        simulator.catchUp()
        fixture.clock.advanceBy(1.minutes)
        simulator.catchUp()

        assertEquals(24, fixture.store.read { allLabels() }.size)
    }

    @Test
    fun `rival trading keeps money and artists in the game`() = runTest {
        val fixture = ServicesFixture()
        val simulator = fixture.simulator()
        simulator.catchUp()
        val budgetBefore = fixture.store.read { allLabels().sumOf { it.budget } }
        val dealsBefore = fixture.dealCount()

        repeat(10) {
            fixture.clock.advanceBy(1.hours)
            simulator.catchUp()
        }

        assertEquals(budgetBefore, fixture.store.read { allLabels().sumOf { it.budget } })
        assertEquals(24 * 16, fixture.store.read { allLabels().sumOf { artistsOfLabel(it.id).size } })
        assertTrue(fixture.dealCount() > dealsBefore)
        assertTrue(fixture.store.read { allListings() }.isNotEmpty())
        assertTrue(fixture.store.read { allLabels() }.all { it.budget >= 0 })
    }

    @Test
    fun `a fairly priced user listing gets bought by a rival`() = runTest {
        val fixture = ServicesFixture()
        val simulator = fixture.simulator()
        simulator.catchUp()
        val seller = fixture.register("seller")
        val budget = fixture.labels.label(seller.user.labelId).label.budget
        val artistId = fixture.anyArtistOf(seller.user.labelId)
        fixture.market.createListing(seller.user.labelId, artistId, 1_000_000)

        repeat(12) {
            fixture.clock.advanceBy(1.hours)
            simulator.catchUp()
        }

        val firstSale = fixture.artists.details(artistId).deals.last()
        assertEquals(seller.user.labelId, firstSale.from.id)
        assertTrue(!firstSale.to.isUserOwned)
        assertEquals(1_000_000L, firstSale.deal.price)
        assertEquals(budget + 1_000_000, fixture.labels.label(seller.user.labelId).label.budget)
        val sold = fixture.activity.page(seller.user.labelId, null, 10).page.items.first()
        assertEquals(ActivityType.ARTIST_SOLD, sold.event.type)
        assertEquals(firstSale.to.id, sold.counterparty?.id)
    }

    @Test
    fun `rivals ignore absurd asking prices and never overspend`() = runTest {
        val fixture = ServicesFixture()
        val simulator = fixture.simulator()
        simulator.catchUp()
        val seller = fixture.register("seller")
        val artistId = fixture.anyArtistOf(seller.user.labelId)
        val listing = fixture.market.createListing(seller.user.labelId, artistId, 50_000_000)

        repeat(24) {
            fixture.clock.advanceBy(1.hours)
            simulator.catchUp()
        }

        assertEquals(seller.user.labelId, fixture.store.read { findArtist(artistId) }?.labelId)
        assertEquals(listing.listing.id, fixture.store.read { findListingOfArtist(artistId) }?.id)
    }

    @Test
    fun `a long pause replays a capped number of ticks`() = runTest {
        val fixture = ServicesFixture()
        val simulator = fixture.simulator(SimulationConfig(maxCatchUpTicks = 5))
        simulator.catchUp()
        val dealsBefore = fixture.dealCount()

        fixture.clock.advanceBy(30.days)
        simulator.catchUp()

        assertEquals(fixture.clock.now, fixture.store.read { simulatedUntil() })
        assertTrue(fixture.dealCount() - dealsBefore <= 5)
    }

    @Test
    fun `the same seed and clock give the same market`() = runTest {
        val first = ServicesFixture(seed = 3)
        val second = ServicesFixture(seed = 3)

        listOf(first, second).forEach { fixture ->
            val simulator = fixture.simulator()
            simulator.catchUp()
            fixture.clock.advanceBy(3.hours)
            simulator.catchUp()
        }

        assertEquals(
            first.store.read { allListings() }.map { it.id }.sorted(),
            second.store.read { allListings() }.map { it.id }.sorted(),
        )
    }

    @Test
    fun `a disabled simulation creates nothing`() = runTest {
        val fixture = ServicesFixture()

        fixture.simulator(SimulationConfig(isEnabled = false)).catchUp()

        assertTrue(fixture.store.read { allLabels() }.isEmpty())
        assertNull(fixture.store.read { simulatedUntil() })
    }

    private fun ServicesFixture.simulator(config: SimulationConfig = SimulationConfig()) =
        RivalMarketSimulator(
            store = store,
            clock = clock,
            random = random,
            executor = executor,
            seeder =
                RivalLabelsSeeder(
                    config,
                    random,
                    LabelGenerator(random, idGenerator),
                    executor,
                    TimeZone.UTC,
                ),
            config = config,
        )

    private suspend fun ServicesFixture.dealCount(): Int = store.read {
        allLabels().flatMap { artistsOfLabel(it.id) }.sumOf { dealsOfArtist(it.id).size }
    }
}
