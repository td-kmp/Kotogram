package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.GlassIconButton
import com.xbot.kotogram.core.designsystem.component.IconButton
import com.xbot.kotogram.core.designsystem.component.IconButtonStyle
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons

internal val IconButtonPage = CatalogPage(
    id = "icon-button",
    section = CatalogSection.Components,
    name = "IconButton",
    description = "An icon in a circle, 44 on touch screens and 32 on a laptop, drawn by Compose everywhere; the " +
        "default has no container and a style gives it one. GlassIconButton is for a bar over content: UIKit's " +
        "and AppKit's glass button on iOS 26 and macOS 26 and later, drawing the icon from the vector, and the " +
        "same IconButton elsewhere.",
    examples = listOf(
        CatalogExample("Standard") { Icons(style = IconButtonStyle) },
        CatalogExample("With a container") {
            Icons(style = IconButtonStyle { containerColor(theme.colors.surfaceContainerHigh) })
        },
        CatalogExample("Disabled") { Icons(style = IconButtonStyle, enabled = false) },
        CatalogExample("GlassIconButton") { GlassIcons(enabled = true) },
        CatalogExample("GlassIconButton, disabled") { GlassIcons(enabled = false) },
    ),
)

@Composable
private fun Icons(
    style: IconButtonStyle,
    enabled: Boolean = true,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.small)) {
        IconButton(KotogramIcons.Close, contentDescription = "Close", onClick = {}, enabled = enabled, style = style)
        IconButton(KotogramIcons.Back, contentDescription = "Back", onClick = {}, enabled = enabled, style = style)
        IconButton(KotogramIcons.Search, contentDescription = "Search", onClick = {}, enabled = enabled, style = style)
        IconButton(KotogramIcons.More, contentDescription = "More", onClick = {}, enabled = enabled, style = style)
    }
}

@Composable
private fun GlassIcons(enabled: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.small)) {
        GlassIconButton(KotogramIcons.Cross, contentDescription = "Close", onClick = {}, enabled = enabled)
        GlassIconButton(KotogramIcons.Back, contentDescription = "Back", onClick = {}, enabled = enabled)
        GlassIconButton(KotogramIcons.Search, contentDescription = "Search", onClick = {}, enabled = enabled)
        GlassIconButton(KotogramIcons.More, contentDescription = "More", onClick = {}, enabled = enabled)
    }
}
