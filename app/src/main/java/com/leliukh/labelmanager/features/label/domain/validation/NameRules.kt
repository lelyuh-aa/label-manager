package com.leliukh.labelmanager.features.label.domain.validation

import com.leliukh.labelmanager.contract.rules.ContractLimits

/**
 * @param name a label name as typed
 * @return whether the server accepts it once trimmed
 */
fun isValidLabelName(name: String): Boolean =
    name.trim().length in ContractLimits.LABEL_NAME_MIN_LENGTH..ContractLimits.LABEL_NAME_MAX_LENGTH

/**
 * @param name a first or last name as typed
 * @return whether the server accepts it once trimmed
 */
fun isValidArtistName(name: String): Boolean =
    name.trim().length in
        ContractLimits.ARTIST_NAME_MIN_LENGTH..ContractLimits.ARTIST_NAME_MAX_LENGTH
