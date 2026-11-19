package com.leliukh.labelmanager.contract.serialization

import kotlinx.serialization.json.Json

/**
 * The JSON configuration shared by every party of the API, so a payload written by one
 * side is always readable by the other.
 *
 * Unknown keys are ignored to let the server add fields without breaking older clients;
 * null fields are omitted from the output.
 */
public val ApiJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}
