package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.component.GlassIconButton
import com.xbot.kotogram.core.designsystem.component.IconButton
import com.xbot.kotogram.core.designsystem.component.TopBar
import com.xbot.kotogram.core.designsystem.component.TopBarStyle
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons

internal val TopBarPage = CatalogPage(
    id = "top-bar",
    section = CatalogSection.Components,
    name = "TopBar",
    description = "A title between a navigation button and actions, with no background of its own. Apple's " +
        "platforms centre the title, as UIKit's navigation bar does, and Material starts it after the navigation " +
        "button: the style's centeredTitle, which defaults to the platform's. The buttons are the caller's: " +
        "GlassIconButton for a bar over content, as a sheet's close button on iOS 26.",
    examples = listOf(
        CatalogExample("Sheet: close and title") {
            TopBar(
                title = { Text("Country") },
                modifier = Modifier.fillMaxWidth(),
                navigation = { GlassIconButton(KotogramIcons.Cross, contentDescription = "Close", onClick = {}) }
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
                }
            )
        },
        CatalogExample("Title at the start, as on Android") {
            TopBar(
                title = { Text("Contacts") },
                modifier = Modifier.fillMaxWidth(),
                navigation = { IconButton(KotogramIcons.Back, contentDescription = "Back", onClick = {}) },
                actions = { IconButton(KotogramIcons.Search, contentDescription = "Search", onClick = {}) },
                style = TopBarStyle { centeredTitle(false) }
            )
        },
        CatalogExample("A long title") {
            TopBar(
                title = {
                    Text(
                        text = "A title long enough to be cut short before the actions",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                navigation = { IconButton(KotogramIcons.Back, contentDescription = "Back", onClick = {}) },
                actions = { IconButton(KotogramIcons.More, contentDescription = "More", onClick = {}) }
            )
        }
    )
)
