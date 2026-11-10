package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.PreviewKotogram
import com.xbot.kotogram.core.designsystem.PreviewKotogramLaptop
import com.xbot.kotogram.core.designsystem.component.platform.OneTimeCodeImeOptions
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import kotlin.math.roundToInt

@Composable
fun CodeField(
    state: TextFieldState,
    length: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = CodeFieldKeyboardOptions,
    onKeyboardAction: KeyboardActionHandler? = null,
    style: CodeFieldStyle = CodeFieldStyle,
    interactionSource: MutableInteractionSource? = null,
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val inputTransformation = remember(length) { CodeInputTransformation(length) }

    @Suppress("NAME_SHADOWING")
    val style = rememberStyle(::CodeFieldStyleScope, CodeFieldStyle.Adaptive then style)
    val shake = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError) shake.shake(style)
    }
    LaunchedEffect(state) {
        snapshotFlow { state.selection to state.text.length }.collect { (selection, end) ->
            if (selection != TextRange(end)) state.edit { placeCursorAtEnd() }
        }
    }
    context(style) {
        val colors = materialTextFieldColors(
            focusedIndicatorColor = style.focusedIndicatorColor,
            unfocusedIndicatorColor = style.unfocusedIndicatorColor,
            disabledIndicatorColor = style.disabledIndicatorColor,
            errorIndicatorColor = style.errorIndicatorColor,
            labelColor = style.labelColor,
        )
        CompositionLocalProvider(LocalTextSelectionColors provides HiddenSelectionColors) {
            BasicTextField(
                state = state,
                modifier = modifier.semantics { contentType = ContentType.SmsOtpCode },
                enabled = enabled,
                inputTransformation = inputTransformation,
                textStyle = style.textStyle,
                keyboardOptions = keyboardOptions,
                onKeyboardAction = onKeyboardAction,
                lineLimits = TextFieldLineLimits.SingleLine,
                interactionSource = interactionSource,
                cursorBrush = SolidColor(Color.Transparent),
                decorator = TextFieldDecorator { innerTextField ->
                    CodeFieldDecoration(
                        innerTextField = innerTextField,
                        text = state.text,
                        length = length,
                        enabled = enabled,
                        isError = isError,
                        supportingText = supportingText,
                        shakeOffset = { shake.value },
                        interactionSource = interactionSource,
                        colors = colors,
                    )
                },
            )
        }
    }
}

@Composable
context(style: CodeFieldStyleScope)
private fun CodeFieldDecoration(
    innerTextField: @Composable () -> Unit,
    text: CharSequence,
    length: Int,
    enabled: Boolean,
    isError: Boolean,
    supportingText: @Composable (() -> Unit)?,
    shakeOffset: () -> Float,
    interactionSource: InteractionSource,
    colors: TextFieldColors,
) {
    val focused by interactionSource.collectIsFocusedAsState()
    val activeIndex = minOf(text.length, length - 1)
    val supportingColor by animateColorAsState(
        targetValue = colors.supportingTextColor(enabled, isError, focused),
        animationSpec = style.borderSpec,
        label = "CodeFieldSupportingText",
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.offset { IntOffset(shakeOffset().dp.roundToPx(), 0) },
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.matchParentSize().alpha(0f)) { innerTextField() }
            CodeCells(length, text, activeIndex, focused, enabled, isError, colors)
        }
        AnimatedContent(
            targetState = supportingText,
            transitionSpec = {
                fadeIn(tween(style.digitMillis)) togetherWith
                    fadeOut(tween(style.digitMillis))
            },
            contentKey = { it != null },
            label = "CodeFieldSupportingText",
        ) { content ->
            if (content != null) {
                Box(Modifier.padding(top = style.supportingGap)) {
                    CompositionLocalProvider(
                        LocalContentColor provides supportingColor,
                        LocalTextStyle provides style.supportingTextStyle,
                        content = content,
                    )
                }
            }
        }
    }
}

@Composable
context(style: CodeFieldStyleScope)
private fun CodeCells(
    length: Int,
    text: CharSequence,
    activeIndex: Int,
    focused: Boolean,
    enabled: Boolean,
    isError: Boolean,
    colors: TextFieldColors,
) {
    Layout(
        content = {
            repeat(length) { index ->
                CodeCell(
                    digit = text.getOrNull(index),
                    active = focused && enabled && index == activeIndex,
                    enabled = enabled,
                    isError = isError,
                    colors = colors,
                )
            }
        },
        measurePolicy = remember(style) { CodeCellsMeasurePolicy(style) },
    )
}

@Composable
context(style: CodeFieldStyleScope)
private fun CodeCell(
    digit: Char?,
    active: Boolean,
    enabled: Boolean,
    isError: Boolean,
    colors: TextFieldColors,
) {
    val borderColor by animateColorAsState(
        targetValue = if (active) {
            colors.indicatorColor(
                enabled,
                isError,
                focused = true,
            )
        } else {
            Color.Transparent
        },
        animationSpec = style.borderSpec,
        label = "CodeCellBorder",
    )
    val textColor = colors.textColor(enabled, isError, active)
    val rise = with(LocalDensity.current) { style.digitRise.roundToPx() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.containerColor(enabled, isError, active), style.shape)
            .border(style.activeBorderWidth, borderColor, style.shape),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = digit,
            transitionSpec = {
                val digitSpec = tween<Float>(style.digitMillis, easing = EaseOut)
                (
                    fadeIn(digitSpec) +
                        slideInVertically(tween(style.digitMillis, easing = EaseOut)) { rise } +
                        scaleIn(style.digitSpring, initialScale = style.digitScale)
                    ) togetherWith (
                    fadeOut(digitSpec) + scaleOut(digitSpec, style.digitScale)
                    ) using SizeTransform(clip = false)
            },
            contentAlignment = Alignment.Center,
            label = "CodeDigit",
        ) { shown ->
            if (shown != null) {
                BasicText(
                    text = shown.toString(),
                    style = style.textStyle.copy(color = textColor),
                    softWrap = false,
                    maxLines = 1,
                )
            }
        }
    }
}

private class CodeCellsMeasurePolicy(
    private val style: CodeFieldStyleScope,
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val count = measurables.size
        val gap = style.cellSpacing.roundToPx()
        val fontSize = style.textStyle.fontSize
        val fontScale = if (fontSize.isSp) {
            maxOf(
                1f,
                fontSize.toPx() / fontSize.value.dp.toPx(),
            )
        } else {
            1f
        }
        val cellWidth = (style.cellSize.width.toPx() * fontScale).roundToInt()
        val height = (style.cellSize.height.toPx() * fontScale).roundToInt()
        val gaps = gap * (count - 1).coerceAtLeast(0)
        val fitting = if (constraints.hasBoundedWidth &&
            count > 0
        ) {
            (constraints.maxWidth - gaps) / count
        } else {
            cellWidth
        }
        val width = minOf(cellWidth, fitting).coerceAtLeast(0)
        val cells = measurables.map { it.measure(Constraints.fixed(width, height)) }
        return layout(width * count + gaps, height) {
            cells.forEachIndexed { index, cell -> cell.placeRelative(index * (width + gap), 0) }
        }
    }
}

private class CodeInputTransformation(
    private val codeLength: Int,
) : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val digits = asCharSequence().filter(Char::isDigit)
        when {
            digits.length <= codeLength -> if (!digits.contentEquals(
                    asCharSequence(),
                )
            ) {
                replace(0, length, digits)
            }

            else -> {
                val inserted = insertedDigits()
                if (inserted.length >=
                    codeLength
                ) {
                    replace(0, length, inserted.take(codeLength))
                } else {
                    revertAllChanges()
                }
            }
        }
        placeCursorAtEnd()
    }

    private fun TextFieldBuffer.insertedDigits(): String {
        val current = asCharSequence()
        val inserted = if (current.startsWith(originalText)) {
            current.subSequence(originalText.length, current.length)
        } else {
            current
        }
        return inserted.filter(Char::isDigit).toString()
    }
}

private suspend fun Animatable<Float, *>.shake(style: CodeFieldStyleScope) {
    snapTo(-style.shakeDistance)
    animateTo(
        targetValue = style.shakeDistance,
        animationSpec = repeatable(
            iterations = style.shakeLegs,
            animation = tween(style.shakeLegMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    snapTo(0f)
}

private val HiddenSelectionColors = TextSelectionColors(
    handleColor = Color.Transparent,
    backgroundColor = Color.Transparent,
)

private val CodeFieldKeyboardOptions = KeyboardOptions(
    keyboardType = KeyboardType.NumberPassword,
    imeAction = ImeAction.Done,
    autoCorrectEnabled = false,
    platformImeOptions = OneTimeCodeImeOptions,
)

@PreviewKotogram
@Composable
private fun CodeFieldPreview() {
    Column(
        modifier = Modifier.padding(KotogramTheme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.large),
    ) {
        CodeField(state = rememberTextFieldState("123"), length = 5)
        CodeField(state = rememberTextFieldState("123456"), length = 6)
        CodeField(state = rememberTextFieldState("12345"), length = 5, enabled = false)
    }
}

@PreviewKotogramLaptop
@Composable
private fun CodeFieldLaptopPreview() = CodeFieldPreview()

@Suppress("MagicNumber")
fun interface CodeFieldStyle : ComponentStyle<CodeFieldStyleScope> {
    companion object : CodeFieldStyle {
        override fun CodeFieldStyleScope.applyStyle() {}

        val Default = CodeFieldStyle {
            cellSize(DpSize(43.dp, 51.dp))
            cellSpacing(15.dp)
            shape(RoundedCornerShape(15.dp))
            textStyle(
                theme.typography.headlineSmall.merge(
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Normal,
                    fontFeatureSettings = "tnum",
                ),
            )
            activeBorderWidth(1.dp)
            focusedIndicatorColor(theme.colors.primary)
            unfocusedIndicatorColor(Color.Transparent)
            disabledIndicatorColor(Color.Transparent)
            errorIndicatorColor(theme.colors.error)
            labelColor(theme.colors.onSurfaceVariant)
            supportingTextStyle(theme.typography.bodyMedium.merge(textAlign = TextAlign.Center))
            supportingGap(theme.spacing.small)
            digitRise(5.dp)
            digitScale(0.1f)
            digitMillis(200)
            digitSpring(spring(dampingRatio = 0.656f, stiffness = 180f))
            borderSpec(tween(200))
            shakeDistance(3f)
            shakeLegs(8)
            shakeLegMillis(40)
        }

        val Adaptive = Default then CodeFieldStyle {
            laptop {
                cellSize(DpSize(40.dp, 40.dp))
                cellSpacing(8.dp)
                shape(theme.shapes.medium)
                textStyle(
                    theme.typography.headlineSmall.merge(
                        fontSize = 24.sp,
                        fontFeatureSettings = "tnum",
                    ),
                )
            }
        }
    }
}

infix fun CodeFieldStyle.then(other: CodeFieldStyle): CodeFieldStyle = when {
    other === CodeFieldStyle -> this

    this === CodeFieldStyle -> other

    else -> CodeFieldStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
class CodeFieldStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<CodeFieldStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var cellSize: DpSize = DpSize.Unspecified
        private set

    var cellSpacing: Dp = Dp.Unspecified
        private set

    var shape: Shape = RectangleShape
        private set

    var textStyle: TextStyle = TextStyle.Default
        private set

    var activeBorderWidth: Dp = Dp.Unspecified
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

    var supportingTextStyle: TextStyle = TextStyle.Default
        private set

    var supportingGap: Dp = Dp.Unspecified
        private set

    var digitRise: Dp = Dp.Unspecified
        private set

    var digitScale: Float = 1f
        private set

    var digitMillis: Int = 0
        private set

    var digitSpring: FiniteAnimationSpec<Float> = snap()
        private set

    var borderSpec: AnimationSpec<Color> = snap()
        private set

    var shakeDistance: Float = 0f
        private set

    var shakeLegs: Int = 0
        private set

    var shakeLegMillis: Int = 0
        private set

    fun cellSize(size: DpSize) {
        cellSize = size
    }

    fun cellSpacing(spacing: Dp) {
        cellSpacing = spacing
    }

    fun shape(shape: Shape) {
        this.shape = shape
    }

    fun textStyle(style: TextStyle) {
        textStyle = style
    }

    fun activeBorderWidth(width: Dp) {
        activeBorderWidth = width
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

    fun supportingTextStyle(style: TextStyle) {
        supportingTextStyle = style
    }

    fun supportingGap(gap: Dp) {
        supportingGap = gap
    }

    fun digitRise(rise: Dp) {
        digitRise = rise
    }

    fun digitScale(scale: Float) {
        digitScale = scale
    }

    fun digitMillis(millis: Int) {
        digitMillis = millis
    }

    fun digitSpring(spec: FiniteAnimationSpec<Float>) {
        digitSpring = spec
    }

    fun borderSpec(spec: AnimationSpec<Color>) {
        borderSpec = spec
    }

    fun shakeDistance(distance: Float) {
        shakeDistance = distance
    }

    fun shakeLegs(legs: Int) {
        shakeLegs = legs
    }

    fun shakeLegMillis(millis: Int) {
        shakeLegMillis = millis
    }
}
