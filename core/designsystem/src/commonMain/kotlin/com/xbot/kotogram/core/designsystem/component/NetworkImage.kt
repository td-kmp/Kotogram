package com.xbot.kotogram.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle

val LocalImageLoader = staticCompositionLocalOf<ImageLoader?> { null }

@Composable
fun NetworkImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    thumbnail: Any? = null,
    contentScale: ContentScale = ContentScale.Crop,
    style: NetworkImageStyle = NetworkImageStyle,
) {
    val imageStyle = rememberStyle(::NetworkImageStyleScope, NetworkImageStyle.Default then style)
    val context = LocalPlatformContext.current
    val imageLoader = LocalImageLoader.current ?: remember(context) { ImageLoader(context) }
    val request = remember(model, context, imageStyle.fadeInMillis) {
        ImageRequest.Builder(context).data(model).crossfade(imageStyle.fadeInMillis).build()
    }
    val placeholder = if (thumbnail != null) {
        rememberAsyncImagePainter(thumbnail, imageLoader)
    } else {
        remember(imageStyle.placeholderColor) { ColorPainter(imageStyle.placeholderColor) }
    }
    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
        placeholder = placeholder,
        error = placeholder,
        contentScale = contentScale,
    )
}

fun interface NetworkImageStyle : ComponentStyle<NetworkImageStyleScope> {
    companion object : NetworkImageStyle {
        override fun NetworkImageStyleScope.applyStyle() {}

        val Default = NetworkImageStyle {
            placeholderColor(theme.colors.surfaceVariant)
            fadeInMillis(200)
        }
    }
}

infix fun NetworkImageStyle.then(other: NetworkImageStyle): NetworkImageStyle = when {
    other === NetworkImageStyle -> this

    this === NetworkImageStyle -> other

    else -> NetworkImageStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

class NetworkImageStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<NetworkImageStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var placeholderColor: Color = Color.Unspecified
        private set

    var fadeInMillis: Int = 0
        private set

    fun placeholderColor(color: Color) {
        placeholderColor = color
    }

    fun fadeInMillis(millis: Int) {
        fadeInMillis = millis
    }
}
