package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
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

// Telegram for macOS's login QR code: a looping matrix until the code, then a slow crossfade.
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QrCode(data: String?, modifier: Modifier = Modifier, contentDescription: String? = null) {
    val shown = rememberRevealedData(data)
    Box(
        modifier = modifier
            .size(QrCodeDefaults.Size)
            .clip(RoundedCornerShape(QrCodeDefaults.CornerRadius))
            .background(QrCodeDefaults.Background),
        contentAlignment = Alignment.Center
    ) {
        // Keyed by presence: a refreshed code replaces the shown one without a crossfade.
        updateTransition(shown, label = "QrCode").Crossfade(
            modifier = Modifier.fillMaxSize().padding(QrCodeDefaults.Padding),
            animationSpec = tween(REVEAL_MILLIS, easing = FastOutSlowInEasing),
            contentKey = { it != null }
        ) { revealed ->
            if (revealed == null) {
                QrPlaceholder(Modifier.fillMaxSize())
            } else {
                QrImage(revealed, contentDescription, Modifier.fillMaxSize())
            }
        }
    }
}

@Suppress("MagicNumber")
object QrCodeDefaults {
    val Size = 210.dp
    val CornerRadius = 10.dp
    val Padding = 12.dp

    // Always dark on light, in any theme: scanners read that best.
    val Background = Color.White
    val Foreground = Color.Black
}

// As on macOS: the matrix plays for 3 s since it appeared, and at least 1 s after the code arrives.
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
                val wait = PLACEHOLDER_MILLIS.milliseconds - placeholderSince.elapsedNow()
                delay(maxOf(MIN_WAIT_MILLIS.milliseconds, wait))
                shown = data
            }

            else -> shown = data
        }
    }
    return shown
}

@Composable
private fun QrPlaceholder(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.DotLottie(Res.readBytes(PLACEHOLDER_PATH))
    }
    Image(
        painter = rememberLottiePainter(
            composition = composition,
            iterations = Compottie.IterateForever
        ),
        contentDescription = null,
        colorFilter = ColorFilter.tint(QrCodeDefaults.Foreground),
        modifier = modifier
    )
}

@Composable
private fun QrImage(data: String, contentDescription: String?, modifier: Modifier = Modifier) {
    val level = remember(data) { levelMatchingPlaceholder(data) }
    // An empty logo keeps the cutout for an icon to come.
    val cutout = remember { EmptyPainter() }
    val qrPainter = rememberQrCodePainter(data, level) {
        errorCorrectionLevel = level
        shapes {
            darkPixel = QrPixelShape.roundCorners()
            ball = QrBallShape.roundCorners(radius = CORNER)
            frame = QrFrameShape.roundCorners(corner = CORNER)
        }
        colors {
            dark = QrBrush.solid(QrCodeDefaults.Foreground)
        }
        logo {
            painter = cutout
            size = CUTOUT_SIZE
            padding = QrLogoPadding.Natural(CUTOUT_PADDING)
        }
    }
    Image(painter = qrPainter, contentDescription = contentDescription, modifier = modifier)
}

// The placeholder draws a 37-module code on the same area: equal modules line their grids up.
// Medium first, as on macOS, where a login link (63 bytes) takes exactly 37.
private fun levelMatchingPlaceholder(data: String): QrErrorCorrectionLevel = listOf(
    QrErrorCorrectionLevel.Medium to QrErrorCorrection.M,
    QrErrorCorrectionLevel.MediumHigh to QrErrorCorrection.Q,
    QrErrorCorrectionLevel.High to QrErrorCorrection.H
).firstOrNull { (_, correction) ->
    QrEncoder(correction).encode(data).width == PLACEHOLDER_MODULES
}?.first ?: QrErrorCorrectionLevel.Medium

private class EmptyPainter : Painter() {
    override val intrinsicSize: Size = Size(1f, 1f)

    override fun DrawScope.onDraw() = Unit
}

private const val PLACEHOLDER_PATH = "files/qr_placeholder.lottie"
private const val REVEAL_MILLIS = 1_200
private const val PLACEHOLDER_MILLIS = 3_000L
private const val MIN_WAIT_MILLIS = 1_000L
private const val CORNER = 0.3f

// With its padding, clears 9 of 37 modules, as macOS does.
private const val CUTOUT_SIZE = 0.22f
private const val CUTOUT_PADDING = 0.1f
private const val PLACEHOLDER_MODULES = 37

// The code itself: QrCode shows its placeholder for the first seconds.
@KotogramPreview
@Composable
private fun QrCodePreview() {
    Box(
        modifier = Modifier
            .size(QrCodeDefaults.Size)
            .clip(RoundedCornerShape(QrCodeDefaults.CornerRadius))
            .background(QrCodeDefaults.Background)
            .padding(QrCodeDefaults.Padding)
    ) {
        QrImage(
            data = "tg://login?token=AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHw",
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}
