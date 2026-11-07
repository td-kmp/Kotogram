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
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CatalogScreenshotTest(
    private val screen: String,
    private val darkTheme: Boolean,
    private val fontScale: Float
) {
    private val windowHeight = if (screen == HOME) HOME_HEIGHT else PAGE_HEIGHT
    private val scaleSuffix = if (fontScale == 1f) "" else "_font${fontScale.toInt()}x"

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun screenshot() = runDesktopComposeUiTest(width = WIDTH, height = windowHeight) {
        // The QR code waits for its animation and fades in; a frozen clock makes every run the same.
        mainClock.autoAdvance = false
        setContent {
            val density = Density(LocalDensity.current.density, fontScale)
            CompositionLocalProvider(LocalDensity provides density) {
                KotogramTheme(darkTheme = darkTheme, dynamicColor = false) {
                    Surface {
                        Column(Modifier.fillMaxWidth().testTag(SCREEN_TAG)) {
                            if (screen == HOME) {
                                CatalogTopBar(title = "Kotogram design system", onBack = null, darkTheme, {})
                                CatalogHome(pages = CatalogPages, onPageClick = {})
                            } else {
                                val page = CatalogPages.first { it.id == screen }
                                CatalogTopBar(title = page.name, onBack = {}, darkTheme, {})
                                CatalogPageScreen(page = page)
                            }
                        }
                    }
                }
            }
        }
        mainClock.advanceTimeBy(SETTLE_MILLIS)
        onNodeWithTag(SCREEN_TAG).captureRoboImage(
            filePath = "screenshots/${screen}_${if (darkTheme) "dark" else "light"}$scaleSuffix.png",
            roborazziOptions = RoborazziOptions(
                compareOptions = RoborazziOptions.CompareOptions(changeThreshold = CHANGE_THRESHOLD)
            )
        )
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}, dark {1}, font scale {2}")
        fun screens(): List<Array<Any>> {
            val themes = (listOf(HOME) + CatalogPages.map { it.id }).flatMap { screen ->
                listOf(false, true).map { darkTheme -> arrayOf<Any>(screen, darkTheme, 1f) }
            }
            // Android lets text grow to twice its size; the fields must grow with it.
            val largeText = LARGE_TEXT_SCREENS.map { screen -> arrayOf<Any>(screen, false, LARGE_FONT_SCALE) }
            return themes + largeText
        }
    }
}

private const val HOME = "home"
private val LARGE_TEXT_SCREENS = listOf("text-field", "secure-text-field", "code-field")
private const val LARGE_FONT_SCALE = 2f
private const val SCREEN_TAG = "catalog-screen"
private const val WIDTH = 900
private const val HOME_HEIGHT = 520
private const val PAGE_HEIGHT = 3000
private const val SETTLE_MILLIS = 6_000L
private const val CHANGE_THRESHOLD = 0.01f
