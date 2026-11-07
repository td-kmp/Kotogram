package com.xbot.kotogram.feature.auth.impl

import arrow.core.Either
import arrow.core.right
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeAuthRepository : AuthRepository {
    override val authorizationState =
        MutableStateFlow<AuthorizationState>(AuthorizationStateWaitPhoneNumber)

    override val isFirstSignIn = MutableStateFlow(true)

    var restarts = 0
        private set

    val phoneNumbers = mutableListOf<String>()

    var phoneNumberResult: Either<AuthError, Unit> = Unit.right()

    override fun restartSignIn() {
        restarts++
        authorizationState.value = AuthorizationStateWaitPhoneNumber
    }

    override suspend fun setAuthenticationPhoneNumber(
        phoneNumber: String
    ): Either<AuthError, Unit> {
        phoneNumbers += phoneNumber
        return phoneNumberResult
    }

    override suspend fun requestQrCodeAuthentication() = ok()

    override suspend fun checkAuthenticationCode(code: String) = ok()

    override suspend fun resendAuthenticationCode() = ok()

    override suspend fun setAuthenticationEmailAddress(emailAddress: String) = ok()

    override suspend fun checkAuthenticationEmailCode(code: String) = ok()

    override suspend fun checkAuthenticationPassword(password: String) = ok()

    override suspend fun requestAuthenticationPasswordRecovery() = ok()

    override suspend fun recoverAuthenticationPassword(recoveryCode: String) = ok()

    override suspend fun registerUser(firstName: String, lastName: String) = ok()

    private fun ok(): Either<AuthError, Unit> = Unit.right()
}
