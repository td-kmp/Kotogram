package com.xbot.kotogram.feature.auth.impl.screen.phone

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import arrow.core.left
import com.xbot.kotogram.feature.auth.domain.LoginEntry
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.EnteredPhoneNumber
import com.xbot.kotogram.feature.auth.domain.model.LoginFlow
import com.xbot.kotogram.feature.auth.impl.FakeAuthRepository
import com.xbot.kotogram.feature.auth.impl.FakeCountryRepository
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import io.xbot.tdlib.api.CountryInfo
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.test.testWithInternalState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PhoneViewModelTest {
    private val russia = country(code = "RU", name = "Russia", callingCode = "7")
    private val uzbekistan = country(code = "UZ", name = "Uzbekistan", callingCode = "998")

    private val loginEntry = LoginEntry(LoginFlow.QrCodeFirst)
    private val repository = FakeAuthRepository()
    private val entered = EnteredPhoneNumber(
        country = uzbekistan,
        callingCode = "998",
        number = "901234567",
    )

    @Test
    fun aNumberDuringQrConfirmationGoesToTheNextAccount() = runTest {
        repository.authorizationState.value =
            AuthorizationStateWaitOtherDeviceConfirmation(link = "tg://login")
        phoneScreen(
            { phoneNumber.setTextAndPlaceCursorAtEnd(entered.callingCode + entered.number) },
            { onAction(PhoneScreenAction.OnSubmit) },
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
            PhoneViewModel(SavedStateHandle(), loginEntry, FakeCountryRepository(listOf(russia)), repository)
        val initial = viewModel.container.stateFlow.value
        assertEquals(uzbekistan, initial.country)
        assertEquals(entered.callingCode + entered.number, viewModel.phoneNumber.text.toString())
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

    @Test
    fun tdlibSplitsTheDigitsIntoCallingCodeAndNumber() = runTest {
        val state = phoneScreen({ phoneNumber.setTextAndPlaceCursorAtEnd("998901234") })
        assertEquals("998", state.callingCode)
        assertEquals("901234", state.number)
        assertEquals(uzbekistan, state.country)
    }

    @Test
    fun aChosenCountryReplacesTheCallingCodeAndKeepsTheNumber() = runTest {
        val state = phoneScreen(
            { phoneNumber.setTextAndPlaceCursorAtEnd("998901234") },
            { onAction(PhoneScreenAction.OnCountrySelect(russia)) },
        )
        assertEquals("7901234", phoneNumber(state))
        assertEquals(russia, state.country)
    }

    private fun phoneNumber(state: PhoneScreenState) = state.callingCode + state.number

    private suspend fun TestScope.phoneScreen(vararg steps: PhoneViewModel.() -> Unit): PhoneScreenState {
        lateinit var state: PhoneScreenState
        val viewModel = PhoneViewModel(
            SavedStateHandle(),
            loginEntry,
            FakeCountryRepository(listOf(russia, uzbekistan)),
            repository,
        )
        viewModel.testWithInternalState(this) {
            val onCreate = runOnCreate()
            runCurrent()
            steps.forEach { step ->
                containerHost.step()
                // Workaround: no Compose frame here to send the edits to snapshotFlow.
                Snapshot.sendApplyNotifications()
                runCurrent()
                Snapshot.sendApplyNotifications()
                runCurrent()
            }
            state = containerHost.container.stateFlow.value
            onCreate.cancel()
            cancelAndIgnoreRemainingItems()
        }
        return state
    }

    private fun country(
        code: String,
        name: String,
        callingCode: String,
    ) = CountryInfo(
        countryCode = code,
        name = name,
        englishName = name,
        flagEmoji = "",
        isHidden = false,
        callingCodes = listOf(callingCode),
    )
}
