package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.component.Button
import com.xbot.kotogram.core.designsystem.component.OptionLabel
import com.xbot.kotogram.core.designsystem.component.OptionPicker

internal val OptionPickerPage = CatalogPage(
    id = "option-picker",
    section = CatalogSection.Components,
    name = "OptionPicker",
    description = "One of many. On a laptop a menu, AppKit's on macOS. On touch screens a sheet made of the design " +
        "system's parts: BottomSheet, UIKit's page sheet on iOS; TopBar with a GlassIconButton to close it; " +
        "SearchField, at the bottom on iOS 26 and at the top elsewhere; ListItem rows under sticky section " +
        "headers, with the platform's FastScroller. The selected option opens in sight with a check; a search " +
        "lists the matches alone, the match in semibold.",
    examples = listOf(CatalogExample("A country, as at sign-in") { CountryPicker() }),
)

@Composable
private fun CountryPicker() {
    var expanded by remember { mutableStateOf(false) }
    var country by remember { mutableStateOf(SampleCountries.first { it.englishName == "Uzbekistan" }) }
    Box {
        Button(onClick = { expanded = true }) { Text("${country.flag} ${country.name}  ${country.code}") }
        OptionPicker(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            options = SampleCountries,
            optionLabel = { OptionLabel(it.name, leading = it.flag, supporting = it.englishName, trailing = it.code) },
            onSelect = { country = it },
            title = "Страна",
            selected = country,
            optionSection = { it.name.take(1) },
            searchPlaceholder = "Поиск",
        )
    }
}
