package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme

internal val SpacingPage = CatalogPage(
    id = "spacing",
    section = CatalogSection.Foundations,
    name = "Spacing",
    description = "KotogramTheme.spacing: the gaps Telegram for macOS uses on its sign-in screens and in its lists. " +
        "Material 3 has no spacing of its own.",
    examples = listOf(
        CatalogExample("Scale") {
            val spacing = KotogramTheme.spacing
            Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                SpacingSample("extraSmall", spacing.extraSmall)
                SpacingSample("small", spacing.small)
                SpacingSample("medium", spacing.medium)
                SpacingSample("large", spacing.large)
                SpacingSample("extraLarge", spacing.extraLarge)
            }
        },
    ),
)

@Composable
private fun SpacingSample(
    name: String,
    value: Dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.large),
    ) {
        Text(name, style = KotogramTheme.typography.bodyMedium, modifier = Modifier.width(LabelWidth))
        Box(Modifier.width(MaxSample), contentAlignment = Alignment.CenterStart) {
            Box(Modifier.size(value).background(KotogramTheme.colors.primary))
        }
        Text(
            text = "${value.value.toInt()} dp",
            style = KotogramTheme.typography.bodySmall,
            color = KotogramTheme.colors.onSurfaceVariant,
        )
    }
}

private val LabelWidth = 120.dp
private val MaxSample = 40.dp
