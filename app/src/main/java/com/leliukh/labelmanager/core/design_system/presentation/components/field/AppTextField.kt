package com.leliukh.labelmanager.core.design_system.presentation.components.field

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.core.design_system.presentation.components.button.CircleIconButton
import com.leliukh.labelmanager.core.design_system.presentation.components.button.CircleIconButtonStyle
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

/**
 * Labelled single-line text field.
 *
 * The label sits above the field; [error] replaces the hint below it and turns the label and
 * the outline red. A password field gets a show/hide toggle.
 *
 * @param value current text
 * @param onValueChange called with the edited text
 * @param label label above the field
 * @param modifier modifier of the whole block
 * @param placeholder text shown while the field is empty
 * @param hint helper text below the field when there is no error
 * @param error error text below the field; null when the value is valid
 * @param leadingIcon optional icon inside the field
 * @param isPassword whether to hide the input and offer a visibility toggle
 * @param enabled whether the field can be edited
 * @param keyboardOptions keyboard type and IME action
 * @param keyboardActions handlers of the IME action
 * @param visualTransformation how the text is displayed, e.g. money formatting
 * @param trailing optional content at the end of the field, e.g. a country picker chevron
 * @param contentType what the field holds, so autofill and password managers can fill it
 * @param onFocusLost called when the field loses focus, e.g. to validate only after typing
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    hint: String? = null,
    error: String? = null,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
    contentType: ContentType? = null,
    onFocusLost: () -> Unit = {},
) {
    val colors = AppThemeWrapper.colors
    val typography = AppThemeWrapper.typography
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var wasFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = when {
            error != null -> colors.negative

            isFocused -> colors.accent

            else -> colors.fill
        },
        label = "fieldBorder",
    )
    val transformation = when {
        isPassword && !isPasswordVisible -> PasswordVisualTransformation()

        else -> visualTransformation
    }
    val effectiveKeyboardOptions = if (isPassword) {
        keyboardOptions.copy(keyboardType = KeyboardType.Password)
    } else {
        keyboardOptions
    }

    LaunchedEffect(isFocused) {
        if (wasFocused && !isFocused) onFocusLost()
        wasFocused = isFocused
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = typography.subheadMedium,
            color = if (error != null) colors.negative else colors.textSecondary,
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = if (contentType != null) {
                Modifier.semantics { this.contentType = contentType }
            } else {
                Modifier
            },
            enabled = enabled,
            singleLine = true,
            textStyle = typography.body.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.accent),
            keyboardOptions = effectiveKeyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = transformation,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppThemeWrapper.dimens.controlHeight)
                        .background(colors.fill, AppThemeWrapper.shapes.control)
                        .border(1.5.dp, borderColor, AppThemeWrapper.shapes.control)
                        .padding(start = 16.dp, end = if (isPassword) 4.dp else 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(19.dp),
                        )
                    }

                    FieldText(
                        isEmpty = value.isEmpty(),
                        placeholder = placeholder,
                        innerTextField = innerTextField,
                        modifier = Modifier.weight(1f),
                    )

                    if (isPassword) {
                        CircleIconButton(
                            icon = if (isPasswordVisible) AppIcons.EyeOff else AppIcons.Eye,
                            contentDescription = if (isPasswordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            },
                            onClick = { isPasswordVisible = !isPasswordVisible },
                            style = CircleIconButtonStyle.Plain,
                        )
                    }

                    trailing?.invoke()
                }
            },
        )

        AnimatedVisibility(
            visible = error != null || hint != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Text(
                text = error ?: hint.orEmpty(),
                style = typography.subhead,
                color = if (error != null) colors.negative else colors.textSecondary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
internal fun FieldText(
    isEmpty: Boolean,
    placeholder: String,
    innerTextField: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        if (isEmpty) {
            Text(
                text = placeholder,
                style = AppThemeWrapper.typography.body,
                color = AppThemeWrapper.colors.textSecondary,
            )
        }

        innerTextField()
    }
}

@ThemePreviews
@Composable
private fun AppTextFieldStatesPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AppTextField(
                value = "",
                onValueChange = {},
                label = "Email",
                placeholder = "producer@label.com",
                leadingIcon = AppIcons.Mail,
            )

            AppTextField(
                value = "producer@rheinvalley.fm",
                onValueChange = {},
                label = "Email",
                leadingIcon = AppIcons.Mail,
            )

            AppTextField(
                value = "secret",
                onValueChange = {},
                label = "Password",
                isPassword = true,
                leadingIcon = AppIcons.Lock,
                error = "At least 8 characters",
            )

            AppTextField(
                value = "Rhein Valley Records",
                onValueChange = {},
                label = "Label name",
                hint = "2 to 40 characters",
            )
        }
    }
}
