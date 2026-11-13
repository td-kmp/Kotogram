package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.ui.Modifier
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.component.SearchField

internal val SearchFieldPage = CatalogPage(
    id = "search-field",
    section = CatalogSection.Components,
    name = "SearchField",
    description = "A capsule with a magnifier, 48 high on touch screens and 32 on a laptop; a clear button while " +
        "it has text. On iOS 26 and later it is UIKit's search field in a glass capsule, and searching brings a " +
        "glass cancel button out of it. Where it sits is the screen's call: at the bottom on iOS 26, at the top " +
        "elsewhere.",
    examples = [
        CatalogExample("Empty") {
            SearchField(rememberTextFieldState(), Modifier.fillMaxWidth(), placeholder = "Search")
        },
        CatalogExample("With a query") {
            SearchField(rememberTextFieldState("Guin"), Modifier.fillMaxWidth(), placeholder = "Search")
        },
    ],
)
