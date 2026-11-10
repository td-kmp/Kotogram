package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.height
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.ProvideFormFactor
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TextFieldTest {
    @Test
    fun keepsItsMinimumHeightAtTheUsualScale() {
        for (laptop in listOf(true, false)) {
            val field = measure(fontScale = 1f, laptop = laptop)
            assertEquals(field.minHeight, field.height, "laptop $laptop")
        }
    }

    @Test
    fun growsWithTheFontScale() {
        for (laptop in listOf(true, false)) {
            val field = measure(fontScale = 2f, laptop = laptop)
            assertTrue(field.height > field.minHeight, "${field.height} at twice the font scale, laptop $laptop")
        }
    }

    private fun measure(
        fontScale: Float,
        laptop: Boolean,
    ): MeasuredField {
        var minHeight = Dp.Unspecified
        var height = Dp.Unspecified
        runComposeUiTest {
            setContent {
                val density = Density(LocalDensity.current.density, fontScale)
                CompositionLocalProvider(LocalDensity provides density) {
                    KotogramTheme(darkTheme = false, dynamicColor = false) {
                        ProvideFormFactor(laptop) {
                            minHeight = rememberStyle(::TextFieldStyleScope, TextFieldStyle.Adaptive).minHeight
                            TextField(state = TextFieldState("Kotogram"), placeholder = { Text("Username") })
                        }
                    }
                }
            }
            height = onNode(hasSetTextAction()).getUnclippedBoundsInRoot().height
        }
        return MeasuredField(height, minHeight)
    }

    private class MeasuredField(
        val height: Dp,
        val minHeight: Dp,
    )
}
