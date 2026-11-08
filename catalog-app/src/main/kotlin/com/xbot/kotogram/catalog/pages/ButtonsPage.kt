package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.Button
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.ProminentButton
import com.xbot.kotogram.core.designsystem.component.TextButton
import kotlinx.coroutines.delay

internal val ButtonsPage = CatalogPage(
    id = "buttons",
    section = CatalogSection.Components,
    name = "Buttons",
    description = "Telegram for macOS's sign-in button on a laptop (Auth_NextView: a pill at least 36 high, 15 on " +
        "each side, 14 medium) and Telegram for iOS's on touch screens (50 high, 17 semibold). ButtonState is " +
        "Enabled, Disabled or Loading; a loading button keeps its colours and width. " +
        "ProminentButton is a screen's main action: this filled button, and UIKit's prominent glass button on " +
        "iOS 26 and later.",
    examples = listOf(
        CatalogExample("Filled") { States { state -> Button(onClick = {}, state = state) { Text("Next") } } },
        CatalogExample("Prominent") { States { state -> ProminentButton("Continue", onClick = {}, state = state) } },
        CatalogExample("Text") {
            States { state -> TextButton(onClick = {}, state = state) { Text("Forgot password?") } }
        },
        CatalogExample("Loads for two seconds when pressed") { LoadingOnPress() }
    )
)

@Composable
private fun States(button: @Composable (ButtonState) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.large)) {
        ButtonState.entries.forEach { button(it) }
    }
}

@Composable
private fun LoadingOnPress() {
    var loading by remember { mutableStateOf(false) }
    LaunchedEffect(loading) {
        if (loading) {
            delay(LOADING_MILLIS)
            loading = false
        }
    }
    ProminentButton(
        text = "Sign in",
        onClick = { loading = true },
        state = ButtonState.of(enabled = true, loading = loading)
    )
}

private const val LOADING_MILLIS = 2_000L
