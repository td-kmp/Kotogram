package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramLaptopPreview
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle

@Composable
fun IconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: IconButtonStyle = IconButtonStyle,
) {
    context(rememberStyle(::IconButtonStyleScope, IconButtonStyle.Adaptive then style)) {
        StyledIconButton(icon, contentDescription, onClick, modifier, enabled)
    }
}

@Composable
context(style: IconButtonStyleScope)
internal fun StyledIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(style.size)
            .clip(style.shape)
            .background(style.containerColor)
            .clickable(
                interactionSource = null,
                indication = KotogramTheme.indication,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(style.iconSize),
            tint = if (enabled) style.contentColor else style.disabledContentColor,
        )
    }
}

@KotogramPreview
@Composable
private fun IconButtonPreview() {
    Row(
        modifier = Modifier.padding(KotogramTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.small),
    ) {
        IconButton(KotogramIcons.Close, contentDescription = null, onClick = {})
        IconButton(KotogramIcons.Search, contentDescription = null, onClick = {})
        IconButton(KotogramIcons.More, contentDescription = null, onClick = {}, enabled = false)
    }
}

@KotogramLaptopPreview
@Composable
private fun IconButtonLaptopPreview() = IconButtonPreview()

@Suppress("MagicNumber")
fun interface IconButtonStyle : ComponentStyle<IconButtonStyleScope> {
    companion object : IconButtonStyle {
        override fun IconButtonStyleScope.applyStyle() {}

        val Default = IconButtonStyle {
            size(44.dp)
            iconSize(24.dp)
            shape(CircleShape)
            containerColor(Color.Transparent)
            contentColor(theme.colors.onSurface)
            disabledContentColor(theme.colors.onSurface.copy(alpha = 0.38f))
        }

        val Adaptive = Default then IconButtonStyle {
            laptop {
                size(32.dp)
            }
        }
    }
}

infix fun IconButtonStyle.then(other: IconButtonStyle): IconButtonStyle = when {
    other === IconButtonStyle -> this

    this === IconButtonStyle -> other

    else -> IconButtonStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

class IconButtonStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<IconButtonStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var size: Dp = Dp.Unspecified
        private set

    var iconSize: Dp = Dp.Unspecified
        private set

    var shape: Shape = RectangleShape
        private set

    var containerColor: Color = Color.Unspecified
        private set

    var contentColor: Color = Color.Unspecified
        private set

    var disabledContentColor: Color = Color.Unspecified
        private set

    fun size(size: Dp) {
        this.size = size
    }

    fun iconSize(size: Dp) {
        iconSize = size
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

    fun disabledContentColor(color: Color) {
        disabledContentColor = color
    }
}
