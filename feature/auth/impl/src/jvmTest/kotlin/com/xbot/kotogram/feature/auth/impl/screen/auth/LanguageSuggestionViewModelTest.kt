package com.xbot.kotogram.feature.auth.impl.screen.auth

import com.xbot.kotogram.feature.auth.impl.FakeAuthRepository
import com.xbot.kotogram.feature.auth.impl.FakeLanguageRepository
import com.xbot.kotogram.feature.auth.impl.FakeLanguageSuggestionRepository
import com.xbot.kotogram.feature.language.domain.model.LanguageSuggestion
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.test.testWithInternalState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LanguageSuggestionViewModelTest {
    private val auth = FakeAuthRepository()
    private val languages = FakeLanguageRepository()
    private val suggestions = FakeLanguageSuggestionRepository()
    private val russian = LanguageSuggestion(languagePackId = "ru", text = "Продолжить на русском")

    @Test
    fun theServersLanguageIsOfferedAtTheFirstSignIn() = runTest {
        suggestions.suggestion.value = russian
        assertEquals(russian, state().suggestion)
    }

    @Test
    fun noLanguageIsOfferedWhenAnotherAccountIsSignedIn() = runTest {
        suggestions.suggestion.value = russian
        auth.isFirstSignIn.value = false
        assertNull(state().suggestion)
    }

    @Test
    fun continuingSwitchesToTheOfferedLanguage() = runTest {
        suggestions.suggestion.value = russian
        languages.switched = CompletableDeferred()
        assertTrue(state(LanguageSuggestionAction.OnContinueClick).isSwitching)
        assertEquals(listOf("ru"), languages.applied)
    }

    private suspend fun TestScope.state(action: LanguageSuggestionAction? = null): LanguageSuggestionState {
        lateinit var state: LanguageSuggestionState
        LanguageSuggestionViewModel(suggestions, languages, auth).testWithInternalState(this) {
            val onCreate = runOnCreate()
            runCurrent()
            action?.let(containerHost::onAction)
            runCurrent()
            state = containerHost.container.stateFlow.value
            onCreate.cancel()
            cancelAndIgnoreRemainingItems()
        }
        return state
    }
}
