package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.fetch.SourceFetchResult
import coil3.request.Options
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
    colors: Map<String, Color> = emptyMap(),
) = LottieImage(
    remember(path) {
        BytesSpec("res_$path") { Res.readBytes(path) }
    },
    iterations,
    colors,
    modifier,
)

@Composable
fun LottieImage(
    model: Any,
    modifier: Modifier = Modifier,
    iterations: Int = 1,
    colors: Map<String, Color> = emptyMap(),
) {
    val context = LocalPlatformContext.current
    val imageLoader = rememberImageLoader()
    val spec = remember(model, imageLoader) {
        val options = Options(context)
        val key = imageLoader.components.key(model, options) ?: model.toString()
        BytesSpec("model_$key") { imageLoader.fetchBytes(model, options) }
    }
    LottieImage(spec, iterations, colors, modifier)
}

private suspend fun ImageLoader.fetchBytes(
    model: Any,
    options: Options,
): ByteArray {
    val data = components.map(model, options)
    val (fetcher, _) = checkNotNull(components.newFetcher(data, options, this)) {
        "No fetcher for $data"
    }
    val result = fetcher.fetch() as? SourceFetchResult ?: error("$data isn't a source")
    return result.source.use { it.source().readByteArray() }
}

@OptIn(ExperimentalCompottieApi::class)
@Composable
private fun LottieImage(
    spec: LottieCompositionSpec,
    iterations: Int,
    colors: Map<String, Color>,
    modifier: Modifier = Modifier,
) {
    val composition by rememberLottieComposition(spec)
    val dynamicProperties = rememberLottieDynamicProperties(colors) {
        colors.forEach { (keyPath, color) -> repaint(keyPath, color) }
    }
    val properties = dynamicProperties.takeIf { colors.isNotEmpty() }
    Image(
        painter = if (iterations == 0) {
            rememberLottiePainter(composition = composition, progress = { 0f }, dynamicProperties = properties)
        } else {
            rememberLottiePainter(composition = composition, iterations = iterations, dynamicProperties = properties)
        },
        contentDescription = null,
        modifier = modifier,
    )
}

// Workaround: Compottie's JsonString keys its cache by the JSON's md5, read and inflated on every show.
@Immutable
private class BytesSpec(
    override val key: String,
    private val read: suspend () -> ByteArray,
) : LottieCompositionSpec {
    override suspend fun load(): LottieComposition {
        val bytes = read()
        return when {
            bytes.startsWith(GZIP_MAGIC) -> LottieComposition.parse(gunzip(bytes).decodeToString())
            bytes.startsWith(ZIP_MAGIC) -> LottieCompositionSpec.DotLottie(bytes).load()
            else -> LottieComposition.parse(bytes.decodeToString())
        }
    }
}

private fun LottieDynamicProperties.repaint(
    keyPath: String,
    tint: Color,
) {
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
