package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.xbot.kotogram.core.designsystem.KotogramLaptopPreview
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle

@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: SwitchStyle = SwitchStyle,
) {
    val switchStyle = rememberStyle(::SwitchStyleScope, SwitchStyle.Adaptive then style)
    with(LocalSwitchOverride.current) {
        SwitchOverrideScope(checked, onCheckedChange, modifier, enabled, switchStyle).Switch()
    }
}

internal interface SwitchOverride {
    @Composable
    fun SwitchOverrideScope.Switch()
}

internal class SwitchOverrideScope(
    val checked: Boolean,
    val onCheckedChange: ((Boolean) -> Unit)?,
    val modifier: Modifier,
    val enabled: Boolean,
    val style: SwitchStyleScope,
)

internal object DefaultSwitchOverride : SwitchOverride {
    @Composable
    override fun SwitchOverrideScope.Switch() {
        val progress = animateFloatAsState(if (checked) 1f else 0f, style.animationSpec)
        val toggle = onCheckedChange?.let { onChange ->
            Modifier
                .minimumInteractiveComponentSize()
                .toggleable(
                    value = checked,
                    interactionSource = null,
                    indication = null,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onChange,
                )
        } ?: Modifier
        Spacer(
            modifier
                .then(toggle)
                .size(style.trackWidth, style.trackHeight)
                .alpha(if (enabled) 1f else style.disabledAlpha)
                .drawWithCache {
                    val radius = size.height / 2
                    val border = style.borderWidth.toPx()
                    val uncheckedThumb = style.uncheckedThumbSize.toPx() / 2
                    val checkedThumb = style.checkedThumbSize.toPx() / 2
                    onDrawBehind {
                        val fraction = progress.value
                        drawRoundRect(
                            color = lerp(style.uncheckedTrackColor, style.checkedTrackColor, fraction),
                            cornerRadius = CornerRadius(radius),
                        )
                        if (fraction < 1f) {
                            drawRoundRect(
                                color = style.uncheckedBorderColor,
                                topLeft = Offset(border / 2, border / 2),
                                size = Size(size.width - border, size.height - border),
                                cornerRadius = CornerRadius(radius - border / 2),
                                alpha = 1f - fraction,
                                style = Stroke(border),
                            )
                        }
                        drawCircle(
                            color = lerp(style.uncheckedThumbColor, style.checkedThumbColor, fraction),
                            radius = lerp(uncheckedThumb, checkedThumb, fraction),
                            center = Offset(lerp(radius, size.width - radius, fraction), radius),
                        )
                    }
                },
        )
    }
}

internal val LocalSwitchOverride: ProvidableCompositionLocal<SwitchOverride> =
    staticCompositionLocalOf { DefaultSwitchOverride }

@KotogramPreview
@Composable
private fun SwitchPreview() {
    Row(
        modifier = Modifier.padding(KotogramTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.large),
    ) {
        Switch(checked = false, onCheckedChange = {})
        Switch(checked = true, onCheckedChange = {})
        Switch(checked = true, onCheckedChange = {}, enabled = false)
    }
}

@KotogramLaptopPreview
@Composable
private fun SwitchLaptopPreview() = SwitchPreview()

@Suppress("MagicNumber")
fun interface SwitchStyle : ComponentStyle<SwitchStyleScope> {
    companion object : SwitchStyle {
        override fun SwitchStyleScope.applyStyle() {}

        val Default = SwitchStyle {
            trackWidth(52.dp)
            trackHeight(32.dp)
            uncheckedThumbSize(16.dp)
            checkedThumbSize(24.dp)
            borderWidth(2.dp)
            checkedTrackColor(theme.colors.primary)
            checkedThumbColor(theme.colors.onPrimary)
            uncheckedTrackColor(theme.colors.surfaceContainerHighest)
            uncheckedThumbColor(theme.colors.outline)
            uncheckedBorderColor(theme.colors.outline)
            disabledAlpha(0.38f)
            animationSpec(tween(150))
        }

        val Adaptive = Default then SwitchStyle {
            laptop {
                trackWidth(34.dp)
                trackHeight(20.dp)
                uncheckedThumbSize(10.dp)
                checkedThumbSize(16.dp)
                borderWidth(1.5.dp)
            }
        }
    }
}

infix fun SwitchStyle.then(other: SwitchStyle): SwitchStyle = when {
    other === SwitchStyle -> this

    this === SwitchStyle -> other

    else -> SwitchStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
class SwitchStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<SwitchStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var trackWidth: Dp = Dp.Unspecified
        private set

    var trackHeight: Dp = Dp.Unspecified
        private set

    var uncheckedThumbSize: Dp = Dp.Unspecified
        private set

    var checkedThumbSize: Dp = Dp.Unspecified
        private set

    var borderWidth: Dp = Dp.Unspecified
        private set

    var checkedTrackColor: Color = Color.Unspecified
        private set

    var checkedThumbColor: Color = Color.Unspecified
        private set

    var uncheckedTrackColor: Color = Color.Unspecified
        private set

    var uncheckedThumbColor: Color = Color.Unspecified
        private set

    var uncheckedBorderColor: Color = Color.Unspecified
        private set

    var disabledAlpha: Float = 1f
        private set

    var animationSpec: AnimationSpec<Float> = snap()
        private set

    fun trackWidth(width: Dp) {
        trackWidth = width
    }

    fun trackHeight(height: Dp) {
        trackHeight = height
    }

    fun uncheckedThumbSize(size: Dp) {
        uncheckedThumbSize = size
    }

    fun checkedThumbSize(size: Dp) {
        checkedThumbSize = size
    }

    fun borderWidth(width: Dp) {
        borderWidth = width
    }

    fun checkedTrackColor(color: Color) {
        checkedTrackColor = color
    }

    fun checkedThumbColor(color: Color) {
        checkedThumbColor = color
    }

    fun uncheckedTrackColor(color: Color) {
        uncheckedTrackColor = color
    }

    fun uncheckedThumbColor(color: Color) {
        uncheckedThumbColor = color
    }

    fun uncheckedBorderColor(color: Color) {
        uncheckedBorderColor = color
    }

    fun disabledAlpha(alpha: Float) {
        disabledAlpha = alpha
    }

    fun animationSpec(spec: AnimationSpec<Float>) {
        animationSpec = spec
    }
}
