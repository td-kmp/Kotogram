package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.component.CodeField

internal val CodeFieldPage = CatalogPage(
    id = "code-field",
    section = CatalogSection.Components,
    name = "CodeField",
    description = "The sign-in code of Telegram for macOS (CodeEntryControl), in Telegram for iOS's cells on touch " +
        "screens: a cell per digit, the next one marked, digits that spring in, a shake at a wrong code. It takes " +
        "a TextFieldState and the code's length.",
    examples = [
        CatalogExample("Typing") { FocusedCodeField() },
        CatalogExample("Complete") {
            CodeField(state = rememberTextFieldState("123456"), length = 6)
        },
        CatalogExample("Shakes at a wrong code; 12345 is right") {
            val state = rememberTextFieldState()
            CodeField(state = state, length = 5, isError = state.text.length == 5 && state.text != "12345")
        },
        CatalogExample("An error under the cells, as Telegram for macOS shows it") {
            CodeField(
                state = rememberTextFieldState("12345"),
                length = 5,
                isError = true,
                supportingText = { Text("Invalid code. Please try again.") },
            )
        },
        CatalogExample("In a column 280 wide: cells narrow when large text needs more room") {
            Box(Modifier.width(280.dp)) {
                CodeField(state = rememberTextFieldState("123456"), length = 6)
            }
        },
        CatalogExample("Disabled while the code is checked") {
            CodeField(state = rememberTextFieldState("12345"), length = 5, enabled = false)
        },
    ],
)

@Composable
private fun FocusedCodeField() {
    val requester = remember { FocusRequester() }
    CodeField(state = rememberTextFieldState("123"), length = 5, modifier = Modifier.focusRequester(requester))
    LaunchedEffect(requester) { requester.requestFocus() }
}
