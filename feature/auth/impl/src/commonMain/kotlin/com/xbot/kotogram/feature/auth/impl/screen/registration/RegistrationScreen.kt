package com.xbot.kotogram.feature.auth.impl.screen.registration

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.isSpecified
import com.xbot.kotogram.core.designsystem.PreviewKotogram
import com.xbot.kotogram.core.designsystem.PreviewKotogramLaptop
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.ProminentButton
import com.xbot.kotogram.core.designsystem.component.TextButton
import com.xbot.kotogram.core.designsystem.component.TextField
import com.xbot.kotogram.core.designsystem.component.rememberPhotoPickerLauncher
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.media.derivedMediaQuery
import com.xbot.kotogram.core.designsystem.media.mediaQuery
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.annotatedStringResource
import com.xbot.kotogram.core.resources.login_new_register_footer
import com.xbot.kotogram.core.resources.login_new_register_header
import com.xbot.kotogram.core.resources.login_new_register_info
import com.xbot.kotogram.core.resources.login_new_register_next
import com.xbot.kotogram.core.resources.login_register_firstname_placeholder
import com.xbot.kotogram.core.resources.login_register_lastname_placeholder
import com.xbot.kotogram.core.resources.navigation_back
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.core.resources.termsofservice_title
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.isBook
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun RegistrationScreen(
    viewModel: RegistrationViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.collectAsState()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    RegistrationScreenContent(
        state = state,
        firstName = viewModel.firstName,
        lastName = viewModel.lastName,
        photo = viewModel.photo,
        onAction = viewModel::onAction,
        modifier = modifier,
        focusRequester = focusRequester,
    )
}

@Composable
internal fun RegistrationScreenContent(
    state: RegistrationScreenState,
    firstName: TextFieldState,
    lastName: TextFieldState,
    photo: ByteArray?,
    onAction: (RegistrationScreenAction) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val style = rememberStyle(::RegistrationScreenStyleScope, RegistrationScreenStyle.Adaptive)
    val posture = mediaQuery { windowPosture }
    val landscape by derivedMediaQuery { windowHeight < style.landscapeHeight }
    val actionAtBottom = !style.mediaQueryInfo.isLaptop && !landscape
    val canSubmit = firstName.text.isNotBlank()
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
                            Avatar(photo, onAction)
                            Spacer(Modifier.height(style.beforeTitle))
                        }
                        Text(
                            text = stringResource(Res.string.login_new_register_header),
                            style = style.titleTextStyle,
                        )
                        Spacer(Modifier.height(style.beforeInfo))
                        Text(
                            text = stringResource(Res.string.login_new_register_info),
                            style = style.infoTextStyle,
                            modifier = Modifier.widthIn(max = style.infoWidth),
                        )
                        Spacer(Modifier.height(style.beforeField))
                        NameFields(firstName, lastName, state.error, onAction, focusRequester)
                        Crossfade(
                            targetState = state.error,
                            animationSpec = style.errorFadeSpec,
                            modifier = Modifier
                                .height(style.afterField)
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
                            RegistrationAction(state, canSubmit, onAction)
                            Spacer(Modifier.height(style.beforeLinks))
                        }
                        Text(
                            text = annotatedStringResource(
                                Res.string.login_new_register_footer,
                                linkStyles = TextLinkStyles(SpanStyle(color = style.linkColor)),
                                onLinkClick = {
                                    onAction(RegistrationScreenAction.OnTermsVisibilityChange(true))
                                },
                            ),
                            style = style.termsTextStyle,
                        )
                    }
                }
                if (posture.isTabletop) Spacer(Modifier.weight(1f))
                if (actionAtBottom) {
                    RegistrationAction(
                        state = state,
                        canSubmit = canSubmit,
                        onAction = onAction,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(style.bottomActionInset),
                    )
                }
            }
        }
    }
    if (state.isTermsVisible) TermsDialog(state.termsOfService, onAction)
}

@Composable
context(style: RegistrationScreenStyleScope)
private fun Avatar(
    photo: ByteArray?,
    onAction: (RegistrationScreenAction) -> Unit,
) {
    val picker =
        rememberPhotoPickerLauncher { onAction(RegistrationScreenAction.OnPhotoPicked(it)) }
    val image by produceState<ImageBitmap?>(null, photo) {
        value = photo?.let { withContext(Dispatchers.Default) { it.decodeToImageBitmap() } }
    }
    Box(
        modifier = Modifier
            .size(style.avatarSize)
            .clip(CircleShape)
            .background(style.avatarColor)
            .then(if (picker != null) Modifier.clickable(onClick = picker::launch) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        val current = image
        if (current != null) {
            Image(
                bitmap = current,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        } else {
            Icon(KotogramIcons.Camera, contentDescription = null, tint = style.avatarIconColor)
        }
    }
}

@Composable
context(style: RegistrationScreenStyleScope)
private fun NameFields(
    firstName: TextFieldState,
    lastName: TextFieldState,
    error: AuthError?,
    onAction: (RegistrationScreenAction) -> Unit,
    focusRequester: FocusRequester,
) {
    val nameOptions =
        KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
    Column(verticalArrangement = Arrangement.spacedBy(style.fieldGap)) {
        TextField(
            state = firstName,
            modifier = Modifier.focusRequester(focusRequester).contentWidth(),
            placeholder = { Text(stringResource(Res.string.login_register_firstname_placeholder)) },
            isError = error != null,
            keyboardOptions = nameOptions,
            lineLimits = TextFieldLineLimits.SingleLine,
        )
        TextField(
            state = lastName,
            modifier = Modifier.contentWidth(),
            placeholder = { Text(stringResource(Res.string.login_register_lastname_placeholder)) },
            keyboardOptions = nameOptions.copy(imeAction = ImeAction.Done),
            onKeyboardAction = { onAction(RegistrationScreenAction.OnSubmit) },
            lineLimits = TextFieldLineLimits.SingleLine,
        )
    }
}

@Composable
context(style: RegistrationScreenStyleScope)
private fun RegistrationAction(
    state: RegistrationScreenState,
    canSubmit: Boolean,
    onAction: (RegistrationScreenAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ProminentButton(
        text = stringResource(Res.string.login_new_register_next),
        onClick = { onAction(RegistrationScreenAction.OnSubmit) },
        state = ButtonState.of(enabled = canSubmit, loading = state.isSubmitting),
        modifier = modifier.contentWidth(),
    )
}

@Composable
private fun TermsDialog(
    terms: String,
    onAction: (RegistrationScreenAction) -> Unit,
) {
    val dismiss = { onAction(RegistrationScreenAction.OnTermsVisibilityChange(false)) }
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(stringResource(Res.string.termsofservice_title)) },
        text = { Text(terms, Modifier.verticalScroll(rememberScrollState())) },
        confirmButton = {
            TextButton(onClick = dismiss) { Text(stringResource(Res.string.navigation_back)) }
        },
    )
}

context(style: RegistrationScreenStyleScope)
private fun Modifier.contentWidth(): Modifier {
    val maxWidth = style.contentMaxWidth
    return if (maxWidth.isSpecified) widthIn(max = maxWidth).fillMaxWidth() else this
}

@PreviewKotogram
@Composable
private fun RegistrationScreenPreview(
    @PreviewParameter(RegistrationScreenStateProvider::class) state: RegistrationScreenState,
) {
    RegistrationScreenContent(
        state = state,
        firstName = TextFieldState("Pavel"),
        lastName = TextFieldState(),
        photo = null,
        onAction = {},
    )
}

@PreviewKotogramLaptop
@Composable
private fun RegistrationScreenLaptopPreview(
    @PreviewParameter(RegistrationScreenStateProvider::class) state: RegistrationScreenState,
) = RegistrationScreenPreview(state)

private class RegistrationScreenStateProvider : PreviewParameterProvider<RegistrationScreenState> {
    override val values = sequenceOf(
        RegistrationScreenState(),
        RegistrationScreenState(error = AuthError.InvalidName),
    )
}
