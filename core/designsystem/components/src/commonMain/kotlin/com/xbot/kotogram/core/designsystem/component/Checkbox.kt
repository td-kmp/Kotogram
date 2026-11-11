package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.State
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.PreviewKotogram
import com.xbot.kotogram.core.designsystem.PreviewKotogramLaptop
import com.xbot.kotogram.core.designsystem.extensions.cross
import com.xbot.kotogram.core.designsystem.extensions.degrees
import com.xbot.kotogram.core.designsystem.extensions.dot
import com.xbot.kotogram.core.designsystem.extensions.lineTo
import com.xbot.kotogram.core.designsystem.extensions.moveTo
import com.xbot.kotogram.core.designsystem.extensions.normalized
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.designsystem.theme.currentPlatform

@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: CheckboxStyle = CheckboxStyle,
) {
    val checkboxStyle = rememberStyle(::CheckboxStyleScope, CheckboxStyle.Adaptive then style)
    with(LocalCheckboxOverride.current) {
        CheckboxOverrideScope(checked, onCheckedChange, modifier, enabled, checkboxStyle).Checkbox()
    }
}

internal interface CheckboxOverride {
    @Composable
    fun CheckboxOverrideScope.Checkbox()
}

internal class CheckboxOverrideScope(
    val checked: Boolean,
    val onCheckedChange: ((Boolean) -> Unit)?,
    val modifier: Modifier,
    val enabled: Boolean,
    val style: CheckboxStyleScope,
)

// Adapted from Material Components for Android's checkbox drawables (mtrl_checkbox_button_*, Apache-2.0).
internal object DefaultCheckboxOverride : CheckboxOverride {
    @Composable
    override fun CheckboxOverrideScope.Checkbox() {
        val transition = updateTransition(checked)
        val motion = transition.animateCheckbox(style)
        val holdsCheckedColor = transition.currentState || transition.targetState
        val boxColor = when {
            enabled && holdsCheckedColor -> style.checkedColor
            enabled -> style.borderColor
            holdsCheckedColor -> style.disabledCheckedColor
            else -> style.disabledUncheckedColor
        }
        val checkmarkColor = if (enabled) style.checkmarkColor else style.disabledCheckmarkColor
        val toggle = onCheckedChange?.let { onChange ->
            Modifier
                .minimumInteractiveComponentSize()
                .toggleable(
                    value = checked,
                    interactionSource = null,
                    indication = ripple(bounded = false, radius = style.stateLayerSize / 2),
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = onChange,
                )
        } ?: Modifier
        Spacer(
            modifier
                .then(toggle)
                .wrapContentSize(Alignment.Center)
                .requiredSize(style.size)
                .drawCheckbox(style, motion, checked, boxColor, checkmarkColor),
        )
    }
}

private class CheckboxMotion(
    val zoom: State<Float>,
    val boxAlpha: State<Float>,
    val checkAlpha: State<Float>,
    val checkTrim: State<Float>,
)

@Composable
private fun Transition<Boolean>.animateCheckbox(style: CheckboxStyleScope) = CheckboxMotion(
    zoom = animateFloat({ if (targetState) style.enterSpec else style.exitSpec }) {
        if (it) 1f else style.collapsedScale
    },
    boxAlpha = animateFloat({ style.fadeSpec }) { if (it) 1f else 0f },
    checkAlpha = animateFloat({ if (targetState) snap() else style.fadeSpec }) { if (it) 1f else 0f },
    checkTrim = animateFloat({ if (targetState) style.enterSpec else snap() }) { if (it) 1f else 0f },
)

private fun Modifier.drawCheckbox(
    style: CheckboxStyleScope,
    motion: CheckboxMotion,
    checked: Boolean,
    boxColor: Color,
    checkmarkColor: Color,
): Modifier = drawWithCache {
    val box = Path().apply { addOutline(style.shape.createOutline(size, layoutDirection, this@drawWithCache)) }
    // Hack: clipped to the shape, a stroke of twice the border is the shape's inset ring.
    val ring = Stroke(style.borderWidth.toPx() * 2)
    val check = PathMeasure().apply { setPath(checkmarkOutline(style), false) }
    val drawn = Path()
    val layer = Paint()
    onDrawBehind {
        drawTinted(boxColor, layer) { color ->
            if (ring.width > 0f) clipPath(box) { drawPath(box, color, style = ring) }
            scale(motion.zoom.value) { drawPath(box, color, alpha = motion.boxAlpha.value) }
        }
        val trim = if (checked) motion.checkTrim.value else 1f
        if (trim > 0f && motion.checkAlpha.value > 0f) {
            drawn.rewind()
            check.getSegment(0f, check.length * trim, drawn, true)
            scale(motion.zoom.value) { drawPath(drawn, checkmarkColor, alpha = motion.checkAlpha.value) }
        }
    }
}

internal val LocalCheckboxOverride: ProvidableCompositionLocal<CheckboxOverride> =
    staticCompositionLocalOf { DefaultCheckboxOverride }

// Workaround: translucent paths that overlap darken each other, so they are drawn opaque into one translucent layer.
private inline fun DrawScope.drawTinted(
    color: Color,
    layer: Paint,
    draw: DrawScope.(Color) -> Unit,
) {
    if (color.alpha >= 1f) {
        draw(color)
        return
    }
    drawIntoCanvas { canvas ->
        layer.alpha = color.alpha
        canvas.saveLayer(Rect(Offset.Zero, size), layer)
        draw(color.copy(alpha = 1f))
        canvas.restore()
    }
}

// Hack: the outline starts at the check's inner corner, as the view's path does, so trimming it draws the check in.
private fun CacheDrawScope.checkmarkOutline(style: CheckboxStyleScope): Path {
    val start = Offset(size.width * style.checkStart.x, size.height * style.checkStart.y)
    val corner = Offset(size.width * style.checkCorner.x, size.height * style.checkCorner.y)
    val end = Offset(size.width * style.checkEnd.x, size.height * style.checkEnd.y)
    val half = style.checkWidth.toPx() / 2
    val toCorner = (corner - start).normalized()
    val toEnd = (end - corner).normalized()
    val turn = if (toCorner cross toEnd < 0f) 1f else -1f
    val inner1 = Offset(toCorner.y, -toCorner.x) * turn
    val inner2 = Offset(toEnd.y, -toEnd.x) * turn
    val miter = (inner1 + inner2) * (half / (1f + (inner1 dot inner2)))
    val bend = Offset(inner1 dot inner2, inner1 cross inner2).degrees()
    val round = style.checkCap == StrokeCap.Round
    return Path().apply {
        moveTo(corner + miter)
        lineTo(start + inner1 * half)
        if (round) arcTo(Rect(start, half), inner1.degrees(), -HALF_TURN * turn, false)
        lineTo(start - inner1 * half)
        if (round) {
            lineTo(corner - inner1 * half)
            arcTo(Rect(corner, half), (-inner1).degrees(), bend, false)
        } else {
            lineTo(corner - miter)
        }
        lineTo(end - inner2 * half)
        if (round) arcTo(Rect(end, half), (-inner2).degrees(), -HALF_TURN * turn, false)
        lineTo(end + inner2 * half)
        close()
    }
}

@PreviewKotogram
@Composable
private fun CheckboxPreview() {
    Row(
        modifier = Modifier.padding(KotogramTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.large),
    ) {
        Checkbox(checked = false, onCheckedChange = {})
        Checkbox(checked = true, onCheckedChange = {})
        Checkbox(checked = true, onCheckedChange = {}, enabled = false)
    }
}

@PreviewKotogramLaptop
@Composable
private fun CheckboxLaptopPreview() = CheckboxPreview()

private const val HALF_TURN = 180f

@Suppress("MagicNumber")
private val EmphasizedDecelerate = CubicBezierEasing(0.1f, 0.7f, 0.1f, 1f)

@Suppress("MagicNumber")
private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.2f)

@Suppress("MagicNumber")
fun interface CheckboxStyle : ComponentStyle<CheckboxStyleScope> {
    companion object : CheckboxStyle {
        override fun CheckboxStyleScope.applyStyle() {}

        val Default = CheckboxStyle {
            val apple = currentPlatform.isApple
            size(if (apple) 22.dp else 18.dp)
            stateLayerSize(40.dp)
            shape(if (apple) CircleShape else RoundedCornerShape(2.dp))
            borderWidth(if (apple) 1.5.dp else 2.dp)
            checkWidth(if (apple) 1.8.dp else 2.dp)
            checkCap(if (apple) StrokeCap.Round else StrokeCap.Butt)
            if (apple) {
                checkmark(Offset(0.27f, 0.52f), Offset(0.43f, 0.68f), Offset(0.74f, 0.36f))
            } else {
                checkmark(Offset(3.7f / 18, 9.3f / 18), Offset(7f / 18, 12.6f / 18), Offset(14.3f / 18, 5.3f / 18))
            }
            checkedColor(theme.colors.primary)
            checkmarkColor(theme.colors.onPrimary)
            borderColor(theme.colors.onSurfaceVariant)
            disabledCheckedColor(theme.colors.onSurface.copy(alpha = 0.38f))
            disabledUncheckedColor(theme.colors.onSurfaceVariant.copy(alpha = 0.38f))
            disabledCheckmarkColor(theme.colors.surface)
            collapsedScale(0.6f)
            enterSpec(tween(350, easing = EmphasizedDecelerate))
            exitSpec(tween(150, easing = EmphasizedAccelerate))
            fadeSpec(tween(50, easing = LinearEasing))
        }

        val Adaptive = Default then CheckboxStyle {
            laptop {
                size(14.dp)
                stateLayerSize(28.dp)
                shape(RoundedCornerShape(3.dp))
                borderWidth(1.dp)
                checkWidth(1.5.dp)
            }
        }
    }
}

infix fun CheckboxStyle.then(other: CheckboxStyle): CheckboxStyle = when {
    other === CheckboxStyle -> this

    this === CheckboxStyle -> other

    else -> CheckboxStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
class CheckboxStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<CheckboxStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var size: Dp = Dp.Unspecified
        private set

    var stateLayerSize: Dp = Dp.Unspecified
        private set

    var shape: Shape = RectangleShape
        private set

    var borderWidth: Dp = Dp.Unspecified
        private set

    var checkWidth: Dp = Dp.Unspecified
        private set

    var checkCap: StrokeCap = StrokeCap.Butt
        private set

    var checkStart: Offset = Offset.Unspecified
        private set

    var checkCorner: Offset = Offset.Unspecified
        private set

    var checkEnd: Offset = Offset.Unspecified
        private set

    var checkedColor: Color = Color.Unspecified
        private set

    var checkmarkColor: Color = Color.Unspecified
        private set

    var borderColor: Color = Color.Unspecified
        private set

    var disabledCheckedColor: Color = Color.Unspecified
        private set

    var disabledUncheckedColor: Color = Color.Unspecified
        private set

    var disabledCheckmarkColor: Color = Color.Unspecified
        private set

    var collapsedScale: Float = 1f
        private set

    var enterSpec: FiniteAnimationSpec<Float> = snap()
        private set

    var exitSpec: FiniteAnimationSpec<Float> = snap()
        private set

    var fadeSpec: FiniteAnimationSpec<Float> = snap()
        private set

    fun size(size: Dp) {
        this.size = size
    }

    fun stateLayerSize(size: Dp) {
        stateLayerSize = size
    }

    fun shape(shape: Shape) {
        this.shape = shape
    }

    fun borderWidth(width: Dp) {
        borderWidth = width
    }

    fun checkWidth(width: Dp) {
        checkWidth = width
    }

    fun checkCap(cap: StrokeCap) {
        checkCap = cap
    }

    fun checkmark(
        start: Offset,
        corner: Offset,
        end: Offset,
    ) {
        checkStart = start
        checkCorner = corner
        checkEnd = end
    }

    fun checkedColor(color: Color) {
        checkedColor = color
    }

    fun checkmarkColor(color: Color) {
        checkmarkColor = color
    }

    fun borderColor(color: Color) {
        borderColor = color
    }

    fun disabledCheckedColor(color: Color) {
        disabledCheckedColor = color
    }

    fun disabledUncheckedColor(color: Color) {
        disabledUncheckedColor = color
    }

    fun disabledCheckmarkColor(color: Color) {
        disabledCheckmarkColor = color
    }

    fun collapsedScale(scale: Float) {
        collapsedScale = scale
    }

    fun enterSpec(spec: FiniteAnimationSpec<Float>) {
        enterSpec = spec
    }

    fun exitSpec(spec: FiniteAnimationSpec<Float>) {
        exitSpec = spec
    }

    fun fadeSpec(spec: FiniteAnimationSpec<Float>) {
        fadeSpec = spec
    }
}
