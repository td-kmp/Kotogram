package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
    val choose: (T) -> Unit = { option ->
        onSelect(option)
        dismiss()
    }
    TopBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = Modifier.fillMaxWidth(),
        navigation = {
            GlassIconButton(
                icon = KotogramIcons.Cross,
                contentDescription = stringResource(Res.string.navigation_close),
                onClick = ::dismiss,
            )
        },
    )
    if (!style.searchAtBottom) OptionSearch(query, searchPlaceholder)
    Box(Modifier.weight(1f).fillMaxWidth()) {
        val indexed = text.isEmpty() && scrollerSections.isNotEmpty()
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                end = if (indexed) style.scrollerGap else 0.dp,
                bottom = if (style.searchAtBottom) style.bottomSearchSpace else 0.dp,
            ),
        ) {
            if (text.isEmpty()) {
                sections.forEach { section -> sectionItems(section, optionLabel, selected, choose) }
            } else {
                val found = options.filter { matches(it, text) }
                itemsIndexed(found) { index, option ->
                    OptionRow(optionLabel(option), option == selected, text, index == found.lastIndex) {
                        choose(option)
                    }
                }
            }
        }
        if (indexed) {
            FastScroller(listState, scrollerSections, Modifier.align(Alignment.CenterEnd).fillMaxHeight())
        }
        if (style.searchAtBottom) {
            OptionSearch(
                query = query,
                placeholder = searchPlaceholder,
                modifier = Modifier.align(Alignment.BottomCenter).imePadding().navigationBarsPadding(),
            )
        }
    }
}

context(style: OptionPickerStyleScope)
private fun <T> LazyListScope.sectionItems(
    section: OptionSection<T>,
    optionLabel: (T) -> OptionLabel,
    selected: T?,
    choose: (T) -> Unit,
) {
    section.title?.let { title ->
        stickyHeader(key = "section $title") {
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(style.sheetColor)
                    .padding(style.headerPadding),
                style = style.headerTextStyle,
            )
        }
    }
    items(section.options.size) { index ->
        val option = section.options[index]
        OptionRow(optionLabel(option), option == selected, query = "", last = index == section.options.lastIndex) {
            choose(option)
        }
    }
}

@Composable
context(style: OptionPickerStyleScope)
private fun OptionSearch(
    query: TextFieldState,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    SearchField(
        state = query,
        modifier = modifier.fillMaxWidth().padding(style.searchPadding),
        placeholder = placeholder,
    )
}

@Composable
context(style: OptionPickerStyleScope)
private fun OptionRow(
    label: OptionLabel,
    selected: Boolean,
    query: String,
    last: Boolean,
    onClick: () -> Unit,
) {
    val rowStyle = remember(style) { style.rowStyle() }
    ListItem(
        headlineContent = {
            Text(highlighted(label.text, query, style.matchStyle), maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        modifier = Modifier.fillMaxWidth(),
        supportingContent = label.supporting?.let { supporting ->
            @Composable { Text(supporting, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        },
        leadingContent = label.leading?.let { leading ->
            @Composable { Text(leading, style = style.leadingTextStyle) }
        },
        trailingContent = if (label.trailing != null || selected) {
            @Composable { OptionTrailing(label.trailing, selected) }
        } else {
            null
        },
        onClick = onClick,
        position = if (last) null else ListItemPosition.Middle,
        style = rowStyle,
    )
}

@Composable
context(style: OptionPickerStyleScope)
private fun OptionTrailing(
    value: String?,
    selected: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(style.checkGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        value?.let { Text(it) }
        if (selected) Icon(KotogramIcons.Check, contentDescription = null, tint = style.checkColor)
    }
}

private fun highlighted(
    text: String,
    query: String,
    match: SpanStyle,
): AnnotatedString {
    val start = if (query.isEmpty()) -1 else text.indexOf(query, ignoreCase = true)
    if (start < 0) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text)
        addStyle(match, start, start + query.length)
    }
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
            sheetColor(theme.colors.surface)
            searchAtBottom(currentPlatform.isApple)
            searchPadding(PaddingValues(horizontal = 16.dp, vertical = 8.dp))
            bottomSearchSpace(72.dp)
            headerPadding(PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp))
            headerTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.colors.onSurfaceVariant,
                ),
            )
            rowMinHeight(56.dp)
            rowPadding(PaddingValues(horizontal = 16.dp, vertical = 8.dp))
            headlineTextStyle(theme.typography.bodyLarge.merge(fontSize = 17.sp))
            supportingTextStyle(theme.typography.bodyMedium.merge(fontSize = 15.sp))
            leadingTextStyle(TextStyle(fontSize = 26.sp))
            matchStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
            checkColor(theme.colors.primary)
            checkGap(6.dp)
            scrollerGap(20.dp)
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

    var searchAtBottom: Boolean = false
        private set

    var searchPadding: PaddingValues = PaddingValues()
        private set

    var bottomSearchSpace: Dp = Dp.Unspecified
        private set

    var headerPadding: PaddingValues = PaddingValues()
        private set

    var headerTextStyle: TextStyle = TextStyle.Default
        private set

    var rowMinHeight: Dp = Dp.Unspecified
        private set

    var rowPadding: PaddingValues = PaddingValues()
        private set

    var headlineTextStyle: TextStyle = TextStyle.Default
        private set

    var supportingTextStyle: TextStyle = TextStyle.Default
        private set

    var leadingTextStyle: TextStyle = TextStyle.Default
        private set

    var matchStyle: SpanStyle = SpanStyle()
        private set

    var checkColor: Color = Color.Unspecified
        private set

    var checkGap: Dp = Dp.Unspecified
        private set

    var scrollerGap: Dp = Dp.Unspecified
        private set

    fun sheetColor(color: Color) {
        sheetColor = color
    }

    fun searchAtBottom(atBottom: Boolean) {
        searchAtBottom = atBottom
    }

    fun searchPadding(padding: PaddingValues) {
        searchPadding = padding
    }

    fun bottomSearchSpace(space: Dp) {
        bottomSearchSpace = space
    }

    fun headerPadding(padding: PaddingValues) {
        headerPadding = padding
    }

    fun headerTextStyle(style: TextStyle) {
        headerTextStyle = style
    }

    fun rowMinHeight(height: Dp) {
        rowMinHeight = height
    }

    fun rowPadding(padding: PaddingValues) {
        rowPadding = padding
    }

    fun headlineTextStyle(style: TextStyle) {
        headlineTextStyle = style
    }

    fun supportingTextStyle(style: TextStyle) {
        supportingTextStyle = style
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

    fun scrollerGap(gap: Dp) {
        scrollerGap = gap
    }

    internal fun rowStyle(): ListItemStyle {
        val picker = this
        return ListItemStyle {
            minHeight(picker.rowMinHeight)
            contentPadding(picker.rowPadding)
            containerColor(picker.sheetColor)
            headlineTextStyle(picker.headlineTextStyle)
            supportingTextStyle(picker.supportingTextStyle)
        }
    }
}
