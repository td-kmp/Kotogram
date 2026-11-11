package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.component.FastScroller

internal val FastScrollerPage = CatalogPage(
    id = "fast-scroller",
    section = CatalogSection.Components,
    name = "FastScroller",
    description = "A handle at the end of a sectioned list that takes it to a section, for touch screens. " +
        "FastScroller is the platform's: UITableView's own index on iOS and elsewhere ScrollIndicator's thumb, " +
        "wider and made to be dragged, after Android's fast scroller: it shows while the list moves, hides a " +
        "moment later and, dragged, names the section in a bubble, ticking with the haptics of a segment as the " +
        "section changes. It lies over the list's margin and takes no room from its rows. A list that only " +
        "shows its place takes ScrollIndicator.",
    examples = listOf(
        CatalogExample("Over a sectioned LazyColumn") {
            SampleCountryList { state, sections ->
                FastScroller(state, sections, Modifier.align(Alignment.CenterEnd).fillMaxHeight())
            }
        },
    ),
)
