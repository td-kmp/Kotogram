package com.xbot.kotogram.feature.auth.presentation.screen.qr

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
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.feature.auth.presentation.component.AuthErrorText
import com.xbot.kotogram.feature.auth.presentation.component.AuthScaffold
import com.xbot.kotogram.feature.auth.presentation.component.asString
import com.xbot.kotogram.feature.auth.presentation.component.toUiText
import com.xbot.kotogram.feature.auth.presentation.resources.Res
import com.xbot.kotogram.feature.auth.presentation.resources.login_by_passkey
import com.xbot.kotogram.feature.auth.presentation.resources.login_by_phone
import com.xbot.kotogram.feature.auth.presentation.resources.qr_step_1
import com.xbot.kotogram.feature.auth.presentation.resources.qr_step_2
import com.xbot.kotogram.feature.auth.presentation.resources.qr_step_3
import com.xbot.kotogram.feature.auth.presentation.resources.qr_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun QrScreen(
    canUsePasskey: Boolean,
    onPhoneLoginClick: () -> Unit,
    onPasskeyLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QrViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    // Only while this screen is shown: under the phone screen, a QR request would race the number.
    LaunchedEffect(state.needsQrCode) {
        if (state.needsQrCode) viewModel.onAction(QrScreenAction.OnRequestQrCode)
    }
    QrScreenContent(
        state = state,
        canUsePasskey = canUsePasskey,
        onPhoneLoginClick = onPhoneLoginClick,
        onPasskeyLoginClick = onPasskeyLoginClick,
        modifier = modifier
    )
}

@Composable
private fun QrScreenContent(
    state: QrScreenState,
    canUsePasskey: Boolean,
    onPhoneLoginClick: () -> Unit,
    onPasskeyLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScaffold(title = stringResource(Res.string.qr_title), modifier = modifier) {
        // TODO(KTG-1): render state.link as a QR code with the logo once the design spec picks the library.
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
        if (canUsePasskey) {
            TextButton(onClick = onPasskeyLoginClick) {
                Text(stringResource(Res.string.login_by_passkey))
            }
        }
    }
}
