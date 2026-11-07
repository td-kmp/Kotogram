package com.xbot.kotogram.feature.auth.impl.screen.phone

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.OptionPicker
import com.xbot.kotogram.core.designsystem.component.PhoneNumberField
import com.xbot.kotogram.core.designsystem.component.ProminentButton
import com.xbot.kotogram.core.designsystem.component.TextButton
import com.xbot.kotogram.core.designsystem.media.Pc
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PlatformFeature
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.Posture
import com.xbot.kotogram.core.designsystem.media.derivedMediaQuery
import com.xbot.kotogram.core.designsystem.media.mediaQuery
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.app_name
import com.xbot.kotogram.core.resources.login_invalidcountrycode
import com.xbot.kotogram.core.resources.login_new_phonenumber
import com.xbot.kotogram.core.resources.login_next
import com.xbot.kotogram.core.resources.login_phonefieldplaceholder
import com.xbot.kotogram.core.resources.login_qr_login
import com.xbot.kotogram.core.resources.searchfield_search
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
import io.xbot.tdlib.api.CountryInfo
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
internal fun PhoneScreen(
    viewModel: PhoneViewModel,
    canUseQr: Boolean,
    onSubmitted: () -> Unit,
    onQrLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.collectAsState()
    val focusRequester = remember { FocusRequester() }
    viewModel.collectSideEffect { effect ->
        when (effect) {
            PhoneScreenSideEffect.Submitted -> onSubmitted()
        }
    }
    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    PhoneScreenContent(
        state = state,
        phoneNumber = viewModel.phoneNumber,
        canUseQr = canUseQr,
        onAction = viewModel::onAction,
        onQrLoginClick = onQrLoginClick,
        modifier = modifier,
        focusRequester = focusRequester
    )
}

@Composable
internal fun PhoneScreenContent(
    state: PhoneScreenState,
    phoneNumber: TextFieldState,
    canUseQr: Boolean,
    onAction: (PhoneScreenAction) -> Unit,
    onQrLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val computer = mediaQuery { PlatformFeature.Pc in platformFeatures }
    val posture = mediaQuery { windowPosture }
    val landscape by derivedMediaQuery { windowHeight < PhoneScreenTokens.LandscapeHeight }
    val tokens = if (computer) PhoneScreenTokens.Desktop else PhoneScreenTokens.Touch
    // At the bottom as Telegram for iOS has it, iPad included; under the field on computers and phones on their side.
    val actionAtBottom = !computer && !landscape
    val invalidCountry = stringResource(Res.string.login_invalidcountrycode)

    Scaffold(modifier = modifier, contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
        Column(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(if (posture == Posture.Book) 0.5f else 1f)
                .padding(innerPadding)
                .imePadding()
        ) {
            BoxWithConstraints(Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight)
                        .padding(horizontal = PhoneScreenTokens.HorizontalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!landscape && posture != Posture.Tabletop) {
                        // Temporary: Kotogram's logo goes here.
                        Box(Modifier.size(tokens.logoSize).background(Color.White))
                        Spacer(Modifier.height(tokens.beforeTitle))
                    }
                    Text(
                        text = stringResource(Res.string.app_name),
                        style = KotogramTheme.typography.bodyLarge.merge(tokens.titleStyle),
                        color = KotogramTheme.colors.onSurface
                    )
                    Spacer(Modifier.height(tokens.beforeInfo))
                    Text(
                        text = stringResource(Res.string.login_new_phonenumber),
                        style = KotogramTheme.typography.bodyLarge.merge(tokens.infoStyle),
                        color = if (computer) KotogramTheme.colors.onSurfaceVariant else KotogramTheme.colors.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = tokens.infoWidth)
                    )
                    Spacer(Modifier.height(tokens.beforeField))
                    PhoneNumberField(
                        country = state.country?.let { "${it.flagEmoji} ${it.name}" }
                            ?: invalidCountry.takeIf { state.callingCode.isNotEmpty() }.orEmpty(),
                        onCountryClick = { onAction(PhoneScreenAction.OnCountryPickerVisibilityChange(true)) },
                        state = phoneNumber,
                        callingCodeLength = state.callingCode.length,
                        numberPattern = state.numberPattern,
                        numberPlaceholder = stringResource(Res.string.login_phonefieldplaceholder),
                        onSubmit = { onAction(PhoneScreenAction.OnSubmit) },
                        readOnly = state.isSubmitting,
                        focusRequester = focusRequester,
                        modifier = if (computer) {
                            Modifier
                        } else {
                            Modifier.widthIn(max = PhoneScreenTokens.MaxWidth).fillMaxWidth()
                        },
                        countryMenu = { CountryPicker(state, onAction) }
                    )
                    Crossfade(
                        targetState = state.error,
                        animationSpec = tween(PhoneScreenTokens.ErrorFadeMillis),
                        modifier = Modifier
                            .height(PhoneScreenTokens.BeforeAction)
                            .padding(top = PhoneScreenTokens.BeforeError)
                    ) { error ->
                        if (error != null) {
                            Text(
                                text = stringResource(error.localizedMessage()),
                                style = KotogramTheme.typography.bodyMedium,
                                color = KotogramTheme.colors.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    if (!actionAtBottom) {
                        PhoneAction(state, tokens, computer, canUseQr, onAction, onQrLoginClick)
                    }
                }
            }
            // Half open on a table: the form above the fold, the keyboard below it.
            if (posture == Posture.Tabletop) Spacer(Modifier.weight(1f))
            if (actionAtBottom) {
                PhoneAction(
                    state = state,
                    tokens = tokens,
                    computer = computer,
                    canUseQr = canUseQr,
                    onAction = onAction,
                    onQrLoginClick = onQrLoginClick,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(PhoneScreenTokens.BottomActionInset)
                )
            }
        }
    }
}

@Composable
private fun PhoneAction(
    state: PhoneScreenState,
    tokens: PhoneScreenTokens,
    computer: Boolean,
    canUseQr: Boolean,
    onAction: (PhoneScreenAction) -> Unit,
    onQrLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (canUseQr && state.number.isEmpty()) {
        TextButton(onClick = onQrLoginClick, modifier = modifier.height(tokens.actionHeight)) {
            Text(
                text = stringResource(Res.string.login_qr_login),
                style = KotogramTheme.typography.bodyLarge.merge(PhoneScreenTokens.QrLoginStyle)
            )
        }
    } else {
        ProminentButton(
            text = stringResource(Res.string.login_next),
            onClick = { onAction(PhoneScreenAction.OnSubmit) },
            state = ButtonState.of(enabled = state.canSubmit, loading = state.isSubmitting),
            modifier = modifier
                .height(tokens.actionHeight)
                .then(if (computer) Modifier else Modifier.widthIn(max = PhoneScreenTokens.MaxWidth).fillMaxWidth())
        )
    }
}

@Composable
private fun CountryPicker(state: PhoneScreenState, onAction: (PhoneScreenAction) -> Unit) {
    OptionPicker(
        expanded = state.isCountryPickerVisible,
        onDismissRequest = { onAction(PhoneScreenAction.OnCountryPickerVisibilityChange(false)) },
        options = state.countries,
        optionText = { "${it.flagEmoji} ${it.name}  +${it.callingCodes.firstOrNull().orEmpty()}" },
        onSelect = { onAction(PhoneScreenAction.OnCountrySelect(it)) },
        selected = state.country,
        searchPlaceholder = stringResource(Res.string.searchfield_search),
        matches = { country, query ->
            country.name.contains(query, ignoreCase = true) ||
                country.englishName.contains(query, ignoreCase = true) ||
                country.callingCodes.any { it.startsWith(query.removePrefix("+")) }
        }
    )
}

// Telegram for macOS's phone screen (Auth_PhoneNumber, Auth_Insets) on computers and Telegram for iOS's
// (AuthorizationSequencePhoneEntryControllerNode) on touch screens, in their points.
@Suppress("MagicNumber")
private enum class PhoneScreenTokens(
    val logoSize: Dp,
    val beforeTitle: Dp,
    val beforeInfo: Dp,
    val beforeField: Dp,
    val infoWidth: Dp,
    val actionHeight: Dp,
    val titleStyle: TextStyle,
    val infoStyle: TextStyle
) {
    Desktop(
        logoSize = 140.dp,
        beforeTitle = 20.dp,
        beforeInfo = 10.dp,
        beforeField = 20.dp,
        infoWidth = Dp.Infinity,
        actionHeight = 36.dp,
        titleStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium),
        infoStyle = TextStyle(fontSize = 13.5.sp)
    ),
    Touch(
        logoSize = 100.dp,
        beforeTitle = 18.dp,
        beforeInfo = 18.dp,
        beforeField = 30.dp,
        infoWidth = 306.dp,
        actionHeight = 50.dp,
        titleStyle = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
        infoStyle = TextStyle(fontSize = 17.sp)
    );

    companion object {
        val HorizontalPadding = 16.dp
        val MaxWidth = 430.dp
        val BeforeError = 10.dp
        val BeforeAction = 40.dp
        val BottomActionInset = 24.dp
        const val ErrorFadeMillis = 200
        val QrLoginStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)

        // WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND.
        val LandscapeHeight = 480.dp
    }
}

@KotogramPreview
@Composable
private fun PhoneScreenPreview(
    @PreviewParameter(PhoneScreenStateProvider::class) state: PhoneScreenState
) {
    PhoneScreenContent(
        state = state,
        phoneNumber = TextFieldState(state.callingCode + state.number),
        canUseQr = true,
        onAction = {},
        onQrLoginClick = {}
    )
}

private class PhoneScreenStateProvider : PreviewParameterProvider<PhoneScreenState> {
    private val country = CountryInfo(
        countryCode = "UZ",
        name = "Uzbekistan",
        englishName = "Uzbekistan",
        flagEmoji = "🇺🇿",
        isHidden = false,
        callingCodes = listOf("998")
    )
    private val empty = PhoneScreenState(
        countries = listOf(country),
        country = country,
        callingCode = "998",
        numberPattern = "-- --- -- --",
        isAcceptingNumber = true
    )
    private val entered = empty.copy(number = "901234567", numberPattern = "90 123 45 67")

    override val values = sequenceOf(
        empty,
        entered,
        entered.copy(isSubmitting = true),
        entered.copy(error = AuthError.InvalidPhoneNumber)
    )
}
