package com.xbot.kotogram.catalog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.core.designsystem.KotogramTheme

@Composable
fun CatalogHome(
    pages: List<CatalogPage>,
    onPageClick: (CatalogPage) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = KotogramTheme.spacing
    LazyVerticalGrid(
        columns = GridCells.Adaptive(CellMinSize),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.large),
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
        verticalArrangement = Arrangement.spacedBy(spacing.small)
    ) {
        items(pages, key = { it.id }) { page ->
            CatalogItem(page = page, onClick = { onPageClick(page) })
        }
    }
}

@Composable
private fun CatalogItem(page: CatalogPage, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = KotogramTheme.spacing
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(CellHeight),
        shape = KotogramTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)
        ) {
            Text(
                text = page.section.title,
                style = KotogramTheme.typography.labelSmall,
                color = KotogramTheme.colors.onSurfaceVariant
            )
            Text(page.name, style = KotogramTheme.typography.titleMedium)
            Text(
                text = page.description,
                style = KotogramTheme.typography.bodySmall,
                color = KotogramTheme.colors.onSurfaceVariant,
                maxLines = DESCRIPTION_LINES,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private val CellMinSize = 200.dp
private val CellHeight = 132.dp
private const val DESCRIPTION_LINES = 4
