package com.xbot.kotogram.feature.auth.impl.screen.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.TextButton
import com.xbot.kotogram.feature.language.domain.model.LanguageSuggestion
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun LanguageSuggestion(viewModel: LanguageSuggestionViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.collectAsState()
    LanguageSuggestionContent(state = state, onAction = viewModel::onAction, modifier = modifier)
}

// Fades in once the server's suggestion and its one string are in, and out when it's the language in use.
@Composable
private fun LanguageSuggestionContent(
    state: LanguageSuggestionState,
    onAction: (LanguageSuggestionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = state.takeIf { it.suggestion != null },
        modifier = modifier,
        transitionSpec = {
            fadeIn(tween(LanguageSuggestionTokens.FADE_MILLIS)) togetherWith
                fadeOut(tween(LanguageSuggestionTokens.FADE_MILLIS)) using null
        },
        contentKey = { it?.suggestion?.languagePackId },
        label = "LanguageSuggestion"
    ) { shown ->
        shown?.suggestion?.let { suggestion ->
            ContinueOnLanguageButton(
                text = suggestion.text,
                isLoading = shown.isSwitching,
                onClick = { onAction(LanguageSuggestionAction.OnContinueClick) }
            )
        }
    }
}

// Its text is in the language it offers, from that language's pack.
@Composable
private fun ContinueOnLanguageButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(onClick = onClick, modifier = modifier, state = ButtonState.of(enabled = true, loading = isLoading)) {
        Text(text)
    }
}

@Suppress("MagicNumber")
private object LanguageSuggestionTokens {
    const val FADE_MILLIS = 200
}

@KotogramPreview
@Composable
private fun LanguageSuggestionPreview(
    @PreviewParameter(LanguageSuggestionStateProvider::class) state: LanguageSuggestionState
) {
    LanguageSuggestionContent(state = state, onAction = {})
}

private class LanguageSuggestionStateProvider : PreviewParameterProvider<LanguageSuggestionState> {
    private val russian = LanguageSuggestion(languagePackId = "ru", text = "Продолжить на русском")

    override val values = sequenceOf(
        LanguageSuggestionState(suggestion = russian),
        LanguageSuggestionState(suggestion = russian, isSwitching = true)
    )
}
