package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme

internal val ShapesPage = CatalogPage(
    id = "shapes",
    section = CatalogSection.Foundations,
    name = "Shapes",
    description = "Material 3's shapes with Telegram for macOS's radius of 10 for fields, cards and the QR code. " +
        "Buttons keep full corners, as on macOS.",
    examples = listOf(
        CatalogExample("Corners") {
            val shapes = KotogramTheme.shapes
            val spacing = KotogramTheme.spacing
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.large),
                verticalArrangement = Arrangement.spacedBy(spacing.large)
            ) {
                ShapeSample("extraSmall", shapes.extraSmall)
                ShapeSample("small", shapes.small)
                ShapeSample("medium · 10", shapes.medium)
                ShapeSample("large", shapes.large)
                ShapeSample("extraLarge", shapes.extraLarge)
                ShapeSample("full · buttons", CircleShape)
            }
        }
    )
)

@Composable
private fun ShapeSample(name: String, shape: Shape) {
    Column(verticalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.extraSmall)) {
        Box(
            Modifier
                .size(width = 120.dp, height = 72.dp)
                .background(KotogramTheme.colors.surfaceContainerHighest, shape)
                .border(1.dp, KotogramTheme.colors.outlineVariant, shape)
        )
        Text(name, style = KotogramTheme.typography.bodyMedium)
    }
}
