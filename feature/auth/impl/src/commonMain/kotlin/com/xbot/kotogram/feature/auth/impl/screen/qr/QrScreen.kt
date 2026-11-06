package com.xbot.kotogram.feature.auth.impl.screen.qr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        BoxWithConstraints(Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(horizontal = QrScreenTokens.HorizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                QrCode(
                    data = state.link,
                    contentDescription = stringResource(Res.string.qr_code_description)
                )
                Spacer(Modifier.height(QrScreenTokens.BetweenHeader))
                Text(
                    text = stringResource(Res.string.qr_title),
                    style = QrScreenTokens.TitleStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.widthIn(max = QrScreenTokens.ContentWidth)
                )
                Spacer(Modifier.height(QrScreenTokens.BetweenHeader))
                QrSteps()
                state.error?.let { error ->
                    Spacer(Modifier.height(QrScreenTokens.BetweenSteps))
                    Text(
                        text = stringResource(error.localizedMessage()),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
                // 20 to the label, as on macOS: the button's own padding gives the other 10.
                Spacer(Modifier.height(QrScreenTokens.BeforeButton))
                TextButton(onClick = onPhoneLoginClick) {
                    Text(stringResource(Res.string.login_by_phone))
                }
            }
        }
    }
}

@Composable
private fun QrSteps(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(QrScreenTokens.BetweenSteps)
    ) {
        listOf(Res.string.qr_step_1, Res.string.qr_step_2, Res.string.qr_step_3)
            .forEachIndexed { index, step ->
                QrStep(number = index + 1, text = stringResource(step))
            }
    }
}

@Composable
private fun QrStep(number: Int, text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(QrScreenTokens.StepNumberSize)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = QrScreenTokens.StepNumberStyle,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(Modifier.width(QrScreenTokens.StepTextGap))
        // As on macOS: a row is its text and 8 more, with the text 2 below its top.
        Text(
            text = text,
            style = QrScreenTokens.StepStyle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            modifier = Modifier
                .widthIn(max = QrScreenTokens.StepTextWidth)
                .padding(top = QrScreenTokens.StepTextTop, bottom = QrScreenTokens.StepTextBottom)
        )
    }
}

// Telegram for macOS's QR login screen, in its points.
@Suppress("MagicNumber")
private object QrScreenTokens {
    val ContentWidth = 380.dp
    val HorizontalPadding = 16.dp
    val BetweenHeader = 20.dp
    val BetweenSteps = 10.dp
    val BeforeButton = 10.dp
    val StepNumberSize = 20.dp
    val StepTextGap = 10.dp
    val StepTextWidth = 350.dp
    val StepTextTop = 2.dp
    val StepTextBottom = 6.dp

    val TitleStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium)
    val StepStyle = TextStyle(fontSize = 13.sp)
    val StepNumberStyle = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Monospace
    )
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
        QrScreenState(link = "tg://login?token=AQFJjO1qHocdoolqLtckd6dPS98k1HhnVDFilU_N_v6psw"),
        QrScreenState(error = AuthError.TooManyAttempts(retryAfter = 30.seconds))
    )
}
