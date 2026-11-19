package com.leliukh.labelmanager.server.data

import com.leliukh.labelmanager.backend.data.StoreWriter
import com.leliukh.labelmanager.backend.domain.models.ActivityEvent
import com.leliukh.labelmanager.backend.domain.models.ActivityType
import com.leliukh.labelmanager.backend.domain.models.Listing
import com.leliukh.labelmanager.backend.domain.models.Artist
import com.leliukh.labelmanager.backend.domain.models.Genre
import com.leliukh.labelmanager.backend.domain.models.RefreshToken
import com.leliukh.labelmanager.backend.domain.models.Label
import com.leliukh.labelmanager.backend.domain.models.Deal
import com.leliukh.labelmanager.backend.domain.models.User
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert

/**
 * Store access inside the current Exposed transaction. Instants are stored as epoch milliseconds
 * and dates as ISO 8601 text, which keeps the schema portable across databases.
 */
internal object ExposedAccess : StoreWriter {

    private const val SIMULATED_UNTIL = "simulated_until"

    override fun findUser(userId: String): User? =
        UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull()?.toUser()

    override fun findUserByEmail(email: String): User? =
        UsersTable.selectAll().where { UsersTable.email eq email }.singleOrNull()?.toUser()

    override fun findLabel(labelId: String): Label? =
        LabelsTable.selectAll().where { LabelsTable.id eq labelId }.singleOrNull()?.toLabel()

    override fun allLabels(): List<Label> = LabelsTable.selectAll().map { it.toLabel() }

    override fun findArtist(artistId: String): Artist? =
        ArtistsTable.selectAll().where { ArtistsTable.id eq artistId }.singleOrNull()?.toArtist()

    override fun artistsOfLabel(labelId: String): List<Artist> =
        ArtistsTable.selectAll().where { ArtistsTable.labelId eq labelId }.map { it.toArtist() }

    override fun findListing(listingId: String): Listing? =
        ListingsTable.selectAll()
            .where { ListingsTable.id eq listingId }
            .singleOrNull()
            ?.toListing()

    override fun findListingOfArtist(artistId: String): Listing? =
        ListingsTable.selectAll()
            .where { ListingsTable.artistId eq artistId }
            .singleOrNull()
            ?.toListing()

    override fun allListings(): List<Listing> = ListingsTable.selectAll().map { it.toListing() }

    override fun dealsOfArtist(artistId: String): List<Deal> =
        DealsTable.selectAll()
            .where { DealsTable.artistId eq artistId }
            .map { it.toDeal() }

    override fun activityOfLabel(labelId: String): List<ActivityEvent> =
        ActivityTable.selectAll().where { ActivityTable.labelId eq labelId }.map { it.toActivity() }

    override fun findRefreshToken(tokenHash: String): RefreshToken? =
        RefreshTokensTable.selectAll()
            .where { RefreshTokensTable.tokenHash eq tokenHash }
            .singleOrNull()
            ?.let {
                RefreshToken(
                    tokenHash = it[RefreshTokensTable.tokenHash],
                    userId = it[RefreshTokensTable.userId],
                    expiresAt = Instant.fromEpochMilliseconds(it[RefreshTokensTable.expiresAt]),
                )
            }

    override fun simulatedUntil(): Instant? =
        MetadataTable.selectAll()
            .where { MetadataTable.key eq SIMULATED_UNTIL }
            .singleOrNull()
            ?.let { Instant.fromEpochMilliseconds(it[MetadataTable.value].toLong()) }

    override fun saveUser(user: User) {
        UsersTable.upsert {
            it[id] = user.id
            it[email] = user.email
            it[passwordHash] = user.passwordHash
            it[labelId] = user.labelId
            it[createdAt] = user.createdAt.toEpochMilliseconds()
        }
    }

    override fun saveLabel(label: Label) {
        LabelsTable.upsert {
            it[id] = label.id
            it[name] = label.name
            it[countryCode] = label.countryCode
            it[budget] = label.budget
            it[ownerUserId] = label.ownerUserId
        }
    }

    override fun saveArtist(artist: Artist) {
        ArtistsTable.upsert {
            it[id] = artist.id
            it[labelId] = artist.labelId
            it[firstName] = artist.firstName
            it[lastName] = artist.lastName
            it[countryCode] = artist.countryCode
            it[genre] = artist.genre.name
            it[birthDate] = artist.birthDate.toString()
            it[marketValue] = artist.marketValue
            it[hits] = artist.hits
        }
    }

    override fun saveListing(listing: Listing) {
        ListingsTable.upsert {
            it[id] = listing.id
            it[artistId] = listing.artistId
            it[sellerLabelId] = listing.sellerLabelId
            it[askingPrice] = listing.askingPrice
            it[listedAt] = listing.listedAt.toEpochMilliseconds()
        }
    }

    override fun deleteListing(listingId: String) {
        ListingsTable.deleteWhere { ListingsTable.id eq listingId }
    }

    override fun saveDeal(deal: Deal) {
        DealsTable.upsert {
            it[id] = deal.id
            it[artistId] = deal.artistId
            it[artistName] = deal.artistName
            it[fromLabelId] = deal.fromLabelId
            it[toLabelId] = deal.toLabelId
            it[price] = deal.price
            it[valueBefore] = deal.valueBefore
            it[valueAfter] = deal.valueAfter
            it[signedAt] = deal.signedAt.toEpochMilliseconds()
        }
    }

    override fun saveActivity(event: ActivityEvent) {
        ActivityTable.upsert {
            it[id] = event.id
            it[labelId] = event.labelId
            it[type] = event.type.name
            it[occurredAt] = event.occurredAt.toEpochMilliseconds()
            it[artistId] = event.artistId
            it[artistFirstName] = event.artistFirstName
            it[artistLastName] = event.artistLastName
            it[artistGenre] = event.artistGenre.name
            it[counterpartyLabelId] = event.counterpartyLabelId
            it[amount] = event.amount
            it[valueBefore] = event.valueBefore
            it[valueAfter] = event.valueAfter
            it[isRead] = event.isRead
        }
    }

    override fun markActivityRead(labelId: String, eventId: String?) {
        ActivityTable.update({
            val unread = (ActivityTable.labelId eq labelId) and (ActivityTable.isRead eq false)
            if (eventId == null) unread else unread and (ActivityTable.id eq eventId)
        }) {
            it[isRead] = true
        }
    }

    override fun saveRefreshToken(token: RefreshToken) {
        RefreshTokensTable.upsert {
            it[tokenHash] = token.tokenHash
            it[userId] = token.userId
            it[expiresAt] = token.expiresAt.toEpochMilliseconds()
        }
    }

    override fun deleteRefreshToken(tokenHash: String) {
        RefreshTokensTable.deleteWhere { RefreshTokensTable.tokenHash eq tokenHash }
    }

    override fun setSimulatedUntil(instant: Instant) {
        MetadataTable.upsert {
            it[key] = SIMULATED_UNTIL
            it[value] = instant.toEpochMilliseconds().toString()
        }
    }

    private fun ResultRow.toUser() =
        User(
            id = this[UsersTable.id],
            email = this[UsersTable.email],
            passwordHash = this[UsersTable.passwordHash],
            labelId = this[UsersTable.labelId],
            createdAt = Instant.fromEpochMilliseconds(this[UsersTable.createdAt]),
        )

    private fun ResultRow.toLabel() =
        Label(
            id = this[LabelsTable.id],
            name = this[LabelsTable.name],
            countryCode = this[LabelsTable.countryCode],
            budget = this[LabelsTable.budget],
            ownerUserId = this[LabelsTable.ownerUserId],
        )

    private fun ResultRow.toArtist() =
        Artist(
            id = this[ArtistsTable.id],
            labelId = this[ArtistsTable.labelId],
            firstName = this[ArtistsTable.firstName],
            lastName = this[ArtistsTable.lastName],
            countryCode = this[ArtistsTable.countryCode],
            genre = Genre.valueOf(this[ArtistsTable.genre]),
            birthDate = LocalDate.parse(this[ArtistsTable.birthDate]),
            marketValue = this[ArtistsTable.marketValue],
            hits = this[ArtistsTable.hits],
        )

    private fun ResultRow.toListing() =
        Listing(
            id = this[ListingsTable.id],
            artistId = this[ListingsTable.artistId],
            sellerLabelId = this[ListingsTable.sellerLabelId],
            askingPrice = this[ListingsTable.askingPrice],
            listedAt = Instant.fromEpochMilliseconds(this[ListingsTable.listedAt]),
        )

    private fun ResultRow.toDeal() =
        Deal(
            id = this[DealsTable.id],
            artistId = this[DealsTable.artistId],
            artistName = this[DealsTable.artistName],
            fromLabelId = this[DealsTable.fromLabelId],
            toLabelId = this[DealsTable.toLabelId],
            price = this[DealsTable.price],
            valueBefore = this[DealsTable.valueBefore],
            valueAfter = this[DealsTable.valueAfter],
            signedAt = Instant.fromEpochMilliseconds(this[DealsTable.signedAt]),
        )

    private fun ResultRow.toActivity() =
        ActivityEvent(
            id = this[ActivityTable.id],
            labelId = this[ActivityTable.labelId],
            type = ActivityType.valueOf(this[ActivityTable.type]),
            occurredAt = Instant.fromEpochMilliseconds(this[ActivityTable.occurredAt]),
            artistId = this[ActivityTable.artistId],
            artistFirstName = this[ActivityTable.artistFirstName],
            artistLastName = this[ActivityTable.artistLastName],
            artistGenre = Genre.valueOf(this[ActivityTable.artistGenre]),
            counterpartyLabelId = this[ActivityTable.counterpartyLabelId],
            amount = this[ActivityTable.amount],
            valueBefore = this[ActivityTable.valueBefore],
            valueAfter = this[ActivityTable.valueAfter],
            isRead = this[ActivityTable.isRead],
        )
}
