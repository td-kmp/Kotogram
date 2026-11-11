package com.xbot.kotogram.feature.auth.impl.screen.phone

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.isSpecified
import com.xbot.kotogram.core.designsystem.PreviewKotogram
import com.xbot.kotogram.core.designsystem.PreviewKotogramLaptop
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.LottieImage
import com.xbot.kotogram.core.designsystem.component.OptionLabel
import com.xbot.kotogram.core.designsystem.component.OptionPicker
import com.xbot.kotogram.core.designsystem.component.PhoneNumberField
import com.xbot.kotogram.core.designsystem.component.ProminentButton
import com.xbot.kotogram.core.designsystem.component.TextButton
import com.xbot.kotogram.core.designsystem.media.derivedMediaQuery
import com.xbot.kotogram.core.designsystem.media.mediaQuery
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.app_name
import com.xbot.kotogram.core.resources.login_countrylabel
import com.xbot.kotogram.core.resources.login_invalidcountrycode
import com.xbot.kotogram.core.resources.login_new_phonenumber
import com.xbot.kotogram.core.resources.login_next
import com.xbot.kotogram.core.resources.login_phonefieldplaceholder
import com.xbot.kotogram.core.resources.login_qr_login
import com.xbot.kotogram.core.resources.searchfield_search
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.isBook
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
import io.xbot.tdlib.api.CountryInfo
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
internal fun PhoneScreen(
    viewModel: PhoneViewModel,
    canUseQr: Boolean,
    onSubmit: () -> Unit,
    onQrLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.collectAsState()
    val focusRequester = remember { FocusRequester() }
    viewModel.collectSideEffect { effect ->
        when (effect) {
            PhoneScreenSideEffect.Submitted -> onSubmit()
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
        focusRequester = focusRequester,
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
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val style = rememberStyle(::PhoneScreenStyleScope, PhoneScreenStyle.Adaptive)
    val posture = mediaQuery { windowPosture }
    val landscape by derivedMediaQuery { windowHeight < style.landscapeHeight }
    val actionAtBottom = !style.mediaQueryInfo.isLaptop && !landscape
    val invalidCountry = stringResource(Res.string.login_invalidcountrycode)

    context(style) {
        Scaffold(
            modifier = modifier,
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { innerPadding ->
            Column(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (posture.isBook) 0.5f else 1f)
                    .padding(innerPadding),
            ) {
                BoxWithConstraints(Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .heightIn(min = maxHeight)
                            .padding(horizontal = style.horizontalPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (!landscape && !posture.isTabletop) {
                            LottieImage(STICKER, Modifier.size(style.logoSize))
                            Spacer(Modifier.height(style.beforeTitle))
                        }
                        Text(
                            text = stringResource(Res.string.app_name),
                            style = style.titleTextStyle,
                        )
                        Spacer(Modifier.height(style.beforeInfo))
                        Text(
                            text = stringResource(Res.string.login_new_phonenumber),
                            style = style.infoTextStyle,
                            modifier = Modifier.widthIn(max = style.infoWidth),
                        )
                        Spacer(Modifier.height(style.beforeField))
                        PhoneNumberField(
                            country = state.country?.let { "${it.flagEmoji} ${it.name}" }
                                ?: invalidCountry.takeIf {
                                    state.callingCode.isNotEmpty()
                                }.orEmpty(),
                            onCountryClick = {
                                onAction(PhoneScreenAction.OnCountryPickerVisibilityChange(true))
                            },
                            state = phoneNumber,
                            callingCodeLength = state.callingCode.length,
                            numberPattern = state.numberPattern,
                            numberPlaceholder = stringResource(
                                Res.string.login_phonefieldplaceholder,
                            ),
                            onSubmit = { onAction(PhoneScreenAction.OnSubmit) },
                            modifier = Modifier.contentWidth(),
                            countryPlaceholder = stringResource(Res.string.login_countrylabel),
                            readOnly = state.isSubmitting,
                            isError = state.error != null,
                            focusRequester = focusRequester,
                            countryMenu = { CountryPicker(state, onAction) },
                        )
                        Crossfade(
                            targetState = state.error,
                            animationSpec = style.errorFadeSpec,
                            modifier = Modifier
                                .height(style.beforeAction)
                                .padding(top = style.beforeError),
                        ) { error ->
                            if (error != null) {
                                Text(
                                    text = stringResource(error.localizedMessage()),
                                    style = style.errorTextStyle,
                                )
                            }
                        }
                        if (!actionAtBottom) {
                            PhoneAction(state, canUseQr, onAction, onQrLoginClick)
                        }
                    }
                }
                if (posture.isTabletop) Spacer(Modifier.weight(1f))
                if (actionAtBottom) {
                    PhoneAction(
                        state = state,
                        canUseQr = canUseQr,
                        onAction = onAction,
                        onQrLoginClick = onQrLoginClick,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(style.bottomActionInset),
                    )
                }
            }
        }
    }
}

@Composable
context(style: PhoneScreenStyleScope)
private fun PhoneAction(
    state: PhoneScreenState,
    canUseQr: Boolean,
    onAction: (PhoneScreenAction) -> Unit,
    onQrLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (canUseQr && state.number.isEmpty()) {
        TextButton(onClick = onQrLoginClick, modifier = modifier) {
            Text(text = stringResource(Res.string.login_qr_login), style = style.qrLoginTextStyle)
        }
    } else {
        ProminentButton(
            text = stringResource(Res.string.login_next),
            onClick = { onAction(PhoneScreenAction.OnSubmit) },
            state = ButtonState.of(enabled = state.canSubmit, loading = state.isSubmitting),
            modifier = modifier.contentWidth(),
        )
    }
}

context(style: PhoneScreenStyleScope)
private fun Modifier.contentWidth(): Modifier {
    val maxWidth = style.contentMaxWidth
    return if (maxWidth.isSpecified) widthIn(max = maxWidth).fillMaxWidth() else this
}

@Composable
private fun CountryPicker(
    state: PhoneScreenState,
    onAction: (PhoneScreenAction) -> Unit,
) {
    OptionPicker(
        expanded = state.isCountryPickerVisible,
        onDismissRequest = { onAction(PhoneScreenAction.OnCountryPickerVisibilityChange(false)) },
        options = state.countries,
        optionLabel = { country ->
            OptionLabel(
                text = country.name,
                leading = country.flagEmoji,
                supporting = country.englishName.takeIf { it != country.name },
                trailing = country.callingCodes.firstOrNull()?.let { "+$it" },
            )
        },
        onSelect = { onAction(PhoneScreenAction.OnCountrySelect(it)) },
        // Workaround: macOS's pack has no title for the picker, so the field's label stands in.
        title = stringResource(Res.string.login_countrylabel).replaceFirstChar { it.titlecase() },
        selected = state.country,
        optionSection = { it.name.take(1).uppercase() },
        searchPlaceholder = stringResource(Res.string.searchfield_search),
        matches = { country, query ->
            country.name.contains(query, ignoreCase = true) ||
                country.englishName.contains(query, ignoreCase = true) ||
                country.callingCodes.any { it.startsWith(query.removePrefix("+")) }
        },
    )
}

@PreviewKotogram
@Composable
private fun PhoneScreenPreview(@PreviewParameter(PhoneScreenStateProvider::class) state: PhoneScreenState) {
    PhoneScreenContent(
        state = state,
        phoneNumber = TextFieldState(state.callingCode + state.number),
        canUseQr = true,
        onAction = {},
        onQrLoginClick = {},
    )
}

@PreviewKotogramLaptop
@Composable
private fun PhoneScreenLaptopPreview(@PreviewParameter(PhoneScreenStateProvider::class) state: PhoneScreenState) =
    PhoneScreenPreview(state)

private class PhoneScreenStateProvider : PreviewParameterProvider<PhoneScreenState> {
    private val country = CountryInfo(
        countryCode = "UZ",
        name = "Uzbekistan",
        englishName = "Uzbekistan",
        flagEmoji = "🇺🇿",
        isHidden = false,
        callingCodes = listOf("998"),
    )
    private val empty = PhoneScreenState(
        countries = listOf(country),
        country = country,
        callingCode = "998",
        numberPattern = "-- --- -- --",
        isAcceptingNumber = true,
    )
    private val entered = empty.copy(number = "901234567", numberPattern = "90 123 45 67")

    override val values = sequenceOf(
        empty,
        entered,
        entered.copy(isSubmitting = true),
        entered.copy(error = AuthError.InvalidPhoneNumber),
    )
}

private const val STICKER = "files/phone.lottie"
