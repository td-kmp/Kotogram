package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
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
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrLogoPadding
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay

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
        // Opaque layers, the last kept under the next as it fades in: what both draw stays still, as on macOS.
        AnimatedContent(
            targetState = revealed,
            transitionSpec = {
                fadeIn(tween(QrCodeTokens.REVEAL_MILLIS, easing = FastOutSlowInEasing)) togetherWith
                    ExitTransition.KeepUntilTransitionsFinished
            },
            contentKey = { it != null },
            label = "QrCode"
        ) { shown ->
            if (shown == null) {
                QrPlaceholder(colors = colors, modifier = Modifier.fillMaxSize())
            } else {
                AnimatedContent(
                    targetState = shown,
                    transitionSpec = {
                        fadeIn(tween(QrCodeTokens.REFRESH_MILLIS, easing = LinearEasing)) togetherWith
                            ExitTransition.KeepUntilTransitionsFinished
                    },
                    label = "QrCodeRefresh"
                ) { code ->
                    QrImage(
                        data = code,
                        contentDescription = contentDescription,
                        contentColor = colors.contentColor,
                        containerColor = colors.containerColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
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
private fun QrPlaceholder(colors: QrCodeColors, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.DotLottie(Res.readBytes(QrCodeTokens.PLACEHOLDER_PATH))
    }
    Image(
        painter = rememberLottiePainter(
            composition = composition,
            iterations = Compottie.IterateForever
        ),
        contentDescription = null,
        colorFilter = ColorFilter.tint(colors.contentColor),
        modifier = modifier
            .background(colors.containerColor)
            .layoutOnQrGrid(QrCodeTokens.PLACEHOLDER_MODULES, QrCodeTokens.ContentPadding)
    )
}

@Composable
private fun QrImage(
    data: String,
    contentDescription: String?,
    contentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    val spec = remember(data) { qrCodeSpec(data, QrCodeTokens.PLACEHOLDER_MODULES) }
    // An empty logo keeps the cutout for an icon to come.
    val cutout = remember { EmptyPainter() }
    val painter = rememberQrCodePainter(data, spec, contentColor) {
        errorCorrectionLevel = spec.level
        shapes {
            darkPixel = TelegramPixelShape
            frame = TelegramFrameShape
            ball = TelegramBallShape
        }
        colors {
            dark = QrBrush.solid(contentColor)
        }
        logo {
            painter = cutout
            size = spec.cutoutShare
            padding = QrLogoPadding.Natural(0f)
        }
    }
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier
            .background(containerColor)
            .layoutOnQrGrid(spec.modules, QrCodeTokens.ContentPadding)
    )
}

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
    const val REFRESH_MILLIS = 200
    const val PLACEHOLDER_MILLIS = 3_000L
    const val MIN_WAIT_MILLIS = 1_000L
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
            contentColor = colors.contentColor,
            containerColor = colors.containerColor,
            modifier = Modifier.fillMaxSize()
        )
    }
}
