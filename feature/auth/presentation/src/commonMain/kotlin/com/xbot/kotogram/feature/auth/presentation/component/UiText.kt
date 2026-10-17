package com.xbot.kotogram.feature.auth.presentation.component

import androidx.compose.runtime.Composable
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.presentation.resources.Res
import com.xbot.kotogram.feature.auth.presentation.resources.error_code_expired
import com.xbot.kotogram.feature.auth.presentation.resources.error_invalid_code
import com.xbot.kotogram.feature.auth.presentation.resources.error_invalid_email
import com.xbot.kotogram.feature.auth.presentation.resources.error_invalid_name
import com.xbot.kotogram.feature.auth.presentation.resources.error_invalid_password
import com.xbot.kotogram.feature.auth.presentation.resources.error_invalid_phone
import com.xbot.kotogram.feature.auth.presentation.resources.error_passkey_cancelled
import com.xbot.kotogram.feature.auth.presentation.resources.error_passkey_unavailable
import com.xbot.kotogram.feature.auth.presentation.resources.error_phone_banned
import com.xbot.kotogram.feature.auth.presentation.resources.error_too_many_attempts
import com.xbot.kotogram.feature.auth.presentation.resources.error_too_many_attempts_seconds
import com.xbot.kotogram.feature.auth.presentation.resources.error_unknown
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

internal class UiText(val resource: StringResource, vararg val args: Any)

@Composable
internal fun UiText.asString(): String = stringResource(resource, *args)

internal suspend fun UiText.resolve(): String = getString(resource, *args)

internal fun AuthError.toUiText(): UiText = when (this) {
    AuthError.InvalidPhoneNumber -> UiText(Res.string.error_invalid_phone)

    AuthError.PhoneNumberBanned -> UiText(Res.string.error_phone_banned)

    AuthError.InvalidCode -> UiText(Res.string.error_invalid_code)

    AuthError.CodeExpired -> UiText(Res.string.error_code_expired)

    AuthError.InvalidPassword -> UiText(Res.string.error_invalid_password)

    AuthError.InvalidEmail -> UiText(Res.string.error_invalid_email)

    AuthError.InvalidName -> UiText(Res.string.error_invalid_name)

    AuthError.PasskeyUnavailable -> UiText(Res.string.error_passkey_unavailable)

    AuthError.PasskeyCancelled -> UiText(Res.string.error_passkey_cancelled)

    is AuthError.TooManyAttempts ->
        retryAfter
            ?.let { UiText(Res.string.error_too_many_attempts_seconds, it.inWholeSeconds) }
            ?: UiText(Res.string.error_too_many_attempts)

    is AuthError.Unknown -> UiText(Res.string.error_unknown, message)
}
