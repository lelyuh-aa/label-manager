package com.leliukh.labelmanager.server.data

import org.jetbrains.exposed.v1.core.Table

private const val ID_LENGTH = 36
private const val NAME_LENGTH = 64
private const val CODE_LENGTH = 16

internal object UsersTable : Table("users") {
    val id = varchar("id", ID_LENGTH)
    val email = varchar("email", 254).uniqueIndex()
    val passwordHash = varchar("password_hash", 256)
    val labelId = varchar("label_id", ID_LENGTH)
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

internal object LabelsTable : Table("labels") {
    val id = varchar("id", ID_LENGTH)
    val name = varchar("name", NAME_LENGTH)
    val countryCode = varchar("country_code", CODE_LENGTH)
    val budget = long("budget")
    val ownerUserId = varchar("owner_user_id", ID_LENGTH).nullable()
    override val primaryKey = PrimaryKey(id)
}

internal object ArtistsTable : Table("artists") {
    val id = varchar("id", ID_LENGTH)
    val labelId = varchar("label_id", ID_LENGTH).index()
    val firstName = varchar("first_name", NAME_LENGTH)
    val lastName = varchar("last_name", NAME_LENGTH)
    val countryCode = varchar("country_code", CODE_LENGTH)
    val genre = varchar("genre", CODE_LENGTH)
    val birthDate = varchar("birth_date", CODE_LENGTH)
    val marketValue = long("market_value")
    val hits = integer("hits")
    override val primaryKey = PrimaryKey(id)
}

internal object ListingsTable : Table("listings") {
    val id = varchar("id", ID_LENGTH)
    val artistId = varchar("artist_id", ID_LENGTH).uniqueIndex()
    val sellerLabelId = varchar("seller_label_id", ID_LENGTH).index()
    val askingPrice = long("asking_price")
    val listedAt = long("listed_at")
    override val primaryKey = PrimaryKey(id)
}

internal object DealsTable : Table("deals") {
    val id = varchar("id", ID_LENGTH)
    val artistId = varchar("artist_id", ID_LENGTH).index()
    val artistName = varchar("artist_name", NAME_LENGTH * 2)
    val fromLabelId = varchar("from_label_id", ID_LENGTH)
    val toLabelId = varchar("to_label_id", ID_LENGTH)
    val price = long("price")
    val valueBefore = long("value_before")
    val valueAfter = long("value_after")
    val signedAt = long("signed_at")
    override val primaryKey = PrimaryKey(id)
}

internal object ActivityTable : Table("activity_events") {
    val id = varchar("id", ID_LENGTH)
    val labelId = varchar("label_id", ID_LENGTH).index()
    val type = varchar("type", CODE_LENGTH * 2)
    val occurredAt = long("occurred_at")
    val artistId = varchar("artist_id", ID_LENGTH)
    val artistFirstName = varchar("artist_first_name", NAME_LENGTH)
    val artistLastName = varchar("artist_last_name", NAME_LENGTH)
    val artistGenre = varchar("artist_genre", CODE_LENGTH)
    val counterpartyLabelId = varchar("counterparty_label_id", ID_LENGTH).nullable()
    val amount = long("amount").nullable()
    val valueBefore = long("value_before").nullable()
    val valueAfter = long("value_after").nullable()
    val isRead = bool("is_read")
    override val primaryKey = PrimaryKey(id)
}

internal object RefreshTokensTable : Table("refresh_tokens") {
    val tokenHash = varchar("token_hash", 64)
    val userId = varchar("user_id", ID_LENGTH).index()
    val expiresAt = long("expires_at")
    override val primaryKey = PrimaryKey(tokenHash)
}

internal object MetadataTable : Table("metadata") {
    val key = varchar("key", NAME_LENGTH)
    val value = varchar("value", NAME_LENGTH)
    override val primaryKey = PrimaryKey(key)
}

internal val allTables =
    arrayOf(
        UsersTable,
        LabelsTable,
        ArtistsTable,
        ListingsTable,
        DealsTable,
        ActivityTable,
        RefreshTokensTable,
        MetadataTable,
    )
