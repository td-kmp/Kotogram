package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.extensions.glyphBounds
import com.xbot.kotogram.core.designsystem.extensions.isDark
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.designsystem.theme.platformRoundedFontFamily
import kotlin.math.floor

enum class AvatarShape { Circle, RoundedRect }

enum class AvatarIcon { SavedMessages, Replies, MyNotes, HiddenAuthor, Archive, HiddenArchive, Deleted }

@Composable
fun Avatar(
    photo: Any?,
    letters: String,
    colorIndex: Int,
    modifier: Modifier = Modifier,
    thumbnail: Any? = null,
    shape: AvatarShape = AvatarShape.Circle,
    contentDescription: String? = null,
    blur: ImageBlur? = null,
    style: AvatarStyle = AvatarStyle,
) {
    val avatarStyle = rememberStyle(::AvatarStyleScope, AvatarStyle.Default then style)
    if (photo != null || thumbnail != null) {
        NetworkImage(
            model = photo,
            contentDescription = contentDescription,
            thumbnail = thumbnail,
            blur = blur,
            modifier = modifier.clip(shape.toShape()),
        )
        return
    }
    val (light, dark) = avatarStyle.gradients[colorIndex.mod(avatarStyle.gradients.size)]
    val measurer = rememberTextMeasurer()
    val blurred = blur != null
    Box(
        modifier = modifier
            .clip(shape.toShape())
            .drawWithCache {
                val gradient = Brush.linearGradient(
                    colors = [light, dark],
                    start = Offset.Zero,
                    end = Offset(0f, size.height),
                )
                if (letters.isEmpty() || blurred || size.minDimension < 1f) {
                    return@drawWithCache onDrawBehind { drawRect(gradient) }
                }
                val text = measurer.measure(
                    text = letters,
                    style = avatarStyle.lettersTextStyle.copy(fontSize = (size.width * LETTERS_SCALE).toSp()),
                )
                val ink = text.glyphBounds()
                // Hack: as Telegram for iOS's AvatarNode, a lone B moves a point right to look centred.
                val nudge = if (letters == "B") 1.dp.toPx() else 0f
                val baseline = size.height - floor(ink.bottom + (size.height - ink.height) / 2)
                val topLeft = Offset(
                    floor((size.width - ink.width) / 2 - ink.left) + nudge,
                    baseline - text.firstBaseline,
                )
                onDrawBehind {
                    drawRect(gradient)
                    drawText(text, topLeft = topLeft)
                }
            },
    )
}

@Composable
fun Avatar(
    icon: AvatarIcon,
    modifier: Modifier = Modifier,
    shape: AvatarShape = AvatarShape.Circle,
    contentDescription: String? = null,
    blur: ImageBlur? = null,
    style: AvatarStyle = AvatarStyle,
) {
    val avatarStyle = rememberStyle(::AvatarStyleScope, AvatarStyle.Default then style)
    val (top, bottom) = when (icon) {
        Deleted -> avatarStyle.grayGradient
        HiddenArchive -> avatarStyle.hiddenArchiveGradient
        else -> avatarStyle.iconGradient
    }
    val tint = if (icon == HiddenArchive) avatarStyle.hiddenArchiveIconColor else avatarStyle.iconColor
    val painter = rememberVectorPainter(icon.vector())
    val blurred = blur != null
    Box(
        modifier = modifier
            .clip(shape.toShape())
            .semantics { if (contentDescription != null) this.contentDescription = contentDescription }
            .drawWithCache {
                val gradient = Brush.linearGradient([top, bottom], Offset.Zero, Offset(0f, size.height))
                val factor = size.width / IconAvatarSide.toPx()
                val iconSize = painter.intrinsicSize * factor
                val topLeft =
                    Offset(floor((size.width - iconSize.width) / 2), floor((size.height - iconSize.height) / 2))
                val filter = ColorFilter.tint(tint)
                onDrawBehind {
                    drawRect(gradient)
                    if (!blurred) {
                        translate(topLeft.x, topLeft.y) {
                            with(painter) { draw(iconSize, colorFilter = filter) }
                        }
                    }
                }
            },
    )
}

private fun AvatarIcon.vector(): ImageVector = when (this) {
    SavedMessages -> KotogramIcons.AvatarSavedMessages
    Replies -> KotogramIcons.AvatarReplies
    MyNotes -> KotogramIcons.AvatarMyNotes
    HiddenAuthor -> KotogramIcons.AvatarAnonymous
    Archive, HiddenArchive -> KotogramIcons.AvatarArchive
    Deleted -> KotogramIcons.AvatarDeleted
}

internal fun AvatarShape.toShape(): Shape = when (this) {
    Circle -> CircleShape
    RoundedRect -> ForumShape
}

fun interface AvatarStyle : ComponentStyle<AvatarStyleScope> {
    companion object : AvatarStyle {
        override fun AvatarStyleScope.applyStyle() {}

        @Suppress("MagicNumber")
        val Default = AvatarStyle {
            gradients(
                [
                    Color(0xFFFF885E) to Color(0xFFFF516A),
                    Color(0xFFFFCD6A) to Color(0xFFFFA85C),
                    Color(0xFF82B1FF) to Color(0xFF665FFF),
                    Color(0xFFA0DE7E) to Color(0xFF54CB68),
                    Color(0xFF00FCFD) to Color(0xFF4ACCCD),
                    Color(0xFF72D5FD) to Color(0xFF2A9EF1),
                    Color(0xFFE0A2F3) to Color(0xFFD669ED),
                ],
            )
            iconGradient(Color(0xFF72D5FD) to Color(0xFF2A9EF1))
            iconColor(Color.White)
            if (theme.colors.surface.isDark()) {
                grayGradient(Color.White.copy(alpha = 0.18f) to Color.White.copy(alpha = 0.22f))
                hiddenArchiveGradient(Color(0xFF666666) to Color(0xFF666666))
                hiddenArchiveIconColor(Color.Black)
            } else {
                grayGradient(Color(0xFFCDCDCD) to Color(0xFFB1B1B1))
                hiddenArchiveGradient(Color(0xFFDEDEE5) to Color(0xFFC5C6CC))
                hiddenArchiveIconColor(Color.White)
            }
            val rounded = platformRoundedFontFamily
            lettersTextStyle(
                TextStyle(
                    color = Color.White,
                    fontFamily = rounded ?: theme.typography.bodyLarge.fontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = if (rounded != null) RoundedLettersTracking else TextUnit.Unspecified,
                ),
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

    var gradients: List<Pair<Color, Color>> = []
        private set

    var lettersTextStyle: TextStyle = TextStyle.Default
        private set

    var iconGradient: Pair<Color, Color> = Color.Unspecified to Color.Unspecified
        private set

    var iconColor: Color = Color.Unspecified
        private set

    var grayGradient: Pair<Color, Color> = Color.Unspecified to Color.Unspecified
        private set

    var hiddenArchiveGradient: Pair<Color, Color> = Color.Unspecified to Color.Unspecified
        private set

    var hiddenArchiveIconColor: Color = Color.Unspecified
        private set

    fun gradients(colors: List<Pair<Color, Color>>) {
        gradients = colors
    }

    fun lettersTextStyle(style: TextStyle) {
        lettersTextStyle = style
    }

    fun iconGradient(colors: Pair<Color, Color>) {
        iconGradient = colors
    }

    fun iconColor(color: Color) {
        iconColor = color
    }

    fun grayGradient(colors: Pair<Color, Color>) {
        grayGradient = colors
    }

    fun hiddenArchiveGradient(colors: Pair<Color, Color>) {
        hiddenArchiveGradient = colors
    }

    fun hiddenArchiveIconColor(color: Color) {
        hiddenArchiveIconColor = color
    }
}

private const val LETTERS_SCALE = 26f / 60f
private val IconAvatarSide = 60.dp
private val ForumShape = RoundedCornerShape(percent = 25)

// Workaround: Skia tracks SF Pro Rounded as at 12 pt, CoreText by size (28 of 92 units at 26 pt).
private val RoundedLettersTracking = (-64f / 2048).em
