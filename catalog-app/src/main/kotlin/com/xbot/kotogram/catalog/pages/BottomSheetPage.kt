package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.BottomSheet
import com.xbot.kotogram.core.designsystem.component.Button
import com.xbot.kotogram.core.designsystem.component.GlassIconButton
import com.xbot.kotogram.core.designsystem.component.ListItem
import com.xbot.kotogram.core.designsystem.component.SearchField
import com.xbot.kotogram.core.designsystem.component.TopBar
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons

internal val BottomSheetPage = CatalogPage(
    id = "bottom-sheet",
    section = CatalogSection.Components,
    name = "BottomSheet",
    description = "A modal sheet, shown while it is composed, in the screen's background colour. On iOS it is " +
        "UIKit's page sheet with the content in a composition of its own, which gets the screen's composition " +
        "locals; elsewhere it is Material 3's modal bottom sheet, expanded. The content brings its own top bar, " +
        "with no drag handle, and closes the sheet with dismiss(), which hides it the way a swipe does.",
    examples = [CatalogExample("Opens on press") { SheetOnPress() }],
)

@Composable
private fun SheetOnPress() {
    var open by remember { mutableStateOf(false) }
    Button(onClick = { open = true }) { Text("Choose a country") }
    if (open) {
        BottomSheet(onDismissRequest = { open = false }) {
            TopBar(
                title = { Text("Country") },
                modifier = Modifier.fillMaxWidth(),
                navigation = {
                    GlassIconButton(KotogramIcons.Cross, contentDescription = "Close", onClick = ::dismiss)
                },
            )
            SearchField(
                state = rememberTextFieldState(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = KotogramTheme.spacing.large),
                placeholder = "Search",
            )
            listOf("🇺🇿" to "Uzbekistan", "🇰🇿" to "Kazakhstan", "🇷🇺" to "Russia").forEach { country ->
                ListItem(
                    headlineContent = { Text(country.second) },
                    leadingContent = { Text(country.first) },
                    onClick = ::dismiss,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
