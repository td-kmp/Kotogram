package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlin.math.roundToInt

@Stable
internal interface ScrollIndicatorState {
    val positionFraction: Float
    val sizeFraction: Float
    val isScrollable: Boolean
    val isScrollInProgress: Boolean

    suspend fun scrollToFraction(fraction: Float)
}

@Composable
internal fun rememberScrollIndicatorState(state: LazyListState): ScrollIndicatorState =
    remember(state) { LazyListIndicatorState(state) }

@Composable
internal fun rememberScrollIndicatorState(state: ScrollState): ScrollIndicatorState =
    remember(state) { ScrollStateIndicatorState(state) }

// Adapted from Wear Compose Material 3's LazyColumnStateAdapter: positions count items; the size follows their count and, own, the viewport.
private class LazyListIndicatorState(
    private val state: LazyListState,
) : ScrollIndicatorState {
    private var latestSizeFraction = 0f
    private var previousItemsCount = 0
    private var previousViewportSize = 0

    override val positionFraction: Float
        get() {
            if (state.layoutInfo.visibleItemsInfo.isEmpty()) return 0f
            val first = decimalFirstItemIndex()
            val fromEnd = state.layoutInfo.totalItemsCount - decimalLastItemIndex()
            val position = if (first + fromEnd == 0f) 0f else first / (first + fromEnd)
            return if (state.layoutInfo.reverseLayout) 1f - position else position
        }

    override val sizeFraction: Float
        get() {
            val info = state.layoutInfo
            val total = info.totalItemsCount
            if (total == 0) return 0f
            val viewportSize = info.viewportEndOffset - info.viewportStartOffset
            if (previousItemsCount != total || previousViewportSize != viewportSize) {
                previousItemsCount = total
                previousViewportSize = viewportSize
                latestSizeFraction = ((decimalLastItemIndex() - decimalFirstItemIndex()) / total).coerceIn(0f, 1f)
            }
            return latestSizeFraction
        }

    override val isScrollable: Boolean get() = state.canScrollForward || state.canScrollBackward

    override val isScrollInProgress: Boolean get() = state.isScrollInProgress

    override suspend fun scrollToFraction(fraction: Float) {
        val info = state.layoutInfo
        val visible = info.visibleItemsInfo
        if (visible.isEmpty()) return
        val position = if (info.reverseLayout) 1f - fraction else fraction
        val first =
            position.coerceIn(0f, 1f) * (info.totalItemsCount - (decimalLastItemIndex() - decimalFirstItemIndex()))
        val index = first.toInt().coerceIn(0, info.totalItemsCount - 1)
        val itemSize = visible.sumOf { it.size }.toFloat() / visible.size
        state.scrollToItem(index, ((first - index) * itemSize).roundToInt())
    }

    private fun decimalLastItemIndex(): Float {
        val info = state.layoutInfo
        val last = info.visibleItemsInfo.lastOrNull() ?: return 0f
        val size = last.size + if (last.index == info.totalItemsCount - 1) info.afterContentPadding else 0
        val shown = (info.viewportEndOffset - last.offset).coerceIn(1, size.coerceAtLeast(1))
        return last.index + shown.toFloat() / size.coerceAtLeast(1)
    }

    private fun decimalFirstItemIndex(): Float {
        val info = state.layoutInfo
        val first = info.visibleItemsInfo.firstOrNull() ?: return 0f
        val padding = if (first.index == 0) info.beforeContentPadding else 0
        val offset = first.offset - info.viewportStartOffset - padding
        return first.index - offset.coerceAtMost(0).toFloat() / (first.size + padding).coerceAtLeast(1)
    }
}

private class ScrollStateIndicatorState(
    private val state: ScrollState,
) : ScrollIndicatorState {
    override val positionFraction: Float
        get() = if (state.maxValue == 0) 0f else state.value.toFloat() / state.maxValue

    override val sizeFraction: Float
        get() {
            val content = state.viewportSize + state.maxValue
            return if (content == 0) 0f else state.viewportSize.toFloat() / content
        }

    override val isScrollable: Boolean get() = state.maxValue > 0

    override val isScrollInProgress: Boolean get() = state.isScrollInProgress

    override suspend fun scrollToFraction(fraction: Float) {
        state.scrollTo((fraction.coerceIn(0f, 1f) * state.maxValue).roundToInt())
    }
}
