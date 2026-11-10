package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

@Composable
fun <T> Menu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<T>,
    itemText: (T) -> String,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    selectedItem: T? = null,
    style: MenuStyle = MenuStyle,
) {
    val menuStyle = rememberStyle(::MenuStyleScope, MenuStyle.Adaptive then style)
    with(LocalMenuOverride.current) {
        MenuOverrideScope(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            titles = items.map(itemText),
            selectedIndex = items.indexOf(selectedItem),
            onSelect = { onItemClick(items[it]) },
            modifier = modifier,
            style = menuStyle,
        ).Menu()
    }
}

internal interface MenuOverride {
    @Composable
    fun MenuOverrideScope.Menu()
}

internal class MenuOverrideScope(
    val expanded: Boolean,
    val onDismissRequest: () -> Unit,
    val titles: List<String>,
    val selectedIndex: Int,
    val onSelect: (Int) -> Unit,
    val modifier: Modifier,
    val style: MenuStyleScope,
)

internal object DefaultMenuOverride : MenuOverride {
    @Composable
    override fun MenuOverrideScope.Menu() {
        val density = LocalDensity.current
        val windowHeight = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }
        val itemHeight = with(density) { style.itemHeight.roundToPx() }
        val scrollState = rememberScrollState()
        val scope = rememberCoroutineScope()
        val menuFocus = remember { FocusRequester() }
        val itemFocus = remember(titles) { List(titles.size) { FocusRequester() } }
        val typeSelect = remember { TypeSelect() }
        fun centre(index: Int) = index * itemHeight - (scrollState.viewportSize - itemHeight) / 2
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier
                .heightIn(max = windowHeight - style.windowMargin * 2)
                .padding(horizontal = style.horizontalPadding, vertical = style.extraVerticalPadding)
                .focusRequester(menuFocus)
                .onPreviewKeyEvent { event ->
                    val index = typeSelect.find(event, titles) ?: return@onPreviewKeyEvent false
                    itemFocus[index].requestFocus()
                    scope.launch { scrollState.animateScrollTo(centre(index)) }
                    true
                }
                .focusable(),
            scrollState = scrollState,
        ) {
            LaunchedEffect(Unit) {
                menuFocus.requestFocus()
                if (selectedIndex >= 0) {
                    snapshotFlow { scrollState.viewportSize }.first { it > 0 }
                    scrollState.scrollTo(centre(selectedIndex))
                }
            }
            titles.forEachIndexed { index, title ->
                DropdownMenuItem(
                    text = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    onClick = {
                        onSelect(index)
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .height(style.itemHeight)
                        .clip(style.itemShape)
                        .focusRequester(itemFocus[index]),
                    trailingIcon = if (index == selectedIndex) {
                        { Icon(KotogramIcons.Check, contentDescription = null) }
                    } else {
                        null
                    },
                    contentPadding = PaddingValues(horizontal = style.itemHorizontalPadding),
                )
            }
        }
    }
}

private class TypeSelect {
    private var query = ""
    private var lastTyped = TimeSource.Monotonic.markNow()

    fun find(
        event: KeyEvent,
        titles: List<String>,
    ): Int? {
        val char =
            event.typedChar()?.takeIf { it.isLetterOrDigit() || (it == ' ' && query.isNotEmpty()) } ?: return null
        if (lastTyped.elapsedNow() > TypeSelectTimeout) query = ""
        lastTyped = TimeSource.Monotonic.markNow()
        query += char
        return titles
            .indexOfFirst { it.dropWhile { c -> !c.isLetterOrDigit() }.startsWith(query, ignoreCase = true) }
            .takeIf { it >= 0 }
    }

    private fun KeyEvent.typedChar(): Char? {
        val plain = type == KeyEventType.KeyDown && !isCtrlPressed && !isMetaPressed && !isAltPressed
        return if (plain) utf16CodePoint.toChar() else null
    }
}

private val TypeSelectTimeout = 1.seconds

internal val LocalMenuOverride: ProvidableCompositionLocal<MenuOverride> =
    staticCompositionLocalOf { DefaultMenuOverride }

@Suppress("MagicNumber")
fun interface MenuStyle : ComponentStyle<MenuStyleScope> {
    companion object : MenuStyle {
        override fun MenuStyleScope.applyStyle() {}

        val Default = MenuStyle {
            itemHeight(48.dp)
            itemHorizontalPadding(12.dp)
            horizontalPadding(0.dp)
            extraVerticalPadding(0.dp)
            itemShape(RectangleShape)
            windowMargin(48.dp)
        }

        val Adaptive = Default then MenuStyle {
            laptop {
                itemHeight(44.dp)
                itemHorizontalPadding(10.dp)
                horizontalPadding(10.dp)
                extraVerticalPadding(4.dp)
                itemShape(theme.shapes.small)
            }
        }
    }
}

infix fun MenuStyle.then(other: MenuStyle): MenuStyle = when {
    other === MenuStyle -> this

    this === MenuStyle -> other

    else -> MenuStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

class MenuStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<MenuStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var itemHeight: Dp = Dp.Unspecified
        private set

    var itemHorizontalPadding: Dp = Dp.Unspecified
        private set

    var horizontalPadding: Dp = Dp.Unspecified
        private set

    var extraVerticalPadding: Dp = Dp.Unspecified
        private set

    var itemShape: Shape = RectangleShape
        private set

    var windowMargin: Dp = Dp.Unspecified
        private set

    fun itemHeight(height: Dp) {
        itemHeight = height
    }

    fun itemHorizontalPadding(padding: Dp) {
        itemHorizontalPadding = padding
    }

    fun horizontalPadding(padding: Dp) {
        horizontalPadding = padding
    }

    fun extraVerticalPadding(padding: Dp) {
        extraVerticalPadding = padding
    }

    fun itemShape(shape: Shape) {
        itemShape = shape
    }

    fun windowMargin(margin: Dp) {
        windowMargin = margin
    }
}
