package com.xbot.kotogram.feature.auth.impl.screen.phone

import arrow.core.left
import com.xbot.kotogram.feature.auth.domain.LoginEntry
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.EnteredPhoneNumber
import com.xbot.kotogram.feature.auth.domain.model.LoginFlow
import com.xbot.kotogram.feature.auth.impl.FakeAuthRepository
import com.xbot.kotogram.feature.auth.impl.FakeCountryRepository
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import io.xbot.tdlib.api.CountryInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.test.testWithInternalState

class PhoneViewModelTest {
    private val russia = country(code = "RU", name = "Russia", callingCode = "7")
    private val uzbekistan = country(code = "UZ", name = "Uzbekistan", callingCode = "998")

    private val loginEntry = LoginEntry(LoginFlow.QrCodeFirst)
    private val repository = FakeAuthRepository()
    private val entered = EnteredPhoneNumber(
        country = uzbekistan,
        callingCode = "998",
        number = "901234567"
    )

    @Test
    fun aNumberDuringQrConfirmationGoesToTheNextAccount() = runTest {
        repository.authorizationState.value =
            AuthorizationStateWaitOtherDeviceConfirmation(link = "tg://login")
        phoneScreen(
            PhoneScreenAction.OnCallingCodeChange(entered.callingCode),
            PhoneScreenAction.OnNumberChange(entered.number),
            PhoneScreenAction.OnSubmit
        )
        assertEquals(1, repository.restarts)
        assertEquals(entered, loginEntry.pendingPhoneNumber.value)
        assertTrue(repository.phoneNumbers.isEmpty())
    }

    @Test
    fun theNextAccountSendsThePendingNumber() = runTest {
        loginEntry.pendingPhoneNumber.value = entered
        val state = phoneScreen()
        assertEquals(listOf(entered.international), repository.phoneNumbers)
        assertNull(loginEntry.pendingPhoneNumber.value)
        assertEquals(entered.number, state.number)
        assertEquals(uzbekistan, state.country)
    }

    @Test
    fun thePendingNumberIsOnScreenFromTheStart() = runTest {
        loginEntry.pendingPhoneNumber.value = entered
        val viewModel =
            PhoneViewModel(loginEntry, FakeCountryRepository(listOf(russia)), repository)
        val initial = viewModel.container.stateFlow.value
        assertEquals(uzbekistan, initial.country)
        assertEquals(entered.number, initial.number)
        assertTrue(initial.isSubmitting)
    }

    @Test
    fun aRejectedPendingNumberStaysOnScreen() = runTest {
        loginEntry.pendingPhoneNumber.value = entered
        repository.phoneNumberResult = AuthError.InvalidPhoneNumber.left()
        val state = phoneScreen()
        assertEquals(AuthError.InvalidPhoneNumber, state.error)
        assertEquals(entered.callingCode, state.callingCode)
        assertEquals(entered.number, state.number)
    }

    private suspend fun TestScope.phoneScreen(vararg actions: PhoneScreenAction): PhoneScreenState {
        lateinit var state: PhoneScreenState
        val viewModel = PhoneViewModel(
            loginEntry,
            FakeCountryRepository(listOf(russia, uzbekistan)),
            repository
        )
        viewModel.testWithInternalState(this) {
            val onCreate = runOnCreate()
            runCurrent()
            actions.forEach {
                containerHost.onAction(it)
                runCurrent()
            }
            state = containerHost.container.stateFlow.value
            onCreate.cancel()
            cancelAndIgnoreRemainingItems()
        }
        return state
    }

    private fun country(code: String, name: String, callingCode: String) = CountryInfo(
        countryCode = code,
        name = name,
        englishName = name,
        flagEmoji = "",
        isHidden = false,
        callingCodes = listOf(callingCode)
    )
}
