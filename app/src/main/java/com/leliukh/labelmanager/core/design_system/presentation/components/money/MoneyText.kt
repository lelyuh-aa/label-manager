package com.leliukh.labelmanager.core.design_system.presentation.components.money

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.formatting.presentation.formatMoney
import com.leliukh.labelmanager.core.formatting.presentation.formatMoneyCompact
import com.leliukh.labelmanager.core.formatting.presentation.maskedMoney

private const val COUNT_DURATION_MS = 700

/**
 * An amount of money that counts up or down to its new value whenever [amount] changes,
 * and cross-fades to a mask while [isMasked].
 *
 * The animation interpolates whole dollars between the old and the new value, so it always
 * ends exactly on [amount].
 *
 * @param amount amount in whole US dollars
 * @param modifier modifier of the text
 * @param style text style, normally one of the money styles
 * @param color text color
 * @param isCompact whether to use the compact form, e.g. `$1.45M`
 * @param isMasked whether to hide the amount behind dots
 */
@Composable
fun MoneyText(
    amount: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = AppThemeWrapper.typography.money,
    color: Color = AppThemeWrapper.colors.textPrimary,
    isCompact: Boolean = false,
    isMasked: Boolean = false,
) {
    var shown by remember { mutableLongStateOf(amount) }
    val progress = remember { Animatable(1f) }

    LaunchedEffect(amount) {
        val from = shown
        if (from == amount) return@LaunchedEffect
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(COUNT_DURATION_MS, easing = FastOutSlowInEasing),
        ) {
            shown = from + ((amount - from) * value).toLong()
        }
        shown = amount
    }

    val text = if (isCompact) formatMoneyCompact(shown) else formatMoney(shown)
    val hiddenDescription = stringResource(R.string.common_amount_hidden)

    AnimatedContent(
        targetState = isMasked,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier.semantics {
            contentDescription = if (isMasked) hiddenDescription else formatMoney(amount)
        },
        label = "moneyMask",
    ) { masked ->
        Text(text = if (masked) maskedMoney() else text, style = style, color = color)
    }
}

/**
 * A percentage change with an arrow, green when the value went up and red when it went down.
 *
 * @param text the formatted percentage, e.g. "30%"
 * @param isIncrease whether the value went up
 * @param modifier modifier of the label
 */
@Composable
fun DeltaLabel(text: String, isIncrease: Boolean, modifier: Modifier = Modifier) {
    val color = if (isIncrease) AppThemeWrapper.colors.positive else AppThemeWrapper.colors.negative

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = if (isIncrease) AppIcons.ArrowUp else AppIcons.ArrowDown,
            contentDescription = stringResource(
                if (isIncrease) R.string.common_value_up else R.string.common_value_down,
            ),
            tint = color,
            modifier = Modifier.size(12.dp),
        )

        Text(text = text, style = AppThemeWrapper.typography.moneySmall, color = color)
    }
}

/**
 * Compact pill with the budget and a toggle that hides it, for screen headers.
 *
 * @param amount the budget in whole US dollars
 * @param isMasked whether the amount is hidden
 * @param onToggleMask called on tap
 * @param modifier modifier of the pill
 */
@Composable
fun BudgetPill(
    amount: Long,
    isMasked: Boolean,
    onToggleMask: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeWrapper.colors

    Row(
        modifier = modifier
            .height(40.dp)
            .clip(AppThemeWrapper.shapes.pill)
            .background(colors.surface)
            .clickable(
                role = Role.Switch,
                onClickLabel = stringResource(
                    if (isMasked) R.string.common_show_budget else R.string.common_hide_budget,
                ),
                onClick = onToggleMask,
            )
            .padding(start = 14.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = AppIcons.Wallet,
            contentDescription = null,
            tint = colors.accentText,
            modifier = Modifier.size(16.dp),
        )

        MoneyText(amount = amount, isMasked = isMasked, style = AppThemeWrapper.typography.money)

        Icon(
            imageVector = if (isMasked) AppIcons.EyeOff else AppIcons.Eye,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(17.dp),
        )
    }
}

@ThemePreviews
@Composable
private fun MoneyTextPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MoneyText(amount = 3_000_000, style = AppThemeWrapper.typography.moneyHero)

            MoneyText(amount = 11_240_000, style = AppThemeWrapper.typography.moneyLarge)

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MoneyText(amount = 1_450_000, isCompact = true)

                DeltaLabel(text = "30%", isIncrease = true)

                DeltaLabel(text = "12%", isIncrease = false)
            }

            MoneyText(amount = 3_000_000, isMasked = true)
        }
    }
}

@ThemePreviews
@Composable
private fun BudgetPillPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BudgetPill(amount = 3_000_000, isMasked = false, onToggleMask = {})

            BudgetPill(amount = 3_000_000, isMasked = true, onToggleMask = {})
        }
    }
}
