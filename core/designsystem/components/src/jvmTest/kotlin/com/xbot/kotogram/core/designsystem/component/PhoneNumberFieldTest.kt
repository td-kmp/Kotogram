package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.ProvideFormFactor
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PhoneNumberFieldTest {
    @Test
    fun writesTheNumberOnItsPatternAndTheRestOfThePatternUnderIt() = runComposeUiTest {
        setField(TextFieldState("99890"), pattern = "90 --- -- --")
        assertEquals("+998 90", shownText())
        onNodeWithText("+998 90 --- -- --", useUnmergedTree = true).assertExists()
    }

    @Test
    fun writesThePatternAfterACodeAlone() = runComposeUiTest {
        setField(TextFieldState("998"), pattern = "-- --- -- --")
        assertEquals("+998", shownText())
        onNodeWithText("+998 -- --- -- --", useUnmergedTree = true).assertExists()
    }

    @Test
    fun writesThePlaceholderAfterACodeWithoutAPattern() = runComposeUiTest {
        setField(TextFieldState("999"), pattern = "")
        assertEquals("+999", shownText())
        onNodeWithText("+999 $PLACEHOLDER", useUnmergedTree = true).assertExists()
    }

    @Test
    fun writesNothingUnderACompleteNumber() = runComposeUiTest {
        setField(TextFieldState("998901234567"), pattern = "90 123 45 67")
        assertEquals("+998 90 123 45 67", shownText())
        onAllNodesWithText("-", substring = true, useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun showsThePlaceholderWithoutDigits() = runComposeUiTest {
        setField(TextFieldState(), pattern = "", callingCodeLength = 0)
        assertEquals("", shownText())
        onNodeWithText(PLACEHOLDER, useUnmergedTree = true).assertExists()
    }

    @Test
    fun writesANumberWithoutACodeRightAfterThePlus() = runComposeUiTest {
        setField(TextFieldState("9996621234"), pattern = "9996621234", callingCodeLength = 0)
        assertEquals("+9996621234", shownText())
    }

    @Test
    fun takesDigitsOnly() = runComposeUiTest {
        val state = TextFieldState()
        setField(state, pattern = "", callingCodeLength = 0)
        onNode(hasSetTextAction()).performTextInput("+9 (98) 90-1")
        assertEquals("998901", state.text.toString())
    }

    @Test
    fun keepsTheCursorAfterTheDigitsWhenThePatternIsTapped() = runComposeUiTest {
        val state = TextFieldState("99890")
        setField(state, pattern = "90 --- -- --")
        onNode(hasSetTextAction()).performTouchInput { click(centerRight) }
        assertEquals(TextRange(state.text.length), state.selection)
        onNode(hasSetTextAction()).performTextInput("1")
        assertEquals("998901", state.text.toString())
        assertEquals(TextRange(state.text.length), state.selection)
    }

    @Test
    fun isTwoRowsHigh() {
        for ((laptop, height) in listOf(false to 100.dp, true to 80.dp)) {
            runComposeUiTest {
                setField(TextFieldState("998"), pattern = "-- --- -- --", laptop = laptop)
                assertEquals(height, onNodeWithTag(FIELD_TAG).getUnclippedBoundsInRoot().height, "laptop $laptop")
            }
        }
    }

    private fun ComposeUiTest.setField(
        state: TextFieldState,
        pattern: String,
        callingCodeLength: Int = CALLING_CODE_LENGTH,
        laptop: Boolean = false,
    ) {
        setContent {
            KotogramTheme(darkTheme = false, dynamicColor = false) {
                ProvideFormFactor(laptop) {
                    PhoneNumberField(
                        country = "Uzbekistan",
                        onCountryClick = {},
                        state = state,
                        callingCodeLength = callingCodeLength,
                        numberPattern = pattern,
                        numberPlaceholder = PLACEHOLDER,
                        onSubmit = {},
                        modifier = Modifier.testTag(FIELD_TAG),
                    )
                }
            }
        }
    }

    private fun ComposeUiTest.shownText(): String = onNode(
        hasSetTextAction(),
    ).fetchSemanticsNode().config.getOrNull(SemanticsProperties.EditableText)?.text.orEmpty()
}

private const val CALLING_CODE_LENGTH = 3
private const val PLACEHOLDER = "phone number"
private const val FIELD_TAG = "field"
