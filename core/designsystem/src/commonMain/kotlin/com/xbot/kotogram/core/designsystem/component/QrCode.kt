package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.resources.Res
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.DotLottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.github.alexzhirkevich.qrose.matrix.qr.QrEncoder
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrLogoPadding
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.roundCorners
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QrCode(
    data: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = QrCodeDefaults.shape,
    colors: QrCodeColors = QrCodeDefaults.colors()
) {
    val revealed = rememberRevealedData(data)
    Surface(
        modifier = modifier.size(QrCodeDefaults.Size),
        shape = shape,
        color = colors.containerColor,
        contentColor = colors.contentColor
    ) {
        // Keyed by presence: a refreshed code replaces the shown one without a crossfade.
        updateTransition(revealed, label = "QrCode").Crossfade(
            modifier = Modifier.padding(QrCodeTokens.ContentPadding),
            animationSpec = tween(QrCodeTokens.REVEAL_MILLIS, easing = FastOutSlowInEasing),
            contentKey = { it != null }
        ) { shown ->
            if (shown == null) {
                QrPlaceholder(color = colors.contentColor, modifier = Modifier.fillMaxSize())
            } else {
                QrImage(
                    data = shown,
                    contentDescription = contentDescription,
                    color = colors.contentColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Suppress("MagicNumber")
object QrCodeDefaults {
    val Size: Dp = 210.dp

    val shape: Shape = RoundedCornerShape(10.dp)

    // Dark on light in any theme: scanners read that best.
    @Composable
    fun colors(
        containerColor: Color = Color.White,
        contentColor: Color = Color.Black
    ): QrCodeColors = QrCodeColors(containerColor = containerColor, contentColor = contentColor)
}

@Immutable
class QrCodeColors(val containerColor: Color, val contentColor: Color) {
    fun copy(
        containerColor: Color = this.containerColor,
        contentColor: Color = this.contentColor
    ): QrCodeColors = QrCodeColors(containerColor = containerColor, contentColor = contentColor)

    override fun equals(other: Any?): Boolean = other is QrCodeColors &&
        containerColor == other.containerColor &&
        contentColor == other.contentColor

    override fun hashCode(): Int = 31 * containerColor.hashCode() + contentColor.hashCode()
}

@Composable
private fun rememberRevealedData(data: String?): String? {
    var shown by remember { mutableStateOf<String?>(null) }
    var placeholderSince by remember { mutableStateOf(TimeSource.Monotonic.markNow()) }
    LaunchedEffect(data) {
        when {
            data == null -> {
                if (shown != null) placeholderSince = TimeSource.Monotonic.markNow()
                shown = null
            }

            shown == null -> {
                val wait = QrCodeTokens.PLACEHOLDER_MILLIS.milliseconds -
                    placeholderSince.elapsedNow()
                delay(maxOf(QrCodeTokens.MIN_WAIT_MILLIS.milliseconds, wait))
                shown = data
            }

            else -> shown = data
        }
    }
    return shown
}

@Composable
private fun QrPlaceholder(color: Color, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.DotLottie(Res.readBytes(QrCodeTokens.PLACEHOLDER_PATH))
    }
    Image(
        painter = rememberLottiePainter(
            composition = composition,
            iterations = Compottie.IterateForever
        ),
        contentDescription = null,
        colorFilter = ColorFilter.tint(color),
        modifier = modifier
    )
}

@Composable
private fun QrImage(
    data: String,
    contentDescription: String?,
    color: Color,
    modifier: Modifier = Modifier
) {
    val level = remember(data) { levelMatchingPlaceholder(data) }
    // An empty logo keeps the cutout for an icon to come.
    val cutout = remember { EmptyPainter() }
    val qrPainter = rememberQrCodePainter(data, level, color) {
        errorCorrectionLevel = level
        shapes {
            darkPixel = QrPixelShape.roundCorners()
            ball = QrBallShape.roundCorners(radius = QrCodeTokens.CORNER)
            frame = QrFrameShape.roundCorners(corner = QrCodeTokens.CORNER)
        }
        colors {
            dark = QrBrush.solid(color)
        }
        logo {
            painter = cutout
            size = QrCodeTokens.CUTOUT_SIZE
            padding = QrLogoPadding.Natural(QrCodeTokens.CUTOUT_PADDING)
        }
    }
    Image(painter = qrPainter, contentDescription = contentDescription, modifier = modifier)
}

private fun levelMatchingPlaceholder(data: String): QrErrorCorrectionLevel = listOf(
    QrErrorCorrectionLevel.Medium to QrErrorCorrection.M,
    QrErrorCorrectionLevel.MediumHigh to QrErrorCorrection.Q,
    QrErrorCorrectionLevel.High to QrErrorCorrection.H
).firstOrNull { (_, correction) ->
    QrEncoder(correction).encode(data).width == QrCodeTokens.PLACEHOLDER_MODULES
}?.first ?: QrErrorCorrectionLevel.Medium

private class EmptyPainter : Painter() {
    override val intrinsicSize: Size = Size(1f, 1f)

    override fun DrawScope.onDraw() = Unit
}

@Suppress("MagicNumber")
private object QrCodeTokens {
    val ContentPadding = 12.dp

    const val PLACEHOLDER_PATH = "files/qr_placeholder.lottie"
    const val PLACEHOLDER_MODULES = 37
    const val REVEAL_MILLIS = 1_200
    const val PLACEHOLDER_MILLIS = 3_000L
    const val MIN_WAIT_MILLIS = 1_000L
    const val CORNER = 0.3f
    const val CUTOUT_SIZE = 0.22f
    const val CUTOUT_PADDING = 0.1f
}

@KotogramPreview
@Composable
private fun QrCodePreview() {
    val colors = QrCodeDefaults.colors()
    Surface(
        modifier = Modifier.size(QrCodeDefaults.Size),
        shape = QrCodeDefaults.shape,
        color = colors.containerColor
    ) {
        QrImage(
            data = "tg://login?token=AQFJjO1qHocdoolqLtckd6dPS98k1HhnVDFilU_N_v6psw",
            contentDescription = null,
            color = colors.contentColor,
            modifier = Modifier.fillMaxSize().padding(QrCodeTokens.ContentPadding)
        )
    }
}
