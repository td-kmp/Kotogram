package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.PreviewKotogram
import com.xbot.kotogram.core.designsystem.PreviewKotogramLaptop
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import kotlinx.coroutines.launch

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

// Copied from Compose Material 3's Switch.kt (Apache-2.0), with the style's sizes and colours in place of its tokens.
internal object DefaultSwitchOverride : SwitchOverride {
    @Composable
    override fun SwitchOverrideScope.Switch() {
        val interactionSource = remember { MutableInteractionSource() }
        val toggle = onCheckedChange?.let { onChange ->
            Modifier
                .minimumInteractiveComponentSize()
                .toggleable(
                    value = checked,
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onChange,
                )
        } ?: Modifier
        val trackColor = when {
            !enabled && checked -> style.disabledCheckedTrackColor
            !enabled -> style.disabledUncheckedTrackColor
            checked -> style.checkedTrackColor
            else -> style.uncheckedTrackColor
        }
        val borderColor = when {
            checked -> Color.Transparent
            enabled -> style.uncheckedBorderColor
            else -> style.disabledUncheckedBorderColor
        }
        val thumbColor = when {
            !enabled && checked -> style.disabledCheckedThumbColor
            !enabled -> style.disabledUncheckedThumbColor
            checked -> style.checkedThumbColor
            else -> style.uncheckedThumbColor
        }
        Box(
            modifier
                .then(toggle)
                .wrapContentSize(Alignment.Center)
                .requiredSize(style.trackWidth, style.trackHeight)
                .border(style.borderWidth, borderColor, CircleShape)
                .background(trackColor, CircleShape),
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .then(ThumbElement(interactionSource, checked, style))
                    .indication(interactionSource, ripple(bounded = false, radius = style.stateLayerSize / 2))
                    .background(thumbColor, CircleShape),
            )
        }
    }
}

private data class ThumbElement(
    val interactionSource: InteractionSource,
    val checked: Boolean,
    val style: SwitchStyleScope,
) : ModifierNodeElement<ThumbNode>() {
    override fun create() = ThumbNode(interactionSource, checked, style)

    override fun update(node: ThumbNode) {
        node.interactionSource = interactionSource
        if (node.checked != checked || node.style !== style) node.invalidateMeasurement()
        node.checked = checked
        node.style = style
        node.update()
    }
}

private class ThumbNode(
    var interactionSource: InteractionSource,
    var checked: Boolean,
    var style: SwitchStyleScope,
) : Modifier.Node(),
    LayoutModifierNode {
    override val shouldAutoInvalidate: Boolean get() = false

    private var isPressed = false
    private var offsetAnim: Animatable<Float, AnimationVector1D>? = null
    private var sizeAnim: Animatable<Float, AnimationVector1D>? = null
    private var initialOffset: Float = Float.NaN
    private var initialSize: Float = Float.NaN

    override fun onAttach() {
        coroutineScope.launch {
            var pressCount = 0
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> pressCount++
                    is PressInteraction.Release -> pressCount--
                    is PressInteraction.Cancel -> pressCount--
                }
                val pressed = pressCount > 0
                if (isPressed != pressed) {
                    isPressed = pressed
                    invalidateMeasurement()
                }
            }
        }
    }

    override fun onReset() {
        super.onReset()
        offsetAnim = null
        sizeAnim = null
        initialSize = Float.NaN
        initialOffset = Float.NaN
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val size = when {
            isPressed -> style.pressedThumbSize
            checked -> style.checkedThumbSize
            else -> style.uncheckedThumbSize
        }.toPx()
        val actualSize = (sizeAnim?.value ?: size).toInt()
        val placeable = measurable.measure(Constraints.fixed(actualSize, actualSize))
        val minBound = ((style.trackHeight - size.toDp()) / 2f).toPx()
        val thumbPadding = (style.trackHeight - style.checkedThumbSize) / 2
        val maxBound = (style.trackWidth - style.checkedThumbSize - thumbPadding).toPx()
        val offset = when {
            isPressed && checked -> maxBound - style.borderWidth.toPx()
            isPressed && !checked -> style.borderWidth.toPx()
            checked -> maxBound
            else -> minBound
        }
        val spec = if (isPressed) snap() else style.animationSpec
        if (sizeAnim?.targetValue != size) {
            coroutineScope.launch { sizeAnim?.animateTo(size, spec) }
        }
        if (offsetAnim?.targetValue != offset) {
            coroutineScope.launch { offsetAnim?.animateTo(offset, spec) }
        }
        if (initialSize.isNaN() && initialOffset.isNaN()) {
            initialSize = size
            initialOffset = offset
        }
        return layout(actualSize, actualSize) {
            placeable.placeRelative(offsetAnim?.value?.toInt() ?: offset.toInt(), 0)
        }
    }

    fun update() {
        if (sizeAnim == null && !initialSize.isNaN()) sizeAnim = Animatable(initialSize)
        if (offsetAnim == null && !initialOffset.isNaN()) offsetAnim = Animatable(initialOffset)
    }
}

internal val LocalSwitchOverride: ProvidableCompositionLocal<SwitchOverride> =
    staticCompositionLocalOf { DefaultSwitchOverride }

@PreviewKotogram
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

@PreviewKotogramLaptop
@Composable
private fun SwitchLaptopPreview() = SwitchPreview()

@Suppress("MagicNumber")
fun interface SwitchStyle : ComponentStyle<SwitchStyleScope> {
    companion object : SwitchStyle {
        override fun SwitchStyleScope.applyStyle() {}

        val Default = SwitchStyle {
            val colors = theme.colors
            trackWidth(52.dp)
            trackHeight(32.dp)
            uncheckedThumbSize(16.dp)
            checkedThumbSize(24.dp)
            pressedThumbSize(28.dp)
            stateLayerSize(40.dp)
            borderWidth(2.dp)
            checkedTrackColor(colors.primary)
            checkedThumbColor(colors.onPrimary)
            uncheckedTrackColor(colors.surfaceContainerHighest)
            uncheckedThumbColor(colors.outline)
            uncheckedBorderColor(colors.outline)
            disabledCheckedTrackColor(colors.onSurface.copy(alpha = 0.12f).compositeOver(colors.surface))
            disabledCheckedThumbColor(colors.surface)
            disabledUncheckedTrackColor(
                colors.surfaceContainerHighest.copy(alpha = 0.12f).compositeOver(colors.surface),
            )
            disabledUncheckedThumbColor(colors.onSurface.copy(alpha = 0.38f).compositeOver(colors.surface))
            disabledUncheckedBorderColor(colors.onSurface.copy(alpha = 0.12f).compositeOver(colors.surface))
            animationSpec(spring(dampingRatio = 0.9f, stiffness = 1400f))
        }

        val Adaptive = Default then SwitchStyle {
            laptop {
                trackWidth(34.dp)
                trackHeight(20.dp)
                uncheckedThumbSize(10.dp)
                checkedThumbSize(16.dp)
                pressedThumbSize(17.dp)
                stateLayerSize(28.dp)
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

    var pressedThumbSize: Dp = Dp.Unspecified
        private set

    var stateLayerSize: Dp = Dp.Unspecified
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

    var disabledCheckedTrackColor: Color = Color.Unspecified
        private set

    var disabledCheckedThumbColor: Color = Color.Unspecified
        private set

    var disabledUncheckedTrackColor: Color = Color.Unspecified
        private set

    var disabledUncheckedThumbColor: Color = Color.Unspecified
        private set

    var disabledUncheckedBorderColor: Color = Color.Unspecified
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

    fun pressedThumbSize(size: Dp) {
        pressedThumbSize = size
    }

    fun stateLayerSize(size: Dp) {
        stateLayerSize = size
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

    fun disabledCheckedTrackColor(color: Color) {
        disabledCheckedTrackColor = color
    }

    fun disabledCheckedThumbColor(color: Color) {
        disabledCheckedThumbColor = color
    }

    fun disabledUncheckedTrackColor(color: Color) {
        disabledUncheckedTrackColor = color
    }

    fun disabledUncheckedThumbColor(color: Color) {
        disabledUncheckedThumbColor = color
    }

    fun disabledUncheckedBorderColor(color: Color) {
        disabledUncheckedBorderColor = color
    }

    fun animationSpec(spec: AnimationSpec<Float>) {
        animationSpec = spec
    }
}
