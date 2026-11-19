package com.leliukh.labelmanager.contract.rules

/**
 * Input limits the server validates, published so clients can validate the same way
 * before sending a request.
 */
public object ContractLimits {

    public const val PASSWORD_MIN_LENGTH: Int = 8
    public const val PASSWORD_MAX_LENGTH: Int = 128
    public const val EMAIL_MAX_LENGTH: Int = 254

    public const val LABEL_NAME_MIN_LENGTH: Int = 2
    public const val LABEL_NAME_MAX_LENGTH: Int = 40

    public const val ARTIST_NAME_MIN_LENGTH: Int = 1
    public const val ARTIST_NAME_MAX_LENGTH: Int = 30

    public const val MIN_ASKING_PRICE: Long = 1L
    public const val MAX_ASKING_PRICE: Long = 1_000_000_000L
}
