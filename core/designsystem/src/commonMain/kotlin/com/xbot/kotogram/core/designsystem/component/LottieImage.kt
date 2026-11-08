package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.xbot.kotogram.core.designsystem.component.platform.gunzip
import com.xbot.kotogram.core.resources.Res
import io.github.alexzhirkevich.compottie.DotLottie
import io.github.alexzhirkevich.compottie.ExperimentalCompottieApi
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.dynamic.LottieDynamicProperties
import io.github.alexzhirkevich.compottie.dynamic.rememberLottieDynamicProperties
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter

@Composable
fun LottieImage(
    path: String,
    modifier: Modifier = Modifier,
    iterations: Int = 1,
    colors: Map<String, Color> = emptyMap()
) = LottieImage(
    remember(path) {
        BytesSpec("res_$path") { Res.readBytes(path) }
    },
    modifier,
    iterations,
    colors
)

@Composable
fun LottieImage(
    tgs: ByteArray,
    key: String,
    modifier: Modifier = Modifier,
    iterations: Int = 1,
    colors: Map<String, Color> = emptyMap()
) = LottieImage(remember(key) { BytesSpec("tgs_$key") { tgs } }, modifier, iterations, colors)

@OptIn(ExperimentalCompottieApi::class)
@Composable
private fun LottieImage(
    spec: LottieCompositionSpec,
    modifier: Modifier,
    iterations: Int,
    colors: Map<String, Color>
) {
    val composition by rememberLottieComposition(spec)
    val dynamicProperties = rememberLottieDynamicProperties(colors) {
        colors.forEach { (keyPath, color) -> repaint(keyPath, color) }
    }
    Image(
        painter = rememberLottiePainter(
            composition = composition,
            iterations = iterations,
            dynamicProperties = dynamicProperties.takeIf { colors.isNotEmpty() }
        ),
        contentDescription = null,
        modifier = modifier
    )
}

// Compottie's JsonString keys its cache by the JSON's md5, read and inflated on every show.
@Immutable
private class BytesSpec(override val key: String, private val read: suspend () -> ByteArray) :
    LottieCompositionSpec {
    override suspend fun load(): LottieComposition {
        val bytes = read()
        return when {
            bytes.startsWith(GZIP_MAGIC) -> LottieComposition.parse(gunzip(bytes).decodeToString())
            bytes.startsWith(ZIP_MAGIC) -> LottieCompositionSpec.DotLottie(bytes).load()
            else -> LottieComposition.parse(bytes.decodeToString())
        }
    }
}

private fun LottieDynamicProperties.repaint(keyPath: String, tint: Color) {
    val parts = keyPath.split('.')
    val shape = parts.drop(1).toTypedArray()
    shapeLayer(parts.first()) {
        if (parts.last().startsWith("Stroke")) {
            stroke(*shape) { color { tint } }
        } else {
            fill(*shape) { color { tint } }
        }
    }
}

private fun ByteArray.startsWith(prefix: ByteArray) =
    size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }

private val GZIP_MAGIC = byteArrayOf(0x1f, 0x8b.toByte())
private val ZIP_MAGIC = byteArrayOf(0x50, 0x4b)
