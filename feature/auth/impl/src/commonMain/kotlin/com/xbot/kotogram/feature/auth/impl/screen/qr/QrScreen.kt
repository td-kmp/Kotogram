package com.xbot.kotogram.feature.auth.impl.screen.qr

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.component.AuthErrorText
import com.xbot.kotogram.feature.auth.impl.component.AuthScaffold
import com.xbot.kotogram.feature.auth.impl.extensions.asString
import com.xbot.kotogram.feature.auth.impl.extensions.toUiText
import com.xbot.kotogram.feature.auth.impl.resources.Res
import com.xbot.kotogram.feature.auth.impl.resources.login_by_phone
import com.xbot.kotogram.feature.auth.impl.resources.qr_step_1
import com.xbot.kotogram.feature.auth.impl.resources.qr_step_2
import com.xbot.kotogram.feature.auth.impl.resources.qr_step_3
import com.xbot.kotogram.feature.auth.impl.resources.qr_title
import kotlin.time.Duration.Companion.seconds
import org.jetbrains.compose.resources.stringResource
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun QrScreen(
    viewModel: QrViewModel,
    onPhoneLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.collectAsState()
    LaunchedEffect(state.needsQrCode) {
        if (state.needsQrCode) viewModel.onAction(QrScreenAction.OnRequestQrCode)
    }
    QrScreenContent(
        state = state,
        onPhoneLoginClick = onPhoneLoginClick,
        modifier = modifier
    )
}

@Composable
private fun QrScreenContent(
    state: QrScreenState,
    onPhoneLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScaffold(title = stringResource(Res.string.qr_title), modifier = modifier) {
        if (state.link == null) {
            CircularProgressIndicator(Modifier.size(48.dp))
        } else {
            Text(state.link, style = MaterialTheme.typography.bodySmall)
        }
        AuthErrorText(state.error?.toUiText()?.asString())
        listOf(Res.string.qr_step_1, Res.string.qr_step_2, Res.string.qr_step_3).forEachIndexed {
                index,
                step
            ->
            Text("${index + 1}. ${stringResource(step)}", modifier = Modifier.fillMaxWidth())
        }
        TextButton(onClick = onPhoneLoginClick) { Text(stringResource(Res.string.login_by_phone)) }
    }
}

@KotogramPreview
@Composable
private fun QrScreenPreview(@PreviewParameter(QrScreenStateProvider::class) state: QrScreenState) {
    QrScreenContent(
        state = state,
        onPhoneLoginClick = {}
    )
}

private class QrScreenStateProvider : PreviewParameterProvider<QrScreenState> {
    override val values = sequenceOf(
        QrScreenState(),
        QrScreenState(link = "tg://login?token=AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHw"),
        QrScreenState(error = AuthError.TooManyAttempts(retryAfter = 30.seconds))
    )
}
