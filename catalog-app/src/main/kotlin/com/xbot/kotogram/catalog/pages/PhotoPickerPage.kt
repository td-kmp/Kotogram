package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.ProminentButton
import com.xbot.kotogram.core.designsystem.component.rememberPhotoPickerLauncher
import org.jetbrains.skia.Image as SkiaImage

internal val PhotoPickerPage = CatalogPage(
    id = "photo-picker",
    section = CatalogSection.Components,
    name = "Photo picker",
    description = "The system's picker of one image, which comes as a JPEG of at most 2048 " +
        "a side: the Photo Picker on Android, PHPicker on iOS, NSOpenPanel on macOS and " +
        "the browser's file dialog on the web. Other desktops have none.",
    examples = [
        CatalogExample("Pick a photo") { PickedPhoto() },
    ],
)

@Composable
private fun PickedPhoto() {
    var photo by remember { mutableStateOf<ImageBitmap?>(null) }
    val launcher = rememberPhotoPickerLauncher { bytes ->
        photo = SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.medium),
    ) {
        Box(
            Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(KotogramTheme.colors.surfaceContainerHighest),
        ) {
            photo?.let { Image(it, contentDescription = null, contentScale = ContentScale.Crop) }
        }
        if (launcher != null) {
            ProminentButton(text = "Choose photo", onClick = launcher::launch)
        } else {
            Text("No picker on this desktop", style = KotogramTheme.typography.bodyMedium)
        }
    }
}
