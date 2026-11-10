package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    readOnly: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() },
    style: PhoneNumberFieldStyle = PhoneNumberFieldStyle,
    countryMenu: @Composable () -> Unit = {},
) {
    @Suppress("NAME_SHADOWING")
    val style = rememberStyle(::PhoneNumberFieldStyleScope, PhoneNumberFieldStyle.Adaptive then style)
    val format = remember(callingCodeLength, numberPattern) {
        PhoneNumberFormat(callingCodeLength, numberPattern)
    }
    context(style) {
        Box(modifier = modifier.width(style.width).background(style.containerColor, style.shape)) {
            Column {
                Box(Modifier.fillMaxWidth().height(style.countryHeight)) {
                    CountryRow(country = country, onClick = onCountryClick, enabled = !readOnly)
                    countryMenu()
                }
                BasicTextField(
                    state = state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(style.numberHeight)
                        .padding(horizontal = style.inset)
                        .focusRequester(focusRequester),
                    readOnly = readOnly,
                    inputTransformation = DigitsOnly,
                    textStyle = style.numberTextStyle,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                    onKeyboardAction = { onSubmit() },
                    lineLimits = TextFieldLineLimits.SingleLine,
                    cursorBrush = SolidColor(style.cursorColor),
                    outputTransformation = format,
                    decorator = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            Text(
                                text = format.hint(state.text, numberPlaceholder),
                                style = style.numberTextStyle.copy(color = style.hintColor),
                                maxLines = 1,
                            )
                            innerTextField()
                        }
                    },
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(start = style.inset, top = style.countryHeight, end = style.inset),
                thickness = Dp.Hairline,
                color = style.separatorColor,
            )
        }
    }
}

@Composable
context(style: PhoneNumberFieldStyleScope)
private fun CountryRow(
    country: String,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(style.countryHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = KotogramTheme.indication,
                enabled = enabled,
                role = Role.DropdownList,
                onClick = onClick,
            )
            .padding(horizontal = style.inset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = country,
            style = style.countryTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = KotogramIcons.ChevronRight,
            contentDescription = null,
            tint = style.chevronColor,
            modifier = Modifier.size(style.chevronSize),
        )
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

    fun hint(
        digits: CharSequence,
        placeholder: String,
    ): AnnotatedString {
        val code = digits.length.coerceAtMost(callingCodeLength)
        if (code == 0) return AnnotatedString("")
        val prefix = "+${digits.subSequence(0, code)} "
        val rest = if (pattern.isEmpty() && digits.length == code) placeholder else pattern
        val typed = (format(digits).length - prefix.length).coerceIn(0, rest.length)
        return buildAnnotatedString {
            withStyle(SpanStyle(color = Color.Transparent)) { append(prefix + rest.take(typed)) }
            append(rest.drop(typed))
        }
    }

    private fun format(digits: CharSequence): String = buildString {
        val code = digits.length.coerceAtMost(callingCodeLength)
        append('+').append(digits, 0, code)
        if (digits.length == code) return@buildString
        append(' ')
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

@Suppress("MagicNumber")
fun interface PhoneNumberFieldStyle : ComponentStyle<PhoneNumberFieldStyleScope> {
    companion object : PhoneNumberFieldStyle {
        override fun PhoneNumberFieldStyleScope.applyStyle() {}

        val Default = PhoneNumberFieldStyle {
            width(280.dp)
            shape(theme.shapes.medium)
            containerColor(theme.colors.surfaceVariant)
            separatorColor(theme.colors.outline.copy(alpha = 0.12f))
            chevronColor(theme.colors.outline)
            cursorColor(theme.colors.primary)
            hintColor(theme.colors.onSurfaceVariant)
            countryHeight(57.dp)
            numberHeight(57.dp)
            inset(16.dp)
            chevronSize(36.dp)
            countryTextStyle(theme.typography.bodyLarge.merge(fontSize = 20.sp, color = theme.colors.onSurface))
            numberTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    color = theme.colors.onSurface,
                ),
            )
        }

        val Adaptive = Default then PhoneNumberFieldStyle {
            laptop {
                countryHeight(40.dp)
                numberHeight(40.dp)
                inset(10.dp)
                chevronSize(30.dp)
                countryTextStyle(theme.typography.bodyLarge.merge(fontSize = 15.sp, color = theme.colors.onSurface))
                numberTextStyle(
                    theme.typography.bodyLarge.merge(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = theme.colors.onSurface,
                    ),
                )
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

class PhoneNumberFieldStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<PhoneNumberFieldStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var width: Dp = Dp.Unspecified
        private set

    var shape: Shape = RectangleShape
        private set

    var containerColor: Color = Color.Unspecified
        private set

    var separatorColor: Color = Color.Unspecified
        private set

    var chevronColor: Color = Color.Unspecified
        private set

    var cursorColor: Color = Color.Unspecified
        private set

    var hintColor: Color = Color.Unspecified
        private set

    var countryHeight: Dp = Dp.Unspecified
        private set

    var numberHeight: Dp = Dp.Unspecified
        private set

    var inset: Dp = Dp.Unspecified
        private set

    var chevronSize: Dp = Dp.Unspecified
        private set

    var countryTextStyle: TextStyle = TextStyle.Default
        private set

    var numberTextStyle: TextStyle = TextStyle.Default
        private set

    fun width(width: Dp) {
        this.width = width
    }

    fun shape(shape: Shape) {
        this.shape = shape
    }

    fun containerColor(color: Color) {
        containerColor = color
    }

    fun separatorColor(color: Color) {
        separatorColor = color
    }

    fun chevronColor(color: Color) {
        chevronColor = color
    }

    fun cursorColor(color: Color) {
        cursorColor = color
    }

    fun hintColor(color: Color) {
        hintColor = color
    }

    fun countryHeight(height: Dp) {
        countryHeight = height
    }

    fun numberHeight(height: Dp) {
        numberHeight = height
    }

    fun inset(inset: Dp) {
        this.inset = inset
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
}
