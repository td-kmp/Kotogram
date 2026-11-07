package com.xbot.kotogram.catalog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogTopBar(
    title: String,
    onBack: (() -> Unit)?,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = KotogramTheme.spacing
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(KotogramIcons.Back, contentDescription = "Back") }
            }
        },
        actions = {
            Row(
                modifier = Modifier.padding(end = spacing.large),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark theme", style = KotogramTheme.typography.bodyLarge)
                Switch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
            }
        }
    )
}
