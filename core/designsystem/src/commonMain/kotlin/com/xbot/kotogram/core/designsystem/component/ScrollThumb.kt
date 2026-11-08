package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// A thumb at the end that follows the list; dragged, it scrolls the list and shows the section in a bubble.
@Composable
fun ScrollThumb(
    state: LazyListState,
    sections: List<FastScrollerSection>,
    modifier: Modifier = Modifier,
    style: ScrollThumbStyle = ScrollThumbStyle
) {
    @Suppress("NAME_SHADOWING")
    val style = rememberStyle(::ScrollThumbStyleScope, ScrollThumbStyle.Default then style)
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val currentSections by rememberUpdatedState(sections)
    var dragging by remember { mutableStateOf(false) }
    var section by remember { mutableStateOf<FastScrollerSection?>(null) }
    val progress = remember(state) { derivedStateOf { scrollProgress(state) } }
    val scrollable by remember(state) { derivedStateOf { state.canScrollForward || state.canScrollBackward } }
    if (!scrollable) return
    Layout(
        content = {
            Box(Modifier.background(if (dragging) style.activeThumbColor else style.thumbColor, style.thumbShape))
            section?.let { current ->
                Box(
                    modifier = Modifier.size(style.bubbleSize).background(style.bubbleColor, style.bubbleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(current.title, style = style.bubbleTextStyle)
                }
            }
        },
        modifier = modifier
            .width(style.touchWidth)
            .pointerInput(state) {
                var scroll: Job? = null
                awaitEachGesture {
                    var change = awaitFirstDown()
                    dragging = true
                    while (change.pressed) {
                        val total = state.layoutInfo.totalItemsCount
                        if (total > 0) {
                            val fraction = (change.position.y / size.height).coerceIn(0f, 1f)
                            val target = (fraction * (total - 1)).roundToInt()
                            val current = currentSections.lastOrNull { it.index <= target }
                            if (current != section) {
                                section = current
                                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            }
                            scroll?.cancel()
                            scroll = scope.launch { state.scrollToItem(target) }
                        }
                        change.consume()
                        val id = change.id
                        change = awaitPointerEvent().changes.firstOrNull { it.id == id } ?: break
                    }
                    dragging = false
                    section = null
                }
            }
    ) { measurables, constraints ->
        val thumbWidth = (if (dragging) style.activeThumbWidth else style.thumbWidth).roundToPx()
        val thumbHeight = (if (dragging) style.activeThumbHeight else style.thumbHeight).roundToPx()
        val thumb = measurables[0].measure(Constraints.fixed(thumbWidth, thumbHeight))
        val bubble = measurables.getOrNull(1)?.measure(Constraints())
        val width = constraints.constrainWidth(style.touchWidth.roundToPx())
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else thumbHeight
        val thumbStart = width - style.endPadding.roundToPx() - thumbWidth
        val thumbTop = ((height - thumbHeight) * progress.value).roundToInt()
        layout(width, height) {
            thumb.place(thumbStart, thumbTop)
            bubble?.place(
                thumbStart - style.bubbleGap.roundToPx() - bubble.width,
                thumbTop + thumbHeight / 2 - bubble.height
            )
        }
    }
}

// The first visible item's position among the items that can scroll by, with the pinned header left out.
private fun scrollProgress(state: LazyListState): Float = when {
    !state.canScrollBackward -> 0f
    !state.canScrollForward -> 1f
    else -> {
        val info = state.layoutInfo
        val first = info.visibleItemsInfo.firstOrNull { it.index == state.firstVisibleItemIndex }
        val scrolled = state.firstVisibleItemIndex + state.firstVisibleItemScrollOffset.toFloat() / (first?.size ?: 1)
        val scrollable = (info.totalItemsCount - info.visibleItemsInfo.size).coerceAtLeast(1)
        (scrolled / scrollable).coerceIn(0f, 1f)
    }
}

// Our own, after the fast scroll of Android's contacts: a pill that widens under the finger and a bubble
// pointing at it.
@Suppress("MagicNumber")
fun interface ScrollThumbStyle : ComponentStyle<ScrollThumbStyleScope> {
    companion object : ScrollThumbStyle {
        override fun ScrollThumbStyleScope.applyStyle() {}

        val Default = ScrollThumbStyle {
            touchWidth(24.dp)
            endPadding(4.dp)
            thumbWidth(4.dp)
            thumbHeight(40.dp)
            activeThumbWidth(8.dp)
            activeThumbHeight(48.dp)
            thumbShape(CircleShape)
            thumbColor(theme.colors.onSurfaceVariant.copy(alpha = 0.45f))
            activeThumbColor(theme.colors.primary)
            bubbleSize(56.dp)
            bubbleShape(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomEnd = 4.dp, bottomStart = 28.dp))
            bubbleColor(theme.colors.primary)
            bubbleTextStyle(
                theme.typography.headlineSmall.merge(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.colors.onPrimary
                )
            )
            bubbleGap(8.dp)
        }
    }
}

infix fun ScrollThumbStyle.then(other: ScrollThumbStyle): ScrollThumbStyle = when {
    other === ScrollThumbStyle -> this
    this === ScrollThumbStyle -> other
    else -> ScrollThumbStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
class ScrollThumbStyleScope(val theme: KotogramTheme.Values, override val mediaQueryInfo: MediaQueryInfo) :
    AdaptiveStyleScope<ScrollThumbStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var touchWidth: Dp = Dp.Unspecified
        private set

    var endPadding: Dp = Dp.Unspecified
        private set

    var thumbWidth: Dp = Dp.Unspecified
        private set

    var thumbHeight: Dp = Dp.Unspecified
        private set

    var activeThumbWidth: Dp = Dp.Unspecified
        private set

    var activeThumbHeight: Dp = Dp.Unspecified
        private set

    var thumbShape: Shape = RectangleShape
        private set

    var thumbColor: Color = Color.Unspecified
        private set

    var activeThumbColor: Color = Color.Unspecified
        private set

    var bubbleSize: Dp = Dp.Unspecified
        private set

    var bubbleShape: Shape = RectangleShape
        private set

    var bubbleColor: Color = Color.Unspecified
        private set

    var bubbleTextStyle: TextStyle = TextStyle.Default
        private set

    var bubbleGap: Dp = Dp.Unspecified
        private set

    fun touchWidth(width: Dp) {
        touchWidth = width
    }

    fun endPadding(padding: Dp) {
        endPadding = padding
    }

    fun thumbWidth(width: Dp) {
        thumbWidth = width
    }

    fun thumbHeight(height: Dp) {
        thumbHeight = height
    }

    fun activeThumbWidth(width: Dp) {
        activeThumbWidth = width
    }

    fun activeThumbHeight(height: Dp) {
        activeThumbHeight = height
    }

    fun thumbShape(shape: Shape) {
        thumbShape = shape
    }

    fun thumbColor(color: Color) {
        thumbColor = color
    }

    fun activeThumbColor(color: Color) {
        activeThumbColor = color
    }

    fun bubbleSize(size: Dp) {
        bubbleSize = size
    }

    fun bubbleShape(shape: Shape) {
        bubbleShape = shape
    }

    fun bubbleColor(color: Color) {
        bubbleColor = color
    }

    fun bubbleTextStyle(style: TextStyle) {
        bubbleTextStyle = style
    }

    fun bubbleGap(gap: Dp) {
        bubbleGap = gap
    }
}
