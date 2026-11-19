package com.leliukh.labelmanager.features.profile.presentation.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButton
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButtonStyle
import com.leliukh.labelmanager.core.design_system.presentation.components.chip.SegmentedControl
import com.leliukh.labelmanager.core.design_system.presentation.components.list.GroupPosition
import com.leliukh.labelmanager.core.design_system.presentation.components.list.GroupedItem
import com.leliukh.labelmanager.core.design_system.presentation.components.list.ListRow
import com.leliukh.labelmanager.core.design_system.presentation.components.list.SectionHeader
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.LargeTitle
import com.leliukh.labelmanager.core.design_system.presentation.components.sheet.AppBottomSheet
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.formatting.presentation.formatMoneyCompact
import com.leliukh.labelmanager.core.settings.domain.models.ThemeMode
import com.leliukh.labelmanager.core.ui_utils.presentation.CollectEvents
import com.leliukh.labelmanager.features.home.presentation.models.HomeTab
import com.leliukh.labelmanager.features.home.presentation.screen.HomeScreen
import com.leliukh.labelmanager.features.profile.presentation.viewmodel.ProfileViewAction
import com.leliukh.labelmanager.features.profile.presentation.viewmodel.ProfileViewEvent
import com.leliukh.labelmanager.features.profile.presentation.viewmodel.ProfileViewModel
import com.leliukh.labelmanager.features.profile.presentation.viewmodel.ProfileViewState

/**
 * The Profile tab: the account and the label, preferences, the app version and Sign out.
 *
 * @param bottomPadding space the content leaves at the bottom for the navigation bar
 * @param onEditLabel called by the label row
 * @param viewModel state holder of the screen
 */
@Composable
fun ProfileScreen(
    bottomPadding: PaddingValues,
    onEditLabel: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CollectEvents(viewModel.uiEventsFlow) { event ->
        when (event) {
            ProfileViewEvent.OpenEditLabel -> onEditLabel()

            ProfileViewEvent.OpenLanguageSettings -> openLanguageSettings(context)
        }
    }

    ProfileContent(
        state = state,
        onAction = viewModel::handleUiAction,
        bottomPadding = bottomPadding,
        canChangeLanguage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
    )
}

@Composable
private fun ProfileContent(
    state: ProfileViewState,
    onAction: (ProfileViewAction) -> Unit,
    bottomPadding: PaddingValues,
    canChangeLanguage: Boolean,
) {
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = statusBar + 12.dp, bottom = bottomPadding.calculateBottomPadding()),
    ) {
        LargeTitle(
            text = stringResource(R.string.profile_title),
            modifier = Modifier.padding(horizontal = AppThemeWrapper.dimens.screenTitleInset),
        )

        AccountCard(state = state, onAction = onAction)

        SectionHeader(
            title = stringResource(R.string.profile_preferences),
            modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
        )

        GroupedItem(position = GroupPosition.First) {
            ListRow(
                title = stringResource(R.string.profile_sale_alerts),
                subtitle = { RowSubtitle(stringResource(R.string.profile_sale_alerts_hint)) },
                leading = { RowIcon(AppIcons.Bell) },
                trailing = {
                    Switch(
                        checked = state.isSaleAlertsEnabled,
                        onCheckedChange = { onAction(ProfileViewAction.SaleAlertsToggled(it)) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = AppThemeWrapper.colors.accent,
                            uncheckedTrackColor = AppThemeWrapper.colors.fill,
                        ),
                    )
                },
            )
        }

        GroupedItem(
            position = if (canChangeLanguage) GroupPosition.Middle else GroupPosition.Last,
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 12.dp,
                    top = 12.dp,
                    bottom = 14.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RowIcon(AppIcons.Moon)

                    Text(
                        text = stringResource(R.string.profile_appearance),
                        style = AppThemeWrapper.typography.bodyMedium,
                        color = AppThemeWrapper.colors.textPrimary,
                    )
                }

                SegmentedControl(
                    options = ThemeMode.entries.map { stringResource(it.labelRes) },
                    selectedIndex = state.themeMode.ordinal,
                    onSelect = { onAction(ProfileViewAction.ThemeChosen(ThemeMode.entries[it])) },
                )
            }
        }

        if (canChangeLanguage) {
            GroupedItem(
                position = GroupPosition.Last,
                onClick = { onAction(ProfileViewAction.LanguageClicked) },
            ) {
                ListRow(
                    title = stringResource(R.string.profile_language),
                    leading = { RowIcon(AppIcons.Globe) },
                    trailing = {
                        val locale = LocalConfiguration.current.locales[0]
                        Text(
                            text = locale.getDisplayLanguage(locale)
                                .replaceFirstChar { it.titlecase(locale) },
                            style = AppThemeWrapper.typography.callout,
                            color = AppThemeWrapper.colors.textSecondary,
                        )
                    },
                    showChevron = true,
                )
            }
        }

        GroupedItem(position = GroupPosition.Single, modifier = Modifier.padding(top = 24.dp)) {
            ListRow(
                title = stringResource(R.string.profile_version),
                leading = { RowIcon(AppIcons.Info) },
                trailing = {
                    Text(
                        text = state.version,
                        style = AppThemeWrapper.typography.callout,
                        color = AppThemeWrapper.colors.textSecondary,
                    )
                },
            )
        }

        AppButton(
            text = stringResource(R.string.profile_sign_out),
            onClick = { onAction(ProfileViewAction.SignOutClicked) },
            style = AppButtonStyle.Destructive,
            icon = AppIcons.LogOut,
            modifier = Modifier
                .padding(horizontal = AppThemeWrapper.dimens.space4)
                .padding(top = 24.dp),
        )
    }

    if (state.isSignOutConfirmOpen) {
        SignOutSheet(state = state, onAction = onAction)
    }
}

@Composable
private fun AccountCard(state: ProfileViewState, onAction: (ProfileViewAction) -> Unit) {
    val colors = AppThemeWrapper.colors

    Column(modifier = Modifier.padding(top = 20.dp)) {
        GroupedItem(position = GroupPosition.First) {
            ListRow(
                title = state.email,
                subtitle = { RowSubtitle(stringResource(R.string.profile_signed_in)) },
                leading = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(colors.accent, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.email.take(1).uppercase(),
                            style = AppThemeWrapper.typography.title,
                            color = colors.onAccent,
                        )
                    }
                },
            )
        }

        GroupedItem(
            position = GroupPosition.Last,
            onClick = { onAction(ProfileViewAction.LabelClicked) },
        ) {
            ListRow(
                title = state.labelName,
                subtitle = {
                    RowSubtitle(
                        pluralStringResource(
                            R.plurals.roster_artist_count,
                            state.artistCount,
                            state.artistCount,
                        ) + " · " + stringResource(
                            R.string.profile_roster_value,
                            formatMoneyCompact(state.rosterValue),
                        ),
                    )
                },
                leading = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(colors.fill, AppThemeWrapper.shapes.small),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = state.labelFlag, fontSize = 24.sp)
                    }
                },
                showChevron = true,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignOutSheet(state: ProfileViewState, onAction: (ProfileViewAction) -> Unit) {
    AppBottomSheet(onDismiss = { onAction(ProfileViewAction.DismissSignOut) }) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(AppThemeWrapper.colors.negativeSoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.LogOut,
                    contentDescription = null,
                    tint = AppThemeWrapper.colors.negative,
                )
            }

            Text(
                text = stringResource(R.string.profile_sign_out_title),
                style = AppThemeWrapper.typography.title,
                color = AppThemeWrapper.colors.textPrimary,
            )

            Text(
                text = stringResource(R.string.profile_sign_out_message, state.email),
                style = AppThemeWrapper.typography.callout,
                color = AppThemeWrapper.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            modifier = Modifier.padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppButton(
                text = stringResource(R.string.profile_sign_out),
                onClick = { onAction(ProfileViewAction.ConfirmSignOut) },
                style = AppButtonStyle.Destructive,
                isLoading = state.isSigningOut,
            )

            AppButton(
                text = stringResource(R.string.market_buy_cancel),
                onClick = { onAction(ProfileViewAction.DismissSignOut) },
                style = AppButtonStyle.Secondary,
                enabled = !state.isSigningOut,
            )
        }
    }
}

@Composable
private fun RowIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(AppThemeWrapper.colors.accentSoft, AppThemeWrapper.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppThemeWrapper.colors.accentText,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun RowSubtitle(text: String) {
    Text(
        text = text,
        style = AppThemeWrapper.typography.subhead,
        color = AppThemeWrapper.colors.textSecondary,
    )
}

private fun openLanguageSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
        .setData(Uri.fromParts("package", context.packageName, null))
    context.startActivity(intent)
}

private val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.System -> R.string.profile_theme_system

        ThemeMode.Light -> R.string.profile_theme_light

        ThemeMode.Dark -> R.string.profile_theme_dark
    }

@Preview(name = "Profile", widthDp = 412, heightDp = 917)
@Composable
private fun ProfilePreview() {
    ProfilePreviewSurface(state = previewState, darkTheme = false)
}

@Preview(name = "Profile, dark", widthDp = 412, heightDp = 917)
@Composable
private fun ProfileDarkPreview() {
    ProfilePreviewSurface(state = previewState.copy(themeMode = ThemeMode.Dark), darkTheme = true)
}

@Preview(name = "Profile, sign out", widthDp = 412, heightDp = 917)
@Composable
private fun ProfileSignOutPreview() {
    ProfilePreviewSurface(
        state = previewState.copy(isSignOutConfirmOpen = true),
        darkTheme = false,
    )
}

@Composable
private fun ProfilePreviewSurface(state: ProfileViewState, darkTheme: Boolean) {
    AppTheme(darkTheme = darkTheme) {
        HomeScreen(
            initialTab = HomeTab.Profile,
            tabContent = { _, bottomPadding ->
                ProfileContent(
                    state = state,
                    onAction = {},
                    bottomPadding = bottomPadding,
                    canChangeLanguage = true,
                )
            },
        )
    }
}

private val previewState = ProfileViewState(
    email = "manager@rheinvalley.fm",
    labelName = "Rhein Valley Records",
    labelFlag = "🇩🇪",
    artistCount = 16,
    rosterValue = 11_240_000,
    version = "1.0.0",
)
