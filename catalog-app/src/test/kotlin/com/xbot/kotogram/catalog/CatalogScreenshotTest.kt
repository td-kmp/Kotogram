package com.xbot.kotogram.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.RoborazziOptions
import com.xbot.kotogram.catalog.pages.CatalogPages
import com.xbot.kotogram.catalog.ui.CatalogHome
import com.xbot.kotogram.catalog.ui.CatalogPageScreen
import com.xbot.kotogram.catalog.ui.CatalogTopBar
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.ProvideFormFactor
import io.github.takahirom.roborazzi.captureRoboImage
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test

@RunWith(Parameterized::class)
class CatalogScreenshotTest(
    private val screen: String,
    private val darkTheme: Boolean,
    private val fontScale: Float,
    private val laptop: Boolean,
) {
    private val windowHeight = if (screen == HOME) HOME_HEIGHT else TALL_SCREENS[screen] ?: PAGE_HEIGHT
    private val scaleSuffix = if (fontScale == 1f) "" else "_font${fontScale.toInt()}x"
    private val formFactorSuffix = if (laptop) "" else "_mobile"

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun screenshot() = runDesktopComposeUiTest(width = WIDTH, height = windowHeight) {
        mainClock.autoAdvance = false
        setContent {
            val density = Density(LocalDensity.current.density, fontScale)
            CompositionLocalProvider(LocalDensity provides density) {
                KotogramTheme(darkTheme = darkTheme, dynamicColor = false) {
                    Surface(color = KotogramTheme.colors.surfaceDim) {
                        Column(Modifier.fillMaxWidth().testTag(SCREEN_TAG)) {
                            if (screen == HOME) {
                                CatalogTopBar(HOME_TITLE, onBack = null, darkTheme, {}, laptop, {})
                                ProvideFormFactor(laptop) { CatalogHome(pages = CatalogPages, onPageClick = {}) }
                            } else {
                                val page = CatalogPages.first { it.id == screen }
                                CatalogTopBar(title = page.name, onBack = {}, darkTheme, {}, laptop, {})
                                ProvideFormFactor(laptop) { CatalogPageScreen(page = page) }
                            }
                        }
                    }
                }
            }
        }
        mainClock.advanceTimeBy(SETTLE_MILLIS)
        onNodeWithTag(SCREEN_TAG).captureRoboImage(
            filePath = "screenshots/${screen}_${if (darkTheme) "dark" else "light"}$scaleSuffix$formFactorSuffix.png",
            roborazziOptions = RoborazziOptions(
                compareOptions = RoborazziOptions.CompareOptions(changeThreshold = CHANGE_THRESHOLD),
            ),
        )
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}, dark {1}, font scale {2}, laptop {3}")
        fun screens(): List<Array<Any>> {
            val themes = (listOf(HOME) + CatalogPages.map { it.id } - MOVING_SCREENS).flatMap { screen ->
                listOf(false, true).map { darkTheme -> arrayOf<Any>(screen, darkTheme, 1f, true) }
            }
            val largeText = FIELD_SCREENS.map { screen -> arrayOf<Any>(screen, false, LARGE_FONT_SCALE, true) }
            val touch = TOUCH_SCREENS.map { screen -> arrayOf<Any>(screen, false, 1f, false) }
            return themes + largeText + touch
        }
    }
}

private const val HOME = "home"
private const val HOME_TITLE = "Kotogram design system"
private val FIELD_SCREENS =
    listOf("text-field", "secure-text-field", "search-field", "code-field", "phone-number-field")
private val TOUCH_SCREENS =
    FIELD_SCREENS + listOf("icon-button", "checkbox", "switch", "top-bar", "chat-list-item", "typography")

private val MOVING_SCREENS = setOf("transitions")
private const val LARGE_FONT_SCALE = 2f
private const val SCREEN_TAG = "catalog-screen"
private const val WIDTH = 900
private const val HOME_HEIGHT = 520
private const val PAGE_HEIGHT = 3000
private val TALL_SCREENS = mapOf("chat-list-item" to 7600)
private const val SETTLE_MILLIS = 6_000L
private const val CHANGE_THRESHOLD = 0.01f
