package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.designsystem.theme.currentPlatform
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.navigation_close
import com.xbot.kotogram.core.resources.stringResource

@Immutable
class OptionLabel(
    val text: String,
    val leading: String? = null,
    val supporting: String? = null,
    val trailing: String? = null,
) {
    internal val menuText: String
        get() = listOfNotNull(leading, text).joinToString(" ") + trailing?.let { "  $it" }.orEmpty()

    internal fun contains(query: String): Boolean =
        text.contains(query, ignoreCase = true) || supporting?.contains(query, ignoreCase = true) == true
}

@Composable
fun <T> OptionPicker(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    options: List<T>,
    optionLabel: (T) -> OptionLabel,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "",
    selected: T? = null,
    optionSection: ((T) -> String)? = null,
    searchPlaceholder: String = "",
    matches: (T, String) -> Boolean = { option, query -> optionLabel(option).contains(query) },
    style: OptionPickerStyle = OptionPickerStyle,
) {
    val pickerStyle = rememberStyle(::OptionPickerStyleScope, OptionPickerStyle.Default then style)
    if (pickerStyle.mediaQueryInfo.isLaptop) {
        Menu(expanded, onDismissRequest, options, { optionLabel(it).menuText }, onSelect, modifier, selected)
    } else if (expanded) {
        context(pickerStyle) {
            val sheetStyle = remember(pickerStyle) {
                val color = pickerStyle.sheetColor
                BottomSheetStyle { containerColor(color) }
            }
            BottomSheet(onDismissRequest = onDismissRequest, modifier = modifier, style = sheetStyle) {
                OptionSheetContent(
                    options = options,
                    optionLabel = optionLabel,
                    onSelect = onSelect,
                    title = title,
                    selected = selected,
                    optionSection = optionSection,
                    searchPlaceholder = searchPlaceholder,
                    matches = matches,
                )
            }
        }
    }
}

private class OptionSection<T>(
    val title: String?,
    val options: List<T>,
)

@Composable
context(style: OptionPickerStyleScope)
private fun <T> BottomSheetScope.OptionSheetContent(
    options: List<T>,
    optionLabel: (T) -> OptionLabel,
    onSelect: (T) -> Unit,
    title: String,
    selected: T?,
    optionSection: ((T) -> String)?,
    searchPlaceholder: String,
    matches: (T, String) -> Boolean,
) {
    val query = rememberTextFieldState()
    val text = query.text.toString().trim()
    val sections = remember(options) {
        if (optionSection == null) {
            listOf(OptionSection(null, options))
        } else {
            options.groupBy(optionSection).map { OptionSection(it.key, it.value) }
        }
    }
    val scrollerSections = remember(sections) { scrollerSections(sections) }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex(sections, selected))
    val scrollBehavior = rememberTopBarScrollBehavior(listState)
    val barStyle = remember(style) {
        val color = style.sheetColor
        TopBarStyle { containerColor(color) }
    }
    val searchStyle = remember(style) {
        val elevation = style.searchElevation
        SearchFieldStyle { shadowElevation(elevation) }
    }
    val density = LocalDensity.current
    var barHeight by remember { mutableStateOf(0.dp) }
    val close: () -> Unit = { dismiss() }
    val choose: (T) -> Unit = { option ->
        onSelect(option)
        dismiss()
    }
    Box(Modifier.weight(1f).fillMaxWidth()) {
        val indexed = text.isEmpty() && scrollerSections.isNotEmpty()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.ime)
                .topBarScrollSource(scrollBehavior),
            contentPadding = PaddingValues(
                start = style.listMargin,
                top = barHeight,
                end = style.listMargin,
                bottom = style.bottomSearchSpace,
            ),
        ) {
            if (text.isEmpty()) {
                sections.forEach { section -> sectionItems(section, optionLabel, selected, choose) }
            } else {
                val found = options.filter { matches(it, text) }
                item { Spacer(Modifier.height(style.headerPadding.calculateTopPadding())) }
                itemsIndexed(found) { index, option ->
                    OptionRow(optionLabel(option), option == selected, text, position(index, found.size)) {
                        choose(option)
                    }
                }
            }
        }
        TopBar(
            title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { barHeight = with(density) { it.height.toDp() } },
            navigation = {
                GlassIconButton(
                    icon = KotogramIcons.Cross,
                    contentDescription = stringResource(Res.string.navigation_close),
                    onClick = close,
                )
            },
            windowInsets = WindowInsets(top = style.barTopInset),
            scrollBehavior = scrollBehavior,
            style = barStyle,
        )
        val scrollerModifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .padding(top = barHeight, bottom = style.bottomSearchSpace)
        if (indexed) {
            FastScroller(listState, scrollerSections, scrollerModifier)
        } else {
            ScrollIndicator(listState, scrollerModifier)
        }
        SearchField(
            state = query,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets(bottom = style.searchBottomInset)))
                .fillMaxWidth()
                .padding(style.searchPadding),
            placeholder = searchPlaceholder,
            style = searchStyle,
        )
    }
}

context(style: OptionPickerStyleScope)
private fun <T> LazyListScope.sectionItems(
    section: OptionSection<T>,
    optionLabel: (T) -> OptionLabel,
    selected: T?,
    choose: (T) -> Unit,
) {
    section.title?.let { title -> item(key = "section $title") { SectionHeader(title) } }
    items(section.options.size) { index ->
        val option = section.options[index]
        OptionRow(optionLabel(option), option == selected, query = "", position(index, section.options.size)) {
            choose(option)
        }
    }
}

@Composable
context(style: OptionPickerStyleScope)
private fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth().padding(style.headerPadding),
        style = style.headerTextStyle,
    )
}

private fun position(
    index: Int,
    count: Int,
): ListItemPosition = when {
    count == 1 -> ListItemPosition.Single
    index == 0 -> ListItemPosition.First
    index == count - 1 -> ListItemPosition.Last
    else -> ListItemPosition.Middle
}

private fun <T> scrollerSections(sections: List<OptionSection<T>>): List<FastScrollerSection> {
    var index = 0
    return sections.mapNotNull { section ->
        val start = index
        index += section.options.size + if (section.title != null) 1 else 0
        section.title?.let { FastScrollerSection(it, start) }
    }
}

private fun <T> selectedIndex(
    sections: List<OptionSection<T>>,
    selected: T?,
): Int {
    var index = 0
    for (section in sections) {
        if (section.title != null) index++
        val position = if (selected == null) -1 else section.options.indexOf(selected)
        if (position >= 0) return (index + position - ROWS_ABOVE_SELECTED).coerceAtLeast(0)
        index += section.options.size
    }
    return 0
}

private const val ROWS_ABOVE_SELECTED = 3

@Suppress("MagicNumber")
fun interface OptionPickerStyle : ComponentStyle<OptionPickerStyleScope> {
    companion object : OptionPickerStyle {
        override fun OptionPickerStyleScope.applyStyle() {}

        val Default = OptionPickerStyle {
            val apple = currentPlatform.isApple
            sheetColor(theme.colors.surfaceDim)
            rowColor(theme.colors.surface)
            headlineColor(theme.colors.onSurface)
            supportingColor(theme.colors.onSurfaceVariant)
            trailingColor(theme.colors.onSurfaceVariant)
            dividerColor(theme.colors.outlineVariant)
            matchStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
            checkColor(theme.colors.primary)
            checkGap(6.dp)
            leadingSize(width = 23.sp, height = 20.sp)
            leadingShift(0.67.dp)
            leadingTextStyle(TextStyle(fontSize = 20.5.sp))
            listMargin(20.dp)
            headerPadding(PaddingValues(start = 20.dp, end = 20.dp, top = 28.67.dp, bottom = 6.33.dp))
            headerTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = if (apple) (-0.43).sp else TextUnit.Unspecified,
                    color = theme.colors.onSurfaceVariant,
                ),
            )
            searchPadding(PaddingValues(horizontal = 28.dp, vertical = 8.dp))
            if (apple) {
                barTopInset(20.dp)
                searchBottomInset(20.dp)
                bottomSearchSpace(86.dp)
                searchElevation(0.dp)
            } else {
                barTopInset(0.dp)
                searchBottomInset(8.dp)
                bottomSearchSpace(72.dp)
                searchElevation(6.dp)
            }
        }
    }
}

infix fun OptionPickerStyle.then(other: OptionPickerStyle): OptionPickerStyle = when {
    other === OptionPickerStyle -> this

    this === OptionPickerStyle -> other

    else -> OptionPickerStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
class OptionPickerStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<OptionPickerStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var sheetColor: Color = Color.Unspecified
        private set

    var rowColor: Color = Color.Unspecified
        private set

    var headlineColor: Color = Color.Unspecified
        private set

    var supportingColor: Color = Color.Unspecified
        private set

    var trailingColor: Color = Color.Unspecified
        private set

    var dividerColor: Color = Color.Unspecified
        private set

    var leadingWidth: TextUnit = TextUnit.Unspecified
        private set

    var leadingHeight: TextUnit = TextUnit.Unspecified
        private set

    var leadingShift: Dp = 0.dp
        private set

    var listMargin: Dp = Dp.Unspecified
        private set

    var barTopInset: Dp = Dp.Unspecified
        private set

    var searchBottomInset: Dp = Dp.Unspecified
        private set

    var searchPadding: PaddingValues = PaddingValues()
        private set

    var bottomSearchSpace: Dp = Dp.Unspecified
        private set

    var searchElevation: Dp = Dp.Unspecified
        private set

    var headerPadding: PaddingValues = PaddingValues()
        private set

    var headerTextStyle: TextStyle = TextStyle.Default
        private set

    var leadingTextStyle: TextStyle = TextStyle.Default
        private set

    var matchStyle: SpanStyle = SpanStyle()
        private set

    var checkColor: Color = Color.Unspecified
        private set

    var checkGap: Dp = Dp.Unspecified
        private set

    fun sheetColor(color: Color) {
        sheetColor = color
    }

    fun rowColor(color: Color) {
        rowColor = color
    }

    fun headlineColor(color: Color) {
        headlineColor = color
    }

    fun supportingColor(color: Color) {
        supportingColor = color
    }

    fun trailingColor(color: Color) {
        trailingColor = color
    }

    fun dividerColor(color: Color) {
        dividerColor = color
    }

    fun leadingSize(
        width: TextUnit,
        height: TextUnit,
    ) {
        leadingWidth = width
        leadingHeight = height
    }

    fun leadingShift(shift: Dp) {
        leadingShift = shift
    }

    fun listMargin(margin: Dp) {
        listMargin = margin
    }

    fun barTopInset(inset: Dp) {
        barTopInset = inset
    }

    fun searchBottomInset(inset: Dp) {
        searchBottomInset = inset
    }

    fun searchPadding(padding: PaddingValues) {
        searchPadding = padding
    }

    fun bottomSearchSpace(space: Dp) {
        bottomSearchSpace = space
    }

    fun searchElevation(elevation: Dp) {
        searchElevation = elevation
    }

    fun headerPadding(padding: PaddingValues) {
        headerPadding = padding
    }

    fun headerTextStyle(style: TextStyle) {
        headerTextStyle = style
    }

    fun leadingTextStyle(style: TextStyle) {
        leadingTextStyle = style
    }

    fun matchStyle(style: SpanStyle) {
        matchStyle = style
    }

    fun checkColor(color: Color) {
        checkColor = color
    }

    fun checkGap(gap: Dp) {
        checkGap = gap
    }

    internal fun rowStyle(): ListItemStyle {
        val picker = this
        return ListItemStyle {
            containerColor(picker.rowColor)
            dividerColor(picker.dividerColor)
            headlineColor(picker.headlineColor)
            supportingColor(picker.supportingColor)
            trailingColor(picker.trailingColor)
        }
    }
}
