package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle

@Composable
fun PhoneNumberField(
    country: String,
    onCountryClick: () -> Unit,
    state: TextFieldState,
    callingCodeLength: Int,
    numberPattern: String,
    numberPlaceholder: String,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    countryPlaceholder: String = "",
    readOnly: Boolean = false,
    isError: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() },
    style: PhoneNumberFieldStyle = PhoneNumberFieldStyle,
    countryMenu: @Composable () -> Unit = {},
) {
    @Suppress("NAME_SHADOWING")
    val style = rememberStyle(::PhoneNumberFieldStyleScope, PhoneNumberFieldStyle.Adaptive then style)
    val countryStyle = remember(style) { style.countryStyle() }
    val numberStyle = remember(style) { style.numberStyle() }
    val format = remember(callingCodeLength, numberPattern) { PhoneNumberFormat(callingCodeLength, numberPattern) }
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val containerColor = rememberUpdatedState(style.containerColor)
    val indicatorColor = animateColorAsState(
        targetValue = when {
            isError -> style.errorIndicatorColor
            focused -> style.focusedIndicatorColor
            else -> Color.Transparent
        },
        animationSpec = style.colorAnimationSpec,
    )
    Column(
        modifier
            .width(style.width)
            .textFieldContainer(style.shape, containerColor, indicatorColor, style.indicatorThickness)
            .clip(style.shape),
    ) {
        Box {
            ListItem(
                headlineContent = {
                    Text(
                        text = country.ifEmpty { countryPlaceholder },
                        color = if (country.isEmpty()) style.hintColor else Color.Unspecified,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                modifier = Modifier.semantics { role = Role.DropdownList },
                trailingContent = {
                    Icon(KotogramIcons.ChevronRight, contentDescription = null, Modifier.size(style.chevronSize))
                },
                onClick = onCountryClick.takeUnless { readOnly },
                position = ListItemPosition.First,
                style = countryStyle,
            )
            countryMenu()
        }
        TextField(
            state = state,
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            readOnly = readOnly,
            placeholder = { Text(numberPlaceholder, maxLines = 1) },
            mask = { Text(format.mask(state.text, numberPlaceholder), color = style.hintColor, maxLines = 1) },
            isError = isError,
            inputTransformation = DigitsOnly,
            outputTransformation = format,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            onKeyboardAction = { onSubmit() },
            lineLimits = TextFieldLineLimits.SingleLine,
            style = numberStyle,
            interactionSource = interactionSource,
        )
    }
}

private fun PhoneNumberFieldStyleScope.countryStyle(): ListItemStyle {
    val field = this
    return ListItemStyle {
        minHeight(field.minRowHeight)
        contentPadding(field.contentPadding)
        groupShape(RoundedCornerShape(0.dp))
        containerColor(Color.Transparent)
        dividerColor(field.separatorColor)
        headlineTextStyle(field.countryTextStyle)
    }
}

private fun PhoneNumberFieldStyleScope.numberStyle(): TextFieldStyle {
    val field = this
    return TextFieldStyle {
        minWidth(0.dp)
        minHeight(field.minRowHeight)
        shape(RectangleShape)
        containerColor(Color.Transparent)
        contentPadding(field.contentPadding)
        textStyle(field.numberTextStyle)
        placeholderTextStyle(field.numberTextStyle.copy(color = field.hintColor))
        focusedIndicatorColor(Color.Transparent)
        unfocusedIndicatorColor(Color.Transparent)
        errorIndicatorColor(Color.Transparent)
    }
}

private class PhoneNumberFormat(
    private val callingCodeLength: Int,
    private val pattern: String,
) : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        format(originalText).forEachIndexed { index, char ->
            if (!char.isDigit()) insert(index, char.toString())
        }
    }

    fun mask(
        digits: CharSequence,
        placeholder: String,
    ): AnnotatedString {
        val code = digits.length.coerceAtMost(callingCodeLength)
        if (code == 0) return AnnotatedString("")
        val onlyCode = digits.length == code
        val written = format(digits)
        val rest = if (pattern.isEmpty() && onlyCode) placeholder else pattern
        val typed = (written.length - code - PLUS_AND_SPACE).coerceIn(0, rest.length)
        return buildAnnotatedString {
            withStyle(SpanStyle(color = Color.Transparent)) { append(if (onlyCode) "$written " else written) }
            append(rest.drop(typed))
        }
    }

    private fun format(digits: CharSequence): String = buildString {
        if (digits.isEmpty()) return@buildString
        val code = digits.length.coerceAtMost(callingCodeLength)
        append('+').append(digits, 0, code)
        if (digits.length == code) return@buildString
        if (code > 0) append(' ')
        var next = code
        for (slot in pattern) {
            if (next == digits.length) break
            append(if (slot.isDigit() || slot == '-') digits[next++] else slot)
        }
        append(digits, next, digits.length)
    }
}

private object DigitsOnly : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val digits = asCharSequence().filter(Char::isDigit).take(MAX_DIGITS)
        if (!asCharSequence().contentEquals(digits)) replace(0, length, digits)
    }
}

private const val MAX_DIGITS = 15
private const val PLUS_AND_SPACE = 2

@Suppress("MagicNumber")
fun interface PhoneNumberFieldStyle : ComponentStyle<PhoneNumberFieldStyleScope> {
    companion object : PhoneNumberFieldStyle {
        override fun PhoneNumberFieldStyleScope.applyStyle() {}

        val Default = PhoneNumberFieldStyle {
            width(280.dp)
            minRowHeight(50.dp)
            shape(theme.shapes.medium)
            contentPadding(PaddingValues(horizontal = 16.dp, vertical = 12.dp))
            containerColor(theme.colors.surfaceContainerHighest)
            separatorColor(theme.colors.outline.copy(alpha = 0.12f))
            hintColor(theme.colors.onSurfaceVariant)
            chevronSize(24.dp)
            countryTextStyle(theme.typography.bodyLarge.merge(fontSize = 17.sp, color = theme.colors.onSurface))
            numberTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Monospace,
                    color = theme.colors.onSurface,
                ),
            )
            indicatorThickness(2.dp)
            focusedIndicatorColor(theme.colors.primary)
            errorIndicatorColor(theme.colors.error)
            colorAnimationSpec(tween(150))
        }

        val Adaptive = Default then PhoneNumberFieldStyle {
            laptop {
                minRowHeight(40.dp)
                contentPadding(PaddingValues(theme.spacing.small))
                chevronSize(20.dp)
                countryTextStyle(theme.typography.bodyLarge.merge(color = theme.colors.onSurface))
                numberTextStyle(
                    theme.typography.bodyLarge.merge(
                        fontFamily = FontFamily.Monospace,
                        color = theme.colors.onSurface,
                    ),
                )
                focusedIndicatorColor(Color.Transparent)
                errorIndicatorColor(Color.Transparent)
            }
        }
    }
}

infix fun PhoneNumberFieldStyle.then(other: PhoneNumberFieldStyle): PhoneNumberFieldStyle = when {
    other === PhoneNumberFieldStyle -> this

    this === PhoneNumberFieldStyle -> other

    else -> PhoneNumberFieldStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
class PhoneNumberFieldStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<PhoneNumberFieldStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var width: Dp = Dp.Unspecified
        private set

    var minRowHeight: Dp = Dp.Unspecified
        private set

    var shape: Shape = RectangleShape
        private set

    var contentPadding: PaddingValues = PaddingValues()
        private set

    var containerColor: Color = Color.Unspecified
        private set

    var separatorColor: Color = Color.Unspecified
        private set

    var hintColor: Color = Color.Unspecified
        private set

    var chevronSize: Dp = Dp.Unspecified
        private set

    var countryTextStyle: TextStyle = TextStyle.Default
        private set

    var numberTextStyle: TextStyle = TextStyle.Default
        private set

    var indicatorThickness: Dp = Dp.Unspecified
        private set

    var focusedIndicatorColor: Color = Color.Unspecified
        private set

    var errorIndicatorColor: Color = Color.Unspecified
        private set

    var colorAnimationSpec: AnimationSpec<Color> = snap()
        private set

    fun width(width: Dp) {
        this.width = width
    }

    fun minRowHeight(height: Dp) {
        minRowHeight = height
    }

    fun shape(shape: Shape) {
        this.shape = shape
    }

    fun contentPadding(padding: PaddingValues) {
        contentPadding = padding
    }

    fun containerColor(color: Color) {
        containerColor = color
    }

    fun separatorColor(color: Color) {
        separatorColor = color
    }

    fun hintColor(color: Color) {
        hintColor = color
    }

    fun chevronSize(size: Dp) {
        chevronSize = size
    }

    fun countryTextStyle(style: TextStyle) {
        countryTextStyle = style
    }

    fun numberTextStyle(style: TextStyle) {
        numberTextStyle = style
    }

    fun indicatorThickness(thickness: Dp) {
        indicatorThickness = thickness
    }

    fun focusedIndicatorColor(color: Color) {
        focusedIndicatorColor = color
    }

    fun errorIndicatorColor(color: Color) {
        errorIndicatorColor = color
    }

    fun colorAnimationSpec(spec: AnimationSpec<Color>) {
        colorAnimationSpec = spec
    }
}
