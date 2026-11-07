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
import com.xbot.kotogram.core.designsystem.media.Pc
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PlatformFeature
import com.xbot.kotogram.core.designsystem.media.mediaQuery

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
    countryMenu: @Composable () -> Unit = {}
) {
    val colors = KotogramTheme.colors
    val computer = mediaQuery { PlatformFeature.Pc in platformFeatures }
    val look = if (computer) PhoneNumberFieldLook.Card else PhoneNumberFieldLook.Rows
    val separatorColor = colors.outline.copy(alpha = PhoneNumberFieldTokens.SeparatorAlpha)
    val textStyle = KotogramTheme.typography.bodyLarge
        .merge(look.numberStyle)
        .copy(color = colors.onSurface)
    val format = remember(callingCodeLength, numberPattern) {
        PhoneNumberFormat(callingCodeLength, numberPattern)
    }
    Box(
        modifier = modifier
            .width(PhoneNumberFieldTokens.Width)
            .then(
                if (look.isCard) Modifier.background(colors.surfaceVariant, KotogramTheme.shapes.medium) else Modifier
            )
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(look.countryHeight)) {
                CountryRow(
                    country = country,
                    onClick = onCountryClick,
                    enabled = !readOnly,
                    look = look,
                    color = if (look.isCard) colors.onSurface else colors.primary
                )
                countryMenu()
            }
            BasicTextField(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(look.numberHeight)
                    .padding(horizontal = look.inset)
                    .focusRequester(focusRequester),
                readOnly = readOnly,
                inputTransformation = DigitsOnly,
                textStyle = textStyle,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                onKeyboardAction = { onSubmit() },
                lineLimits = TextFieldLineLimits.SingleLine,
                cursorBrush = SolidColor(colors.primary),
                outputTransformation = format,
                decorator = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = format.hint(state.text, numberPlaceholder),
                            style = textStyle.copy(color = colors.onSurfaceVariant),
                            maxLines = 1
                        )
                        innerTextField()
                    }
                }
            )
        }
        val separators = if (look.isCard) {
            listOf(look.countryHeight)
        } else {
            listOf(0.dp, look.countryHeight, look.countryHeight + look.numberHeight)
        }
        separators.forEach { y ->
            HorizontalDivider(
                modifier = Modifier.padding(start = look.inset, top = y, end = look.inset),
                thickness = Dp.Hairline,
                color = separatorColor
            )
        }
    }
}

@Composable
private fun CountryRow(
    country: String,
    onClick: () -> Unit,
    enabled: Boolean,
    look: PhoneNumberFieldLook,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(look.countryHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = KotogramTheme.indication,
                enabled = enabled,
                role = Role.DropdownList,
                onClick = onClick
            )
            .padding(horizontal = look.inset),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = country,
            style = KotogramTheme.typography.bodyLarge.merge(look.countryStyle),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = KotogramIcons.ChevronRight,
            contentDescription = null,
            tint = KotogramTheme.colors.outline,
            modifier = Modifier.size(look.chevronSize)
        )
    }
}

// macOS's grey card (Auth_PhoneInput) on the desktop, iOS's rows between hairlines
// (AuthorizationSequencePhoneEntryControllerNode) on phones, in their points.
@Suppress("MagicNumber")
private enum class PhoneNumberFieldLook(
    val isCard: Boolean,
    val countryHeight: Dp,
    val numberHeight: Dp,
    val inset: Dp,
    val chevronSize: Dp,
    val countryStyle: TextStyle,
    val numberStyle: TextStyle
) {
    Card(
        isCard = true,
        countryHeight = 40.dp,
        numberHeight = 40.dp,
        inset = 10.dp,
        chevronSize = 30.dp,
        countryStyle = TextStyle(fontSize = 15.sp),
        numberStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
    ),
    Rows(
        isCard = false,
        countryHeight = 67.dp,
        numberHeight = 57.dp,
        inset = 24.dp,
        chevronSize = 36.dp,
        countryStyle = TextStyle(fontSize = 20.sp),
        numberStyle = TextStyle(fontSize = 20.sp, fontFamily = FontFamily.Monospace)
    )
}

// "+", the calling code, a space and the number on TDLib's pattern, where a digit or '-' stands for a digit.
private class PhoneNumberFormat(
    private val callingCodeLength: Int,
    private val pattern: String
) : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        format(originalText).forEachIndexed { index, char ->
            if (!char.isDigit()) insert(index, char.toString())
        }
    }

    // The pattern past what is typed, drawn behind the text as Telegram for macOS's placeholder.
    fun hint(digits: CharSequence, placeholder: String): AnnotatedString {
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

// E.164 allows 15 digits, calling code included.
private const val MAX_DIGITS = 15

private object PhoneNumberFieldTokens {
    // Telegram for macOS's field and M3's TextField minimum; a caller's width constraint overrides it.
    val Width = 280.dp

    // grayIcon over grayBackground at this alpha is Telegram's day border (EAEAEA); its dark palettes set border
    // to grayBackground itself, so the separator and the chevron vanish at night.
    const val SeparatorAlpha = 0.12f
}
