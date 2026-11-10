package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
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
import com.xbot.kotogram.core.designsystem.component.ListItem
import com.xbot.kotogram.core.designsystem.component.ListItemPosition
import com.xbot.kotogram.core.designsystem.component.Switch
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons

internal val ListItemPage = CatalogPage(
    id = "list-item",
    section = CatalogSection.Components,
    name = "ListItem",
    description = "Material 3's slots (overline, headline, supporting, leading, trailing) with the insets of " +
        "Telegram for macOS's rows (GeneralRowItem: 12 and 14). A position groups rows as macOS's settings do: " +
        "rounded ends and a divider under the text.",
    examples = listOf(
        CatalogExample("Grouped, as in settings") { SettingsGroup() },
        CatalogExample("Plain, as in a list of chats or countries") {
            ListItem(
                headlineContent = { Text("Uzbekistan") },
                leadingContent = { Text("🇺🇿") },
                trailingContent = { Text("+998") },
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        },
        CatalogExample("Overline and two lines") {
            ListItem(
                overlineContent = { Text("Username") },
                headlineContent = { Text("@kotogram") },
                supportingContent = { Text("Others can find you by it and message you without your number.") },
                leadingContent = { Icon(KotogramIcons.AtSign, contentDescription = null) },
                position = ListItemPosition.Single,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        CatalogExample("Disabled") {
            ListItem(
                headlineContent = { Text("Delete account") },
                leadingContent = { Icon(KotogramIcons.Delete, contentDescription = null) },
                onClick = {},
                enabled = false,
                position = ListItemPosition.Single,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ),
)

@Composable
private fun SettingsGroup() {
    var sync by remember { mutableStateOf(true) }
    Column(
        Modifier
            .fillMaxWidth()
            .background(KotogramTheme.colors.surfaceDim, KotogramTheme.shapes.medium)
            .padding(KotogramTheme.spacing.small),
    ) {
        ListItem(
            headlineContent = { Text("Notifications") },
            leadingContent = { Icon(KotogramIcons.Bell, contentDescription = null) },
            trailingContent = { Icon(KotogramIcons.ChevronRight, contentDescription = null) },
            onClick = {},
            position = ListItemPosition.First,
        )
        ListItem(
            headlineContent = { Text("Language") },
            leadingContent = { Icon(KotogramIcons.Globe, contentDescription = null) },
            trailingContent = { Text("English") },
            onClick = {},
            position = ListItemPosition.Middle,
        )
        ListItem(
            headlineContent = { Text("Sync contacts") },
            leadingContent = { Icon(KotogramIcons.Person, contentDescription = null) },
            trailingContent = { Switch(checked = sync, onCheckedChange = { sync = it }) },
            position = ListItemPosition.Last,
        )
    }
}
