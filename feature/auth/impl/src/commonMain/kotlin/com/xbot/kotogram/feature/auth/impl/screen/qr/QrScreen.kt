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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.xbot.kotogram.core.designsystem.KotogramLaptopPreview
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.component.QrCode
import com.xbot.kotogram.core.designsystem.component.TextButton
import com.xbot.kotogram.core.designsystem.media.derivedMediaQuery
import com.xbot.kotogram.core.designsystem.media.mediaQuery
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.login_qr1_help1
import com.xbot.kotogram.core.resources.login_qr1_help3
import com.xbot.kotogram.core.resources.login_qr2_help2
import com.xbot.kotogram.core.resources.login_qr_cancel
import com.xbot.kotogram.core.resources.login_qr_title
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.core.resources.voiceover_login_qrcode
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.isBook
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
    val style = rememberStyle(::QrScreenStyleScope, QrScreenStyle.Adaptive)
    val laptop = style.mediaQueryInfo.isLaptop
    val posture = mediaQuery { windowPosture }
    val landscape by derivedMediaQuery { windowHeight < style.landscapeHeight }
    val qrCode: @Composable () -> Unit = {
        QrCode(
            data = state.link,
            contentDescription = stringResource(Res.string.voiceover_login_qrcode)
        )
    }

    context(style) {
        Scaffold(
            modifier = modifier,
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            val screen = Modifier.fillMaxSize().padding(innerPadding)
            when {
                // The code on one side of a phone on its side or of a book-folded screen, the steps on the other.
                !laptop && (landscape || posture.isBook) -> Row(screen) {
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        qrCode()
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = style.touchPadding),
                        verticalArrangement = Arrangement.Center
                    ) {
                        QrInstructions(state, TextAlign.Start)
                        PhoneLoginButton(onPhoneLoginClick)
                    }
                }

                // Half open on a table: the code above the fold, the steps below it.
                !laptop && posture.isTabletop -> Column(screen) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        qrCode()
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = style.touchPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        QrInstructions(state, TextAlign.Center)
                        PhoneLoginButton(onPhoneLoginClick)
                    }
                }

                else -> Column(screen) {
                    BoxWithConstraints(Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .heightIn(min = maxHeight)
                                .padding(horizontal = style.horizontalPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            qrCode()
                            Spacer(Modifier.height(style.beforeTitle))
                            QrInstructions(state, TextAlign.Center)
                            if (laptop) PhoneLoginButton(onPhoneLoginClick)
                        }
                    }
                    // Where the phone screen keeps its Next on touch screens.
                    if (!laptop) {
                        PhoneLoginButton(
                            onClick = onPhoneLoginClick,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = style.touchPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
context(style: QrScreenStyleScope)
private fun ColumnScope.QrInstructions(state: QrScreenState, titleAlign: TextAlign) {
    Text(
        text = stringResource(Res.string.login_qr_title),
        style = style.titleTextStyle,
        textAlign = titleAlign,
        maxLines = 2,
        modifier = Modifier.widthIn(max = style.contentWidth)
    )
    Spacer(Modifier.height(style.beforeSteps))
    Column(verticalArrangement = Arrangement.spacedBy(style.betweenSteps)) {
        listOf(Res.string.login_qr1_help1, Res.string.login_qr2_help2, Res.string.login_qr1_help3)
            .forEachIndexed { index, step ->
                QrStep(number = index + 1, text = stringResource(step))
            }
    }
    state.error?.let { error ->
        Spacer(Modifier.height(style.betweenSteps))
        Text(text = stringResource(error.localizedMessage()), style = style.errorTextStyle)
    }
}

@Composable
context(style: QrScreenStyleScope)
private fun QrStep(number: Int, text: String) {
    Row {
        Box(
            modifier = Modifier
                .size(style.stepNumberSize)
                .background(style.stepNumberColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number.toString(), style = style.stepNumberTextStyle)
        }
        Spacer(Modifier.width(style.stepTextGap))
        // As on macOS: a row is its text and 8 more, with the text 2 below its top.
        Text(
            text = text,
            style = style.stepTextStyle,
            maxLines = 2,
            modifier = Modifier
                .widthIn(max = style.stepTextWidth)
                .padding(top = style.stepTextTop, bottom = style.stepTextBottom)
        )
    }
}

@Composable
context(style: QrScreenStyleScope)
private fun ColumnScope.PhoneLoginButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    // 20 to the label on macOS: the button's own padding gives the other 10.
    Spacer(Modifier.height(style.beforeButton))
    TextButton(onClick = onClick, modifier = modifier) {
        Text(stringResource(Res.string.login_qr_cancel), style = style.buttonTextStyle)
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

@KotogramLaptopPreview
@Composable
private fun QrScreenLaptopPreview(
    @PreviewParameter(QrScreenStateProvider::class) state: QrScreenState
) = QrScreenPreview(state)

private class QrScreenStateProvider : PreviewParameterProvider<QrScreenState> {
    override val values = sequenceOf(
        QrScreenState(),
        QrScreenState(link = "tg://login?token=AQFJjO1qHocdoolqLtckd6dPS98k1HhnVDFilU_N_v6psw"),
        QrScreenState(error = AuthError.TooManyAttempts(retryAfter = 30.seconds))
    )
}
