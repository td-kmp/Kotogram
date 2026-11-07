package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.repeatable
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
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.KotogramTheme
import kotlin.math.roundToInt

@Composable
fun CodeField(
    state: TextFieldState,
    length: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = CodeFieldDefaults.textStyle,
    keyboardOptions: KeyboardOptions = CodeFieldDefaults.keyboardOptions,
    onKeyboardAction: KeyboardActionHandler? = null,
    shape: Shape = CodeFieldDefaults.shape,
    colors: TextFieldColors = CodeFieldDefaults.colors(),
    interactionSource: MutableInteractionSource? = null
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val inputTransformation = remember(length) { CodeInputTransformation(length) }
    val shake = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError) shake.shake()
    }
    // Digits go in at the end only: a tap or a selection elsewhere would edit a cell the user can't see.
    LaunchedEffect(state) {
        snapshotFlow { state.selection to state.text.length }.collect { (selection, end) ->
            if (selection != TextRange(end)) state.edit { placeCursorAtEnd() }
        }
    }
    CompositionLocalProvider(LocalTextSelectionColors provides HiddenSelectionColors) {
        BasicTextField(
            state = state,
            modifier = modifier.semantics { contentType = ContentType.SmsOtpCode },
            enabled = enabled,
            inputTransformation = inputTransformation,
            textStyle = textStyle,
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
                    textStyle = textStyle,
                    shape = shape,
                    colors = colors
                )
            }
        )
    }
}

@Suppress("MagicNumber")
object CodeFieldDefaults {
    val CellSize = 40.dp

    val CellSpacing = 8.dp

    val ActiveBorderWidth = 1.dp

    val shape: Shape
        @Composable @ReadOnlyComposable
        get() = KotogramTheme.shapes.medium

    val textStyle: TextStyle
        @Composable @ReadOnlyComposable
        get() = KotogramTheme.typography.headlineSmall.copy(fontSize = 24.sp, fontFeatureSettings = "tnum")

    val keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.NumberPassword,
        imeAction = ImeAction.Done,
        autoCorrectEnabled = false
    )

    // Telegram for macOS marks the cell to fill on every platform, unlike the text field's focus ring.
    @Composable
    fun colors(): TextFieldColors {
        val colors = KotogramTheme.colors
        return TextFieldDefaults.colors().copy(
            focusedIndicatorColor = colors.primary,
            errorIndicatorColor = colors.error
        )
    }
}

@Composable
private fun CodeFieldDecoration(
    innerTextField: @Composable () -> Unit,
    text: CharSequence,
    length: Int,
    enabled: Boolean,
    isError: Boolean,
    supportingText: @Composable (() -> Unit)?,
    shakeOffset: () -> Float,
    interactionSource: InteractionSource,
    textStyle: TextStyle,
    shape: Shape,
    colors: TextFieldColors
) {
    val focused by interactionSource.collectIsFocusedAsState()
    val activeIndex = minOf(text.length, length - 1)
    val supportingColor by animateColorAsState(
        targetValue = colors.supportingTextColor(enabled, isError, focused),
        animationSpec = tween(CodeFieldTokens.BORDER_MILLIS),
        label = "CodeFieldSupportingText"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Only the cells shake, as Telegram for macOS shakes its control and not the error under it.
        Box(
            modifier = Modifier.offset { IntOffset(shakeOffset().dp.roundToPx(), 0) },
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.matchParentSize().alpha(0f)) { innerTextField() }
            CodeCells(length, text, activeIndex, focused, enabled, isError, textStyle, shape, colors)
        }
        AnimatedContent(
            targetState = supportingText,
            transitionSpec = {
                fadeIn(tween(CodeFieldTokens.DIGIT_MILLIS)) togetherWith fadeOut(tween(CodeFieldTokens.DIGIT_MILLIS))
            },
            contentKey = { it != null },
            label = "CodeFieldSupportingText"
        ) { content ->
            if (content != null) {
                Box(Modifier.padding(top = KotogramTheme.spacing.small)) {
                    CompositionLocalProvider(
                        LocalContentColor provides supportingColor,
                        LocalTextStyle provides KotogramTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                        content = content
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeCells(
    length: Int,
    text: CharSequence,
    activeIndex: Int,
    focused: Boolean,
    enabled: Boolean,
    isError: Boolean,
    textStyle: TextStyle,
    shape: Shape,
    colors: TextFieldColors
) {
    Layout(
        content = {
            repeat(length) { index ->
                CodeCell(
                    digit = text.getOrNull(index),
                    active = focused && enabled && index == activeIndex,
                    enabled = enabled,
                    isError = isError,
                    textStyle = textStyle,
                    shape = shape,
                    colors = colors
                )
            }
        },
        measurePolicy = remember(textStyle.fontSize) { CodeCellsMeasurePolicy(textStyle.fontSize) }
    )
}

@Composable
private fun CodeCell(
    digit: Char?,
    active: Boolean,
    enabled: Boolean,
    isError: Boolean,
    textStyle: TextStyle,
    shape: Shape,
    colors: TextFieldColors
) {
    val borderColor by animateColorAsState(
        targetValue = if (active) colors.indicatorColor(enabled, isError, focused = true) else Color.Transparent,
        animationSpec = tween(CodeFieldTokens.BORDER_MILLIS),
        label = "CodeCellBorder"
    )
    val textColor = colors.textColor(enabled, isError, active)
    val rise = with(LocalDensity.current) { CodeFieldTokens.DigitRise.roundToPx() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.containerColor(enabled, isError, active), shape)
            .border(CodeFieldDefaults.ActiveBorderWidth, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = digit,
            transitionSpec = {
                (
                    fadeIn(tween(CodeFieldTokens.DIGIT_MILLIS, easing = EaseOut)) +
                        slideInVertically(tween(CodeFieldTokens.DIGIT_MILLIS, easing = EaseOut)) { rise } +
                        scaleIn(DigitSpring, initialScale = CodeFieldTokens.DIGIT_SCALE)
                    ) togetherWith (
                    fadeOut(tween(CodeFieldTokens.DIGIT_MILLIS, easing = EaseOut)) +
                        scaleOut(tween(CodeFieldTokens.DIGIT_MILLIS, easing = EaseOut), CodeFieldTokens.DIGIT_SCALE)
                    ) using SizeTransform(clip = false)
            },
            contentAlignment = Alignment.Center,
            label = "CodeDigit"
        ) { shown ->
            if (shown != null) {
                BasicText(
                    text = shown.toString(),
                    style = textStyle.copy(color = textColor),
                    softWrap = false,
                    maxLines = 1
                )
            }
        }
    }
}

// Cells grow with the digits, which follow the system's font scale, and narrow when the row runs out of width.
private class CodeCellsMeasurePolicy(private val fontSize: TextUnit) : MeasurePolicy {
    override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
        val count = measurables.size
        val gap = CodeFieldDefaults.CellSpacing.roundToPx()
        val minSide = CodeFieldDefaults.CellSize.roundToPx()
        val side = if (fontSize.isSp) {
            maxOf(minSide, (fontSize.toPx() * CodeFieldTokens.CELL_PER_DIGIT).roundToInt())
        } else {
            minSide
        }
        val gaps = gap * (count - 1).coerceAtLeast(0)
        val fitting = if (constraints.hasBoundedWidth && count > 0) (constraints.maxWidth - gaps) / count else side
        val width = minOf(side, fitting).coerceAtLeast(0)
        val cells = measurables.map { it.measure(Constraints.fixed(width, side)) }
        return layout(width * count + gaps, side) {
            cells.forEachIndexed { index, cell -> cell.placeRelative(index * (width + gap), 0) }
        }
    }
}

private class CodeInputTransformation(private val codeLength: Int) : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val digits = asCharSequence().filter(Char::isDigit)
        when {
            digits.length <= codeLength -> if (!digits.contentEquals(asCharSequence())) replace(0, length, digits)
            else -> {
                // A whole code pasted over a full field replaces it, as Telegram for macOS's insertAll does.
                val inserted = insertedDigits()
                if (inserted.length >= codeLength) replace(0, length, inserted.take(codeLength)) else revertAllChanges()
            }
        }
        placeCursorAtEnd()
    }

    // The cursor stays at the end, so whatever follows the old text is what came in.
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

private suspend fun Animatable<Float, *>.shake() {
    snapTo(-CodeFieldTokens.SHAKE_DISTANCE)
    animateTo(
        targetValue = CodeFieldTokens.SHAKE_DISTANCE,
        animationSpec = repeatable(
            iterations = CodeFieldTokens.SHAKE_LEGS,
            animation = tween(CodeFieldTokens.SHAKE_LEG_MILLIS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    snapTo(0f)
}

private val HiddenSelectionColors = TextSelectionColors(
    handleColor = Color.Transparent,
    backgroundColor = Color.Transparent
)

private val DigitSpring = spring<Float>(dampingRatio = 0.656f, stiffness = 180f)

@Suppress("MagicNumber")
private object CodeFieldTokens {
    val DigitRise = 5.dp

    const val CELL_PER_DIGIT = 40f / 24f
    const val DIGIT_MILLIS = 200
    const val DIGIT_SCALE = 0.1f
    const val BORDER_MILLIS = 200
    const val SHAKE_DISTANCE = 3f
    const val SHAKE_LEGS = 8
    const val SHAKE_LEG_MILLIS = 40
}

@KotogramPreview
@Composable
private fun CodeFieldPreview() {
    Column(
        modifier = Modifier.padding(KotogramTheme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.large)
    ) {
        CodeField(state = rememberTextFieldState("123"), length = 5)
        CodeField(state = rememberTextFieldState("123456"), length = 6)
        CodeField(state = rememberTextFieldState("12345"), length = 5, enabled = false)
    }
}
