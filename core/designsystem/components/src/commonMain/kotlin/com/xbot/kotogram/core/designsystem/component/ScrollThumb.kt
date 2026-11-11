package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrainWidth
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Adapted from AOSP's android.widget.FastScroller as Widget.Material.FastScroll shows it; a touch takes the thumb alone, as RecyclerView's.
@Composable
context(style: FastScrollerStyleScope)
internal fun ScrollThumb(
    state: LazyListState,
    sections: List<FastScrollerSection>,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val currentSections by rememberUpdatedState(sections)
    val indicator = rememberScrollIndicatorState(state)
    val thumbSize = remember(indicator) { derivedStateOf { indicator.sizeFraction } }
    val scroller = remember(state) { ThumbScroller(state, scope, haptics) { currentSections } }
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
    val indicatorStyle = rememberIndicatorStyle(scroller.dragging)
    Layout(
        content = {
            context(indicatorStyle) {
                ScrollIndicatorBar(
                    position = { scroller.position },
                    size = { thumbSize.value },
                    modifier = Modifier.layoutId(ThumbSlot.Indicator).then(decor),
                )
            }
            if (grabbable) Box(Modifier.layoutId(ThumbSlot.Target).thumbDrag(scroller))
            if (previewed || bubbleShown) {
                Bubble(scroller.preview, { bubbleAlpha.value }, Modifier.layoutId(ThumbSlot.Bubble))
            }
        },
        modifier = modifier.width(style.touchTarget),
        measurePolicy = remember(scroller, thumbSize, style, indicatorStyle) {
            ThumbMeasurePolicy(scroller, thumbSize, style, indicatorStyle)
        },
    )
}

@Composable
context(style: FastScrollerStyleScope)
private fun rememberIndicatorStyle(dragging: Boolean): ScrollIndicatorStyleScope {
    val indicatorStyle = remember(style, dragging) {
        val width = style.thumbWidth
        val minHeight = style.minThumbHeight
        val activeColor = style.activeThumbColor
        ScrollIndicatorStyle.Default then ScrollIndicatorStyle {
            indicatorWidth(width)
            minIndicatorHeight(minHeight)
            if (dragging) indicatorColor(activeColor)
        }
    }
    return rememberStyle(::ScrollIndicatorStyleScope, indicatorStyle)
}

@Composable
context(style: FastScrollerStyleScope)
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
                delay(style.hideDelay)
                scroller.visible = false
            }
        }
    }
    LaunchedEffect(scroller) {
        snapshotFlow { scroller.dragging }.collectLatest { dragging -> if (!dragging) scroller.followList() }
    }
}

private enum class ThumbSlot { Indicator, Target, Bubble }

private class ThumbMeasurePolicy(
    private val scroller: ThumbScroller,
    private val thumbSize: State<Float>,
    private val style: FastScrollerStyleScope,
    private val indicatorStyle: ScrollIndicatorStyleScope,
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val width = constraints.constrainWidth(style.touchTarget.roundToPx())
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else constraints.minHeight
        val thumbHeight = context(indicatorStyle) { indicatorThumbLength(height.toFloat(), thumbSize.value) }
        val targetHeight = maxOf(thumbHeight.roundToInt(), style.touchTarget.roundToPx())
        val bar = measurables.slot(ThumbSlot.Indicator)?.measure(Constraints.fixedHeight(height))
        val target = measurables.slot(ThumbSlot.Target)?.measure(Constraints.fixed(width, targetHeight))
        val bubble = measurables.slot(ThumbSlot.Bubble)?.measure(Constraints())
        scroller.trackHeight = height.toFloat()
        scroller.thumbHeight = thumbHeight
        return layout(width, height) {
            val centre = scroller.thumbCentre
            val barStart = width - (bar?.width ?: 0)
            scroller.targetTop = (centre - targetHeight / 2f).roundToInt()
            bar?.placeRelative(barStart, 0)
            target?.placeRelative(0, scroller.targetTop)
            bubble?.placeRelative(
                x = barStart - style.bubbleGap.roundToPx() - bubble.width,
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
context(style: FastScrollerStyleScope)
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

    var trackHeight = 0f

    var thumbHeight = 0f

    var targetTop = 0

    private var dragPosition by mutableFloatStateOf(0f)

    private var dropped by mutableStateOf(false)

    private var scroll: Job? = null

    private val range: Float
        get() = trackHeight - thumbHeight

    val position: Float
        get() = if (dragging || dropped) dragPosition else list.thumbPosition(sections())

    val thumbCentre: Float
        get() = position * range + thumbHeight / 2f

    fun press() {
        if (!dropped) dragPosition = list.thumbPosition(sections())
        dragging = true
    }

    fun dragTo(y: Float) {
        val position = if (range <= 0f) 0f else ((y - thumbHeight / 2f) / range).coerceIn(0f, 1f)
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
