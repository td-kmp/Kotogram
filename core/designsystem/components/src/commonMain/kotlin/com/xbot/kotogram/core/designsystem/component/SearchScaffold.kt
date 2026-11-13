package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.platform.windowChrome
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.designsystem.theme.currentPlatform
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Immutable
class BarAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)

@Immutable
class BarTextAction(
    val label: String,
    val onClick: () -> Unit,
)

@Suppress("LongParameterList")
@Composable
fun SearchScaffold(
    title: @Composable () -> Unit,
    searchPlaceholder: String,
    onSearchClick: () -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    editAction: BarTextAction? = null,
    actions: List<BarAction> = [],
    primaryAction: BarAction? = null,
    header: @Composable () -> Unit = {},
    style: SearchScaffoldStyle = SearchScaffoldStyle,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scaffoldStyle = rememberStyle(::SearchScaffoldStyleScope, SearchScaffoldStyle.Adaptive then style)
    val haze = rememberHazeState()
    val metrics = remember { HeaderMetrics() }
    val density = LocalDensity.current
    val statusTop = WindowInsets.statusBars.getTop(density)
    val navigationBottom = WindowInsets.navigationBars.getBottom(density)
    val floating = primaryAction.takeIf { scaffoldStyle.floatingPrimaryAction }
    context(scaffoldStyle) {
        SubcomposeLayout(modifier.fillMaxSize()) { constraints ->
            val width = constraints.maxWidth
            val headerPlaceables = subcompose(ScaffoldSlot.Header) {
                Header(
                    title = title,
                    searchPlaceholder = searchPlaceholder,
                    onSearchClick = onSearchClick,
                    listState = listState,
                    editAction = editAction,
                    actions = actions,
                    primaryAction = primaryAction.takeIf { !scaffoldStyle.floatingPrimaryAction },
                    header = header,
                    haze = haze,
                    metrics = metrics,
                    statusTop = statusTop,
                )
            }.map { it.measure(Constraints(minWidth = width, maxWidth = width, maxHeight = constraints.maxHeight)) }
            val padding = PaddingValues(top = metrics.fullHeight.toDp())
            val contentPlaceables = subcompose(ScaffoldSlot.Content) {
                Box(Modifier.fillMaxSize().hazeSource(haze)) { content(padding) }
            }.map { it.measure(constraints) }
            val fabPlaceables = floating?.let { action ->
                subcompose(ScaffoldSlot.Fab) { Fab(action, listState) }
                    .map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
            }.orEmpty()
            layout(width, constraints.maxHeight) {
                contentPlaceables.forEach { it.place(0, 0) }
                headerPlaceables.forEach { it.place(0, 0) }
                val margin = scaffoldStyle.fabMargin.roundToPx()
                fabPlaceables.forEach {
                    it.place(width - margin - it.width, constraints.maxHeight - margin - navigationBottom - it.height)
                }
            }
        }
    }
}

private enum class ScaffoldSlot {
    Header,
    Content,
    Fab,
}

private enum class HeaderSlot {
    Edge,
    Row,
    Search,
    Panel,
}

private class HeaderMetrics {
    var fullHeight: Int = 0
}

@Suppress("LongParameterList", "LongMethod")
@Composable
context(style: SearchScaffoldStyleScope)
private fun Header(
    title: @Composable () -> Unit,
    searchPlaceholder: String,
    onSearchClick: () -> Unit,
    listState: LazyListState,
    editAction: BarTextAction?,
    actions: List<BarAction>,
    primaryAction: BarAction?,
    header: @Composable () -> Unit,
    haze: HazeState,
    metrics: HeaderMetrics,
    statusTop: Int,
) {
    val searchAction = BarAction(KotogramIcons.Search, searchPlaceholder, onSearchClick).takeIf { style.searchInRow }
    val chrome = windowChrome()
    Layout(
        content = {
            EdgeEffect(haze, listState, Modifier.layoutId(HeaderSlot.Edge))
            TitleRow(
                title = title,
                editAction = editAction.takeIf { style.showsEditAction },
                actions = actions,
                primaryAction = primaryAction,
                searchAction = searchAction,
                startInset = chrome?.start ?: 0,
                endInset = chrome?.end ?: 0,
                modifier = Modifier.layoutId(HeaderSlot.Row),
            )
            if (!style.searchInRow) {
                SearchEntry(searchPlaceholder, onSearchClick, Modifier.layoutId(HeaderSlot.Search))
            }
            Box(Modifier.layoutId(HeaderSlot.Panel)) { header() }
        },
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val rowTop = if (chrome != null) 0 else statusTop + style.rowTop.roundToPx()
        val rowHeight = chrome?.height ?: style.rowHeight.roundToPx()
        val zone = if (style.searchInRow) 0 else style.searchZoneHeight.roundToPx()
        val field = style.searchHeight.roundToPx()
        val fieldBottom = style.searchBottomInset.roundToPx()
        val side = style.horizontalPadding.roundToPx()
        val panelBottom = style.panelBottomInset.roundToPx()
        val rowPlaceable = measurables.first {
            it.layoutId == HeaderSlot.Row
        }.measure(Constraints.fixed(width, rowHeight))
        val panel = measurables.first { it.layoutId == HeaderSlot.Panel }
            .measure(Constraints(minWidth = width, maxWidth = width))
        val fullHeight = rowTop + rowHeight + style.rowBottomGap.roundToPx() + zone + panel.height + panelBottom
        metrics.fullHeight = fullHeight

        val foldDistance = style.foldDistance.roundToPx()
        val firstItem = listState.firstVisibleItemIndex == 0
        val scrolled = if (firstItem) listState.firstVisibleItemScrollOffset else Int.MAX_VALUE
        val fold = if (foldDistance > 0) min(scrolled, foldDistance) else 0
        val folded = if (foldDistance > 0) fold.toFloat() / foldDistance else 0f
        val visible = fullHeight - fold

        val search = measurables.firstOrNull { it.layoutId == HeaderSlot.Search }
        val lowCut = if (zone > 0) (zone - field).toFloat() / zone else 0f
        val progress = if (foldDistance > 0) ((1f - folded - lowCut) / (1f - lowCut)).coerceIn(0f, 1f) else 1f
        val fieldHeight = (field * progress).roundToInt()
        val searchPlaceable = search?.measure(Constraints.fixed(max(0, width - 2 * side), fieldHeight))
        val panelTop = visible - panelBottom - panel.height
        val zoneTop = panelTop - zone - ((zone - field) * 0.5f * folded).roundToInt()

        val edgeHeight = fullHeight + style.edgeExtension.roundToPx() - (zone * folded).roundToInt()
        val edge = measurables.first { it.layoutId == HeaderSlot.Edge }
            .measure(Constraints.fixed(width, max(0, edgeHeight)))

        layout(width, visible) {
            edge.place(0, 0)
            searchPlaceable?.placeWithLayer(side, zoneTop + zone - fieldBottom - fieldHeight) { alpha = progress }
            panel.place(0, panelTop)
            rowPlaceable.place(0, rowTop)
        }
    }
}

private enum class RowSlot {
    Leading,
    Title,
    Trailing,
}

@Composable
context(style: SearchScaffoldStyleScope)
private fun TitleRow(
    title: @Composable () -> Unit,
    editAction: BarTextAction?,
    actions: List<BarAction>,
    primaryAction: BarAction?,
    searchAction: BarAction?,
    startInset: Int,
    endInset: Int,
    modifier: Modifier = Modifier,
) {
    Layout(
        content = {
            Box(Modifier.layoutId(RowSlot.Leading)) {
                editAction?.let { GlassButton(it.label, it.onClick, style = style.editButtonStyle) }
            }
            Box(Modifier.layoutId(RowSlot.Title)) {
                CompositionLocalProvider(LocalTextStyle provides style.titleTextStyle, content = title)
            }
            val touchTarget = if (style.touchTargets) LocalMinimumInteractiveComponentSize.current else Dp.Unspecified
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides touchTarget) {
                Row(
                    modifier = Modifier.layoutId(RowSlot.Trailing),
                    horizontalArrangement = Arrangement.spacedBy(style.groupGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val group = listOfNotNull(searchAction) + actions
                    if (group.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(style.actionGap)) {
                            group.forEach {
                                GlassIconButton(it.icon, it.label, it.onClick, style = style.actionButtonStyle)
                            }
                        }
                    }
                    primaryAction?.let {
                        GlassIconButton(
                            it.icon,
                            it.label,
                            it.onClick,
                            style = style.actionButtonStyle then style.primaryButtonStyle,
                        )
                    }
                }
            }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val side = style.horizontalPadding.roundToPx()
        val start = side + startInset
        val end = side + endInset
        val gap = style.titleGap.roundToPx()
        val loose = Constraints(maxWidth = width, maxHeight = height)
        val leading = measurables.first { it.layoutId == RowSlot.Leading }.measure(loose)
        val trailing = measurables.first { it.layoutId == RowSlot.Trailing }.measure(loose)
        val leadingEnd = if (leading.width >
            0
        ) start + leading.width + gap else start + style.titleStartInset.roundToPx()
        val trailingStart = if (trailing.width > 0) width - end - trailing.width - gap else width - end
        val titleWidth = if (style.centeredTitle) {
            width - 2 * max(leadingEnd, width - trailingStart)
        } else {
            trailingStart - leadingEnd
        }
        val titlePlaceable = measurables.first { it.layoutId == RowSlot.Title }
            .measure(Constraints(maxWidth = max(0, titleWidth), maxHeight = height))
        layout(width, height) {
            leading.place(start, (height - leading.height) / 2)
            trailing.place(width - end - trailing.width, (height - trailing.height) / 2)
            val titleX = if (style.centeredTitle) (width - titlePlaceable.width) / 2 else leadingEnd
            titlePlaceable.place(titleX, (height - titlePlaceable.height) / 2)
        }
    }
}

@Composable
context(style: SearchScaffoldStyleScope)
private fun SearchEntry(
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(style.searchShape)
            .background(style.searchColor)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = style.searchContentPadding),
        horizontalArrangement = Arrangement.spacedBy(style.searchIconGap, style.searchAlignment),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            KotogramIcons.Search,
            contentDescription = null,
            Modifier.size(style.searchIconSize),
            tint = style.searchContentColor,
        )
        Text(placeholder, style = style.searchTextStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// Adapted from Telegram for iOS's ChatListNavigationBar: its numbers, and UIKit's edge effect fading out at the bottom.
@Composable
context(style: SearchScaffoldStyleScope)
private fun EdgeEffect(
    haze: HazeState,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val overlapped by remember(listState) { derivedStateOf { listState.canScrollBackward } }
    val alpha by animateFloatAsState(if (overlapped) 1f else 0f, style.edgeFadeSpec)
    val fade = with(LocalDensity.current) { style.edgeFadeLength.toPx() }
    var height by remember { mutableFloatStateOf(0f) }
    Box(
        modifier
            .onSizeChanged { height = it.height.toFloat() }
            .graphicsLayer { this.alpha = alpha }
            .hazeBlur(
                HazeInput.Sources(haze),
                HazeBlurStyle {
                    blurEnabled(true)
                    blurRadius(style.edgeBlurRadius)
                    noiseFactor(0f)
                    backgroundColor(style.containerColor)
                    progressive(
                        HazeProgressive.verticalGradient(
                            startY = height - fade,
                            startIntensity = 1f,
                            endIntensity = 0f,
                        ),
                    )
                },
            )
            .drawWithContent {
                drawContent()
                val tint = style.containerColor.copy(alpha = style.edgeTintAlpha)
                val solid = ((size.height - fade) / size.height).coerceIn(0f, 1f)
                drawRect(Brush.verticalGradient(0f to tint, solid to tint, 1f to Color.Transparent))
            },
    )
}

@Composable
context(style: SearchScaffoldStyleScope)
private fun Fab(
    action: BarAction,
    listState: LazyListState,
) {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(listState) {
        var last = 0
        snapshotFlow { listState.firstVisibleItemIndex * FAB_ITEM_WEIGHT + listState.firstVisibleItemScrollOffset }
            .collect { position ->
                if (position <= 0 || position < last) visible = true else if (position > last) visible = false
                last = position
            }
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(style.fabSlideSpec) { it * 2 },
        exit = slideOutVertically(style.fabSlideSpec) { it * 2 },
    ) {
        FloatingActionButton(action.icon, action.label, action.onClick)
    }
}

private const val FAB_ITEM_WEIGHT = 100_000

@Suppress("MagicNumber")
fun interface SearchScaffoldStyle : ComponentStyle<SearchScaffoldStyleScope> {
    companion object : SearchScaffoldStyle {
        override fun SearchScaffoldStyleScope.applyStyle() {}

        val Default = SearchScaffoldStyle {
            val apple = currentPlatform.isApple
            showsEditAction(apple)
            floatingPrimaryAction(!apple)
            searchInRow(!apple)
            centeredTitle(true)
            rowTop(if (apple) 10.dp else 0.dp)
            rowHeight(if (apple) 44.dp else 64.dp)
            rowBottomGap(if (apple) 2.dp else 4.dp)
            horizontalPadding(if (apple) 16.dp else 4.dp)
            titleGap(8.dp)
            titleStartInset(if (apple) 0.dp else 12.dp)
            groupGap(12.dp)
            actionGap(if (apple) 8.dp else 0.dp)
            searchZoneHeight(60.dp)
            searchHeight(44.dp)
            searchBottomInset(8.dp)
            foldDistance(if (apple) 54.dp else 0.dp)
            panelBottomInset(9.dp)
            searchShape(CircleShape)
            searchColor(theme.colors.surfaceContainerHigh)
            searchContentColor(theme.colors.onSurfaceVariant)
            searchTextStyle(theme.typography.bodyLarge.merge(color = theme.colors.onSurfaceVariant))
            searchIconSize(24.dp)
            searchIconGap(4.dp)
            searchContentPadding(12.dp)
            searchAlignment(Alignment.CenterHorizontally)
            containerColor(theme.colors.surface)
            titleTextStyle(theme.typography.titleLarge.merge(color = theme.colors.onSurface))
            editButtonStyle(
                ButtonStyle {
                    minHeight(44.dp)
                    contentPadding(PaddingValues(horizontal = 12.dp))
                    containerColor(Color.Transparent)
                    contentColor(theme.colors.onSurface)
                    textStyle(theme.typography.bodyLargeEmphasized)
                },
            )
            touchTargets(true)
            actionButtonStyle(IconButtonStyle)
            primaryButtonStyle(
                IconButtonStyle {
                    containerColor(theme.colors.primary)
                    contentColor(theme.colors.onPrimary)
                },
            )
            edgeBlurRadius(15.dp)
            edgeTintAlpha(0.85f)
            edgeFadeLength(54.dp)
            edgeExtension(14.dp)
            edgeFadeSpec(tween(200))
            fabMargin(16.dp)
            fabSlideSpec(tween(250))
        }

        val Adaptive = Default then SearchScaffoldStyle {
            laptop {
                showsEditAction(false)
                floatingPrimaryAction(false)
                searchInRow(false)
                centeredTitle(false)
                rowTop(0.dp)
                rowHeight(52.dp)
                rowBottomGap(0.dp)
                horizontalPadding(8.dp)
                titleStartInset(20.dp)
                groupGap(8.dp)
                actionGap(8.dp)
                touchTargets(false)
                actionButtonStyle(IconButtonStyle { size(36.dp) })
                searchZoneHeight(36.dp)
                searchHeight(28.dp)
                searchBottomInset(4.dp)
                foldDistance(0.dp)
                panelBottomInset(8.dp)
                searchIconSize(16.dp)
                searchContentPadding(8.dp)
                searchAlignment(Alignment.Start)
                edgeFadeLength(24.dp)
            }
        }
    }
}

infix fun SearchScaffoldStyle.then(other: SearchScaffoldStyle): SearchScaffoldStyle = when {
    other === SearchScaffoldStyle -> this

    this === SearchScaffoldStyle -> other

    else -> SearchScaffoldStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
class SearchScaffoldStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<SearchScaffoldStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var showsEditAction: Boolean = false
        private set
    var floatingPrimaryAction: Boolean = false
        private set
    var searchInRow: Boolean = false
        private set
    var centeredTitle: Boolean = true
        private set
    var rowTop: Dp = Dp.Unspecified
        private set
    var rowHeight: Dp = Dp.Unspecified
        private set
    var rowBottomGap: Dp = Dp.Unspecified
        private set
    var horizontalPadding: Dp = Dp.Unspecified
        private set
    var titleGap: Dp = Dp.Unspecified
        private set
    var titleStartInset: Dp = Dp.Unspecified
        private set
    var groupGap: Dp = Dp.Unspecified
        private set
    var actionGap: Dp = Dp.Unspecified
        private set
    var searchZoneHeight: Dp = Dp.Unspecified
        private set
    var searchHeight: Dp = Dp.Unspecified
        private set
    var searchBottomInset: Dp = Dp.Unspecified
        private set
    var foldDistance: Dp = Dp.Unspecified
        private set
    var panelBottomInset: Dp = Dp.Unspecified
        private set
    var searchShape: Shape = CircleShape
        private set
    var searchColor: Color = Color.Unspecified
        private set
    var searchContentColor: Color = Color.Unspecified
        private set
    var searchTextStyle: TextStyle = TextStyle.Default
        private set
    var searchIconSize: Dp = Dp.Unspecified
        private set
    var searchIconGap: Dp = Dp.Unspecified
        private set
    var searchContentPadding: Dp = Dp.Unspecified
        private set
    var searchAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
        private set
    var containerColor: Color = Color.Unspecified
        private set
    var titleTextStyle: TextStyle = TextStyle.Default
        private set
    var touchTargets: Boolean = true
        private set
    var actionButtonStyle: IconButtonStyle = IconButtonStyle
        private set
    var editButtonStyle: ButtonStyle = ButtonStyle
        private set
    var primaryButtonStyle: IconButtonStyle = IconButtonStyle
        private set
    var edgeBlurRadius: Dp = Dp.Unspecified
        private set
    var edgeTintAlpha: Float = 0f
        private set
    var edgeFadeLength: Dp = Dp.Unspecified
        private set
    var edgeExtension: Dp = Dp.Unspecified
        private set
    var edgeFadeSpec: AnimationSpec<Float> = snap()
        private set
    var fabMargin: Dp = Dp.Unspecified
        private set
    var fabSlideSpec: FiniteAnimationSpec<IntOffset> = snap()
        private set

    fun showsEditAction(shows: Boolean) {
        showsEditAction = shows
    }

    fun floatingPrimaryAction(floating: Boolean) {
        floatingPrimaryAction = floating
    }

    fun searchInRow(inRow: Boolean) {
        searchInRow = inRow
    }

    fun centeredTitle(centered: Boolean) {
        centeredTitle = centered
    }

    fun rowTop(top: Dp) {
        rowTop = top
    }

    fun rowHeight(height: Dp) {
        rowHeight = height
    }

    fun rowBottomGap(gap: Dp) {
        rowBottomGap = gap
    }

    fun horizontalPadding(padding: Dp) {
        horizontalPadding = padding
    }

    fun titleGap(gap: Dp) {
        titleGap = gap
    }

    fun titleStartInset(inset: Dp) {
        titleStartInset = inset
    }

    fun groupGap(gap: Dp) {
        groupGap = gap
    }

    fun actionGap(gap: Dp) {
        actionGap = gap
    }

    fun searchZoneHeight(height: Dp) {
        searchZoneHeight = height
    }

    fun searchHeight(height: Dp) {
        searchHeight = height
    }

    fun searchBottomInset(inset: Dp) {
        searchBottomInset = inset
    }

    fun foldDistance(distance: Dp) {
        foldDistance = distance
    }

    fun panelBottomInset(inset: Dp) {
        panelBottomInset = inset
    }

    fun searchShape(shape: Shape) {
        searchShape = shape
    }

    fun searchColor(color: Color) {
        searchColor = color
    }

    fun searchContentColor(color: Color) {
        searchContentColor = color
    }

    fun searchTextStyle(style: TextStyle) {
        searchTextStyle = style
    }

    fun searchIconSize(size: Dp) {
        searchIconSize = size
    }

    fun searchIconGap(gap: Dp) {
        searchIconGap = gap
    }

    fun searchContentPadding(padding: Dp) {
        searchContentPadding = padding
    }

    fun searchAlignment(alignment: Alignment.Horizontal) {
        searchAlignment = alignment
    }

    fun containerColor(color: Color) {
        containerColor = color
    }

    fun titleTextStyle(style: TextStyle) {
        titleTextStyle = style
    }

    fun touchTargets(enforced: Boolean) {
        touchTargets = enforced
    }

    fun actionButtonStyle(style: IconButtonStyle) {
        actionButtonStyle = style
    }

    fun editButtonStyle(style: ButtonStyle) {
        editButtonStyle = style
    }

    fun primaryButtonStyle(style: IconButtonStyle) {
        primaryButtonStyle = style
    }

    fun edgeBlurRadius(radius: Dp) {
        edgeBlurRadius = radius
    }

    fun edgeTintAlpha(alpha: Float) {
        edgeTintAlpha = alpha
    }

    fun edgeFadeLength(length: Dp) {
        edgeFadeLength = length
    }

    fun edgeExtension(extension: Dp) {
        edgeExtension = extension
    }

    fun edgeFadeSpec(spec: AnimationSpec<Float>) {
        edgeFadeSpec = spec
    }

    fun fabMargin(margin: Dp) {
        fabMargin = margin
    }

    fun fabSlideSpec(spec: FiniteAnimationSpec<IntOffset>) {
        fabSlideSpec = spec
    }
}
