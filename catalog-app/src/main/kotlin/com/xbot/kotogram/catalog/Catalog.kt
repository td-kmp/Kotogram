package com.xbot.kotogram.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.xbot.kotogram.catalog.pages.CatalogPages
import com.xbot.kotogram.catalog.ui.CatalogHome
import com.xbot.kotogram.catalog.ui.CatalogPageScreen
import com.xbot.kotogram.catalog.ui.CatalogTopBar
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.animation.rememberNavTransitions
import com.xbot.kotogram.core.designsystem.style.ProvideFormFactor

@Composable
fun Catalog(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    laptop: Boolean,
    onLaptopChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    startPage: String? = null
) {
    val backStack = remember {
        mutableStateListOf<CatalogKey>(CatalogKey.Home).apply {
            CatalogPages.find { it.id == startPage }?.let { add(CatalogKey.Page(it.id)) }
        }
    }
    val current = backStack.last()
    val onBack: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    // The window's title bar is the background colour; so is everything under it.
    Surface(modifier.fillMaxSize(), color = KotogramTheme.colors.background) {
        Column {
            CatalogTopBar(
                title = when (current) {
                    CatalogKey.Home -> HOME_TITLE
                    is CatalogKey.Page -> current.page.name
                },
                onBack = onBack.takeIf { backStack.size > 1 },
                darkTheme = darkTheme,
                onDarkThemeChange = onDarkThemeChange,
                laptop = laptop,
                onLaptopChange = onLaptopChange
            )
            ProvideFormFactor(laptop) {
                val transitions = rememberNavTransitions<Scene<CatalogKey>>()
                NavDisplay(
                    backStack = backStack,
                    onBack = onBack,
                    transitionSpec = transitions.push,
                    popTransitionSpec = transitions.pop,
                    predictivePopTransitionSpec = transitions.predictivePop,
                    entryProvider = entryProvider {
                        entry<CatalogKey.Home> {
                            CatalogHome(
                                pages = CatalogPages,
                                onPageClick = { backStack.add(CatalogKey.Page(it.id)) },
                                modifier = Modifier.background(KotogramTheme.colors.background)
                            )
                        }
                        entry<CatalogKey.Page> { key ->
                            CatalogPageScreen(
                                page = key.page,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(KotogramTheme.colors.background)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }
                )
            }
        }
    }
}

private sealed interface CatalogKey {
    data object Home : CatalogKey

    data class Page(val id: String) : CatalogKey {
        val page = CatalogPages.first { it.id == id }
    }
}

private const val HOME_TITLE = "Kotogram design system"
