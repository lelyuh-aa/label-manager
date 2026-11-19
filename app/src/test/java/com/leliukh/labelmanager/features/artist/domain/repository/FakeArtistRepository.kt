package com.leliukh.labelmanager.features.artist.domain.repository

import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.network.domain.map
import com.leliukh.labelmanager.features.artist.domain.models.ArtistDetails
import com.leliukh.labelmanager.features.artist.domain.models.LabelSummary
import com.leliukh.labelmanager.features.artist.domain.models.Deal
import com.leliukh.labelmanager.features.label.domain.models.Artist
import com.leliukh.labelmanager.features.label.domain.repository.testArtist
import kotlin.time.Instant

/**
 * [ArtistRepository] with preset answers that remembers what it was asked.
 *
 * @param details answer to every [getArtist], before marking own artists
 */
class FakeArtistRepository(
    var details: ApiResult<ArtistDetails> = ApiResult.Success(testDetails()),
) : ArtistRepository {

    /** Answer to every [updateArtist]. */
    var updateResult: ApiResult<Artist> = ApiResult.Success(testArtist())

    var loads = 0
        private set

    /** Arguments of every [updateArtist]. */
    val updates = mutableListOf<List<String>>()

    override suspend fun getArtist(artistId: String, myLabelId: String?): ApiResult<ArtistDetails> {
        loads++
        return details.map { it.copy(isMine = it.label.id == myLabelId) }
    }

    override suspend fun updateArtist(
        artistId: String,
        firstName: String,
        lastName: String,
        countryCode: String,
    ): ApiResult<Artist> {
        updates += listOf(artistId, firstName, lastName, countryCode)
        return updateResult
    }
}

/**
 * An artist of `label-1` bought once from Garonne Records.
 *
 * @param deals the deals, newest first
 */
fun testDetails(deals: List<Deal> = listOf(testDeal())) = ArtistDetails(
    artist = testArtist(firstName = "Théo", lastName = "Marchand", marketValue = 1_450_000),
    label = LabelSummary(id = "label-1", name = "Rhein Valley Records", countryCode = "DE"),
    deals = deals,
    isMine = false,
)

/** Garonne Records → Rhein Valley Records for $1M, value $1M → $1.72M. */
fun testDeal() = Deal(
    id = "deal-1",
    from = LabelSummary(id = "label-2", name = "Garonne Records", countryCode = "FR"),
    to = LabelSummary(id = "label-1", name = "Rhein Valley Records", countryCode = "DE"),
    price = 1_000_000,
    valueBefore = 1_000_000,
    valueAfter = 1_450_000,
    signedAt = Instant.parse("2026-10-18T10:00:00Z"),
)
