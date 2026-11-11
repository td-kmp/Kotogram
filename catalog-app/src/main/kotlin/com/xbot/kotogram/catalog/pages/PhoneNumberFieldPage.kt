package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.component.PhoneNumberField

internal val PhoneNumberFieldPage = CatalogPage(
    id = "phone-number-field",
    section = CatalogSection.Components,
    name = "PhoneNumberField",
    description = "A country and a number in one card: a ListItem that opens the country list over a TextField " +
        "without a fill of its own, in the text field's sizes and with its ring on touch screens. One " +
        "TextFieldState holds the digits of the calling code and the number, and what is left of the country's " +
        "pattern shows under the text. Here every number is Uzbekistan's.",
    examples = listOf(
        CatalogExample("A country and its pattern") { SamplePhoneNumberField("998") },
        CatalogExample("Typing") { SamplePhoneNumberField("99890123") },
        CatalogExample("Complete") { SamplePhoneNumberField("998901234567") },
        CatalogExample("No country yet") { SamplePhoneNumberField("") },
        CatalogExample("A code no country has") { SamplePhoneNumberField("999") },
        CatalogExample("An error: a red ring on touch screens") {
            SamplePhoneNumberField("998901234567", isError = true)
        },
        CatalogExample("Read-only while the number is sent") {
            SamplePhoneNumberField("998901234567", readOnly = true)
        },
        CatalogExample("Focused: a ring on touch screens, none on a laptop") {
            SamplePhoneNumberField("99890", focused = true)
        },
    ),
)

@Composable
private fun SamplePhoneNumberField(
    digits: String,
    isError: Boolean = false,
    readOnly: Boolean = false,
    focused: Boolean = false,
) {
    val state = rememberTextFieldState(digits)
    val requester = remember { FocusRequester() }
    val known = state.text.startsWith(CALLING_CODE)
    PhoneNumberField(
        country = when {
            known -> COUNTRY
            state.text.isEmpty() -> ""
            else -> "Invalid Country Code"
        },
        onCountryClick = {},
        state = state,
        callingCodeLength = if (known) CALLING_CODE.length else state.text.length,
        numberPattern = if (known) pattern(state.text.drop(CALLING_CODE.length)) else "",
        numberPlaceholder = "phone number",
        onSubmit = {},
        countryPlaceholder = "country",
        readOnly = readOnly,
        isError = isError,
        focusRequester = requester,
    )
    if (focused) LaunchedEffect(requester) { requester.requestFocus() }
}

private fun pattern(number: CharSequence): String {
    var next = 0
    return PATTERN.map { slot -> if (slot == '-' && next < number.length) number[next++] else slot }.joinToString("")
}

private const val COUNTRY = "🇺🇿 Uzbekistan"
private const val CALLING_CODE = "998"
private const val PATTERN = "-- --- -- --"
