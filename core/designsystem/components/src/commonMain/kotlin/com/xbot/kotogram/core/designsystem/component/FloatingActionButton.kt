package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import androidx.compose.material3.FloatingActionButton as MaterialFloatingActionButton

@Composable
fun FloatingActionButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: FloatingActionButtonStyle = FloatingActionButtonStyle,
) {
    val fabStyle = rememberStyle(::FloatingActionButtonStyleScope, FloatingActionButtonStyle.Default then style)
    MaterialFloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(fabStyle.size),
        shape = fabStyle.shape,
        containerColor = fabStyle.containerColor,
        contentColor = fabStyle.contentColor,
        elevation = FloatingActionButtonDefaults.elevation(),
    ) {
        Icon(icon, contentDescription, Modifier.size(fabStyle.iconSize))
    }
}

@Suppress("MagicNumber")
fun interface FloatingActionButtonStyle : ComponentStyle<FloatingActionButtonStyleScope> {
    companion object : FloatingActionButtonStyle {
        override fun FloatingActionButtonStyleScope.applyStyle() {}

        val Default = FloatingActionButtonStyle {
            size(56.dp)
            iconSize(24.dp)
            shape(RoundedCornerShape(16.dp))
            containerColor(theme.colors.primary)
            contentColor(theme.colors.onPrimary)
        }
    }
}

infix fun FloatingActionButtonStyle.then(other: FloatingActionButtonStyle): FloatingActionButtonStyle = when {
    other === FloatingActionButtonStyle -> this

    this === FloatingActionButtonStyle -> other

    else -> FloatingActionButtonStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

class FloatingActionButtonStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<FloatingActionButtonStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var size: Dp = Dp.Unspecified
        private set

    var iconSize: Dp = Dp.Unspecified
        private set

    var shape: Shape = RoundedCornerShape(0.dp)
        private set

    var containerColor: Color = Color.Unspecified
        private set

    var contentColor: Color = Color.Unspecified
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
}
