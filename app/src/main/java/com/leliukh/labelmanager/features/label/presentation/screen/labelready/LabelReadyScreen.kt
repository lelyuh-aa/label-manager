package com.leliukh.labelmanager.features.label.presentation.screen.labelready

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButton
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.EmptyState
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.SkeletonBlock
import com.leliukh.labelmanager.core.design_system.presentation.components.money.MoneyText
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.formatting.presentation.formatMoney
import com.leliukh.labelmanager.core.ui_utils.presentation.CollectEvents
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.presentation.models.colors
import com.leliukh.labelmanager.features.label.presentation.models.shortLabelRes
import com.leliukh.labelmanager.features.label.presentation.viewmodel.labelready.LabelReadyViewAction
import com.leliukh.labelmanager.features.label.presentation.viewmodel.labelready.LabelReadyViewEvent
import com.leliukh.labelmanager.features.label.presentation.viewmodel.labelready.LabelReadyViewModel
import com.leliukh.labelmanager.features.label.presentation.viewmodel.labelready.LabelReadyViewState
import com.leliukh.labelmanager.features.label.presentation.viewmodel.labelready.GenreCountVO
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val BUDGET_REVEAL_DELAY_MS = 350L
private const val GLOW_ALPHA = 0.22f
private const val GLOW_CENTER_SHARE = 0.55f
private val GlowRadius = 300.dp

/**
 * The one-time moment after sign-up that presents the generated label: its name and country,
 * the budget counting up from zero and the roster by genre.
 *
 * @param onContinue called by "Meet the roster"
 * @param viewModel state holder of the screen
 */
@Composable
fun LabelReadyScreen(
    onContinue: () -> Unit,
    viewModel: LabelReadyViewModel = hiltViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    CollectEvents(viewModel.uiEventsFlow) { event ->
        when (event) {
            LabelReadyViewEvent.OpenHome -> onContinue()
        }
    }

    LabelReadyContent(state = state, onAction = viewModel::handleUiAction)
}

@Composable
private fun LabelReadyContent(
    state: LabelReadyViewState,
    onAction: (LabelReadyViewAction) -> Unit,
    countBudgetUp: Boolean = true,
) {
    val colors = AppThemeWrapper.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        Glow()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            val viewportHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = viewportHeight)
                    .padding(horizontal = 20.dp)
                    .padding(top = 48.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LabelReadyBody(state = state, onAction = onAction, countBudgetUp = countBudgetUp)

                AppButton(
                    text = stringResource(R.string.label_ready_continue),
                    onClick = { onAction(LabelReadyViewAction.Continue) },
                    trailingIcon = AppIcons.ArrowRight,
                    enabled = !state.isLoading,
                    modifier = Modifier.padding(top = 32.dp),
                )
            }
        }
    }
}

@Composable
private fun LabelReadyBody(
    state: LabelReadyViewState,
    onAction: (LabelReadyViewAction) -> Unit,
    countBudgetUp: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Header()

        Spacer(modifier = Modifier.height(28.dp))

        AnimatedContent(
            targetState = state.isError,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "labelReadyBody",
        ) { isError ->
            if (isError) {
                EmptyState(
                    icon = AppIcons.WifiOff,
                    title = stringResource(R.string.label_ready_error_title),
                    message = stringResource(R.string.label_ready_error_message),
                    actionLabel = stringResource(R.string.common_retry),
                    onAction = { onAction(LabelReadyViewAction.Retry) },
                )
            } else {
                LabelCard(state = state, countBudgetUp = countBudgetUp)
            }
        }
    }
}

@Composable
private fun Glow() {
    val color = AppThemeWrapper.colors.accent.copy(alpha = GLOW_ALPHA)

    Canvas(modifier = Modifier.fillMaxWidth().height(GlowRadius * 2)) {
        val center = Offset(size.width / 2, GlowRadius.toPx() * GLOW_CENTER_SHARE)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color, color.copy(alpha = 0f)),
                center = center,
                radius = GlowRadius.toPx(),
            ),
            radius = GlowRadius.toPx(),
            center = center,
        )
    }
}

@Composable
private fun Header() {
    val colors = AppThemeWrapper.colors
    val tileShape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier
            .size(72.dp)
            .shadow(16.dp, tileShape, ambientColor = colors.accent, spotColor = colors.accent)
            .background(colors.accent, tileShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.Check,
            contentDescription = null,
            tint = colors.onAccent,
            modifier = Modifier.size(34.dp),
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = stringResource(R.string.label_ready_title),
        style = AppThemeWrapper.typography.largeTitle,
        color = colors.textPrimary,
        modifier = Modifier.semantics { heading() },
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = stringResource(R.string.label_ready_subtitle),
        style = AppThemeWrapper.typography.callout,
        color = colors.textSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.widthIn(max = 300.dp),
    )
}

@Composable
private fun LabelCard(state: LabelReadyViewState, countBudgetUp: Boolean) {
    val colors = AppThemeWrapper.colors
    var shownBudget by remember { mutableLongStateOf(if (countBudgetUp) 0L else state.budget) }

    LaunchedEffect(state.budget) {
        if (countBudgetUp) delay(BUDGET_REVEAL_DELAY_MS.milliseconds)
        shownBudget = state.budget
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = state.flag, fontSize = 26.sp, modifier = Modifier.padding(end = 12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                LoadingText(
                    text = state.labelName,
                    isLoading = state.isLoading,
                    width = 180,
                    style = AppThemeWrapper.typography.title,
                )

                LoadingText(
                    text = stringResource(R.string.label_ready_country, state.countryName),
                    isLoading = state.isLoading,
                    width = 220,
                    style = AppThemeWrapper.typography.subhead,
                    color = colors.textSecondary,
                )
            }
        }

        HorizontalDivider(thickness = 1.dp, color = colors.separator)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.label_ready_budget),
                style = AppThemeWrapper.typography.subheadMedium,
                color = colors.textSecondary,
            )

            MoneyText(
                amount = shownBudget,
                style = AppThemeWrapper.typography.moneyHero.copy(fontWeight = FontWeight.SemiBold),
                color = colors.accentText,
            )

            Text(
                text = stringResource(R.string.label_ready_budget_hint),
                style = AppThemeWrapper.typography.subhead,
                color = colors.textSecondary,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.genres.forEach { line ->
                GenreTile(line = line, modifier = Modifier.weight(1f))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.fill, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = AppIcons.Music,
                contentDescription = null,
                tint = colors.info,
                modifier = Modifier.size(16.dp),
            )

            Text(
                text = stringResource(R.string.label_ready_roster_value, state.artistCount),
                style = AppThemeWrapper.typography.callout,
                color = colors.textSecondary,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            )

            Text(
                text = formatMoney(state.rosterValue),
                style = AppThemeWrapper.typography.money,
                color = colors.textPrimary,
            )
        }
    }
}

@Composable
private fun GenreTile(line: GenreCountVO, modifier: Modifier = Modifier) {
    val genreColors = line.genre.colors

    Column(
        modifier = modifier
            .background(genreColors.container, RoundedCornerShape(16.dp))
            .padding(top = 12.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = line.count.toString(),
            style = AppThemeWrapper.typography.moneyLarge.copy(fontSize = 26.sp),
            color = genreColors.content,
        )

        Text(
            text = stringResource(line.genre.shortLabelRes),
            style = AppThemeWrapper.typography.micro,
            color = genreColors.content,
        )
    }
}

@Composable
private fun LoadingText(
    text: String,
    isLoading: Boolean,
    width: Int,
    style: TextStyle,
    color: Color = AppThemeWrapper.colors.textPrimary,
) {
    if (isLoading) {
        SkeletonBlock(
            width = width.dp,
            height = 16.dp,
            modifier = Modifier.padding(vertical = 4.dp),
        )
    } else {
        Text(text = text, style = style, color = color)
    }
}

@Preview(name = "Label ready", widthDp = 412, heightDp = 917)
@Composable
private fun LabelReadyPreview() {
    LabelReadyPreviewSurface(state = previewState, darkTheme = false)
}

@Preview(name = "Label ready, dark", widthDp = 412, heightDp = 917)
@Composable
private fun LabelReadyDarkPreview() {
    LabelReadyPreviewSurface(state = previewState, darkTheme = true)
}

@Preview(name = "Label ready, loading", widthDp = 412, heightDp = 917)
@Composable
private fun LabelReadyLoadingPreview() {
    LabelReadyPreviewSurface(state = LabelReadyViewState(), darkTheme = false)
}

@Preview(name = "Label ready, error", widthDp = 412, heightDp = 917)
@Composable
private fun LabelReadyErrorPreview() {
    LabelReadyPreviewSurface(
        state = LabelReadyViewState(isLoading = false, isError = true),
        darkTheme = false,
    )
}

@Composable
private fun LabelReadyPreviewSurface(state: LabelReadyViewState, darkTheme: Boolean) {
    AppTheme(darkTheme = darkTheme) {
        LabelReadyContent(state = state, onAction = {}, countBudgetUp = false)
    }
}

private val previewState = LabelReadyViewState(
    isLoading = false,
    labelName = "Rhein Valley Records",
    countryName = "Germany",
    flag = "🇩🇪",
    budget = 3_000_000,
    rosterValue = 9_600_000,
    artistCount = 16,
    genres = listOf(
        GenreCountVO(Genre.Pop, 4),
        GenreCountVO(Genre.Rock, 4),
        GenreCountVO(Genre.HipHop, 4),
        GenreCountVO(Genre.Electronic, 4),
    ),
)
