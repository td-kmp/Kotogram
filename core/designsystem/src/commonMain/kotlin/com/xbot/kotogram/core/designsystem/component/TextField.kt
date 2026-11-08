package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramLaptopPreview
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import androidx.compose.material3.TextFieldDefaults as MaterialTextFieldDefaults

@Composable
fun TextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
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
    style: TextFieldStyle = TextFieldStyle,
    interactionSource: MutableInteractionSource? = null
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    @Suppress("NAME_SHADOWING")
    val style = rememberStyle(::TextFieldStyleScope, TextFieldStyle.Adaptive then style)
    context(style) {
        val colors = textFieldColors()
        CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
            BasicTextField(
                state = state,
                modifier = modifier.defaultMinSize(minWidth = style.minWidth),
                enabled = enabled,
                readOnly = readOnly,
                inputTransformation = inputTransformation,
                textStyle = textStyleWithColor(style.textStyle, colors, enabled, isError, interactionSource),
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
                        colors = colors,
                        title = title,
                        suggestion = suggestion,
                        placeholder = placeholder,
                        leadingIcon = leadingIcon,
                        trailingIcon = trailingIcon,
                        prefix = prefix,
                        suffix = suffix,
                        supportingText = supportingText
                    )
                },
                scrollState = scrollState
            )
        }
    }
}

@Composable
fun SecureTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
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
    style: TextFieldStyle = TextFieldStyle,
    interactionSource: MutableInteractionSource? = null
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    @Suppress("NAME_SHADOWING")
    val style = rememberStyle(::TextFieldStyleScope, TextFieldStyle.Adaptive then style)
    context(style) {
        val colors = textFieldColors()
        CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
            BasicSecureTextField(
                state = state,
                modifier = modifier.defaultMinSize(minWidth = style.minWidth),
                enabled = enabled,
                inputTransformation = inputTransformation,
                textStyle = textStyleWithColor(style.textStyle, colors, enabled, isError, interactionSource),
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
                        colors = colors,
                        title = title,
                        suggestion = suggestion,
                        placeholder = placeholder,
                        leadingIcon = leadingIcon,
                        trailingIcon = trailingIcon,
                        prefix = prefix,
                        suffix = suffix,
                        supportingText = supportingText
                    )
                },
                textObfuscationMode = textObfuscationMode,
                textObfuscationCharacter = textObfuscationCharacter
            )
        }
    }
}

// Material 3's colours of a field in its states, with the indicator and label colours of a style.
@Composable
internal fun materialTextFieldColors(
    focusedIndicatorColor: Color,
    unfocusedIndicatorColor: Color,
    disabledIndicatorColor: Color,
    errorIndicatorColor: Color,
    labelColor: Color
): TextFieldColors = MaterialTextFieldDefaults.colors(
    focusedIndicatorColor = focusedIndicatorColor,
    unfocusedIndicatorColor = unfocusedIndicatorColor,
    disabledIndicatorColor = disabledIndicatorColor,
    errorIndicatorColor = errorIndicatorColor,
    focusedLabelColor = labelColor,
    errorLabelColor = labelColor,
    errorTrailingIconColor = labelColor
)

@Composable
context(style: TextFieldStyleScope)
private fun textFieldColors(): TextFieldColors = materialTextFieldColors(
    focusedIndicatorColor = style.focusedIndicatorColor,
    unfocusedIndicatorColor = style.unfocusedIndicatorColor,
    disabledIndicatorColor = style.disabledIndicatorColor,
    errorIndicatorColor = style.errorIndicatorColor,
    labelColor = style.labelColor
)

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
context(style: TextFieldStyleScope)
private fun TextFieldDecoration(
    innerTextField: @Composable () -> Unit,
    isEmpty: Boolean,
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource,
    colors: TextFieldColors,
    title: @Composable (() -> Unit)?,
    suggestion: @Composable (() -> Unit)?,
    placeholder: @Composable (() -> Unit)?,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    prefix: @Composable (() -> Unit)?,
    suffix: @Composable (() -> Unit)?,
    supportingText: @Composable (() -> Unit)?
) {
    val focused by interactionSource.collectIsFocusedAsState()
    val colorSpec = style.colorAnimationSpec
    val containerColor = animateColorAsState(colors.containerColor(enabled, isError, focused), colorSpec)
    val indicatorColor = animateColorAsState(colors.indicatorColor(enabled, isError, focused), colorSpec)
    val titleColor by animateColorAsState(colors.labelColor(enabled, isError, focused), colorSpec)
    val supportingColor by animateColorAsState(colors.supportingTextColor(enabled, isError, focused), colorSpec)
    val slotStyle = style.textStyle.copy(color = Color.Unspecified)
    val layoutDirection = LocalLayoutDirection.current
    val sidePadding = Modifier.padding(
        start = style.contentPadding.calculateStartPadding(layoutDirection),
        end = style.contentPadding.calculateEndPadding(layoutDirection)
    )
    val measurePolicy = remember(style) { TextFieldMeasurePolicy(style) }
    Layout(
        content = {
            Box(
                Modifier
                    .layoutId(TextFieldSlot.Container)
                    .textFieldContainer(
                        shape = style.shape,
                        color = containerColor,
                        indicatorColor = indicatorColor,
                        indicatorThickness = style.indicatorThickness
                    )
            )
            if (title != null) {
                Box(
                    Modifier
                        .layoutId(TextFieldSlot.Title)
                        .then(sidePadding)
                        .padding(bottom = style.titleGap)
                ) {
                    ProvideContentColorTextStyle(titleColor, style.titleTextStyle, title)
                }
            }
            if (suggestion != null) {
                Box(
                    Modifier
                        .layoutId(TextFieldSlot.Suggestion)
                        .then(sidePadding)
                        .padding(bottom = style.titleGap)
                ) {
                    ProvideContentColorTextStyle(titleColor, style.suggestionTextStyle, suggestion)
                }
            }
            if (leadingIcon != null) {
                Box(
                    Modifier
                        .layoutId(TextFieldSlot.Leading)
                        .padding(start = style.iconInset)
                        .size(style.iconSize),
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
                        .padding(end = style.iconInset)
                        .size(style.iconSize),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides colors.trailingIconColor(enabled, isError, focused),
                        content = trailingIcon
                    )
                }
            }
            if (prefix != null) {
                Box(Modifier.layoutId(TextFieldSlot.Prefix).padding(end = style.affixGap)) {
                    ProvideContentColorTextStyle(colors.prefixColor(enabled, isError, focused), slotStyle, prefix)
                }
            }
            if (suffix != null) {
                Box(Modifier.layoutId(TextFieldSlot.Suffix).padding(start = style.affixGap)) {
                    ProvideContentColorTextStyle(colors.suffixColor(enabled, isError, focused), slotStyle, suffix)
                }
            }
            if (placeholder != null && isEmpty) {
                Box(Modifier.layoutId(TextFieldSlot.Placeholder)) {
                    ProvideContentColorTextStyle(
                        colors.placeholderColor(enabled, isError, focused),
                        style.placeholderTextStyle,
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
                    transitionSpec = { fadeIn(style.supportingFadeSpec) togetherWith fadeOut(style.supportingFadeSpec) },
                    contentKey = { it != null },
                    label = "SupportingText"
                ) { text ->
                    if (text != null) {
                        Box(sidePadding.padding(top = style.supportingGap)) {
                            ProvideContentColorTextStyle(supportingColor, style.supportingTextStyle, text)
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
private class TextFieldMeasurePolicy(private val style: TextFieldStyleScope) : MeasurePolicy {
    override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
        val contentPadding = style.contentPadding
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val leading = measurables.measure(TextFieldSlot.Leading, loose)
        val trailing = measurables.measure(TextFieldSlot.Trailing, loose)
        val start = if (leading == null) contentPadding.calculateStartPadding(layoutDirection) else style.iconGap
        val end = if (trailing == null) contentPadding.calculateEndPadding(layoutDirection) else style.iconGap
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
            style.minHeight.roundToPx(),
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

private const val DEFAULT_OBFUSCATION_CHARACTER: Char = '•'

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

@KotogramLaptopPreview
@Composable
private fun TextFieldLaptopPreview() = TextFieldPreview()

// Telegram for macOS's inputs on a laptop: 40 high, its radius and insets (Auth_Insets), no focus ring. On touch
// screens our own: Telegram for iOS's 17-point type in a field as high as its buttons (50), with Android's and iOS's
// focus ring.
@Suppress("MagicNumber")
fun interface TextFieldStyle : ComponentStyle<TextFieldStyleScope> {
    companion object : TextFieldStyle {
        override fun TextFieldStyleScope.applyStyle() {}

        val Default = TextFieldStyle {
            minWidth(280.dp)
            minHeight(50.dp)
            shape(theme.shapes.medium)
            contentPadding(PaddingValues(horizontal = 16.dp, vertical = 12.dp))
            textStyle(theme.typography.bodyLarge.merge(fontSize = 17.sp))
            placeholderTextStyle(theme.typography.bodyLarge.merge(fontSize = 17.sp))
            titleTextStyle(theme.typography.bodyMedium.merge(fontSize = 15.sp))
            suggestionTextStyle(theme.typography.bodyMedium.merge(fontSize = 15.sp))
            supportingTextStyle(theme.typography.bodyMedium.merge(fontSize = 15.sp))
            titleGap(theme.spacing.extraSmall)
            supportingGap(theme.spacing.small)
            iconSize(32.dp)
            iconInset(8.dp)
            iconGap(4.dp)
            affixGap(6.dp)
            indicatorThickness(2.dp)
            focusedIndicatorColor(theme.colors.primary)
            unfocusedIndicatorColor(Color.Transparent)
            disabledIndicatorColor(Color.Transparent)
            errorIndicatorColor(theme.colors.error)
            labelColor(theme.colors.onSurfaceVariant)
            colorAnimationSpec(tween(150))
            supportingFadeSpec(tween(200))
        }

        val Adaptive = Default then TextFieldStyle {
            laptop {
                minHeight(40.dp)
                contentPadding(PaddingValues(theme.spacing.small))
                textStyle(theme.typography.bodyLarge)
                placeholderTextStyle(theme.typography.bodyMedium)
                titleTextStyle(theme.typography.bodySmall)
                suggestionTextStyle(theme.typography.bodyMedium)
                supportingTextStyle(theme.typography.bodyMedium)
                iconSize(28.dp)
                iconInset(6.dp)
                affixGap(4.dp)
                focusedIndicatorColor(Color.Transparent)
                errorIndicatorColor(Color.Transparent)
            }
        }
    }
}

infix fun TextFieldStyle.then(other: TextFieldStyle): TextFieldStyle = when {
    other === TextFieldStyle -> this
    this === TextFieldStyle -> other
    else -> TextFieldStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

class TextFieldStyleScope(val theme: KotogramTheme.Values, override val mediaQueryInfo: MediaQueryInfo) :
    AdaptiveStyleScope<TextFieldStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var minWidth: Dp = Dp.Unspecified
        private set

    var minHeight: Dp = Dp.Unspecified
        private set

    var shape: Shape = RectangleShape
        private set

    var contentPadding: PaddingValues = PaddingValues()
        private set

    var textStyle: TextStyle = TextStyle.Default
        private set

    var placeholderTextStyle: TextStyle = TextStyle.Default
        private set

    var titleTextStyle: TextStyle = TextStyle.Default
        private set

    var suggestionTextStyle: TextStyle = TextStyle.Default
        private set

    var supportingTextStyle: TextStyle = TextStyle.Default
        private set

    var titleGap: Dp = Dp.Unspecified
        private set

    var supportingGap: Dp = Dp.Unspecified
        private set

    var iconSize: Dp = Dp.Unspecified
        private set

    var iconInset: Dp = Dp.Unspecified
        private set

    var iconGap: Dp = Dp.Unspecified
        private set

    var affixGap: Dp = Dp.Unspecified
        private set

    var indicatorThickness: Dp = Dp.Unspecified
        private set

    var focusedIndicatorColor: Color = Color.Unspecified
        private set

    var unfocusedIndicatorColor: Color = Color.Unspecified
        private set

    var disabledIndicatorColor: Color = Color.Unspecified
        private set

    var errorIndicatorColor: Color = Color.Unspecified
        private set

    var labelColor: Color = Color.Unspecified
        private set

    var colorAnimationSpec: AnimationSpec<Color> = snap()
        private set

    var supportingFadeSpec: FiniteAnimationSpec<Float> = snap()
        private set

    fun minWidth(width: Dp) {
        minWidth = width
    }

    fun minHeight(height: Dp) {
        minHeight = height
    }

    fun shape(shape: Shape) {
        this.shape = shape
    }

    fun contentPadding(padding: PaddingValues) {
        contentPadding = padding
    }

    fun textStyle(style: TextStyle) {
        textStyle = style
    }

    fun placeholderTextStyle(style: TextStyle) {
        placeholderTextStyle = style
    }

    fun titleTextStyle(style: TextStyle) {
        titleTextStyle = style
    }

    fun suggestionTextStyle(style: TextStyle) {
        suggestionTextStyle = style
    }

    fun supportingTextStyle(style: TextStyle) {
        supportingTextStyle = style
    }

    fun titleGap(gap: Dp) {
        titleGap = gap
    }

    fun supportingGap(gap: Dp) {
        supportingGap = gap
    }

    fun iconSize(size: Dp) {
        iconSize = size
    }

    fun iconInset(inset: Dp) {
        iconInset = inset
    }

    fun iconGap(gap: Dp) {
        iconGap = gap
    }

    fun affixGap(gap: Dp) {
        affixGap = gap
    }

    fun indicatorThickness(thickness: Dp) {
        indicatorThickness = thickness
    }

    fun focusedIndicatorColor(color: Color) {
        focusedIndicatorColor = color
    }

    fun unfocusedIndicatorColor(color: Color) {
        unfocusedIndicatorColor = color
    }

    fun disabledIndicatorColor(color: Color) {
        disabledIndicatorColor = color
    }

    fun errorIndicatorColor(color: Color) {
        errorIndicatorColor = color
    }

    fun labelColor(color: Color) {
        labelColor = color
    }

    fun colorAnimationSpec(spec: AnimationSpec<Color>) {
        colorAnimationSpec = spec
    }

    fun supportingFadeSpec(spec: FiniteAnimationSpec<Float>) {
        supportingFadeSpec = spec
    }
}
