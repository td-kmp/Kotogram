package com.xbot.kotogram.catalog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.GlassIconButton
import com.xbot.kotogram.core.designsystem.component.Switch
import com.xbot.kotogram.core.designsystem.component.TopBar
import com.xbot.kotogram.core.designsystem.component.TopBarStyle
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons

@Composable
fun CatalogTopBar(
    title: String,
    onBack: (() -> Unit)?,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    laptop: Boolean,
    onLaptopChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    TopBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = modifier.fillMaxWidth(),
        navigation = if (onBack != null) {
            { GlassIconButton(KotogramIcons.Back, contentDescription = "Back", onClick = onBack) }
        } else {
            null
        },
        actions = {
            LabeledSwitch("Mobile", checked = !laptop, onCheckedChange = { onLaptopChange(!it) })
            LabeledSwitch("Dark theme", checked = darkTheme, onCheckedChange = onDarkThemeChange)
        },
        style = CatalogTopBarStyle,
    )
}

@Composable
private fun LabeledSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = KotogramTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private val CatalogTopBarStyle = TopBarStyle {
    centeredTitle(false)
    horizontalPadding(theme.spacing.large)
    actionGap(theme.spacing.large)
}
