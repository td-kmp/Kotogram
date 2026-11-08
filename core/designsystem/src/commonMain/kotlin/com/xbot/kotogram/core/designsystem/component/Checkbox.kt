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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramLaptopPreview
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.designsystem.theme.currentPlatform

// AppKit's checkbox on macOS; drawn by Compose elsewhere, iOS included, since UIKit has no checkbox outside Catalyst.
@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: CheckboxStyle = CheckboxStyle
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
    val style: CheckboxStyleScope
)

internal object DefaultCheckboxOverride : CheckboxOverride {
    @Composable
    override fun CheckboxOverrideScope.Checkbox() {
        val progress = animateFloatAsState(if (checked) 1f else 0f, style.animationSpec)
        val toggle = onCheckedChange?.let { onChange ->
            Modifier
                .minimumInteractiveComponentSize()
                .toggleable(
                    value = checked,
                    interactionSource = null,
                    indication = null,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = onChange
                )
        } ?: Modifier
        Spacer(
            modifier
                .then(toggle)
                .size(style.size)
                .alpha(if (enabled) 1f else style.disabledAlpha)
                .drawWithCache {
                    val border = style.borderWidth.toPx()
                    val container = style.shape.createOutline(size, layoutDirection, this)
                    val outline = style.shape.createOutline(
                        Size(size.width - border, size.height - border),
                        layoutDirection,
                        this
                    )
                    val check = PathMeasure().apply {
                        setPath(
                            Path().apply {
                                moveTo(size.width * CHECK_START_X, size.height * CHECK_START_Y)
                                lineTo(size.width * CHECK_CORNER_X, size.height * CHECK_CORNER_Y)
                                lineTo(size.width * CHECK_END_X, size.height * CHECK_END_Y)
                            },
                            false
                        )
                    }
                    val drawn = Path()
                    val checkStroke = Stroke(style.checkWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    onDrawBehind {
                        val fraction = progress.value
                        if (fraction < 1f) {
                            translate(border / 2, border / 2) {
                                drawOutline(outline, style.borderColor, alpha = 1f - fraction, style = Stroke(border))
                            }
                        }
                        if (fraction > 0f) {
                            drawOutline(container, style.checkedColor, alpha = fraction)
                            drawn.reset()
                            check.getSegment(0f, check.length * fraction, drawn, true)
                            drawPath(drawn, style.checkmarkColor, style = checkStroke)
                        }
                    }
                }
        )
    }
}

internal val LocalCheckboxOverride: ProvidableCompositionLocal<CheckboxOverride> =
    staticCompositionLocalOf { DefaultCheckboxOverride }

@KotogramPreview
@Composable
private fun CheckboxPreview() {
    Row(
        modifier = Modifier.padding(KotogramTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.large)
    ) {
        Checkbox(checked = false, onCheckedChange = {})
        Checkbox(checked = true, onCheckedChange = {})
        Checkbox(checked = true, onCheckedChange = {}, enabled = false)
    }
}

@KotogramLaptopPreview
@Composable
private fun CheckboxLaptopPreview() = CheckboxPreview()

private const val CHECK_START_X = 0.27f
private const val CHECK_START_Y = 0.52f
private const val CHECK_CORNER_X = 0.43f
private const val CHECK_CORNER_Y = 0.68f
private const val CHECK_END_X = 0.74f
private const val CHECK_END_Y = 0.36f

// On touch screens Material 3's 18 rounded square, and iOS's 22 circle of a selected row on Apple's platforms; our
// own 14 square on a laptop.
@Suppress("MagicNumber")
fun interface CheckboxStyle : ComponentStyle<CheckboxStyleScope> {
    companion object : CheckboxStyle {
        override fun CheckboxStyleScope.applyStyle() {}

        val Default = CheckboxStyle {
            val apple = currentPlatform.isApple
            size(if (apple) 22.dp else 18.dp)
            shape(if (apple) CircleShape else RoundedCornerShape(2.dp))
            borderWidth(if (apple) 1.5.dp else 2.dp)
            checkWidth(if (apple) 1.8.dp else 2.dp)
            checkedColor(theme.colors.primary)
            checkmarkColor(theme.colors.onPrimary)
            borderColor(theme.colors.onSurfaceVariant)
            disabledAlpha(0.38f)
            animationSpec(tween(150))
        }

        val Adaptive = Default then CheckboxStyle {
            laptop {
                size(14.dp)
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
class CheckboxStyleScope(val theme: KotogramTheme.Values, override val mediaQueryInfo: MediaQueryInfo) :
    AdaptiveStyleScope<CheckboxStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var size: Dp = Dp.Unspecified
        private set

    var shape: Shape = RectangleShape
        private set

    var borderWidth: Dp = Dp.Unspecified
        private set

    var checkWidth: Dp = Dp.Unspecified
        private set

    var checkedColor: Color = Color.Unspecified
        private set

    var checkmarkColor: Color = Color.Unspecified
        private set

    var borderColor: Color = Color.Unspecified
        private set

    var disabledAlpha: Float = 1f
        private set

    var animationSpec: AnimationSpec<Float> = snap()
        private set

    fun size(size: Dp) {
        this.size = size
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

    fun checkedColor(color: Color) {
        checkedColor = color
    }

    fun checkmarkColor(color: Color) {
        checkmarkColor = color
    }

    fun borderColor(color: Color) {
        borderColor = color
    }

    fun disabledAlpha(alpha: Float) {
        disabledAlpha = alpha
    }

    fun animationSpec(spec: AnimationSpec<Float>) {
        animationSpec = spec
    }
}
