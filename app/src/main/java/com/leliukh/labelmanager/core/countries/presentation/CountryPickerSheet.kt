package com.leliukh.labelmanager.core.countries.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.contract.dto.reference.Countries
import com.leliukh.labelmanager.core.design_system.presentation.components.field.SearchField
import com.leliukh.labelmanager.core.design_system.presentation.components.sheet.AppBottomSheet
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.formatting.presentation.countryDisplayName
import com.leliukh.labelmanager.core.formatting.presentation.countryFlagEmoji
import com.leliukh.labelmanager.core.formatting.presentation.normalizedForSearch
import java.text.Collator
import java.util.Locale

/**
 * A country the API accepts, named in the user's language.
 *
 * @property code ISO 3166-1 alpha-2 code
 * @property name localised name
 * @property flag flag emoji
 */
data class CountryOption(
    val code: String,
    val name: String,
    val flag: String,
)

/**
 * Every country the API accepts, sorted by its name in [locale].
 *
 * @param locale language of the names and of the sort order
 */
fun countryOptions(locale: Locale = Locale.getDefault()): List<CountryOption> {
    val collator = Collator.getInstance(locale)
    return Countries.all
        .map { country ->
            CountryOption(
                code = country.code,
                name = countryDisplayName(country.code, locale),
                flag = countryFlagEmoji(country.code),
            )
        }
        .sortedWith(compareBy(collator) { it.name })
}

/**
 * Bottom sheet with a searchable list of countries; the chosen one is ticked. With
 * [onSelectAny] the list starts with "Any country", for filters.
 *
 * @param selectedCode code of the current country; null for any
 * @param onSelect called with the tapped country's code; the caller closes the sheet
 * @param onDismiss called when the sheet is swiped away
 * @param onSelectAny called by "Any country"; null hides that row
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerSheet(
    selectedCode: String?,
    onSelect: (code: String) -> Unit,
    onDismiss: () -> Unit,
    onSelectAny: (() -> Unit)? = null,
) {
    val options = remember { countryOptions() }
    var query by rememberSaveable { mutableStateOf("") }
    val needle = query.trim().normalizedForSearch()
    val shown = options.filter { needle.isEmpty() || needle in it.name.normalizedForSearch() }

    AppBottomSheet(onDismiss = onDismiss, title = stringResource(R.string.country_picker_title)) {
        SearchField(
            query = query,
            onQueryChange = { query = it },
            placeholder = stringResource(R.string.country_picker_search),
        )

        LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
            if (onSelectAny != null && needle.isEmpty()) {
                item(key = "any") {
                    CountryRow(
                        flag = "🌍",
                        name = stringResource(R.string.country_picker_any),
                        isSelected = selectedCode == null,
                        onClick = onSelectAny,
                    )
                }
            }

            items(shown, key = { it.code }) { option ->
                CountryRow(
                    flag = option.flag,
                    name = option.name,
                    isSelected = option.code == selectedCode,
                    onClick = { onSelect(option.code) },
                )
            }
        }
    }
}

@Composable
private fun CountryRow(flag: String, name: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = AppThemeWrapper.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(AppThemeWrapper.shapes.control)
            .background(if (isSelected) colors.accentSoft else colors.surface)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { selected = isSelected }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(text = flag, fontSize = 20.sp)

        Text(
            text = name,
            style = if (isSelected) {
                AppThemeWrapper.typography.bodySemibold
            } else {
                AppThemeWrapper.typography.body
            },
            color = if (isSelected) colors.accentText else colors.textPrimary,
            modifier = Modifier.weight(1f),
        )

        if (isSelected) {
            Icon(
                imageVector = AppIcons.Check,
                contentDescription = null,
                tint = colors.accentText,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
