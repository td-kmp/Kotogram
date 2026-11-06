package com.xbot.kotogram.feature.auth.impl.screen.qr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.component.QrCode
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.login_by_phone
import com.xbot.kotogram.core.resources.qr_code_description
import com.xbot.kotogram.core.resources.qr_step_1
import com.xbot.kotogram.core.resources.qr_step_2
import com.xbot.kotogram.core.resources.qr_step_3
import com.xbot.kotogram.core.resources.qr_title
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
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
    Scaffold(modifier = modifier, contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .wrapContentWidth()
                .widthIn(max = 420.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.qr_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            QrCode(
                data = state.link,
                contentDescription = stringResource(Res.string.qr_code_description)
            )
            state.error?.let { error ->
                Text(
                    text = stringResource(error.localizedMessage()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            listOf(Res.string.qr_step_1, Res.string.qr_step_2, Res.string.qr_step_3)
                .forEachIndexed { index, step ->
                    Text("${index + 1}. ${stringResource(step)}", Modifier.fillMaxWidth())
                }
            TextButton(onClick = onPhoneLoginClick) {
                Text(stringResource(Res.string.login_by_phone))
            }
        }
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
