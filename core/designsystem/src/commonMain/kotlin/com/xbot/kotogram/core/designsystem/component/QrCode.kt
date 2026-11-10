package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
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
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

@Composable
fun QrCode(
    data: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    style: QrCodeStyle = QrCodeStyle,
) {
    @Suppress("NAME_SHADOWING")
    val style = rememberStyle(::QrCodeStyleScope, QrCodeStyle.Default then style)
    val revealed = rememberRevealedData(data)
    context(style) {
        QrCodeSurface(modifier) {
            AnimatedContent(
                targetState = revealed,
                transitionSpec = {
                    fadeIn(style.revealSpec) togetherWith ExitTransition.KeepUntilTransitionsFinished
                },
                contentKey = { it != null },
                label = "QrCode",
            ) { shown ->
                if (shown == null) {
                    QrPlaceholder(Modifier.fillMaxSize())
                } else {
                    AnimatedContent(
                        targetState = shown,
                        transitionSpec = {
                            fadeIn(style.refreshSpec) togetherWith ExitTransition.KeepUntilTransitionsFinished
                        },
                        label = "QrCodeRefresh",
                    ) { code ->
                        QrImage(data = code, contentDescription = contentDescription, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
context(style: QrCodeStyleScope)
private fun QrCodeSurface(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.size(style.size),
        shape = style.shape,
        color = style.containerColor,
        contentColor = style.contentColor,
        content = content,
    )
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
context(style: QrCodeStyleScope)
private fun QrPlaceholder(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.DotLottie(Res.readBytes(PLACEHOLDER_PATH))
    }
    Image(
        painter = rememberLottiePainter(
            composition = composition,
            iterations = Compottie.IterateForever,
        ),
        contentDescription = null,
        colorFilter = ColorFilter.tint(style.contentColor),
        modifier = modifier
            .background(style.containerColor)
            .layoutOnQrGrid(PLACEHOLDER_MODULES, style.contentPadding),
    )
}

@Composable
context(style: QrCodeStyleScope)
private fun QrImage(
    data: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val contentColor = style.contentColor
    val spec = remember(data) { qrCodeSpec(data, PLACEHOLDER_MODULES) }
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
            .background(style.containerColor)
            .layoutOnQrGrid(spec.modules, style.contentPadding),
    )
}

private class EmptyPainter : Painter() {
    override val intrinsicSize: Size = Size(1f, 1f)

    override fun DrawScope.onDraw() = Unit
}

private const val PLACEHOLDER_PATH = "files/qr_placeholder.lottie"
private const val PLACEHOLDER_MODULES = 37
private const val PLACEHOLDER_MILLIS = 3_000L
private const val MIN_WAIT_MILLIS = 1_000L

@KotogramPreview
@Composable
private fun QrCodePreview() {
    context(rememberStyle(::QrCodeStyleScope, QrCodeStyle.Default)) {
        QrCodeSurface(Modifier) {
            QrImage(
                data = "tg://login?token=AQFJjO1qHocdoolqLtckd6dPS98k1HhnVDFilU_N_v6psw",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Suppress("MagicNumber")
fun interface QrCodeStyle : ComponentStyle<QrCodeStyleScope> {
    companion object : QrCodeStyle {
        override fun QrCodeStyleScope.applyStyle() {}

        val Default = QrCodeStyle {
            size(210.dp)
            shape(theme.shapes.medium)
            containerColor(Color.White)
            contentColor(Color.Black)
            contentPadding(12.dp)
            revealSpec(tween(1_200, easing = FastOutSlowInEasing))
            refreshSpec(tween(200, easing = LinearEasing))
        }
    }
}

infix fun QrCodeStyle.then(other: QrCodeStyle): QrCodeStyle = when {
    other === QrCodeStyle -> this

    this === QrCodeStyle -> other

    else -> QrCodeStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

class QrCodeStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<QrCodeStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var size: Dp = Dp.Unspecified
        private set

    var shape: Shape = RectangleShape
        private set

    var containerColor: Color = Color.Unspecified
        private set

    var contentColor: Color = Color.Unspecified
        private set

    var contentPadding: Dp = Dp.Unspecified
        private set

    var revealSpec: FiniteAnimationSpec<Float> = snap()
        private set

    var refreshSpec: FiniteAnimationSpec<Float> = snap()
        private set

    fun size(size: Dp) {
        this.size = size
    }

    fun shape(shape: Shape) {
        this.shape = shape
    }

    fun containerColor(color: Color) {
        containerColor = color
    }

    fun contentColor(color: Color) {
        contentColor = color
    }

    fun contentPadding(padding: Dp) {
        contentPadding = padding
    }

    fun revealSpec(spec: FiniteAnimationSpec<Float>) {
        revealSpec = spec
    }

    fun refreshSpec(spec: FiniteAnimationSpec<Float>) {
        refreshSpec = spec
    }
}
