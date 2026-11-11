package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.GlassIconButton
import com.xbot.kotogram.core.designsystem.component.IconButton
import com.xbot.kotogram.core.designsystem.component.ListItem
import com.xbot.kotogram.core.designsystem.component.TopBar
import com.xbot.kotogram.core.designsystem.component.TopBarStyle
import com.xbot.kotogram.core.designsystem.component.rememberTopBarScrollBehavior
import com.xbot.kotogram.core.designsystem.component.topBarScrollSource
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons

internal val TopBarPage = CatalogPage(
    id = "top-bar",
    section = CatalogSection.Components,
    name = "TopBar",
    description = "A title between a navigation button and actions. Material 3's top app bar by default; on Apple's " +
        "platforms a bar with UIKit's sizes, a centred title and, under it, the scroll edge effect of iOS 27: " +
        "what scrolls beneath is blurred, tinted with the screen's colour and closed by a hairline. The buttons " +
        "are the caller's: GlassIconButton for a bar over content, as a sheet's close button on iOS 26.",
    examples = listOf(
        CatalogExample("Content scrolls under the bar") {
            ScrolledUnder()
        },
        CatalogExample("Sheet: close and title") {
            TopBar(
                title = { Text("Country") },
                modifier = Modifier.fillMaxWidth(),
                navigation = { GlassIconButton(KotogramIcons.Cross, contentDescription = "Close", onClick = {}) },
            )
        },
        CatalogExample("Screen: back, title and actions") {
            TopBar(
                title = { Text("Contacts") },
                modifier = Modifier.fillMaxWidth(),
                navigation = { IconButton(KotogramIcons.Back, contentDescription = "Back", onClick = {}) },
                actions = {
                    IconButton(KotogramIcons.Search, contentDescription = "Search", onClick = {})
                    IconButton(KotogramIcons.More, contentDescription = "More", onClick = {})
                },
            )
        },
        CatalogExample("Title at the start, as on Android") {
            TopBar(
                title = { Text("Contacts") },
                modifier = Modifier.fillMaxWidth(),
                navigation = { IconButton(KotogramIcons.Back, contentDescription = "Back", onClick = {}) },
                actions = { IconButton(KotogramIcons.Search, contentDescription = "Search", onClick = {}) },
                style = TopBarStyle { centeredTitle(false) },
            )
        },
        CatalogExample("A long title") {
            TopBar(
                title = {
                    Text(
                        text = "A title long enough to be cut short before the actions",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                navigation = { IconButton(KotogramIcons.Back, contentDescription = "Back", onClick = {}) },
                actions = { IconButton(KotogramIcons.More, contentDescription = "More", onClick = {}) },
            )
        },
    ),
)

@Composable
private fun ScrolledUnder() {
    val state = rememberLazyListState()
    val behavior = rememberTopBarScrollBehavior(state)
    var barHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    Box(Modifier.fillMaxWidth().height(SCROLLED_HEIGHT).background(KotogramTheme.colors.surface)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().topBarScrollSource(behavior),
            state = state,
            contentPadding = PaddingValues(top = barHeight),
        ) {
            items(SCROLLED_ROWS) { index ->
                ListItem(headlineContent = { Text("Row ${index + 1}") }, modifier = Modifier.fillMaxWidth())
            }
        }
        TopBar(
            title = { Text("Chats") },
            modifier = Modifier.fillMaxWidth().onSizeChanged { barHeight = with(density) { it.height.toDp() } },
            windowInsets = WindowInsets(),
            scrollBehavior = behavior,
        )
    }
}

private val SCROLLED_HEIGHT = 240.dp
private const val SCROLLED_ROWS = 30
