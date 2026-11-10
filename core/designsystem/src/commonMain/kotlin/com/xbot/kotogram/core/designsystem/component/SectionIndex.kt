package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import kotlinx.coroutines.launch

@Composable
fun SectionIndex(
    state: LazyListState,
    sections: List<FastScrollerSection>,
    modifier: Modifier = Modifier,
    style: SectionIndexStyle = SectionIndexStyle,
) {
    @Suppress("NAME_SHADOWING")
    val style = rememberStyle(::SectionIndexStyleScope, SectionIndexStyle.Default then style)
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val currentSections by rememberUpdatedState(sections)
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .width(style.width)
                .pointerInput(state) {
                    var selected = -1
                    awaitEachGesture {
                        var change = awaitFirstDown()
                        while (change.pressed) {
                            val list = currentSections
                            if (list.isNotEmpty()) {
                                val index = (change.position.y / size.height * list.size).toInt()
                                    .coerceIn(0, list.lastIndex)
                                if (index != selected) {
                                    selected = index
                                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    scope.launch { state.scrollToItem(list[index].index) }
                                }
                            }
                            change.consume()
                            val id = change.id
                            change = awaitPointerEvent().changes.firstOrNull { it.id == id } ?: break
                        }
                        selected = -1
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            sections.forEach { section -> Text(section.title, style = style.textStyle) }
        }
    }
}

@Suppress("MagicNumber")
fun interface SectionIndexStyle : ComponentStyle<SectionIndexStyleScope> {
    companion object : SectionIndexStyle {
        override fun SectionIndexStyleScope.applyStyle() {}

        val Default = SectionIndexStyle {
            width(20.dp)
            textStyle(
                theme.typography.labelSmall.merge(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.colors.primary,
                ),
            )
        }
    }
}

infix fun SectionIndexStyle.then(other: SectionIndexStyle): SectionIndexStyle = when {
    other === SectionIndexStyle -> this

    this === SectionIndexStyle -> other

    else -> SectionIndexStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

class SectionIndexStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<SectionIndexStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var width: Dp = Dp.Unspecified
        private set

    var textStyle: TextStyle = TextStyle.Default
        private set

    fun width(width: Dp) {
        this.width = width
    }

    fun textStyle(style: TextStyle) {
        textStyle = style
    }
}
