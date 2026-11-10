package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

data class FastScrollerSection(
    val title: String,
    val index: Int,
)

@Composable
fun FastScroller(
    state: LazyListState,
    sections: List<FastScrollerSection>,
    modifier: Modifier = Modifier,
) {
    with(LocalFastScrollerOverride.current) {
        FastScrollerOverrideScope(state, sections, modifier).FastScroller()
    }
}

internal interface FastScrollerOverride {
    @Composable
    fun FastScrollerOverrideScope.FastScroller()
}

internal class FastScrollerOverrideScope(
    val state: LazyListState,
    val sections: List<FastScrollerSection>,
    val modifier: Modifier,
)

internal object ThumbFastScrollerOverride : FastScrollerOverride {
    @Composable
    override fun FastScrollerOverrideScope.FastScroller() {
        ScrollThumb(state, sections, modifier)
    }
}

internal val LocalFastScrollerOverride: ProvidableCompositionLocal<FastScrollerOverride> =
    staticCompositionLocalOf { ThumbFastScrollerOverride }
