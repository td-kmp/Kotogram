package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.TextField
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons

internal val TextFieldPage = CatalogPage(
    id = "text-field",
    section = CatalogSection.Components,
    name = "TextField",
    description = "Telegram for macOS's input over BasicTextField on a laptop, larger and with a focus ring on " +
        "touch screens. It takes a TextFieldState, plus a title and a suggestion above the field.",
    examples = listOf(
        CatalogExample("Placeholder") {
            TextField(state = rememberTextFieldState(), placeholder = { Text("Enter a username") })
        },
        CatalogExample("Title, suggestion and description") {
            val state = rememberTextFieldState()
            TextField(
                state = state,
                title = { Text("Username") },
                suggestion = { Suggestions(state, "kotogram_cat", "kotogram42") },
                placeholder = { Text("Enter a username") },
                prefix = { Text("@") },
                supportingText = { Text("You can use a–z, 0–9 and underscores.") }
            )
        },
        CatalogExample("Error") {
            TextField(
                state = rememberTextFieldState("admin"),
                prefix = { Text("@") },
                isError = true,
                supportingText = { Text("This username is already taken.") }
            )
        },
        CatalogExample("Prefix and suffix") {
            TextField(
                state = rememberTextFieldState("90 123 45 67"),
                title = { Text("Phone number") },
                prefix = { Text("+998") },
                suffix = { Text("UZ") }
            )
        },
        CatalogExample("Icons") {
            val state = rememberTextFieldState("Kotogram")
            TextField(
                state = state,
                placeholder = { Text("Search") },
                leadingIcon = { Icon(KotogramIcons.Search, contentDescription = null) },
                trailingIcon = { ClearButton(state) }
            )
        },
        CatalogExample("Several lines") {
            TextField(
                state = rememberTextFieldState(
                    "A few words about yourself that run over a line, then another, and one more to show the growth."
                ),
                modifier = Modifier.width(280.dp),
                title = { Text("Bio") },
                lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 1, maxHeightInLines = 4),
                supportingText = { Text("70 characters left.") }
            )
        },
        CatalogExample("Disabled") {
            TextField(state = rememberTextFieldState("Disabled"), enabled = false)
        },
        CatalogExample("Focused: a ring on touch screens, none on a laptop") {
            FocusedTextField()
        }
    )
)

@Composable
private fun Suggestions(state: TextFieldState, vararg names: String) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.extraSmall)) {
        Text("Try")
        names.forEach { name ->
            Text(
                text = name,
                color = KotogramTheme.colors.primary,
                modifier = Modifier.clickable { state.setTextAndPlaceCursorAtEnd(name) }
            )
        }
    }
}

@Composable
private fun ClearButton(state: TextFieldState) {
    if (state.text.isEmpty()) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .clickable { state.clearText() },
        contentAlignment = Alignment.Center
    ) {
        Icon(KotogramIcons.Clear, contentDescription = "Clear")
    }
}

@Composable
private fun FocusedTextField() {
    val requester = remember { FocusRequester() }
    TextField(
        state = rememberTextFieldState("kotogram"),
        modifier = Modifier.focusRequester(requester),
        title = { Text("Username") }
    )
    LaunchedEffect(requester) { requester.requestFocus() }
}
