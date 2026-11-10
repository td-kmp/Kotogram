package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt

// Adapted from AOSP's android.widget.FastScroller as Widget.Material.FastScroll draws it; a touch takes the thumb alone, as RecyclerView's.
@Composable
fun ScrollThumb(
    state: LazyListState,
    sections: List<FastScrollerSection>,
    modifier: Modifier = Modifier,
    style: ScrollThumbStyle = ScrollThumbStyle,
) {
    @Suppress("NAME_SHADOWING")
    val style = rememberStyle(::ScrollThumbStyleScope, ScrollThumbStyle.Default then style)
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val currentSections by rememberUpdatedState(sections)
    val scroller = remember(state) { ThumbScroller(state, scope, haptics) { currentSections } }
    context(style) {
        ShowWhileScrolling(state, scroller)
        val shown = animateFloatAsState(
            targetValue = if (scroller.visible) 1f else 0f,
            animationSpec = if (scroller.visible) style.showSpec else style.hideSpec,
        )
        val previewed = scroller.dragging && scroller.section != null
        val bubbleAlpha = animateFloatAsState(
            targetValue = if (previewed) 1f else 0f,
            animationSpec = if (previewed) style.showSpec else style.hideSpec,
        )
        val grabbable by remember { derivedStateOf { shown.value > 0f } }
        val bubbleShown by remember { derivedStateOf { bubbleAlpha.value > 0f } }
        val slide = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
        val decor = Modifier.graphicsLayer {
            alpha = shown.value
            translationX = (1f - shown.value) * style.thumbWidth.toPx() * slide
        }
        val thumbColor = if (scroller.dragging) style.activeThumbColor else style.thumbColor
        Layout(
            content = {
                Box(Modifier.layoutId(ThumbSlot.Track).then(decor).background(style.trackColor))
                Box(Modifier.layoutId(ThumbSlot.Thumb).then(decor).background(thumbColor, style.thumbShape))
                if (grabbable) Box(Modifier.layoutId(ThumbSlot.Target).thumbDrag(scroller))
                if (previewed || bubbleShown) {
                    Bubble(scroller.preview, { bubbleAlpha.value }, Modifier.layoutId(ThumbSlot.Bubble))
                }
            },
            modifier = modifier.width(style.touchTarget),
            measurePolicy = remember(scroller, style) { ThumbMeasurePolicy(scroller, style) },
        )
    }
}

@Composable
context(style: ScrollThumbStyleScope)
private fun ShowWhileScrolling(
    state: LazyListState,
    scroller: ThumbScroller,
) {
    val longList by remember(state, style) {
        derivedStateOf {
            val info = state.layoutInfo
            val visible = info.visibleItemsInfo.size
            visible > 0 && info.totalItemsCount / visible >= style.minPages
        }
    }
    LaunchedEffect(scroller, longList, style) {
        if (!longList) {
            scroller.visible = false
            return@LaunchedEffect
        }
        snapshotFlow { state.firstVisibleItemIndex to scroller.dragging }.collectLatest { (_, dragging) ->
            scroller.visible = true
            if (!dragging) {
                delay(style.hideDelayMillis)
                scroller.visible = false
            }
        }
    }
    LaunchedEffect(scroller) {
        snapshotFlow { scroller.dragging }.collectLatest { dragging -> if (!dragging) scroller.followList() }
    }
}

private enum class ThumbSlot { Track, Thumb, Target, Bubble }

private class ThumbMeasurePolicy(
    private val scroller: ThumbScroller,
    private val style: ScrollThumbStyleScope,
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val width = constraints.constrainWidth(style.touchTarget.roundToPx())
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else constraints.minHeight
        val thumbWidth = style.thumbWidth.roundToPx()
        val thumbHeight = style.thumbHeight.roundToPx().coerceAtMost(height)
        val targetHeight = maxOf(thumbHeight, style.touchTarget.roundToPx())
        val track = measurables.slot(ThumbSlot.Track)?.measure(Constraints.fixed(thumbWidth, height))
        val thumb = measurables.slot(ThumbSlot.Thumb)?.measure(Constraints.fixed(thumbWidth, thumbHeight))
        val target = measurables.slot(ThumbSlot.Target)?.measure(Constraints.fixed(width, targetHeight))
        val bubble = measurables.slot(ThumbSlot.Bubble)?.measure(Constraints())
        scroller.trackHeight = height
        scroller.thumbHeight = thumbHeight
        return layout(width, height) {
            val centre = scroller.thumbCentre
            val thumbStart = width - thumbWidth
            scroller.targetTop = (centre - targetHeight / 2f).roundToInt()
            track?.placeRelative(thumbStart, 0)
            thumb?.placeRelative(thumbStart, (centre - thumbHeight / 2f).roundToInt())
            target?.placeRelative(0, scroller.targetTop)
            bubble?.placeRelative(
                x = thumbStart - style.bubbleGap.roundToPx() - bubble.width,
                y = (centre - bubble.height).roundToInt().coerceAtMost(height - bubble.height).coerceAtLeast(0),
            )
        }
    }

    private fun List<Measurable>.slot(slot: ThumbSlot): Measurable? = firstOrNull { it.layoutId == slot }
}

// Hack: the touched box moves with the thumb, so a pointer's place in it says nothing; its place on the track does.
private fun Modifier.thumbDrag(scroller: ThumbScroller): Modifier = pointerInput(scroller) {
    awaitEachGesture {
        val down = awaitFirstDown()
        down.consume()
        scroller.press()
        try {
            drag(down.id) { change ->
                change.consume()
                scroller.dragTo(scroller.targetTop + change.position.y)
            }
        } finally {
            scroller.release()
        }
    }
}

@Composable
context(style: ScrollThumbStyleScope)
private fun Bubble(
    title: String,
    alpha: () -> Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha() }
            .defaultMinSize(style.bubbleMinSize, style.bubbleMinSize)
            .background(style.bubbleColor, style.bubbleShape)
            .padding(horizontal = style.bubblePadding),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(title, animationSpec = style.sectionSpec) { Text(it, maxLines = 1, style = style.bubbleTextStyle) }
    }
}

@Stable
private class ThumbScroller(
    private val list: LazyListState,
    private val scope: CoroutineScope,
    private val haptics: HapticFeedback,
    private val sections: () -> List<FastScrollerSection>,
) {
    var visible by mutableStateOf(false)

    var dragging by mutableStateOf(false)
        private set

    var section by mutableStateOf<FastScrollerSection?>(null)
        private set

    var preview by mutableStateOf("")
        private set

    var trackHeight = 0

    var thumbHeight = 0

    var targetTop = 0

    private var dragPosition by mutableFloatStateOf(0f)

    private var dropped by mutableStateOf(false)

    private var scroll: Job? = null

    private val range: Int
        get() = trackHeight - thumbHeight

    val thumbCentre: Float
        get() {
            val position = if (dragging || dropped) dragPosition else list.thumbPosition(sections())
            return position * range + thumbHeight / 2f
        }

    fun press() {
        if (!dropped) dragPosition = list.thumbPosition(sections())
        dragging = true
    }

    fun dragTo(y: Float) {
        val position = if (range <= 0) 0f else ((y - thumbHeight / 2f) / range).coerceIn(0f, 1f)
        dragPosition = position
        val total = list.layoutInfo.totalItemsCount
        if (total == 0) return
        val all = sections()
        val current = if (all.size > 1) all[(position * all.size).toInt().coerceIn(0, all.lastIndex)] else null
        if (current != section) {
            section = current
            if (current != null) {
                preview = current.title
                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
            }
        }
        scroll?.cancel()
        scroll = scope.launch { list.scrollToItem(itemAt(position, all, total)) }
    }

    fun release() {
        dragging = false
        section = null
        dropped = true
    }

    // Adapted from FastScroller.onScroll: a dropped thumb stays where the finger left it until the list moves.
    suspend fun followList() {
        scroll?.join()
        val index = list.firstVisibleItemIndex
        val offset = list.firstVisibleItemScrollOffset
        snapshotFlow { list.firstVisibleItemIndex != index || list.firstVisibleItemScrollOffset != offset }.first { it }
        dropped = false
    }
}

private fun itemAt(
    position: Float,
    sections: List<FastScrollerSection>,
    total: Int,
): Int {
    val index = if (sections.size > 1) {
        val place = position * sections.size
        val section = place.toInt().coerceIn(0, sections.lastIndex)
        val start = sections[section].index
        val end = sections.getOrNull(section + 1)?.index ?: total
        start + ((end - start) * (place - section)).toInt()
    } else {
        (position * total).toInt()
    }
    return index.coerceIn(0, total - 1)
}

private fun LazyListState.thumbPosition(sections: List<FastScrollerSection>): Float {
    val info = layoutInfo
    val visible = info.visibleItemsInfo
    val total = info.totalItemsCount
    val first = firstVisibleItemIndex
    val position = when {
        visible.isEmpty() || total == 0 -> 0f

        sections.isEmpty() -> if (visible.size == total) 0f else first.toFloat() / (total - visible.size)

        else -> {
            val firstSize = visible.firstOrNull { it.index == first }?.size ?: 0
            val hidden = if (firstSize == 0) 0f else firstVisibleItemScrollOffset.toFloat() / firstSize
            val section = sections.indexOfLast { it.index <= first }.coerceAtLeast(0)
            val start = sections[section].index
            val end = sections.getOrNull(section + 1)?.index ?: total
            val within = if (end == start) 0f else (first + hidden - start) / (end - start)
            val placed = (section + within) / sections.size
            // Adapted from FastScroller.getPosFromItemCount: the last row's way into view takes the thumb the rest of the track.
            val last = visible.last()
            val lastShown = info.viewportEndOffset - info.afterContentPadding - last.offset
            val atEnd = first > 0 && last.index == total - 1
            if (atEnd && lastShown > 0 && last.size > 0) placed + (1f - placed) * lastShown / last.size else placed
        }
    }
    return position.coerceIn(0f, 1f)
}

// Adapted from AOSP's AccelerateDecelerateInterpolator, the default of the platform's animators.
private val AccelerateDecelerate = Easing { ((cos((it + 1) * PI) / 2) + 0.5).toFloat() }

@Suppress("MagicNumber")
fun interface ScrollThumbStyle : ComponentStyle<ScrollThumbStyleScope> {
    companion object : ScrollThumbStyle {
        override fun ScrollThumbStyleScope.applyStyle() {}

        val Default = ScrollThumbStyle {
            touchTarget(48.dp)
            thumbWidth(8.dp)
            thumbHeight(48.dp)
            thumbShape(RectangleShape)
            thumbColor(theme.colors.onSurfaceVariant)
            activeThumbColor(theme.colors.primary)
            trackColor(theme.colors.onSurfaceVariant.copy(alpha = 0.22f))
            bubbleMinSize(88.dp)
            bubblePadding(22.dp)
            bubbleShape(RoundedCornerShape(topStart = 44.dp, topEnd = 44.dp, bottomEnd = 0.dp, bottomStart = 44.dp))
            bubbleColor(theme.colors.primary)
            bubbleTextStyle(theme.typography.displayMedium.merge(color = theme.colors.onPrimary))
            bubbleGap(16.dp)
            showSpec(tween(150, easing = AccelerateDecelerate))
            hideSpec(tween(300, easing = AccelerateDecelerate))
            sectionSpec(tween(50, easing = AccelerateDecelerate))
            hideDelayMillis(1500)
            minPages(4)
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
class ScrollThumbStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<ScrollThumbStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var touchTarget: Dp = Dp.Unspecified
        private set

    var thumbWidth: Dp = Dp.Unspecified
        private set

    var thumbHeight: Dp = Dp.Unspecified
        private set

    var thumbShape: Shape = RectangleShape
        private set

    var thumbColor: Color = Color.Unspecified
        private set

    var activeThumbColor: Color = Color.Unspecified
        private set

    var trackColor: Color = Color.Unspecified
        private set

    var bubbleMinSize: Dp = Dp.Unspecified
        private set

    var bubblePadding: Dp = Dp.Unspecified
        private set

    var bubbleShape: Shape = RectangleShape
        private set

    var bubbleColor: Color = Color.Unspecified
        private set

    var bubbleTextStyle: TextStyle = TextStyle.Default
        private set

    var bubbleGap: Dp = Dp.Unspecified
        private set

    var showSpec: AnimationSpec<Float> = snap()
        private set

    var hideSpec: AnimationSpec<Float> = snap()
        private set

    var sectionSpec: FiniteAnimationSpec<Float> = snap()
        private set

    var hideDelayMillis: Long = 0
        private set

    var minPages: Int = 0
        private set

    fun touchTarget(size: Dp) {
        touchTarget = size
    }

    fun thumbWidth(width: Dp) {
        thumbWidth = width
    }

    fun thumbHeight(height: Dp) {
        thumbHeight = height
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

    fun trackColor(color: Color) {
        trackColor = color
    }

    fun bubbleMinSize(size: Dp) {
        bubbleMinSize = size
    }

    fun bubblePadding(padding: Dp) {
        bubblePadding = padding
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

    fun showSpec(spec: AnimationSpec<Float>) {
        showSpec = spec
    }

    fun hideSpec(spec: AnimationSpec<Float>) {
        hideSpec = spec
    }

    fun sectionSpec(spec: FiniteAnimationSpec<Float>) {
        sectionSpec = spec
    }

    fun hideDelayMillis(millis: Long) {
        hideDelayMillis = millis
    }

    fun minPages(pages: Int) {
        minPages = pages
    }
}
