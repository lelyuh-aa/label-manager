package com.leliukh.labelmanager.backend.security

import com.leliukh.labelmanager.backend.BackendConfig
import com.leliukh.labelmanager.backend.domain.errors.DomainException
import com.leliukh.labelmanager.backend.domain.models.RefreshToken
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

/**
 * The verified content of an access token.
 *
 * @property userId id of the signed-in account
 * @property labelId id of the account's label
 * @property expiresAt moment the token stops being accepted
 */
data class AccessClaims(
    val userId: String,
    val labelId: String,
    val expiresAt: Instant,
)

/**
 * An access token ready to hand to the client.
 *
 * @property token the encoded token
 * @property expiresAt moment it stops being accepted
 */
data class AccessToken(
    val token: String,
    val expiresAt: Instant,
)

/**
 * A new refresh token: the plain value for the client and the record to store.
 *
 * @property token the plain token, never stored
 * @property record what the store keeps: the token's hash, owner and expiry
 */
data class IssuedRefreshToken(
    val token: String,
    val record: RefreshToken,
)

/**
 * Issues and verifies tokens.
 *
 * Access tokens are compact JWTs (`HS256`) carrying the account and label ids. Refresh tokens are
 * 256-bit random strings; only their SHA-256 hash is stored, so a leaked store cannot be used to
 * sign in.
 *
 * @param config token secret and lifetimes
 * @param clock source of the current time
 * @param secureRandom source of refresh tokens
 */
class TokenService(
    private val config: BackendConfig,
    private val clock: Clock,
    private val secureRandom: SecureRandom = SecureRandom(),
) {

    private val key = SecretKeySpec(config.tokenSecret.encodeToByteArray(), HMAC_ALGORITHM)
    private val encodedHeader = base64Url.encode(HEADER.encodeToByteArray())

    /**
     * @param userId id of the account
     * @param labelId id of the account's label
     * @return a signed access token valid for [BackendConfig.accessTokenTtl]
     */
    fun issueAccessToken(userId: String, labelId: String): AccessToken {
        val now = clock.now()
        val expiresAt = Instant.fromEpochSeconds((now + config.accessTokenTtl).epochSeconds)
        val payload = buildJsonObject {
            put(CLAIM_SUBJECT, userId)
            put(CLAIM_LABEL, labelId)
            put(CLAIM_ISSUED_AT, now.epochSeconds)
            put(CLAIM_EXPIRES_AT, expiresAt.epochSeconds)
        }
        val signingInput =
            encodedHeader + "." + base64Url.encode(payload.toString().encodeToByteArray())
        return AccessToken(signingInput + "." + sign(signingInput), expiresAt)
    }

    /**
     * @param token an access token from an `Authorization` header
     * @return the token's claims
     * @throws DomainException.Unauthorized when the token is malformed, forged or expired
     */
    fun verifyAccessToken(token: String): AccessClaims {
        val parts = token.split(".")
        if (parts.size != JWT_PARTS || parts[0] != encodedHeader) throw invalidToken()
        val signingInput = parts[0] + "." + parts[1]
        val expectedSignature = sign(signingInput).encodeToByteArray()
        if (!MessageDigest.isEqual(expectedSignature, parts[2].encodeToByteArray())) {
            throw invalidToken()
        }
        val payload =
            runCatching {
                Json.decodeFromString(
                    JsonObject.serializer(),
                    base64Url.decode(parts[1]).decodeToString(),
                )
            }
                .getOrNull() ?: throw invalidToken()
        val userId = payload.string(CLAIM_SUBJECT) ?: throw invalidToken()
        val labelId = payload.string(CLAIM_LABEL) ?: throw invalidToken()
        val expiresAt =
            payload[CLAIM_EXPIRES_AT]?.jsonPrimitive?.longOrNull?.let(Instant::fromEpochSeconds)
                ?: throw invalidToken()
        if (clock.now() >= expiresAt) throw DomainException.Unauthorized("The access token expired")
        return AccessClaims(userId, labelId, expiresAt)
    }

    /**
     * @param userId id of the account the token is for
     * @return a new refresh token valid for [BackendConfig.refreshTokenTtl]
     */
    fun issueRefreshToken(userId: String): IssuedRefreshToken {
        val token = base64Url.encode(ByteArray(REFRESH_TOKEN_BYTES).also(secureRandom::nextBytes))
        val record =
            RefreshToken(
                tokenHash = hashRefreshToken(token),
                userId = userId,
                expiresAt = clock.now() + config.refreshTokenTtl,
            )
        return IssuedRefreshToken(token, record)
    }

    /**
     * @param token a plain refresh token
     * @return its SHA-256 hash in hex, the key it is stored under
     */
    fun hashRefreshToken(token: String): String =
        MessageDigest.getInstance(SHA_256).digest(token.encodeToByteArray()).joinToString("") {
            "%02x".format(it)
        }

    private fun sign(signingInput: String): String {
        val mac = Mac.getInstance(HMAC_ALGORITHM).apply { init(key) }
        return base64Url.encode(mac.doFinal(signingInput.encodeToByteArray()))
    }

    private fun JsonObject.string(name: String): String? =
        (this[name] as? JsonPrimitive)?.takeIf { it.isString }?.content

    private fun invalidToken() = DomainException.Unauthorized("The access token is invalid")

    private companion object {
        const val HMAC_ALGORITHM = "HmacSHA256"
        const val SHA_256 = "SHA-256"
        const val HEADER = """{"alg":"HS256","typ":"JWT"}"""
        const val CLAIM_SUBJECT = "sub"
        const val CLAIM_LABEL = "tid"
        const val CLAIM_ISSUED_AT = "iat"
        const val CLAIM_EXPIRES_AT = "exp"
        const val JWT_PARTS = 3
        const val REFRESH_TOKEN_BYTES = 32
        val base64Url = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
    }
}
