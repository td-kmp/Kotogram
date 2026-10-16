package com.xbot.kotogram.feature.auth.domain.repository

import com.xbot.kotogram.feature.auth.domain.model.AuthException
import com.xbot.kotogram.feature.auth.domain.model.AuthState
import kotlinx.coroutines.flow.Flow

/** Every suspend function throws [AuthException] on failure. */
interface AuthRepository {
    val authState: Flow<AuthState>

    suspend fun setPhoneNumber(phoneNumber: String)

    suspend fun requestQrCode()

    suspend fun checkCode(code: String)

    suspend fun resendCode()

    suspend fun setEmailAddress(email: String)

    suspend fun checkEmailCode(code: String)

    suspend fun checkPassword(password: String)

    suspend fun requestPasswordRecovery()

    suspend fun recoverPassword(recoveryCode: String)

    suspend fun register(firstName: String, lastName: String)

    suspend fun logOut()
}
