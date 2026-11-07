package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.KotogramTheme
import androidx.compose.material3.TextFieldDefaults as MaterialTextFieldDefaults

@Composable
fun TextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextFieldDefaults.textStyle,
    title: @Composable (() -> Unit)? = null,
    suggestion: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState(),
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    contentPadding: PaddingValues = TextFieldDefaults.contentPadding(),
    interactionSource: MutableInteractionSource? = null
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            state = state,
            modifier = modifier.defaultMinSize(minWidth = TextFieldDefaults.MinWidth),
            enabled = enabled,
            readOnly = readOnly,
            inputTransformation = inputTransformation,
            textStyle = textStyleWithColor(textStyle, colors, enabled, isError, interactionSource),
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            lineLimits = lineLimits,
            onTextLayout = onTextLayout,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(colors.cursorColor(isError)),
            outputTransformation = outputTransformation,
            decorator = TextFieldDecorator { innerTextField ->
                TextFieldDecoration(
                    innerTextField = innerTextField,
                    isEmpty = state.text.isEmpty(),
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    textStyle = textStyle,
                    title = title,
                    suggestion = suggestion,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
                    shape = shape,
                    colors = colors,
                    contentPadding = contentPadding
                )
            },
            scrollState = scrollState
        )
    }
}

@Composable
fun SecureTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = TextFieldDefaults.textStyle,
    title: @Composable (() -> Unit)? = null,
    suggestion: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    inputTransformation: InputTransformation? = null,
    textObfuscationMode: TextObfuscationMode = TextObfuscationMode.RevealLastTyped,
    textObfuscationCharacter: Char = DEFAULT_OBFUSCATION_CHARACTER,
    keyboardOptions: KeyboardOptions = SecureTextFieldKeyboardOptions,
    onKeyboardAction: KeyboardActionHandler? = null,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    contentPadding: PaddingValues = TextFieldDefaults.contentPadding(),
    interactionSource: MutableInteractionSource? = null
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicSecureTextField(
            state = state,
            modifier = modifier.defaultMinSize(minWidth = TextFieldDefaults.MinWidth),
            enabled = enabled,
            inputTransformation = inputTransformation,
            textStyle = textStyleWithColor(textStyle, colors, enabled, isError, interactionSource),
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            onTextLayout = onTextLayout,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(colors.cursorColor(isError)),
            decorator = TextFieldDecorator { innerTextField ->
                TextFieldDecoration(
                    innerTextField = innerTextField,
                    isEmpty = state.text.isEmpty(),
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    textStyle = textStyle,
                    title = title,
                    suggestion = suggestion,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
                    shape = shape,
                    colors = colors,
                    contentPadding = contentPadding
                )
            },
            textObfuscationMode = textObfuscationMode,
            textObfuscationCharacter = textObfuscationCharacter
        )
    }
}

@Suppress("MagicNumber")
object TextFieldDefaults {
    val MinWidth: Dp = 280.dp

    val MinHeight: Dp = 40.dp

    val FocusedIndicatorThickness: Dp = 2.dp

    val shape: Shape
        @Composable @ReadOnlyComposable
        get() = KotogramTheme.shapes.medium

    val textStyle: TextStyle
        @Composable @ReadOnlyComposable
        get() = KotogramTheme.typography.bodyLarge

    @Composable
    fun contentPadding(
        start: Dp = KotogramTheme.spacing.small,
        top: Dp = KotogramTheme.spacing.small,
        end: Dp = KotogramTheme.spacing.small,
        bottom: Dp = KotogramTheme.spacing.small
    ): PaddingValues = PaddingValues(start = start, top = top, end = end, bottom = bottom)

    @Composable
    fun colors(): TextFieldColors {
        val colors = KotogramTheme.colors
        return MaterialTextFieldDefaults.colors(
            focusedIndicatorColor = if (platformFocusRing) colors.primary else Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = if (platformFocusRing) colors.error else Color.Transparent,
            focusedLabelColor = colors.onSurfaceVariant,
            errorLabelColor = colors.onSurfaceVariant,
            errorTrailingIconColor = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun textStyleWithColor(
    textStyle: TextStyle,
    colors: TextFieldColors,
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource
): TextStyle {
    val color = textStyle.color.takeOrElse {
        val focused by interactionSource.collectIsFocusedAsState()
        colors.textColor(enabled, isError, focused)
    }
    return textStyle.merge(TextStyle(color = color))
}

@Composable
private fun TextFieldDecoration(
    innerTextField: @Composable () -> Unit,
    isEmpty: Boolean,
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource,
    textStyle: TextStyle,
    title: @Composable (() -> Unit)?,
    suggestion: @Composable (() -> Unit)?,
    placeholder: @Composable (() -> Unit)?,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    prefix: @Composable (() -> Unit)?,
    suffix: @Composable (() -> Unit)?,
    supportingText: @Composable (() -> Unit)?,
    shape: Shape,
    colors: TextFieldColors,
    contentPadding: PaddingValues
) {
    val focused by interactionSource.collectIsFocusedAsState()
    val colorSpec = tween<Color>(TextFieldTokens.COLOR_MILLIS)
    val containerColor = animateColorAsState(colors.containerColor(enabled, isError, focused), colorSpec)
    val indicatorColor = animateColorAsState(colors.indicatorColor(enabled, isError, focused), colorSpec)
    val titleColor by animateColorAsState(colors.labelColor(enabled, isError, focused), colorSpec)
    val supportingColor by animateColorAsState(colors.supportingTextColor(enabled, isError, focused), colorSpec)
    val typography = KotogramTheme.typography
    val spacing = KotogramTheme.spacing
    val slotStyle = textStyle.copy(color = Color.Unspecified)
    val layoutDirection = LocalLayoutDirection.current
    val sidePadding = Modifier.padding(
        start = contentPadding.calculateStartPadding(layoutDirection),
        end = contentPadding.calculateEndPadding(layoutDirection)
    )
    val measurePolicy = remember(contentPadding) { TextFieldMeasurePolicy(contentPadding) }
    Layout(
        content = {
            Box(
                Modifier
                    .layoutId(TextFieldSlot.Container)
                    .textFieldContainer(
                        shape = shape,
                        color = containerColor,
                        indicatorColor = indicatorColor,
                        indicatorThickness = TextFieldDefaults.FocusedIndicatorThickness
                    )
            )
            if (title != null) {
                Box(
                    Modifier
                        .layoutId(TextFieldSlot.Title)
                        .then(sidePadding)
                        .padding(bottom = spacing.extraSmall)
                ) {
                    ProvideContentColorTextStyle(titleColor, typography.bodySmall, title)
                }
            }
            if (suggestion != null) {
                Box(
                    Modifier
                        .layoutId(TextFieldSlot.Suggestion)
                        .then(sidePadding)
                        .padding(bottom = spacing.extraSmall)
                ) {
                    ProvideContentColorTextStyle(titleColor, typography.bodyMedium, suggestion)
                }
            }
            if (leadingIcon != null) {
                Box(
                    Modifier
                        .layoutId(TextFieldSlot.Leading)
                        .padding(start = TextFieldTokens.IconInset)
                        .size(TextFieldTokens.IconSize),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides colors.leadingIconColor(enabled, isError, focused),
                        content = leadingIcon
                    )
                }
            }
            if (trailingIcon != null) {
                Box(
                    Modifier
                        .layoutId(TextFieldSlot.Trailing)
                        .padding(end = TextFieldTokens.IconInset)
                        .size(TextFieldTokens.IconSize),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides colors.trailingIconColor(enabled, isError, focused),
                        content = trailingIcon
                    )
                }
            }
            if (prefix != null) {
                Box(Modifier.layoutId(TextFieldSlot.Prefix).padding(end = TextFieldTokens.AffixGap)) {
                    ProvideContentColorTextStyle(colors.prefixColor(enabled, isError, focused), slotStyle, prefix)
                }
            }
            if (suffix != null) {
                Box(Modifier.layoutId(TextFieldSlot.Suffix).padding(start = TextFieldTokens.AffixGap)) {
                    ProvideContentColorTextStyle(colors.suffixColor(enabled, isError, focused), slotStyle, suffix)
                }
            }
            if (placeholder != null && isEmpty) {
                Box(Modifier.layoutId(TextFieldSlot.Placeholder)) {
                    ProvideContentColorTextStyle(
                        colors.placeholderColor(enabled, isError, focused),
                        typography.bodyMedium,
                        placeholder
                    )
                }
            }
            Box(Modifier.layoutId(TextFieldSlot.Text), propagateMinConstraints = true) {
                innerTextField()
            }
            Box(Modifier.layoutId(TextFieldSlot.Supporting)) {
                AnimatedContent(
                    targetState = supportingText,
                    transitionSpec = {
                        fadeIn(tween(TextFieldTokens.SUPPORTING_FADE_MILLIS)) togetherWith
                            fadeOut(tween(TextFieldTokens.SUPPORTING_FADE_MILLIS))
                    },
                    contentKey = { it != null },
                    label = "SupportingText"
                ) { text ->
                    if (text != null) {
                        Box(sidePadding.padding(top = spacing.small)) {
                            ProvideContentColorTextStyle(supportingColor, typography.bodyMedium, text)
                        }
                    }
                }
            }
        },
        measurePolicy = measurePolicy
    )
}

@Composable
private fun ProvideContentColorTextStyle(
    contentColor: Color,
    textStyle: TextStyle,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalTextStyle provides LocalTextStyle.current.merge(textStyle),
        content = content
    )
}

private fun Modifier.textFieldContainer(
    shape: Shape,
    color: State<Color>,
    indicatorColor: State<Color>,
    indicatorThickness: Dp
): Modifier = drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    val thickness = indicatorThickness.toPx()
    val indicatorOutline = shape.createOutline(
        Size(size.width - thickness, size.height - thickness),
        layoutDirection,
        this
    )
    onDrawBehind {
        drawOutline(outline, color.value)
        translate(thickness / 2, thickness / 2) {
            drawOutline(indicatorOutline, indicatorColor.value, style = Stroke(thickness))
        }
    }
}

private enum class TextFieldSlot {
    Container,
    Title,
    Suggestion,
    Leading,
    Trailing,
    Prefix,
    Suffix,
    Placeholder,
    Text,
    Supporting
}

// The container is as wide as the incoming minimum or its content; the text above and below wraps inside it.
private class TextFieldMeasurePolicy(private val contentPadding: PaddingValues) : MeasurePolicy {
    override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val leading = measurables.measure(TextFieldSlot.Leading, loose)
        val trailing = measurables.measure(TextFieldSlot.Trailing, loose)
        val start = if (leading == null) {
            contentPadding.calculateStartPadding(layoutDirection)
        } else {
            TextFieldTokens.IconGap
        }
        val end = if (trailing == null) contentPadding.calculateEndPadding(layoutDirection) else TextFieldTokens.IconGap
        val top = contentPadding.calculateTopPadding().roundToPx()
        val vertical = top + contentPadding.calculateBottomPadding().roundToPx()
        var occupied = leading.widthOrZero + trailing.widthOrZero + start.roundToPx() + end.roundToPx()
        val prefix = measurables.measure(TextFieldSlot.Prefix, loose.offset(horizontal = -occupied))
        occupied += prefix.widthOrZero
        val suffix = measurables.measure(TextFieldSlot.Suffix, loose.offset(horizontal = -occupied))
        occupied += suffix.widthOrZero

        val textConstraints = constraints.copy(minHeight = 0).offset(horizontal = -occupied, vertical = -vertical)
        val text = measurables.measure(TextFieldSlot.Text, textConstraints)!!
        val placeholder = measurables.measure(TextFieldSlot.Placeholder, textConstraints.copy(minWidth = 0))
        val contentHeight = maxOf(text.height, placeholder.heightOrZero, prefix.heightOrZero, suffix.heightOrZero)
        val width = constraints.constrainWidth(occupied + maxOf(text.width, placeholder.widthOrZero))
        val height = maxOf(
            TextFieldDefaults.MinHeight.roundToPx(),
            contentHeight + vertical,
            leading.heightOrZero,
            trailing.heightOrZero
        )
        val container = measurables.measure(TextFieldSlot.Container, Constraints.fixed(width, height))!!

        val aside = Constraints(maxWidth = width)
        val title = measurables.measure(TextFieldSlot.Title, aside)
        val suggestion = measurables.measure(TextFieldSlot.Suggestion, aside)
        val supporting = measurables.measure(TextFieldSlot.Supporting, aside)
        val containerTop = title.heightOrZero + suggestion.heightOrZero

        return layout(width, constraints.constrainHeight(containerTop + height + supporting.heightOrZero)) {
            title?.placeRelative(0, 0)
            suggestion?.placeRelative(0, title.heightOrZero)
            container.placeRelative(0, containerTop)
            leading?.placeRelative(0, containerTop + (height - leading.height) / 2)
            trailing?.placeRelative(width - trailing.width, containerTop + (height - trailing.height) / 2)
            val contentTop = containerTop + top + (height - vertical - contentHeight) / 2
            val contentStart = leading.widthOrZero + start.roundToPx()
            prefix?.placeRelative(contentStart, contentTop)
            placeholder?.placeRelative(
                contentStart + prefix.widthOrZero,
                contentTop + (contentHeight - placeholder.height) / 2
            )
            text.placeRelative(contentStart + prefix.widthOrZero, contentTop)
            suffix?.placeRelative(width - trailing.widthOrZero - end.roundToPx() - suffix.width, contentTop)
            supporting?.placeRelative(0, containerTop + height)
        }
    }
}

private fun List<Measurable>.measure(slot: TextFieldSlot, constraints: Constraints): Placeable? =
    firstOrNull { it.layoutId == slot }?.measure(constraints)

private val Placeable?.widthOrZero: Int get() = this?.width ?: 0

private val Placeable?.heightOrZero: Int get() = this?.height ?: 0

private val SecureTextFieldKeyboardOptions =
    KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Password)

private const val DEFAULT_OBFUSCATION_CHARACTER: Char = '\u2022'

@Suppress("MagicNumber")
private object TextFieldTokens {
    val IconSize = 28.dp
    val IconInset = 6.dp
    val IconGap = 4.dp
    val AffixGap = 4.dp

    const val COLOR_MILLIS = 150
    const val SUPPORTING_FADE_MILLIS = 200
}

@KotogramPreview
@Composable
private fun TextFieldPreview() {
    val spacing = KotogramTheme.spacing
    Column(Modifier.padding(spacing.large), verticalArrangement = Arrangement.spacedBy(spacing.large)) {
        TextField(
            state = rememberTextFieldState(),
            title = { Text("Username") },
            suggestion = { Text("Try kotogram_cat") },
            placeholder = { Text("Enter a username") },
            prefix = { Text("@") },
            supportingText = { Text("You can use a–z, 0–9 and underscores.") }
        )
        TextField(
            state = rememberTextFieldState("admin"),
            prefix = { Text("@") },
            supportingText = { Text("This username is already taken.") },
            isError = true
        )
        SecureTextField(
            state = rememberTextFieldState("kotogram"),
            placeholder = { Text("Your password") }
        )
        TextField(state = rememberTextFieldState("Disabled"), enabled = false)
    }
}
