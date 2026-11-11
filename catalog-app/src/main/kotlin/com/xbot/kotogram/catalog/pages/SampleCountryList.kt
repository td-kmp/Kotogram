package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.FastScrollerSection
import com.xbot.kotogram.core.designsystem.component.ListItem
import com.xbot.kotogram.core.designsystem.component.ListItemPosition

@Composable
internal fun SampleCountryList(scroller: @Composable BoxScope.(LazyListState, List<FastScrollerSection>) -> Unit) {
    val state = rememberLazyListState()
    Box(Modifier.fillMaxWidth().height(SampleListHeight)) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = SampleListMargin),
        ) {
            CountryGroups.forEach { group ->
                item(key = group.letter) { SectionHeader(group.letter) }
                itemsIndexed(group.countries, key = { _, country -> country.englishName }) { index, country ->
                    ListItem(
                        headlineContent = { Text(country.name) },
                        leadingContent = { Text(country.flag) },
                        trailingContent = { Text(country.code) },
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        position = position(index, group.countries.size),
                    )
                }
            }
        }
        scroller(state, CountrySections)
    }
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

@Composable
private fun SectionHeader(letter: String) {
    Text(
        text = letter,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KotogramTheme.spacing.medium, vertical = KotogramTheme.spacing.small),
        style = KotogramTheme.typography.labelLarge,
        color = KotogramTheme.colors.onSurfaceVariant,
    )
}

private class CountryGroup(
    val letter: String,
    val countries: List<SampleCountry>,
)

private val CountryGroups: List<CountryGroup> =
    SampleCountries.groupBy { it.name.take(1) }.map { entry -> CountryGroup(entry.key, entry.value) }

private val CountrySections: List<FastScrollerSection> = CountryGroups
    .runningFold(0) { index, group -> index + 1 + group.countries.size }
    .zip(CountryGroups) { index, group -> FastScrollerSection(group.letter, index) }

internal val SampleListHeight = 360.dp

internal val SampleListMargin = 20.dp
