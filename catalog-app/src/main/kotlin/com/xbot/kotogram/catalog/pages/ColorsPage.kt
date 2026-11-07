package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme

internal val ColorsPage = CatalogPage(
    id = "colors",
    section = CatalogSection.Foundations,
    name = "Colors",
    description = "Material 3's roles, filled with Telegram for macOS's Day Classic and Night Accent palettes. " +
        "On Android 12 and later the system's dynamic colours take their place.",
    examples = listOf(
        CatalogExample("Accent") {
            val colors = KotogramTheme.colors
            ColorRoles(
                "primary" to colors.primary,
                "onPrimary" to colors.onPrimary,
                "primaryContainer" to colors.primaryContainer,
                "secondary" to colors.secondary,
                "tertiary" to colors.tertiary,
                "inversePrimary" to colors.inversePrimary
            )
        },
        CatalogExample("Surfaces") {
            val colors = KotogramTheme.colors
            ColorRoles(
                "surface" to colors.surface,
                "surfaceDim" to colors.surfaceDim,
                "surfaceBright" to colors.surfaceBright,
                "surfaceContainerLowest" to colors.surfaceContainerLowest,
                "surfaceContainerLow" to colors.surfaceContainerLow,
                "surfaceContainer" to colors.surfaceContainer,
                "surfaceContainerHigh" to colors.surfaceContainerHigh,
                "surfaceContainerHighest" to colors.surfaceContainerHighest,
                "inverseSurface" to colors.inverseSurface
            )
        },
        CatalogExample("Content") {
            val colors = KotogramTheme.colors
            ColorRoles(
                "onSurface" to colors.onSurface,
                "onSurfaceVariant" to colors.onSurfaceVariant,
                "outline" to colors.outline,
                "outlineVariant" to colors.outlineVariant,
                "inverseOnSurface" to colors.inverseOnSurface
            )
        },
        CatalogExample("Error") {
            val colors = KotogramTheme.colors
            ColorRoles(
                "error" to colors.error,
                "onError" to colors.onError,
                "errorContainer" to colors.errorContainer,
                "onErrorContainer" to colors.onErrorContainer
            )
        }
    )
)

@Composable
private fun ColorRoles(vararg roles: Pair<String, Color>) {
    val spacing = KotogramTheme.spacing
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.large)
    ) {
        roles.forEach { (name, color) -> ColorSwatch(name, color) }
    }
}

@Composable
private fun ColorSwatch(name: String, color: Color) {
    val shape = KotogramTheme.shapes.medium
    Column(
        modifier = Modifier.width(SwatchWidth),
        verticalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.extraSmall)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(SwatchHeight)
                .background(color, shape)
                .border(1.dp, KotogramTheme.colors.outlineVariant, shape)
        )
        Text(name, style = KotogramTheme.typography.bodyMedium)
        Text(
            text = hex(color),
            style = KotogramTheme.typography.bodySmall,
            color = KotogramTheme.colors.onSurfaceVariant
        )
    }
}

private fun hex(color: Color): String = "#%06X".format(color.toArgb() and RGB_MASK)

private val SwatchWidth = 150.dp
private val SwatchHeight = 48.dp
private const val RGB_MASK = 0xFFFFFF
