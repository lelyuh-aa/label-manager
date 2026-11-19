package com.leliukh.labelmanager.features.auth.presentation.screen.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.features.auth.presentation.viewmodel.signup.PasswordRuleVO

/**
 * The password requirements under the password field, each ticked off with a green check as
 * soon as the typed password meets it.
 *
 * @param rules the requirements and whether each is met
 * @param modifier modifier of the list
 */
@Composable
fun PasswordRules(rules: List<PasswordRuleVO>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        rules.forEach { rule -> Rule(rule) }
    }
}

@Composable
private fun Rule(rule: PasswordRuleVO) {
    val colors = AppThemeWrapper.colors
    val dotColor by animateColorAsState(
        targetValue = if (rule.isMet) colors.positiveSoft else colors.fill,
        label = "ruleDot",
    )
    val stateText = stringResource(
        if (rule.isMet) R.string.auth_password_rule_met else R.string.auth_password_rule_missing,
    )

    Row(
        modifier = Modifier.semantics { stateDescription = stateText },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(dotColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(targetState = rule.isMet, label = "ruleIcon") { isMet ->
                Icon(
                    imageVector = if (isMet) AppIcons.Check else AppIcons.Minus,
                    contentDescription = null,
                    tint = if (isMet) colors.positive else colors.textSecondary,
                    modifier = Modifier.size(11.dp),
                )
            }
        }

        Text(
            text = stringResource(rule.labelRes),
            style = AppThemeWrapper.typography.subhead,
            color = if (rule.isMet) colors.textPrimary else colors.textSecondary,
        )
    }
}

@ThemePreviews
@Composable
private fun PasswordRulesPreview() {
    PreviewSurface {
        PasswordRules(
            rules = listOf(
                PasswordRuleVO(R.string.auth_password_rule_length, isMet = true),
                PasswordRuleVO(R.string.auth_password_rule_letter, isMet = true),
                PasswordRuleVO(R.string.auth_password_rule_digit, isMet = false),
            ),
        )
    }
}
