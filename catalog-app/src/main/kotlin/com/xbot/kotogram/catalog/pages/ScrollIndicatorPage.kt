package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.ScrollIndicator

internal val ScrollIndicatorPage = CatalogPage(
    id = "scroll-indicator",
    section = CatalogSection.Components,
    name = "ScrollIndicator",
    description = "Where a list or a scrolling column stands and how much of it shows, along its end edge. On " +
        "iOS and macOS the system's own indicator, and a drag on it moves the content. Elsewhere a track the " +
        "whole height of the edge with a thumb as long as the part in view, which follows the scroll as it " +
        "happens: Wear OS's indicator in look, Android's scrollbar in size and timing, 4 wide, shown while the " +
        "content scrolls and faded 300 ms after. A sectioned list on a touch screen takes FastScroller instead.",
    examples = [
        CatalogExample("Over a LazyColumn") {
            SampleCountryList { state, _ ->
                ScrollIndicator(state, Modifier.align(Alignment.CenterEnd).fillMaxHeight())
            }
        },
        CatalogExample("Over a scrolling Column") { ScrollingColumn() },
    ],
)

@Composable
private fun ScrollingColumn() {
    val state = rememberScrollState()
    Box(Modifier.fillMaxWidth().height(SampleListHeight)) {
        Column(Modifier.fillMaxSize().verticalScroll(state).padding(horizontal = SampleListMargin)) {
            SampleCountries.forEach { country ->
                Text(
                    text = "${country.flag} ${country.name}",
                    modifier = Modifier.padding(vertical = KotogramTheme.spacing.small),
                )
            }
        }
        ScrollIndicator(state, Modifier.align(Alignment.CenterEnd).fillMaxHeight())
    }
}
