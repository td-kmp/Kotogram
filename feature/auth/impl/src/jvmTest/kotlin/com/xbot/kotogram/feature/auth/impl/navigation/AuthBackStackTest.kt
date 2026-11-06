package com.xbot.kotogram.feature.auth.impl.navigation

import com.xbot.kotogram.feature.auth.domain.model.LoginMethod
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthBackStackTest {
    @Test
    fun theQrCodeIsTheEntryBeforeTheFirstStep() = assertEquals(
        listOf(AuthStep.QrCode),
        authBackStack(LoginMethod.QrCode, step = null, dismissed = null)
    )

    @Test
    fun pastAStepTheEntryIsThePhone() = assertEquals(
        listOf(AuthStep.PhoneNumber, AuthStep.Code),
        authBackStack(LoginMethod.QrCode, step = AuthStep.Code, dismissed = null)
    )

    @Test
    fun aStepTheUserWentBackFromStaysHidden() = assertEquals(
        listOf(AuthStep.PhoneNumber),
        authBackStack(LoginMethod.PhoneNumber, step = AuthStep.Code, dismissed = AuthStep.Code)
    )

    @Test
    fun aNewStepShowsAfterGoingBackFromAnother() = assertEquals(
        listOf(AuthStep.PhoneNumber, AuthStep.Password),
        authBackStack(LoginMethod.PhoneNumber, step = AuthStep.Password, dismissed = AuthStep.Code)
    )

    @Test
    fun withoutAStepThereIsOnlyTheEntry() = assertEquals(
        listOf(AuthStep.PhoneNumber),
        authBackStack(LoginMethod.PhoneNumber, step = null, dismissed = null)
    )
}
