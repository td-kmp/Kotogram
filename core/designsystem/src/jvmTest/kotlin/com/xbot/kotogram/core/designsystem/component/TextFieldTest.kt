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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TextFieldTest {
    @Test
    fun keepsItsMinimumHeightAtTheUsualScale() {
        assertEquals(TextFieldDefaults.MinHeight, fieldHeight(fontScale = 1f))
    }

    @Test
    fun growsWithTheFontScale() {
        val height = fieldHeight(fontScale = 2f)
        assertTrue(height > TextFieldDefaults.MinHeight, "$height at twice the font scale")
    }

    private fun fieldHeight(fontScale: Float): Dp {
        var height = Dp.Unspecified
        runComposeUiTest {
            setContent {
                val density = Density(LocalDensity.current.density, fontScale)
                CompositionLocalProvider(LocalDensity provides density) {
                    KotogramTheme(darkTheme = false, dynamicColor = false) {
                        TextField(state = TextFieldState("Kotogram"), placeholder = { Text("Username") })
                    }
                }
            }
            height = onNode(hasSetTextAction()).getUnclippedBoundsInRoot().height
        }
        return height
    }
}
