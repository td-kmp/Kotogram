package com.xbot.kotogram.feature.auth.impl.screen.qr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.QrCode
import com.xbot.kotogram.core.designsystem.component.TextButton
import com.xbot.kotogram.core.designsystem.media.Pc
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PlatformFeature
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.Posture
import com.xbot.kotogram.core.designsystem.media.derivedMediaQuery
import com.xbot.kotogram.core.designsystem.media.mediaQuery
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.login_qr1_help1
import com.xbot.kotogram.core.resources.login_qr1_help3
import com.xbot.kotogram.core.resources.login_qr2_help2
import com.xbot.kotogram.core.resources.login_qr_cancel
import com.xbot.kotogram.core.resources.login_qr_title
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.core.resources.voiceover_login_qrcode
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
import kotlin.time.Duration.Companion.seconds
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
internal fun QrScreenContent(
    state: QrScreenState,
    onPhoneLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val computer = mediaQuery { PlatformFeature.Pc in platformFeatures }
    val posture = mediaQuery { windowPosture }
    val landscape by derivedMediaQuery { windowHeight < QrScreenTokens.LandscapeHeight }
    val tokens = if (computer) QrScreenTokens.Desktop else QrScreenTokens.Touch
    val qrCode: @Composable () -> Unit = {
        QrCode(data = state.link, contentDescription = stringResource(Res.string.voiceover_login_qrcode))
    }

    Scaffold(modifier = modifier, contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
        val screen = Modifier.fillMaxSize().padding(innerPadding)
        when {
            // The code on one side of a phone on its side or of a book-folded screen, the steps on the other.
            !computer && (landscape || posture == Posture.Book) -> Row(screen) {
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) { qrCode() }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = QrScreenTokens.TouchPadding),
                    verticalArrangement = Arrangement.Center
                ) {
                    QrInstructions(state, tokens, TextAlign.Start)
                    PhoneLoginButton(tokens, onPhoneLoginClick)
                }
            }
            // Half open on a table: the code above the fold, the steps below it.
            !computer && posture == Posture.Tabletop -> Column(screen) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { qrCode() }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = QrScreenTokens.TouchPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    QrInstructions(state, tokens, TextAlign.Center)
                    PhoneLoginButton(tokens, onPhoneLoginClick)
                }
            }
            else -> Column(screen) {
                BoxWithConstraints(Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .heightIn(min = maxHeight)
                            .padding(horizontal = QrScreenTokens.HorizontalPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        qrCode()
                        Spacer(Modifier.height(tokens.beforeTitle))
                        QrInstructions(state, tokens, TextAlign.Center)
                        if (computer) PhoneLoginButton(tokens, onPhoneLoginClick)
                    }
                }
                // Where the phone screen keeps its Next on touch screens.
                if (!computer) {
                    PhoneLoginButton(
                        tokens = tokens,
                        onClick = onPhoneLoginClick,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = QrScreenTokens.TouchPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.QrInstructions(state: QrScreenState, tokens: QrScreenTokens, titleAlign: TextAlign) {
    Text(
        text = stringResource(Res.string.login_qr_title),
        style = KotogramTheme.typography.bodyLarge.merge(tokens.titleStyle),
        color = KotogramTheme.colors.onSurface,
        textAlign = titleAlign,
        maxLines = 2,
        modifier = Modifier.widthIn(max = QrScreenTokens.ContentWidth)
    )
    Spacer(Modifier.height(tokens.beforeSteps))
    Column(verticalArrangement = Arrangement.spacedBy(tokens.betweenSteps)) {
        listOf(Res.string.login_qr1_help1, Res.string.login_qr2_help2, Res.string.login_qr1_help3)
            .forEachIndexed { index, step ->
                QrStep(number = index + 1, text = stringResource(step), tokens = tokens)
            }
    }
    state.error?.let { error ->
        Spacer(Modifier.height(tokens.betweenSteps))
        Text(
            text = stringResource(error.localizedMessage()),
            color = KotogramTheme.colors.error,
            style = KotogramTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QrStep(number: Int, text: String, tokens: QrScreenTokens) {
    Row {
        Box(
            modifier = Modifier
                .size(tokens.stepNumberSize)
                .background(KotogramTheme.colors.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = KotogramTheme.typography.bodyLarge.merge(tokens.stepNumberStyle),
                color = KotogramTheme.colors.onPrimary
            )
        }
        Spacer(Modifier.width(tokens.stepTextGap))
        // As on macOS: a row is its text and 8 more, with the text 2 below its top.
        Text(
            text = text,
            style = KotogramTheme.typography.bodyLarge.merge(tokens.stepStyle),
            color = KotogramTheme.colors.onSurface,
            maxLines = 2,
            modifier = Modifier
                .widthIn(max = QrScreenTokens.StepTextWidth)
                .padding(top = QrScreenTokens.StepTextTop, bottom = tokens.stepTextBottom)
        )
    }
}

@Composable
private fun ColumnScope.PhoneLoginButton(tokens: QrScreenTokens, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // 20 to the label on macOS: the button's own padding gives the other 10.
    Spacer(Modifier.height(tokens.beforeButton))
    TextButton(onClick = onClick, modifier = modifier.heightIn(min = tokens.buttonHeight)) {
        Text(
            text = stringResource(Res.string.login_qr_cancel),
            style = tokens.buttonStyle?.let(KotogramTheme.typography.bodyLarge::merge) ?: LocalTextStyle.current
        )
    }
}

// Telegram for macOS's QR login screen on computers, in its points; on touch screens our own, in Telegram for
// iOS's type: it has no such screen.
@Suppress("MagicNumber")
private enum class QrScreenTokens(
    val beforeTitle: Dp,
    val beforeSteps: Dp,
    val betweenSteps: Dp,
    val beforeButton: Dp,
    val buttonHeight: Dp,
    val stepNumberSize: Dp,
    val stepTextGap: Dp,
    val stepTextBottom: Dp,
    val titleStyle: TextStyle,
    val stepStyle: TextStyle,
    val stepNumberStyle: TextStyle,
    val buttonStyle: TextStyle?
) {
    Desktop(
        beforeTitle = 20.dp,
        beforeSteps = 20.dp,
        betweenSteps = 10.dp,
        beforeButton = 10.dp,
        buttonHeight = 0.dp,
        stepNumberSize = 20.dp,
        stepTextGap = 10.dp,
        stepTextBottom = 6.dp,
        titleStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
        stepStyle = TextStyle(fontSize = 13.sp),
        stepNumberStyle = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        ),
        buttonStyle = null
    ),
    Touch(
        beforeTitle = 30.dp,
        beforeSteps = 24.dp,
        betweenSteps = 14.dp,
        beforeButton = 12.dp,
        buttonHeight = 50.dp,
        stepNumberSize = 26.dp,
        stepTextGap = 14.dp,
        stepTextBottom = 0.dp,
        titleStyle = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
        stepStyle = TextStyle(fontSize = 17.sp),
        stepNumberStyle = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        ),
        buttonStyle = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium)
    );

    companion object {
        val ContentWidth = 380.dp
        val HorizontalPadding = 16.dp
        val TouchPadding = 24.dp
        val StepTextWidth = 350.dp
        val StepTextTop = 2.dp

        // WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND.
        val LandscapeHeight = 480.dp
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
        QrScreenState(link = "tg://login?token=AQFJjO1qHocdoolqLtckd6dPS98k1HhnVDFilU_N_v6psw"),
        QrScreenState(error = AuthError.TooManyAttempts(retryAfter = 30.seconds))
    )
}
