package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.ProgressIndicator
import com.xbot.kotogram.core.designsystem.component.ProgressIndicatorStyle

internal val ProgressIndicatorPage = CatalogPage(
    id = "progress-indicator",
    section = CatalogSection.Components,
    name = "ProgressIndicator",
    description = "An indeterminate spinner, as large as its modifier makes it (20 without a size): UIKit's " +
        "UIActivityIndicatorView on iOS, AppKit's NSProgressIndicator on macOS, Material 3's " +
        "CircularProgressIndicator elsewhere. Native spinners are medium or small up to 32 and large above, and " +
        "draw their own stroke; the style's stroke width is Material's.",
    examples = listOf(
        CatalogExample("Sizes") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.large),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProgressIndicator()
                ProgressIndicator(Modifier.size(40.dp))
                ProgressIndicator(Modifier.size(40.dp), style = ProgressIndicatorStyle { strokeWidth(4.dp) })
            }
        },
        CatalogExample("On the primary colour") {
            Box(
                Modifier
                    .background(KotogramTheme.colors.primary, KotogramTheme.shapes.medium)
                    .padding(KotogramTheme.spacing.medium),
            ) {
                ProgressIndicator(style = ProgressIndicatorStyle { color(theme.colors.onPrimary) })
            }
        },
    ),
)
