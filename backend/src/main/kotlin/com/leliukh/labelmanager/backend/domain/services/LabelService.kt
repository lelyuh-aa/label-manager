package com.leliukh.labelmanager.backend.domain.services

import com.leliukh.labelmanager.backend.data.BackendStore
import com.leliukh.labelmanager.backend.domain.models.LabelView
import com.leliukh.labelmanager.backend.domain.validation.validate
import com.leliukh.labelmanager.contract.rules.ContractLimits

/**
 * The signed-in user's own label.
 *
 * @param store persistent state
 */
class LabelService(private val store: BackendStore) {

    /**
     * @param labelId id of the caller's label
     * @return the label with its roster
     */
    suspend fun label(labelId: String): LabelView = store.read { labelView(requireLabel(labelId)) }

    /**
     * @param labelId id of the caller's label
     * @param name new name, trimmed; null keeps the current one
     * @param countryCode new ISO 3166-1 alpha-2 country; null keeps the current one
     * @return the updated label
     */
    suspend fun update(labelId: String, name: String?, countryCode: String?): LabelView {
        val trimmedName = name?.trim()
        validate {
            trimmedName?.let {
                length(
                    "name",
                    it,
                    ContractLimits.LABEL_NAME_MIN_LENGTH..ContractLimits.LABEL_NAME_MAX_LENGTH,
                )
            }
            countryCode?.let { country("countryCode", it) }
        }
        return store.write {
            val label = requireLabel(labelId)
            val updated =
                label.copy(
                    name = trimmedName ?: label.name,
                    countryCode = countryCode ?: label.countryCode,
                )
            saveLabel(updated)
            labelView(updated)
        }
    }
}
