package com.leliukh.labelmanager.backend.api.endpoints

import com.leliukh.labelmanager.backend.api.mappers.toDomain
import com.leliukh.labelmanager.backend.api.mappers.toDto
import com.leliukh.labelmanager.backend.api.responses.HttpStatus
import com.leliukh.labelmanager.backend.api.responses.jsonResponse
import com.leliukh.labelmanager.backend.api.responses.noContentResponse
import com.leliukh.labelmanager.backend.api.routing.CallContext
import com.leliukh.labelmanager.backend.api.routing.Router
import com.leliukh.labelmanager.backend.api.routing.RoutesBuilder
import com.leliukh.labelmanager.backend.domain.services.ActivityService
import com.leliukh.labelmanager.backend.domain.services.AuthService
import com.leliukh.labelmanager.backend.domain.services.ListingQuery
import com.leliukh.labelmanager.backend.domain.services.ListingSort
import com.leliukh.labelmanager.backend.domain.services.MarketService
import com.leliukh.labelmanager.backend.domain.services.ArtistService
import com.leliukh.labelmanager.backend.domain.services.LabelService
import com.leliukh.labelmanager.backend.domain.validation.InputValidation
import com.leliukh.labelmanager.contract.dto.activity.ActivityPageDto
import com.leliukh.labelmanager.contract.dto.auth.AuthResponseDto
import com.leliukh.labelmanager.contract.dto.auth.LoginRequestDto
import com.leliukh.labelmanager.contract.dto.auth.RefreshRequestDto
import com.leliukh.labelmanager.contract.dto.auth.RegisterRequestDto
import com.leliukh.labelmanager.contract.dto.auth.UserDto
import com.leliukh.labelmanager.contract.dto.common.PageDto
import com.leliukh.labelmanager.contract.dto.market.CreateListingRequestDto
import com.leliukh.labelmanager.contract.dto.market.ListingDto
import com.leliukh.labelmanager.contract.dto.market.ListingSortDto
import com.leliukh.labelmanager.contract.dto.market.PurchaseResultDto
import com.leliukh.labelmanager.contract.dto.artist.ArtistDetailsDto
import com.leliukh.labelmanager.contract.dto.artist.ArtistDto
import com.leliukh.labelmanager.contract.dto.artist.GenreDto
import com.leliukh.labelmanager.contract.dto.artist.UpdateArtistRequestDto
import com.leliukh.labelmanager.contract.dto.reference.Countries
import com.leliukh.labelmanager.contract.dto.reference.CountryDto
import com.leliukh.labelmanager.contract.dto.label.LabelDto
import com.leliukh.labelmanager.contract.dto.label.UpdateLabelRequestDto
import com.leliukh.labelmanager.contract.routes.ApiQuery
import com.leliukh.labelmanager.contract.routes.ApiRoutes
import kotlinx.datetime.LocalDate
import kotlinx.serialization.builtins.ListSerializer

/**
 * The services every endpoint delegates to.
 *
 * @property auth registration, sign-in and tokens
 * @property labels the caller's label
 * @property artists artist details and editing
 * @property market the market
 * @property activity the activity feed
 * @property today the current date, for artist ages
 */
internal class EndpointServices(
    val auth: AuthService,
    val labels: LabelService,
    val artists: ArtistService,
    val market: MarketService,
    val activity: ActivityService,
    val today: () -> LocalDate,
)

/**
 * @param services what the endpoints delegate to
 * @return the router of every endpoint of [ApiRoutes]
 */
internal fun apiRouter(services: EndpointServices): Router =
    RoutesBuilder()
        .apply {
            authEndpoints(services)
            labelEndpoints(services)
            marketEndpoints(services)
            activityEndpoints(services)
            get(ApiRoutes.COUNTRIES) {
                jsonResponse(ListSerializer(CountryDto.serializer()), Countries.all)
            }
        }
        .build()

private fun RoutesBuilder.authEndpoints(services: EndpointServices) {
    post(ApiRoutes.REGISTER) {
        val request = body(RegisterRequestDto.serializer())
        val session = services.auth.register(request.email, request.password)
        jsonResponse(AuthResponseDto.serializer(), session.toDto(), HttpStatus.CREATED)
    }
    post(ApiRoutes.LOGIN) {
        val request = body(LoginRequestDto.serializer())
        val session = services.auth.login(request.email, request.password)
        jsonResponse(AuthResponseDto.serializer(), session.toDto())
    }
    post(ApiRoutes.REFRESH) {
        val session = services.auth.refresh(body(RefreshRequestDto.serializer()).refreshToken)
        jsonResponse(AuthResponseDto.serializer(), session.toDto())
    }
    post(ApiRoutes.LOGOUT) {
        services.auth.logout(body(RefreshRequestDto.serializer()).refreshToken)
        noContentResponse()
    }
    get(ApiRoutes.ME) {
        val claims = claims()
        val user = services.auth.user(claims.userId)
        jsonResponse(UserDto.serializer(), user.toDto())
    }
}

private fun RoutesBuilder.labelEndpoints(services: EndpointServices) {
    get(ApiRoutes.LABEL) {
        val label = services.labels.label(claims().labelId)
        jsonResponse(LabelDto.serializer(), label.toDto(services.today()))
    }
    patch(ApiRoutes.LABEL) {
        val labelId = claims().labelId
        val request = body(UpdateLabelRequestDto.serializer())
        val label = services.labels.update(labelId, request.name, request.countryCode)
        jsonResponse(LabelDto.serializer(), label.toDto(services.today()))
    }
    get(ApiRoutes.ARTIST) {
        claims()
        val details = services.artists.details(path(ApiRoutes.ARTIST_ID))
        jsonResponse(ArtistDetailsDto.serializer(), details.toDto(services.today()))
    }
    patch(ApiRoutes.ARTIST) {
        val labelId = claims().labelId
        val request = body(UpdateArtistRequestDto.serializer())
        val artist =
            services.artists.update(
                callerLabelId = labelId,
                artistId = path(ApiRoutes.ARTIST_ID),
                firstName = request.firstName,
                lastName = request.lastName,
                countryCode = request.countryCode,
            )
        jsonResponse(ArtistDto.serializer(), artist.toDto(services.today()))
    }
}

private fun RoutesBuilder.marketEndpoints(services: EndpointServices) {
    get(ApiRoutes.LISTINGS) {
        claims()
        val page = services.market.listings(listingQuery())
        jsonResponse(PageDto.serializer(ListingDto.serializer()), page.toDto(services.today()))
    }
    post(ApiRoutes.LISTINGS) {
        val labelId = claims().labelId
        val request = body(CreateListingRequestDto.serializer())
        val listing = services.market.createListing(labelId, request.artistId, request.askingPrice)
        jsonResponse(ListingDto.serializer(), listing.toDto(services.today()), HttpStatus.CREATED)
    }
    delete(ApiRoutes.LISTING) {
        services.market.withdraw(claims().labelId, path(ApiRoutes.LISTING_ID))
        noContentResponse()
    }
    post(ApiRoutes.LISTING_PURCHASE) {
        val result = services.market.purchase(claims().labelId, path(ApiRoutes.LISTING_ID))
        jsonResponse(PurchaseResultDto.serializer(), result.toDto(services.today()))
    }
}

private fun RoutesBuilder.activityEndpoints(services: EndpointServices) {
    get(ApiRoutes.ACTIVITY) {
        val labelId = claims().labelId
        val validation = InputValidation()
        val limit = validation.limit(query(ApiQuery.LIMIT))
        validation.throwIfInvalid()
        val page = services.activity.page(labelId, query(ApiQuery.CURSOR), limit)
        jsonResponse(ActivityPageDto.serializer(), page.toDto())
    }
    post(ApiRoutes.ACTIVITY_READ) {
        services.activity.markAllRead(claims().labelId)
        noContentResponse()
    }
    post(ApiRoutes.ACTIVITY_EVENT_READ) {
        services.activity.markRead(claims().labelId, path(ApiRoutes.EVENT_ID))
        noContentResponse()
    }
}

private fun CallContext.listingQuery(): ListingQuery {
    val validation = InputValidation()
    val query =
        ListingQuery(
            artistName = query(ApiQuery.ARTIST_NAME),
            labelName = query(ApiQuery.LABEL_NAME),
            artistCountry = query(ApiQuery.ARTIST_COUNTRY)?.uppercase(),
            labelCountry = query(ApiQuery.LABEL_COUNTRY)?.uppercase(),
            minPrice = validation.wholeNumber(ApiQuery.MIN_PRICE, query(ApiQuery.MIN_PRICE)),
            maxPrice = validation.wholeNumber(ApiQuery.MAX_PRICE, query(ApiQuery.MAX_PRICE)),
            genres = validation.genres(query(ApiQuery.GENRES)),
            sort = validation.sort(query(ApiQuery.SORT)),
            cursor = query(ApiQuery.CURSOR),
            limit = validation.limit(query(ApiQuery.LIMIT)),
        )
    validation.throwIfInvalid()
    return query
}

private fun InputValidation.wholeNumber(name: String, value: String?): Long? = value?.let {
    it.toLongOrNull() ?: null.also { reject(name, "must be a whole number") }
}

private fun InputValidation.limit(value: String?): Int {
    val limit = value?.toIntOrNull()
    return when {
        value == null -> ApiQuery.DEFAULT_LIMIT

        limit == null || limit !in 1..ApiQuery.MAX_LIMIT -> {
            reject(ApiQuery.LIMIT, "must be between 1 and ${ApiQuery.MAX_LIMIT}")
            ApiQuery.DEFAULT_LIMIT
        }

        else -> limit
    }
}

private fun InputValidation.genres(value: String?) =
    value
        ?.split(',')
        ?.map(String::trim)
        ?.filter(String::isNotEmpty)
        ?.mapNotNull { name ->
            GenreDto.entries.firstOrNull { it.name == name.uppercase() }?.toDomain()
                ?: null.also { reject(ApiQuery.GENRES, "has an unknown genre $name") }
        }
        ?.toSet()
        .orEmpty()

private fun InputValidation.sort(value: String?): ListingSort =
    value?.let { name ->
        ListingSortDto.entries.firstOrNull { it.name == name.uppercase() }?.toDomain()
            ?: null.also { reject(ApiQuery.SORT, "must be one of ${ListingSortDto.entries}") }
    } ?: ListingSort.NEWEST
