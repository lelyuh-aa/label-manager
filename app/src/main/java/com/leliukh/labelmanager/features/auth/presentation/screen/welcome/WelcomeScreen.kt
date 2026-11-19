package com.leliukh.labelmanager.features.auth.presentation.screen.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButton
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButtonStyle
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

private const val HERO_SHARE = 0.52f
private const val LOGO_OF_LABEL = 1.28f
private val MinHeroHeight = 340.dp
private val MaxHeroHeight = 500.dp
private val MinLogoSize = 88.dp
private val MaxLogoSize = 120.dp
private val TitleGap = 28.dp
private const val LOGO_CORNER_SHARE = 0.3f
private const val LOGO_IMAGE_SHARE = 0.64f
private const val LOGO_IMAGE_HEIGHT_SHARE = 0.68f

/**
 * First screen of a signed-out user: the label logo on a stylised vinyl record, a one-line
 * summary of the game and the two ways in.
 *
 * The record takes about half of the screen height and the logo sits on its centre label;
 * the text always starts below the record. On short screens the
 * whole screen scrolls instead of squeezing the content together.
 *
 * @param onCreateAccount called by the primary button
 * @param onSignIn called by the secondary button
 */
@Composable
fun WelcomeScreen(
    onCreateAccount: () -> Unit,
    onSignIn: () -> Unit
) {
    val colors = AppThemeWrapper.colors

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        val heroHeight = (maxHeight * HERO_SHARE).coerceIn(MinHeroHeight, MaxHeroHeight)
        val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val vinyl = VinylGeometry.of(maxWidth, heroHeight, statusBar)
        val logoSize = (vinyl.labelRadius * LOGO_OF_LABEL)
            .coerceIn(MinLogoSize, MaxLogoSize)
        val screenHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .heightIn(min = screenHeight),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box {
                VinylBackground(height = heroHeight, geometry = vinyl)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(vinyl.center - logoSize / 2))

                    Logo(size = logoSize)

                    Spacer(
                        modifier = Modifier.height(
                            heroHeight - vinyl.center - logoSize / 2 + TitleGap,
                        ),
                    )

                    Introduction()
                }
            }

            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 32.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppButton(
                    text = stringResource(R.string.welcome_create_account),
                    onClick = onCreateAccount,
                )

                AppButton(
                    text = stringResource(R.string.welcome_sign_in),
                    onClick = onSignIn,
                    style = AppButtonStyle.Secondary,
                )
            }
        }
    }
}

@Composable
private fun Introduction() {
    val colors = AppThemeWrapper.colors

    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.welcome_title),
            style = AppThemeWrapper.typography.largeTitle.copy(
                fontSize = 36.sp,
                lineHeight = 40.sp,
                letterSpacing = (-0.04).em,
            ),
            color = colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.welcome_tagline),
            style = AppThemeWrapper.typography.body.copy(lineHeight = 24.sp),
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 340.dp),
        )

        Spacer(modifier = Modifier.height(18.dp))

        Facts()
    }
}

@Composable
private fun Logo(size: Dp) {
    val colors = AppThemeWrapper.colors
    val shape = RoundedCornerShape(size * LOGO_CORNER_SHARE)

    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = 24.dp,
                shape = shape,
                ambientColor = colors.shadow,
                spotColor = colors.shadow,
            )
            .background(colors.surface, shape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = stringResource(R.string.welcome_logo_description),
            modifier = Modifier.size(
                width = size * LOGO_IMAGE_SHARE,
                height = size * LOGO_IMAGE_HEIGHT_SHARE,
            ),
        )
    }
}

@Composable
private fun Facts() {
    // Scrolls sideways instead of wrapping when a translation or a large font does not fit.
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState()),
    ) {
        listOf(
            R.string.welcome_fact_artists,
            R.string.welcome_fact_budget,
            R.string.welcome_fact_market,
        ).forEach { fact ->
            Text(
                text = stringResource(fact),
                style = AppThemeWrapper.typography.subheadMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = AppThemeWrapper.colors.textPrimary,
                maxLines = 1,
                modifier = Modifier
                    .background(AppThemeWrapper.colors.surface, AppThemeWrapper.shapes.pill)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Preview(name = "Welcome", widthDp = 412, heightDp = 917)
@Composable
private fun WelcomeScreenPreview() {
    AppTheme(darkTheme = false) {
        WelcomeScreen(onCreateAccount = {}, onSignIn = {})
    }
}

@Preview(name = "Welcome, dark", widthDp = 412, heightDp = 917)
@Composable
private fun WelcomeScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        WelcomeScreen(onCreateAccount = {}, onSignIn = {})
    }
}
