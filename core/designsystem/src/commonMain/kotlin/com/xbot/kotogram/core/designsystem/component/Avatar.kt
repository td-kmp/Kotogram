package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle

@Composable
fun Avatar(
    photo: Any?,
    letters: String,
    colorIndex: Int,
    modifier: Modifier = Modifier,
    thumbnail: Any? = null,
    contentDescription: String? = null,
    style: AvatarStyle = AvatarStyle,
) {
    val avatarStyle = rememberStyle(::AvatarStyleScope, AvatarStyle.Default then style)
    if (photo != null || thumbnail != null) {
        NetworkImage(
            model = photo,
            contentDescription = contentDescription,
            thumbnail = thumbnail,
            modifier = modifier.clip(CircleShape),
        )
        return
    }
    val (start, end) = avatarStyle.gradients[colorIndex.mod(avatarStyle.gradients.size)]
    BoxWithConstraints(
        modifier = modifier
            .clip(CircleShape)
            .drawBehind {
                drawRect(
                    Brush.linearGradient(
                        listOf(start, end),
                        Offset(0f, size.height),
                        Offset(size.width, 0f),
                    ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        val fontSize = with(LocalDensity.current) { (maxWidth * avatarStyle.lettersScale).toSp() }
        Text(text = letters, style = avatarStyle.lettersTextStyle.merge(fontSize = fontSize))
    }
}

fun interface AvatarStyle : ComponentStyle<AvatarStyleScope> {
    companion object : AvatarStyle {
        override fun AvatarStyleScope.applyStyle() {}

        @Suppress("MagicNumber")
        val Default = AvatarStyle {
            gradients(
                listOf(
                    Color(0xFFFF885E) to Color(0xFFFF516A),
                    Color(0xFFFFCD6A) to Color(0xFFFFA85C),
                    Color(0xFF82B1FF) to Color(0xFF665FFF),
                    Color(0xFFA0DE7E) to Color(0xFF54CB68),
                    Color(0xFF00FCFD) to Color(0xFF4ACCCD),
                    Color(0xFF72D5FD) to Color(0xFF2A9EF1),
                    Color(0xFFE0A2F3) to Color(0xFFD669ED),
                ),
            )
            lettersScale(26f / 60f)
            lettersTextStyle(
                theme.typography.bodyLarge.merge(color = Color.White, fontWeight = FontWeight.Bold),
            )
        }
    }
}

infix fun AvatarStyle.then(other: AvatarStyle): AvatarStyle = when {
    other === AvatarStyle -> this

    this === AvatarStyle -> other

    else -> AvatarStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

class AvatarStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<AvatarStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var gradients: List<Pair<Color, Color>> = emptyList()
        private set

    var lettersScale: Float = 0f
        private set

    var lettersTextStyle: TextStyle = TextStyle.Default
        private set

    fun gradients(colors: List<Pair<Color, Color>>) {
        gradients = colors
    }

    fun lettersScale(scale: Float) {
        lettersScale = scale
    }

    fun lettersTextStyle(style: TextStyle) {
        lettersTextStyle = style
    }
}
