package com.leliukh.labelmanager.features.auth.presentation.screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.button.CircleIconButton
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.Banner
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.BannerTone
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.features.auth.presentation.models.AuthMessageVO

/**
 * Shared frame of the sign-in and sign-up forms: back button, title, subtitle, an error
 * banner, the fields and a link to the other form at the bottom.
 *
 * The form scrolls and moves above the keyboard, so the button stays reachable on small
 * screens.
 *
 * @param title large title
 * @param subtitle one-line explanation under the title
 * @param message error banner; hidden when null
 * @param footerText text before the footer link
 * @param footerAction label of the footer link
 * @param onFooterAction called by the footer link
 * @param onBack called by the back button
 * @param content the fields and the submit button
 */
@Composable
fun AuthFormLayout(
    title: String,
    subtitle: String,
    message: AuthMessageVO?,
    footerText: String,
    footerAction: String,
    onFooterAction: () -> Unit,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AppThemeWrapper.colors

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        val viewportHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .heightIn(min = viewportHeight)
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                CircleIconButton(
                    icon = AppIcons.ChevronLeft,
                    contentDescription = stringResource(R.string.common_back),
                    onClick = onBack,
                    modifier = Modifier.offset(x = (-4).dp),
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = title,
                    style = AppThemeWrapper.typography.largeTitle,
                    color = colors.textPrimary,
                    modifier = Modifier.semantics { heading() },
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitle,
                    style = AppThemeWrapper.typography.callout,
                    color = colors.textSecondary,
                )

                AnimatedVisibility(
                    visible = message != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    if (message != null) {
                        Banner(
                            title = stringResource(message.titleRes),
                            message = stringResource(message.messageRes),
                            tone = BannerTone.Error,
                            modifier = Modifier.padding(top = 24.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Column(verticalArrangement = Arrangement.spacedBy(18.dp), content = content)
            }

            FooterLink(text = footerText, action = footerAction, onAction = onFooterAction)
        }
    }
}

@Composable
private fun FooterLink(text: String, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = AppThemeWrapper.typography.callout,
            color = AppThemeWrapper.colors.textSecondary,
        )

        Text(
            text = action,
            style = AppThemeWrapper.typography.callout.copy(fontWeight = FontWeight.SemiBold),
            color = AppThemeWrapper.colors.accentText,
            modifier = Modifier
                .clip(AppThemeWrapper.shapes.small)
                .clickable(role = Role.Button, onClick = onAction)
                .padding(horizontal = 6.dp, vertical = 10.dp),
        )
    }
}
