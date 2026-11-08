package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.NetworkImage
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Surface

internal val NetworkImagePage = CatalogPage(
    id = "network-image",
    section = CatalogSection.Components,
    name = "Network image",
    description =
        "An image Coil loads. In the app the account's loader reads TDLib's files, downloading " +
            "them first; a minithumbnail can stand in until then. Here the images are bytes.",
    examples = listOf(
        CatalogExample("Loaded, as an avatar and a photo") {
            Row(horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.large)) {
                NetworkImage(SampleImage, null, Modifier.size(56.dp).clip(CircleShape))
                NetworkImage(
                    model = SampleImage,
                    contentDescription = null,
                    modifier = Modifier.size(
                        width = 160.dp,
                        height = 100.dp
                    ).clip(RoundedCornerShape(12.dp))
                )
            }
        },
        CatalogExample("Loading or failed: the placeholder") {
            Row(horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.large)) {
                NetworkImage(null, null, Modifier.size(56.dp).clip(CircleShape))
                NetworkImage(byteArrayOf(1, 2, 3), null, Modifier.size(56.dp).clip(CircleShape))
            }
        },
        CatalogExample("A thumbnail until the image comes") {
            Thumbnailed()
        }
    )
)

@Composable
private fun Thumbnailed() {
    NetworkImage(
        model = null,
        contentDescription = null,
        thumbnail = SampleThumbnail,
        modifier = Modifier.size(width = 160.dp, height = 100.dp).clip(RoundedCornerShape(12.dp))
    )
}

private val SampleImage: ByteArray = sampleImage(320)
private val SampleThumbnail: ByteArray = sampleImage(16)

@Suppress("MagicNumber")
private fun sampleImage(side: Int): ByteArray {
    val surface = Surface.makeRasterN32Premul(side, side)
    surface.canvas.drawPaint(Paint().apply { color = Color.makeRGB(0x2A, 0x9E, 0xF1) })
    val accent = Paint().apply { color = Color.makeRGB(0x8E, 0x5B, 0xF5) }
    surface.canvas.drawCircle(side * 0.7f, side * 0.3f, side * 0.35f, accent)
    return requireNotNull(surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)).bytes
}
