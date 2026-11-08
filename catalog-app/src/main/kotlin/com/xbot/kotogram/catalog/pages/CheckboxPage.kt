package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.Checkbox
import com.xbot.kotogram.core.designsystem.component.CheckboxStyle
import com.xbot.kotogram.core.designsystem.component.ListItem

internal val CheckboxPage = CatalogPage(
    id = "checkbox",
    section = CatalogSection.Components,
    name = "Checkbox",
    description = "AppKit's checkbox on macOS, at its own size. Elsewhere Compose draws it, the check drawing itself " +
        "in: Material 3's 18 rounded square on touch screens, the circle of a selected row on iOS, since UIKit " +
        "has a checkbox only in Mac Catalyst, and a 14 square on a laptop.",
    examples = listOf(
        CatalogExample("Press to toggle") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.large),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Toggle(initial = false)
                Toggle(initial = true)
            }
        },
        CatalogExample("Material's square, as on Android") {
            var checked by remember { mutableStateOf(true) }
            Checkbox(checked = checked, onCheckedChange = { checked = it }, style = MaterialCheckbox)
        },
        CatalogExample("Disabled") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.large),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = false, onCheckedChange = {}, enabled = false)
                Checkbox(checked = true, onCheckedChange = {}, enabled = false)
            }
        },
        CatalogExample("In a list") {
            Column {
                listOf("Photos", "Videos", "Files").forEachIndexed { index, name ->
                    var checked by remember { mutableStateOf(index == 0) }
                    ListItem(
                        headlineContent = { Text(name) },
                        leadingContent = { Checkbox(checked = checked, onCheckedChange = null) },
                        onClick = { checked = !checked },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
)

// What a touch screen gets off Apple's platforms, for a look at it on a Mac.
private val MaterialCheckbox = CheckboxStyle {
    size(18.dp)
    shape(RoundedCornerShape(2.dp))
    borderWidth(2.dp)
    checkWidth(2.dp)
}

@Composable
private fun Toggle(initial: Boolean) {
    var checked by remember { mutableStateOf(initial) }
    Checkbox(checked = checked, onCheckedChange = { checked = it })
}
