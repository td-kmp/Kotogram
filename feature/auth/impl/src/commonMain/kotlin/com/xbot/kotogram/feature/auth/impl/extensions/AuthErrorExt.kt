package com.xbot.kotogram.feature.auth.impl.extensions

import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.component.UiText
import com.xbot.kotogram.feature.auth.impl.resources.Res
import com.xbot.kotogram.feature.auth.impl.resources.error_code_expired
import com.xbot.kotogram.feature.auth.impl.resources.error_invalid_code
import com.xbot.kotogram.feature.auth.impl.resources.error_invalid_email
import com.xbot.kotogram.feature.auth.impl.resources.error_invalid_name
import com.xbot.kotogram.feature.auth.impl.resources.error_invalid_password
import com.xbot.kotogram.feature.auth.impl.resources.error_invalid_phone
import com.xbot.kotogram.feature.auth.impl.resources.error_phone_banned
import com.xbot.kotogram.feature.auth.impl.resources.error_too_many_attempts
import com.xbot.kotogram.feature.auth.impl.resources.error_too_many_attempts_seconds
import com.xbot.kotogram.feature.auth.impl.resources.error_unknown

internal fun AuthError.toUiText(): UiText = when (this) {
    AuthError.InvalidPhoneNumber -> UiText(Res.string.error_invalid_phone)

    AuthError.PhoneNumberBanned -> UiText(Res.string.error_phone_banned)

    AuthError.InvalidCode -> UiText(Res.string.error_invalid_code)

    AuthError.CodeExpired -> UiText(Res.string.error_code_expired)

    AuthError.InvalidPassword -> UiText(Res.string.error_invalid_password)

    AuthError.InvalidEmail -> UiText(Res.string.error_invalid_email)

    AuthError.InvalidName -> UiText(Res.string.error_invalid_name)

    is AuthError.TooManyAttempts ->
        retryAfter
            ?.let { UiText(Res.string.error_too_many_attempts_seconds, it.inWholeSeconds) }
            ?: UiText(Res.string.error_too_many_attempts)

    is AuthError.Unknown -> UiText(Res.string.error_unknown, message)
}
