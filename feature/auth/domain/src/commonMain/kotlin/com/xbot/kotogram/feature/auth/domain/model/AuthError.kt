package com.xbot.kotogram.feature.auth.domain.model

sealed interface AuthError {
    data object InvalidPhoneNumber : AuthError

    data object PhoneNumberBanned : AuthError

    data object InvalidCode : AuthError

    data object CodeExpired : AuthError

    data object InvalidPassword : AuthError

    data object InvalidEmail : AuthError

    data object InvalidName : AuthError

    data object PasskeyUnavailable : AuthError

    data object PasskeyCancelled : AuthError

    data class TooManyAttempts(val retryAfterSeconds: Int?) : AuthError

    data class Unknown(val message: String) : AuthError
}

class AuthException(val error: AuthError, cause: Throwable? = null) :
    Exception(error.toString(), cause)
