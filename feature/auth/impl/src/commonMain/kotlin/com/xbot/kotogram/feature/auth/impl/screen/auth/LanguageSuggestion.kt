package com.xbot.kotogram.feature.auth.impl.screen.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.xbot.kotogram.core.designsystem.PreviewKotogram
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.TextButton
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.feature.language.domain.model.LanguageSuggestion
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun LanguageSuggestion(
    viewModel: LanguageSuggestionViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.collectAsState()
    LanguageSuggestionContent(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@Composable
private fun LanguageSuggestionContent(
    state: LanguageSuggestionState,
    onAction: (LanguageSuggestionAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = rememberStyle(::LanguageSuggestionStyleScope, LanguageSuggestionStyle.Default)
    AnimatedContent(
        targetState = state.takeIf { it.suggestion != null },
        modifier = modifier,
        transitionSpec = { fadeIn(style.fadeSpec) togetherWith fadeOut(style.fadeSpec) using null },
        contentKey = { it?.suggestion?.languagePackId },
        label = "LanguageSuggestion",
    ) { shown ->
        shown?.suggestion?.let { suggestion ->
            ContinueOnLanguageButton(
                text = suggestion.text,
                isLoading = shown.isSwitching,
                onClick = { onAction(LanguageSuggestionAction.OnContinueClick) },
            )
        }
    }
}

@Composable
private fun ContinueOnLanguageButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(onClick = onClick, modifier = modifier, state = ButtonState.of(enabled = true, loading = isLoading)) {
        Text(text)
    }
}

@PreviewKotogram
@Composable
private fun LanguageSuggestionPreview(
    @PreviewParameter(LanguageSuggestionStateProvider::class) state: LanguageSuggestionState,
) {
    LanguageSuggestionContent(state = state, onAction = {})
}

private class LanguageSuggestionStateProvider : PreviewParameterProvider<LanguageSuggestionState> {
    private val russian = LanguageSuggestion(languagePackId = "ru", text = "Продолжить на русском")

    override val values = sequenceOf(
        LanguageSuggestionState(suggestion = russian),
        LanguageSuggestionState(suggestion = russian, isSwitching = true),
    )
}
