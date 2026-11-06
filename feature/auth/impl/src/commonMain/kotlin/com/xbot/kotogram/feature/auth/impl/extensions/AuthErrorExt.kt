package com.xbot.kotogram.feature.auth.impl.extensions

import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.StringResource
import com.xbot.kotogram.core.resources.error_code_expired
import com.xbot.kotogram.core.resources.error_invalid_code
import com.xbot.kotogram.core.resources.error_invalid_email
import com.xbot.kotogram.core.resources.error_invalid_name
import com.xbot.kotogram.core.resources.error_invalid_password
import com.xbot.kotogram.core.resources.error_invalid_phone
import com.xbot.kotogram.core.resources.error_phone_banned
import com.xbot.kotogram.core.resources.error_too_many_attempts
import com.xbot.kotogram.core.resources.error_too_many_attempts_seconds
import com.xbot.kotogram.core.resources.error_unknown
import com.xbot.kotogram.feature.auth.domain.model.AuthError

internal fun AuthError.localizedMessage(): StringResource = when (this) {
    AuthError.InvalidPhoneNumber -> StringResource.Text(Res.string.error_invalid_phone)

    AuthError.PhoneNumberBanned -> StringResource.Text(Res.string.error_phone_banned)

    AuthError.InvalidCode -> StringResource.Text(Res.string.error_invalid_code)

    AuthError.CodeExpired -> StringResource.Text(Res.string.error_code_expired)

    AuthError.InvalidPassword -> StringResource.Text(Res.string.error_invalid_password)

    AuthError.InvalidEmail -> StringResource.Text(Res.string.error_invalid_email)

    AuthError.InvalidName -> StringResource.Text(Res.string.error_invalid_name)

    is AuthError.TooManyAttempts ->
        retryAfter
            ?.let {
                StringResource.Text(Res.string.error_too_many_attempts_seconds, it.inWholeSeconds)
            }
            ?: StringResource.Text(Res.string.error_too_many_attempts)

    is AuthError.Unknown -> StringResource.Text(Res.string.error_unknown, message)
}
