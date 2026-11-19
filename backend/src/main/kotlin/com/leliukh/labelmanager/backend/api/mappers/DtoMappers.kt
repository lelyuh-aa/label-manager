package com.leliukh.labelmanager.backend.api.mappers

import com.leliukh.labelmanager.backend.domain.models.ActivityPage
import com.leliukh.labelmanager.backend.domain.models.ActivityType
import com.leliukh.labelmanager.backend.domain.models.ActivityView
import com.leliukh.labelmanager.backend.domain.models.ListingView
import com.leliukh.labelmanager.backend.domain.models.Page
import com.leliukh.labelmanager.backend.domain.models.ArtistDetailsView
import com.leliukh.labelmanager.backend.domain.models.ArtistView
import com.leliukh.labelmanager.backend.domain.models.Genre
import com.leliukh.labelmanager.backend.domain.models.PurchaseView
import com.leliukh.labelmanager.backend.domain.models.Label
import com.leliukh.labelmanager.backend.domain.models.LabelView
import com.leliukh.labelmanager.backend.domain.models.DealView
import com.leliukh.labelmanager.backend.domain.models.User
import com.leliukh.labelmanager.backend.domain.rules.GameRules
import com.leliukh.labelmanager.backend.domain.services.ListingSort
import com.leliukh.labelmanager.backend.domain.services.Session
import com.leliukh.labelmanager.contract.dto.activity.ActivityEventDto
import com.leliukh.labelmanager.contract.dto.activity.ActivityPageDto
import com.leliukh.labelmanager.contract.dto.activity.ActivityArtistDto
import com.leliukh.labelmanager.contract.dto.activity.ActivityTypeDto
import com.leliukh.labelmanager.contract.dto.auth.AuthResponseDto
import com.leliukh.labelmanager.contract.dto.auth.UserDto
import com.leliukh.labelmanager.contract.dto.common.PageDto
import com.leliukh.labelmanager.contract.dto.market.ListingDto
import com.leliukh.labelmanager.contract.dto.market.ListingSortDto
import com.leliukh.labelmanager.contract.dto.market.PurchaseResultDto
import com.leliukh.labelmanager.contract.dto.artist.ListingSummaryDto
import com.leliukh.labelmanager.contract.dto.artist.ArtistDetailsDto
import com.leliukh.labelmanager.contract.dto.artist.ArtistDto
import com.leliukh.labelmanager.contract.dto.artist.GenreDto
import com.leliukh.labelmanager.contract.dto.label.LabelDto
import com.leliukh.labelmanager.contract.dto.label.LabelSummaryDto
import com.leliukh.labelmanager.contract.dto.deal.DealDto
import kotlinx.datetime.LocalDate

internal fun Genre.toDto(): GenreDto =
    when (this) {
        Genre.POP -> GenreDto.POP

        Genre.ROCK -> GenreDto.ROCK

        Genre.HIP_HOP -> GenreDto.HIP_HOP

        Genre.ELECTRONIC -> GenreDto.ELECTRONIC
    }

internal fun GenreDto.toDomain(): Genre =
    when (this) {
        GenreDto.POP -> Genre.POP

        GenreDto.ROCK -> Genre.ROCK

        GenreDto.HIP_HOP -> Genre.HIP_HOP

        GenreDto.ELECTRONIC -> Genre.ELECTRONIC
    }

internal fun ListingSortDto.toDomain(): ListingSort =
    when (this) {
        ListingSortDto.NEWEST -> ListingSort.NEWEST

        ListingSortDto.PRICE_ASCENDING -> ListingSort.PRICE_ASCENDING

        ListingSortDto.PRICE_DESCENDING -> ListingSort.PRICE_DESCENDING
    }

internal fun User.toDto(): UserDto = UserDto(id = id, email = email, labelId = labelId)

internal fun Session.toDto(): AuthResponseDto =
    AuthResponseDto(
        accessToken = accessToken,
        accessTokenExpiresAt = accessTokenExpiresAt,
        refreshToken = refreshToken,
        user = user.toDto(),
    )

internal fun Label.toSummaryDto(): LabelSummaryDto =
    LabelSummaryDto(id = id, name = name, countryCode = countryCode)

internal fun ArtistView.toDto(today: LocalDate): ArtistDto =
    ArtistDto(
        id = artist.id,
        labelId = artist.labelId,
        firstName = artist.firstName,
        lastName = artist.lastName,
        countryCode = artist.countryCode,
        genre = artist.genre.toDto(),
        birthDate = artist.birthDate,
        age = GameRules.ageOn(artist.birthDate, today),
        marketValue = artist.marketValue,
        hits = artist.hits,
        listing =
            listing?.let {
                ListingSummaryDto(id = it.id, askingPrice = it.askingPrice, listedAt = it.listedAt)
            },
    )

internal fun LabelView.toDto(today: LocalDate): LabelDto =
    LabelDto(
        id = label.id,
        name = label.name,
        countryCode = label.countryCode,
        budget = label.budget,
        value = value,
        artists = artists.map { it.toDto(today) },
    )

internal fun DealView.toDto(): DealDto =
    DealDto(
        id = deal.id,
        artistId = deal.artistId,
        artistName = deal.artistName,
        from = from.toSummaryDto(),
        to = to.toSummaryDto(),
        price = deal.price,
        valueBefore = deal.valueBefore,
        valueAfter = deal.valueAfter,
        signedAt = deal.signedAt,
    )

internal fun ArtistDetailsView.toDto(today: LocalDate): ArtistDetailsDto =
    ArtistDetailsDto(
        artist = artist.toDto(today),
        label = label.toSummaryDto(),
        deals = deals.map { it.toDto() },
    )

internal fun ListingView.toDto(today: LocalDate): ListingDto =
    ListingDto(
        id = listing.id,
        artist = artist.toDto(today),
        seller = seller.toSummaryDto(),
        askingPrice = listing.askingPrice,
        listedAt = listing.listedAt,
    )

internal fun Page<ListingView>.toDto(today: LocalDate): PageDto<ListingDto> =
    PageDto(items = items.map { it.toDto(today) }, nextCursor = nextCursor, totalCount = totalCount)

internal fun PurchaseView.toDto(today: LocalDate): PurchaseResultDto =
    PurchaseResultDto(deal = deal.toDto(), artist = artist.toDto(today), budget = budget)

internal fun ActivityView.toDto(): ActivityEventDto =
    ActivityEventDto(
        id = event.id,
        type = event.type.toDto(),
        occurredAt = event.occurredAt,
        artist =
            ActivityArtistDto(
                id = event.artistId,
                firstName = event.artistFirstName,
                lastName = event.artistLastName,
                genre = event.artistGenre.toDto(),
            ),
        counterparty = counterparty?.toSummaryDto(),
        amount = event.amount,
        valueBefore = event.valueBefore,
        valueAfter = event.valueAfter,
        isRead = event.isRead,
    )

internal fun ActivityPage.toDto(): ActivityPageDto =
    ActivityPageDto(
        items = page.items.map { it.toDto() },
        nextCursor = page.nextCursor,
        unreadCount = unreadCount,
    )

private fun ActivityType.toDto(): ActivityTypeDto =
    when (this) {
        ActivityType.ARTIST_BOUGHT -> ActivityTypeDto.ARTIST_BOUGHT

        ActivityType.ARTIST_SOLD -> ActivityTypeDto.ARTIST_SOLD

        ActivityType.ARTIST_LISTED -> ActivityTypeDto.ARTIST_LISTED

        ActivityType.ARTIST_UNLISTED -> ActivityTypeDto.ARTIST_UNLISTED
    }
