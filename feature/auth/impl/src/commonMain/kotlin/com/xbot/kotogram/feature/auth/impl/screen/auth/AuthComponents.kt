package com.xbot.kotogram.feature.auth.impl.screen.auth

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramPreview

// Its text is in the language it offers, from that language's pack.
@Composable
internal fun ContinueOnLanguageButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(onClick = onClick, modifier = modifier, enabled = !isLoading) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
        } else {
            Text(text)
        }
    }
}

@KotogramPreview
@Composable
private fun ContinueOnLanguageButtonPreview(@PreviewParameter(IsLoadingProvider::class) isLoading: Boolean) {
    ContinueOnLanguageButton(text = "Продолжить на русском", isLoading = isLoading, onClick = {})
}

private class IsLoadingProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}
