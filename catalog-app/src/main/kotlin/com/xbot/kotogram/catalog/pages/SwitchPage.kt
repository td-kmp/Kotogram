package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.ListItem
import com.xbot.kotogram.core.designsystem.component.Switch

internal val SwitchPage = CatalogPage(
    id = "switch",
    section = CatalogSection.Components,
    name = "Switch",
    description = "UIKit's UISwitch on iOS and AppKit's NSSwitch on macOS, at their own sizes; UIKit's takes the " +
        "style's track colour, AppKit's the system accent. Elsewhere Compose draws Material 3's switch on touch " +
        "screens and a smaller one on a laptop. A change the caller doesn't take goes back.",
    examples = listOf(
        CatalogExample("Press to toggle") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.large),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Toggle(initial = false)
                Toggle(initial = true)
            }
        },
        CatalogExample("Disabled") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.large),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(checked = false, onCheckedChange = {}, enabled = false)
                Switch(checked = true, onCheckedChange = {}, enabled = false)
            }
        },
        CatalogExample("Turns down every change") {
            Switch(checked = false, onCheckedChange = {})
        },
        CatalogExample("In a list") {
            Column {
                listOf("Notifications", "Sounds", "Previews").forEachIndexed { index, name ->
                    var checked by remember { mutableStateOf(index != 1) }
                    ListItem(
                        headlineContent = { Text(name) },
                        trailingContent = { Switch(checked = checked, onCheckedChange = { checked = it }) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
    ),
)

@Composable
private fun Toggle(initial: Boolean) {
    var checked by remember { mutableStateOf(initial) }
    Switch(checked = checked, onCheckedChange = { checked = it })
}
