package com.leliukh.labelmanager.backend.domain.errors

/**
 * A request the rules of the game reject. Thrown inside a store transaction it also rolls the
 * transaction back. The API layer maps every subtype to an error response.
 *
 * @param message explanation for logs and the error body
 */
sealed class DomainException(message: String) : RuntimeException(message) {

    /** @param fieldErrors field name to problem description */
    class ValidationFailed(val fieldErrors: Map<String, String>) :
        DomainException("Validation failed: ${fieldErrors.keys.joinToString()}")

    class EmailTaken : DomainException("An account with this email already exists")

    class InvalidCredentials : DomainException("Wrong email or password")

    /** @param message what is wrong with the credentials */
    class Unauthorized(message: String) : DomainException(message)

    /** @param message which resource the caller may not touch */
    class Forbidden(message: String) : DomainException(message)

    /** @param message which resource is missing */
    class NotFound(message: String) : DomainException(message)

    class ArtistAlreadyListed : DomainException("The artist is already on the market")

    class ListingNotAvailable : DomainException("The listing was sold or withdrawn")

    class CannotBuyOwnArtist : DomainException("A label cannot buy its own artist")

    /**
     * @param required the asking price
     * @param available the buyer's budget
     */
    class InsufficientBudget(val required: Long, val available: Long) :
        DomainException("The budget of $available is below the asking price of $required")
}
