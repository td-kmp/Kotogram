package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.animation.rememberNavTransitions
import com.xbot.kotogram.core.designsystem.component.Checkbox
import com.xbot.kotogram.core.designsystem.component.GlassIconButton
import com.xbot.kotogram.core.designsystem.component.ProgressIndicator
import com.xbot.kotogram.core.designsystem.component.ProminentButton
import com.xbot.kotogram.core.designsystem.component.Switch
import com.xbot.kotogram.core.designsystem.component.TextButton
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import kotlinx.coroutines.delay

internal val TransitionsPage = CatalogPage(
    id = "transitions",
    section = CatalogSection.Foundations,
    name = "Transitions",
    description = "The platform's screen transitions (rememberNavTransitions) between screens made of native " +
        "controls. On macOS and iOS those lie over the Compose canvas, so nothing Compose draws covers them: the " +
        "transition clips the screen underneath to the part still seen, and the controls are cut with it. " +
        "-Dcatalog.autoplay=true pushes and pops by itself.",
    examples = listOf(CatalogExample("Push and pop") { TransitionsExample() }),
)

@Composable
private fun TransitionsExample() {
    val backStack = remember { mutableStateListOf(0) }
    val transitions = rememberNavTransitions<Scene<Int>>()
    val pop: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    if (System.getProperty("catalog.autoplay").toBoolean()) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(AUTOPLAY_PAUSE_MILLIS)
                backStack.add(backStack.size)
                delay(AUTOPLAY_PAUSE_MILLIS)
                pop()
            }
        }
    }
    Box(Modifier.fillMaxWidth().height(ExampleHeight).clipToBounds()) {
        NavDisplay(
            backStack = backStack,
            onBack = pop,
            transitionSpec = transitions.push,
            popTransitionSpec = transitions.pop,
            predictivePopTransitionSpec = transitions.predictivePop,
            entryProvider = entryProvider {
                entry<Int> { depth ->
                    TransitionScreen(
                        depth = depth,
                        onNext = { backStack.add(backStack.size) },
                        onBack = pop.takeIf { depth > 0 },
                    )
                }
            },
        )
    }
}

@Composable
private fun TransitionScreen(
    depth: Int,
    onNext: () -> Unit,
    onBack: (() -> Unit)?,
) {
    val colors = KotogramTheme.colors
    val spacing = KotogramTheme.spacing
    val background = if (depth % 2 == 0) colors.surface else colors.surfaceContainerHighest
    Column(
        modifier = Modifier.fillMaxSize().background(background).padding(spacing.large),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            GlassIconButton(KotogramIcons.Back, "Back", onClick = { onBack?.invoke() }, enabled = onBack != null)
            Text("Screen $depth", style = KotogramTheme.typography.titleMedium)
            GlassIconButton(KotogramIcons.Search, "Search", onClick = {})
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Toggles()
            ProgressIndicator()
            Toggles()
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            ProminentButton("Next", onClick = onNext)
            Box(Modifier.background(Marker).padding(spacing.small)) { TextButton(onClick = onNext) { Text("Compose") } }
            ProminentButton("Next", onClick = onNext)
        }
    }
}

@Composable
private fun Toggles() {
    Row(horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.small)) {
        var switched by remember { mutableStateOf(true) }
        var checked by remember { mutableStateOf(true) }
        Switch(checked = switched, onCheckedChange = { switched = it })
        Checkbox(checked = checked, onCheckedChange = { checked = it })
    }
}

private val ExampleHeight = 260.dp

private val Marker = Color(0x33FF9500)

private const val AUTOPLAY_PAUSE_MILLIS = 1500L
