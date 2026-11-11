package com.xbot.kotogram.catalog.model

import androidx.compose.runtime.Composable

enum class CatalogSection(
    val title: String,
) {
    Foundations("Foundations"),
    Components("Components"),
}

class CatalogExample(
    val name: String,
    val content: @Composable () -> Unit,
)

class CatalogPage(
    val id: String,
    val section: CatalogSection,
    val name: String,
    val description: String,
    val examples: List<CatalogExample>,
)
