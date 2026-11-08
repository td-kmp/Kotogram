package com.xbot.kotogram.feature.auth.impl.screen.phone

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.xbot.kotogram.feature.auth.domain.LoginEntry
import com.xbot.kotogram.feature.auth.domain.extensions.acceptsPhoneNumber
import com.xbot.kotogram.feature.auth.domain.model.EnteredPhoneNumber
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.auth.domain.repository.CountryRepository
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
import io.xbot.tdlib.api.CountryInfo
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class PhoneViewModel(
    savedStateHandle: SavedStateHandle,
    private val loginEntry: LoginEntry,
    private val countryRepository: CountryRepository,
    private val authRepository: AuthRepository
) : ViewModel(),
    OrbitContainerHost<PhoneScreenState, PhoneScreenState, PhoneScreenSideEffect> {

    private val pending = loginEntry.pendingPhoneNumber.getAndUpdate { null }

    @OptIn(SavedStateHandleSaveableApi::class)
    val phoneNumber by savedStateHandle.saveable(saver = TextFieldState.Saver) {
        TextFieldState(pending?.let { it.callingCode + it.number }.orEmpty())
    }

    override val container = orbitContainer<PhoneScreenState, PhoneScreenSideEffect>(
        PhoneScreenState(
            country = pending?.country,
            callingCode = pending?.callingCode.orEmpty(),
            number = pending?.number.orEmpty(),
            isSubmitting = pending != null
        )
    ) {
        coroutineScope {
            launch { loadCountries() }
            launch { followInput() }
            if (pending != null) {
                launch {
                    authRepository.authorizationState.first {
                        it == AuthorizationStateWaitPhoneNumber
                    }
                    submit(pending)
                }
            }
            authRepository.authorizationState.collect { authState ->
                val isAccepting = authState.acceptsPhoneNumber ||
                    authState is AuthorizationStateWaitOtherDeviceConfirmation
                reduce { state.copy(isAcceptingNumber = isAccepting) }
            }
        }
    }

    private suspend fun Syntax<PhoneScreenState, PhoneScreenSideEffect>.loadCountries() {
        val countries = countryRepository.getCountries().getOrNull().orEmpty()
            .filterNot { it.isHidden }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        reduce { state.copy(countries = countries) }
        if (phoneNumber.text.isEmpty()) {
            val detected = countryRepository.getCountryCode().getOrNull()
            countries.firstOrNull { it.countryCode == detected }?.let(::selectCountry)
        }
    }

    private suspend fun Syntax<PhoneScreenState, PhoneScreenSideEffect>.followInput() = coroutineScope {
        val digits = snapshotFlow { phoneNumber.text.toString() }
        launch { digits.drop(1).collect { reduce { state.copy(error = null) } } }
        digits.collectLatest { describe(it) }
    }

    private suspend fun Syntax<PhoneScreenState, PhoneScreenSideEffect>.describe(digits: String) {
        val info = digits.takeIf { it.isNotEmpty() }
            ?.let { countryRepository.getPhoneNumberInfo(it).getOrNull() }
        val callingCode = info?.countryCallingCode ?: digits
        val number = digits.removePrefix(callingCode)
        reduce {
            val chosen = state.country?.takeIf { number.isEmpty() && callingCode in it.callingCodes }
            state.copy(
                country = chosen ?: info?.country,
                callingCode = callingCode,
                number = number,
                numberPattern = info?.formattedPhoneNumber.orEmpty()
            )
        }
    }

    fun onAction(action: PhoneScreenAction) {
        when (action) {
            is PhoneScreenAction.OnCountryPickerVisibilityChange -> onCountryPickerVisibilityChange(action.visible)

            is PhoneScreenAction.OnCountrySelect -> onCountrySelect(action.country)

            PhoneScreenAction.OnSubmit -> onSubmit()
        }
    }

    private fun onCountryPickerVisibilityChange(visible: Boolean) = intent {
        reduce { state.copy(isCountryPickerVisible = visible) }
    }

    private fun onCountrySelect(country: CountryInfo) = intent {
        reduce { state.copy(country = country, isCountryPickerVisible = false) }
        selectCountry(country, state.number)
    }

    private fun selectCountry(country: CountryInfo, number: String = "") {
        phoneNumber.setTextAndPlaceCursorAtEnd(country.callingCodes.firstOrNull().orEmpty() + number)
    }

    private fun onSubmit() = intent {
        if (!state.canSubmit) return@intent
        val phoneNumber = EnteredPhoneNumber(state.country, state.callingCode, state.number)
        val authorizationState = authRepository.authorizationState.first()
        if (authorizationState is AuthorizationStateWaitOtherDeviceConfirmation) {
            reduce { state.copy(isSubmitting = true, error = null) }
            loginEntry.pendingPhoneNumber.value = phoneNumber
            authRepository.restartSignIn()
        } else {
            submit(phoneNumber)
        }
    }

    private suspend fun Syntax<PhoneScreenState, PhoneScreenSideEffect>.submit(
        phoneNumber: EnteredPhoneNumber
    ) {
        reduce { state.copy(isSubmitting = true, error = null) }
        authRepository.setAuthenticationPhoneNumber(phoneNumber.international).fold(
            ifLeft = { error -> reduce { state.copy(isSubmitting = false, error = error) } },
            ifRight = {
                reduce { state.copy(isSubmitting = false) }
                postSideEffect(PhoneScreenSideEffect.Submitted)
            }
        )
    }
}
