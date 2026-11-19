package com.leliukh.labelmanager.features.market.presentation.screen.dealdone

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButton
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButtonStyle
import com.leliukh.labelmanager.core.design_system.presentation.components.money.DeltaLabel
import com.leliukh.labelmanager.core.design_system.presentation.components.money.MoneyText
import com.leliukh.labelmanager.core.design_system.presentation.components.artist.ArtistAvatar
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.formatting.presentation.countryDisplayName
import com.leliukh.labelmanager.core.formatting.presentation.formatMoney
import com.leliukh.labelmanager.core.formatting.presentation.formatPercentChange
import com.leliukh.labelmanager.features.market.presentation.navigation.DealDoneRoute
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.presentation.models.colors
import com.leliukh.labelmanager.features.label.presentation.models.nameRes
import com.leliukh.labelmanager.features.label.presentation.models.shortLabelRes

private const val GLOW_ALPHA = 0.22f
private const val AVATAR_START_SCALE = 0.6f

/**
 * The moment after a purchase: the artist joins the label, the new market value counts up from
 * the old one after a light haptic, and the price and the budget left are shown.
 *
 * @param deal what to show
 * @param onOpenArtist opens the bought artist
 * @param onBack returns to the market
 * @param animate whether to play the entrance; off for screenshots
 */
@Composable
fun DealDoneScreen(
    deal: DealDoneRoute,
    onOpenArtist: () -> Unit,
    onBack: () -> Unit,
    animate: Boolean = true,
) {
    val colors = AppThemeWrapper.colors
    val haptics = LocalHapticFeedback.current
    val avatarScale = remember { Animatable(if (animate) AVATAR_START_SCALE else 1f) }
    var isRevealed by remember { mutableStateOf(!animate) }

    LaunchedEffect(Unit) {
        if (!animate) return@LaunchedEffect
        avatarScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        isRevealed = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        Glow()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 64.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.scale(avatarScale.value)) {
                ArtistAvatar(
                    initials = deal.initials,
                    badge = stringResource(deal.genre.shortLabelRes),
                    colors = deal.genre.colors,
                    size = 96.dp,
                    badgeBorder = colors.background,
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(30.dp)
                        .background(colors.background, CircleShape)
                        .padding(3.dp)
                        .background(colors.positive, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = AppIcons.Check,
                        contentDescription = null,
                        tint = colors.onAccent,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }

            Text(
                text = stringResource(R.string.deal_done_welcome, deal.labelName),
                style = AppThemeWrapper.typography.subheadMedium,
                color = colors.accentText,
                modifier = Modifier.padding(top = 24.dp),
            )

            Text(
                text = deal.fullName,
                style = AppThemeWrapper.typography.largeTitle,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )

            Text(
                text = stringResource(deal.genre.nameRes) + " · " +
                    countryDisplayName(deal.countryCode) + " · " +
                    pluralStringResource(R.plurals.artist_age, deal.age, deal.age),
                style = AppThemeWrapper.typography.callout,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )

            ValueCard(deal = deal, isRevealed = isRevealed)

            Box(modifier = Modifier.weight(1f))

            AppButton(text = stringResource(R.string.deal_done_see_artist), onClick = onOpenArtist)

            AppButton(
                text = stringResource(R.string.deal_done_back),
                onClick = onBack,
                style = AppButtonStyle.Secondary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun ValueCard(deal: DealDoneRoute, isRevealed: Boolean) {
    val colors = AppThemeWrapper.colors

    Column(
        modifier = Modifier
            .padding(top = 28.dp)
            .fillMaxWidth()
            .background(colors.surface, AppThemeWrapper.shapes.card)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.deal_done_new_value),
                style = AppThemeWrapper.typography.subheadMedium,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )

            DeltaLabel(
                text = formatPercentChange(deal.valueBefore, deal.valueAfter),
                isIncrease = true,
            )
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MoneyText(
                amount = if (isRevealed) deal.valueAfter else deal.valueBefore,
                style = AppThemeWrapper.typography.moneyHero,
            )

            Text(
                text = formatMoney(deal.valueBefore),
                style = AppThemeWrapper.typography.callout,
                color = colors.textTertiary,
                textDecoration = TextDecoration.LineThrough,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 1.dp,
            color = colors.separator,
        )

        Row(modifier = Modifier.padding(top = 6.dp)) {
            Fact(
                label = stringResource(R.string.deal_done_paid),
                value = formatMoney(deal.price),
                modifier = Modifier.weight(1f),
            )

            Fact(
                label = stringResource(R.string.deal_done_budget_left),
                value = formatMoney(deal.budget),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Fact(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = AppThemeWrapper.typography.caption,
            color = AppThemeWrapper.colors.textSecondary,
        )

        Text(
            text = value,
            style = AppThemeWrapper.typography.bodySemibold,
            color = AppThemeWrapper.colors.textPrimary,
        )
    }
}

@Composable
private fun Glow() {
    val color = AppThemeWrapper.colors.positive.copy(alpha = GLOW_ALPHA)

    Canvas(modifier = Modifier.fillMaxWidth().height(420.dp)) {
        val center = Offset(size.width / 2, size.height * 0.3f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color, color.copy(alpha = 0f)),
                center = center,
                radius = size.height / 2,
            ),
            radius = size.height / 2,
            center = center,
        )
    }
}

@Preview(name = "Deal done", widthDp = 412, heightDp = 917)
@Composable
private fun DealDonePreview() {
    AppTheme(darkTheme = false) {
        DealDoneScreen(deal = previewDeal, onOpenArtist = {}, onBack = {}, animate = false)
    }
}

@Preview(name = "Deal done, dark", widthDp = 412, heightDp = 917)
@Composable
private fun DealDoneDarkPreview() {
    AppTheme(darkTheme = true) {
        DealDoneScreen(deal = previewDeal, onOpenArtist = {}, onBack = {}, animate = false)
    }
}

private val previewDeal = DealDoneRoute(
    artistId = "p1",
    fullName = "Chidi Okafor",
    initials = "CO",
    genre = Genre.HipHop,
    countryCode = "NG",
    age = 23,
    labelName = "Rhein Valley Records",
    price = 1_450_000,
    valueBefore = 1_180_000,
    valueAfter = 1_350_000,
    budget = 2_032_150,
)
