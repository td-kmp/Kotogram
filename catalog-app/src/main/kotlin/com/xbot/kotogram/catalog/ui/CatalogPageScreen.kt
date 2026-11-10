package com.xbot.kotogram.catalog.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.core.designsystem.KotogramTheme

@Composable
fun CatalogPageScreen(
    page: CatalogPage,
    modifier: Modifier = Modifier,
) {
    val spacing = KotogramTheme.spacing
    Column(
        modifier = modifier.padding(horizontal = spacing.extraLarge, vertical = spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        Text(
            text = page.description,
            style = KotogramTheme.typography.bodyLarge,
            color = KotogramTheme.colors.onSurfaceVariant,
        )
        page.examples.forEach { example -> CatalogExampleCard(example) }
    }
}

@Composable
private fun CatalogExampleCard(
    example: CatalogExample,
    modifier: Modifier = Modifier,
) {
    val spacing = KotogramTheme.spacing
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        Text(example.name, style = KotogramTheme.typography.titleSmall)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, KotogramTheme.colors.outlineVariant, KotogramTheme.shapes.medium)
                .padding(spacing.large),
        ) {
            example.content()
        }
    }
}
