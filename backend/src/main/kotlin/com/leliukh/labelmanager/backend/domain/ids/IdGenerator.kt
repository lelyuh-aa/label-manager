package com.leliukh.labelmanager.backend.domain.ids

import kotlin.random.Random

/**
 * Creates ids in the UUID version 4 format from a given [Random], so generated data is reproducible
 * when the random source is seeded.
 *
 * @param random source of randomness
 */
class IdGenerator(private val random: Random) {

    /** @return a new random id such as `3f2b8c1e-9a4d-4b6f-8e21-5c7d9a0b1e42` */
    fun next(): String {
        val bytes = random.nextBytes(UUID_BYTES)
        bytes[VERSION_BYTE] = ((bytes[VERSION_BYTE].toInt() and 0x0f) or 0x40).toByte()
        bytes[VARIANT_BYTE] = ((bytes[VARIANT_BYTE].toInt() and 0x3f) or 0x80).toByte()
        val hex = bytes.joinToString("") { "%02x".format(it) }
        return listOf(
                hex.substring(0, 8),
                hex.substring(8, 12),
                hex.substring(12, 16),
                hex.substring(16, 20),
                hex.substring(20),
            )
            .joinToString("-")
    }

    private companion object {
        const val UUID_BYTES = 16
        const val VERSION_BYTE = 6
        const val VARIANT_BYTE = 8
    }
}
