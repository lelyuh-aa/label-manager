package com.leliukh.labelmanager.backend.domain.services

import com.leliukh.labelmanager.backend.domain.errors.DomainException
import com.leliukh.labelmanager.backend.domain.models.ActivityType
import com.leliukh.labelmanager.backend.domain.models.ListingView
import com.leliukh.labelmanager.backend.domain.models.Genre
import com.leliukh.labelmanager.backend.testing.ServicesFixture
import com.leliukh.labelmanager.backend.testing.expectFailure
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import com.leliukh.labelmanager.backend.domain.rules.GameRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketServiceTest {

    private val fixture = ServicesFixture()

    @Test
    fun `buying moves the artist and the money and raises the value`() = runTest {
        val seller = fixture.register("seller")
        val buyer = fixture.register("buyer")
        val artistId = fixture.anyArtistOf(seller.user.labelId)
        val sellerBudget = fixture.labels.label(seller.user.labelId).label.budget
        val buyerBudget = fixture.labels.label(buyer.user.labelId).label.budget
        val before = fixture.store.read { findArtist(artistId) }!!
        val listing = fixture.market.createListing(seller.user.labelId, artistId, 1_500_000)

        val result = fixture.market.purchase(buyer.user.labelId, listing.listing.id)

        assertEquals(buyerBudget - 1_500_000, result.budget)
        assertEquals(sellerBudget + 1_500_000, fixture.labels.label(seller.user.labelId).label.budget)
        assertEquals(buyer.user.labelId, result.artist.artist.labelId)
        assertEquals(
            GameRules.valueAfterDeal(before.marketValue, before.hits),
            result.artist.artist.marketValue,
        )
        assertEquals(15, fixture.labels.label(seller.user.labelId).artists.size)
        assertEquals(17, fixture.labels.label(buyer.user.labelId).artists.size)
        assertNull(fixture.store.read { findListing(listing.listing.id) })
        val history = fixture.artists.details(artistId).deals
        assertEquals(1, history.size)
        assertEquals(before.marketValue, history.single().deal.valueBefore)
        assertEquals(1_500_000L, history.single().deal.price)
    }

    @Test
    fun `label value is the sum of artist values`() = runTest {
        val seller = fixture.register("seller")
        val buyer = fixture.register("buyer")
        val artistId = fixture.anyArtistOf(seller.user.labelId)
        val artistValue = fixture.store.read { findArtist(artistId) }!!.marketValue
        val buyerValue = fixture.labels.label(buyer.user.labelId).value
        val sellerValue = fixture.labels.label(seller.user.labelId).value
        assertEquals(
            fixture.labels.label(buyer.user.labelId).artists.sumOf { it.artist.marketValue },
            buyerValue,
        )
        val listing = fixture.market.createListing(seller.user.labelId, artistId, 1_000)

        val bought = fixture.market.purchase(buyer.user.labelId, listing.listing.id)

        assertEquals(
            buyerValue + bought.artist.artist.marketValue,
            fixture.labels.label(buyer.user.labelId).value,
        )
        assertEquals(sellerValue - artistValue, fixture.labels.label(seller.user.labelId).value)
    }

    @Test
    fun `both labels see the deal in their activity`() = runTest {
        val seller = fixture.register("seller")
        val buyer = fixture.register("buyer")
        val listing =
            fixture.market.createListing(
                seller.user.labelId,
                fixture.anyArtistOf(seller.user.labelId),
                2_000_000,
            )
        fixture.clock.advanceBy(1.minutes)

        fixture.market.purchase(buyer.user.labelId, listing.listing.id)

        val sellerFeed = fixture.activity.page(seller.user.labelId, null, 20)
        assertEquals(
            listOf(ActivityType.ARTIST_SOLD, ActivityType.ARTIST_LISTED),
            sellerFeed.page.items.map { it.event.type },
        )
        assertEquals(buyer.user.labelId, sellerFeed.page.items.first().counterparty?.id)
        assertEquals(2, sellerFeed.unreadCount)
        val buyerFeed = fixture.activity.page(buyer.user.labelId, null, 20)
        assertEquals(ActivityType.ARTIST_BOUGHT, buyerFeed.page.items.single().event.type)
        assertEquals(2_000_000L, buyerFeed.page.items.single().event.amount)

        fixture.activity.markAllRead(seller.user.labelId)
        assertEquals(0, fixture.activity.page(seller.user.labelId, null, 20).unreadCount)
        assertEquals(1, fixture.activity.page(buyer.user.labelId, null, 20).unreadCount)
    }

    @Test
    fun `a label cannot buy its own artist or overspend`() = runTest {
        val seller = fixture.register("seller")
        val buyer = fixture.register("buyer")
        val cheap =
            fixture.market.createListing(
                seller.user.labelId,
                fixture.anyArtistOf(seller.user.labelId),
                1_000,
            )
        val artists = fixture.store.read { artistsOfLabel(seller.user.labelId) }
        val pricey = fixture.market.createListing(seller.user.labelId, artists[1].id, 900_000_000)

        expectFailure<DomainException.CannotBuyOwnArtist> {
            fixture.market.purchase(seller.user.labelId, cheap.listing.id)
        }
        val shortfall =
            expectFailure<DomainException.InsufficientBudget> {
                fixture.market.purchase(buyer.user.labelId, pricey.listing.id)
            }
        assertEquals(900_000_000L, shortfall.required)
        assertEquals(16, fixture.labels.label(buyer.user.labelId).artists.size)
    }

    @Test
    fun `a sold listing is no longer available`() = runTest {
        val seller = fixture.register("seller")
        val buyer = fixture.register("buyer")
        val other = fixture.register("other")
        val listing =
            fixture.market.createListing(
                seller.user.labelId,
                fixture.anyArtistOf(seller.user.labelId),
                1_000,
            )
        fixture.market.purchase(buyer.user.labelId, listing.listing.id)

        expectFailure<DomainException.ListingNotAvailable> {
            fixture.market.purchase(other.user.labelId, listing.listing.id)
        }
    }

    @Test
    fun `of two simultaneous buyers exactly one gets the artist`() = runTest {
        val seller = fixture.register("seller")
        val buyers = List(6) { fixture.register("buyer$it") }
        val listing =
            fixture.market.createListing(
                seller.user.labelId,
                fixture.anyArtistOf(seller.user.labelId),
                1_000,
            )

        val results =
            withContext(Dispatchers.Default) {
                buyers
                    .map { buyer ->
                        async {
                            runCatching {
                                fixture.market.purchase(buyer.user.labelId, listing.listing.id)
                            }
                        }
                    }
                    .awaitAll()
            }

        assertEquals(1, results.count { it.isSuccess })
        assertTrue(
            results
                .filter { it.isFailure }
                .all { it.exceptionOrNull() is DomainException.ListingNotAvailable }
        )
        assertEquals(15, fixture.labels.label(seller.user.labelId).artists.size)
    }

    @Test
    fun `only the owner lists an artist, once, at a valid price`() = runTest {
        val seller = fixture.register("seller")
        val stranger = fixture.register("stranger")
        val artistId = fixture.anyArtistOf(seller.user.labelId)

        expectFailure<DomainException.Forbidden> {
            fixture.market.createListing(stranger.user.labelId, artistId, 1_000)
        }
        expectFailure<DomainException.ValidationFailed> {
            fixture.market.createListing(seller.user.labelId, artistId, 0)
        }
        expectFailure<DomainException.NotFound> {
            fixture.market.createListing(seller.user.labelId, "missing", 1_000)
        }
        fixture.market.createListing(seller.user.labelId, artistId, 1_000)
        expectFailure<DomainException.ArtistAlreadyListed> {
            fixture.market.createListing(seller.user.labelId, artistId, 2_000)
        }
        assertEquals(
            1_000L,
            fixture.labels
                .label(seller.user.labelId)
                .artists
                .first { it.artist.id == artistId }
                .listing
                ?.askingPrice,
        )
    }

    @Test
    fun `only the seller withdraws a listing`() = runTest {
        val seller = fixture.register("seller")
        val stranger = fixture.register("stranger")
        val listing =
            fixture.market.createListing(
                seller.user.labelId,
                fixture.anyArtistOf(seller.user.labelId),
                1_000,
            )

        expectFailure<DomainException.Forbidden> {
            fixture.market.withdraw(stranger.user.labelId, listing.listing.id)
        }
        fixture.clock.advanceBy(1.minutes)
        fixture.market.withdraw(seller.user.labelId, listing.listing.id)

        assertTrue(fixture.market.listings(ListingQuery()).items.isEmpty())
        assertEquals(
            ActivityType.ARTIST_UNLISTED,
            fixture.activity.page(seller.user.labelId, null, 1).page.items.single().event.type,
        )
        expectFailure<DomainException.NotFound> {
            fixture.market.withdraw(seller.user.labelId, listing.listing.id)
        }
    }

    @Test
    fun `the market filters by name, label, country, price and genre`() = runTest {
        val seller = fixture.register("seller")
        val label = fixture.labels.update(seller.user.labelId, "Garonne Records", "FR").label
        val roster = fixture.store.read { artistsOfLabel(label.id) }
        roster.forEachIndexed { index, artist ->
            fixture.clock.advanceBy(1.minutes)
            fixture.market.createListing(label.id, artist.id, 1_000_000L + index * 100_000L)
        }
        val pop = roster.first { it.genre == Genre.POP }

        assertEquals(16, fixture.market.listings(ListingQuery(limit = 50)).totalCount)
        assertEquals(16, fixture.market.listings(ListingQuery(labelName = "garonne")).totalCount)
        assertEquals(0, fixture.market.listings(ListingQuery(labelName = "lagos")).totalCount)
        assertEquals(16, fixture.market.listings(ListingQuery(labelCountry = "FR")).totalCount)
        val popOnly = ListingQuery(genres = setOf(Genre.POP))
        assertEquals(4, fixture.market.listings(popOnly).totalCount)
        assertEquals(3, fixture.market.listings(ListingQuery(minPrice = 2_300_000L)).totalCount)
        assertEquals(
            2,
            fixture.market
                .listings(ListingQuery(minPrice = 1_100_000L, maxPrice = 1_200_000L))
                .totalCount,
        )
        assertTrue(
            fixture.market
                .listings(ListingQuery(artistName = pop.lastName.uppercase()))
                .items
                .any { it.artist.artist.id == pop.id }
        )
        val country = roster.first().countryCode
        assertEquals(
            roster.count { it.countryCode == country },
            fixture.market.listings(ListingQuery(artistCountry = country)).totalCount,
        )
    }

    @Test
    fun `pages follow the sort order without repeats`() = runTest {
        val seller = fixture.register("seller")
        val roster = fixture.store.read { artistsOfLabel(seller.user.labelId) }
        roster.forEachIndexed { index, artist ->
            fixture.clock.advanceBy(1.minutes)
            fixture.market.createListing(
                seller.user.labelId,
                artist.id,
                1_000L * ((index * 7) % 20 + 1),
            )
        }

        ListingSort.entries.forEach { sort ->
            val collected = mutableListOf<ListingView>()
            var cursor: String? = null
            do {
                val page =
                    fixture.market.listings(ListingQuery(sort = sort, cursor = cursor, limit = 6))
                collected += page.items
                cursor = page.nextCursor
            } while (cursor != null)

            assertEquals(16, collected.map { it.listing.id }.toSet().size)
            val expected =
                when (sort) {
                    ListingSort.NEWEST -> collected.sortedByDescending { it.listing.listedAt }

                    ListingSort.PRICE_ASCENDING -> collected.sortedBy { it.listing.askingPrice }

                    ListingSort.PRICE_DESCENDING ->
                        collected.sortedByDescending { it.listing.askingPrice }
                }
            assertEquals(expected, collected)
        }
        expectFailure<DomainException.ValidationFailed> {
            fixture.market.listings(ListingQuery(cursor = "not-a-cursor"))
        }
    }
}
