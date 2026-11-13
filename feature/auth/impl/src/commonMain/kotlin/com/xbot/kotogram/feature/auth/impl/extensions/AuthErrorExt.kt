package com.xbot.kotogram.feature.auth.impl.extensions

import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.StringResource
import com.xbot.kotogram.core.resources.email_invalidemailerror
import com.xbot.kotogram.core.resources.login_floodwait
import com.xbot.kotogram.core.resources.login_invalidfirstnameerror
import com.xbot.kotogram.core.resources.login_new_phonebannederror
import com.xbot.kotogram.core.resources.password_hash_invalid
import com.xbot.kotogram.core.resources.phone_code_expired
import com.xbot.kotogram.core.resources.phone_code_invalid
import com.xbot.kotogram.core.resources.phone_number_invalid
import com.xbot.kotogram.core.resources.unknownerror
import com.xbot.kotogram.feature.auth.domain.model.AuthError

internal fun AuthError.localizedMessage(): StringResource = when (this) {
    InvalidPhoneNumber -> StringResource.Text(Res.string.phone_number_invalid)
    PhoneNumberBanned -> StringResource.Text(Res.string.login_new_phonebannederror)
    InvalidCode -> StringResource.Text(Res.string.phone_code_invalid)
    CodeExpired -> StringResource.Text(Res.string.phone_code_expired)
    InvalidPassword -> StringResource.Text(Res.string.password_hash_invalid)
    InvalidEmail -> StringResource.Text(Res.string.email_invalidemailerror)
    InvalidName -> StringResource.Text(Res.string.login_invalidfirstnameerror)
    is TooManyAttempts -> StringResource.Text(Res.string.login_floodwait)
    is Unknown -> StringResource.Text(Res.string.unknownerror)
}
