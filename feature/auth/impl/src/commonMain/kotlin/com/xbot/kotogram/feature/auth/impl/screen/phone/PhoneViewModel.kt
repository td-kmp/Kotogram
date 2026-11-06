package com.xbot.kotogram.feature.auth.impl.screen.phone

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.domain.extensions.acceptsPhoneNumber
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.auth.domain.repository.CountryRepository
import io.xbot.tdlib.api.CountryInfo
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class PhoneViewModel(
    private val countryRepository: CountryRepository,
    private val authRepository: AuthRepository
) : ViewModel(),
    OrbitContainerHost<PhoneScreenState, PhoneScreenState, PhoneScreenSideEffect> {

    override val container =
        orbitContainer<PhoneScreenState, PhoneScreenSideEffect>(PhoneScreenState()) {
            coroutineScope {
                launch { loadCountries() }
                authRepository.authorizationState.collect { authState ->
                    reduce { state.copy(isAcceptingNumber = authState.acceptsPhoneNumber) }
                }
            }
        }

    private suspend fun Syntax<PhoneScreenState, PhoneScreenSideEffect>.loadCountries() {
        val countries = countryRepository.getCountries().getOrNull().orEmpty()
            .filterNot { it.isHidden }
            .sortedBy { it.name }
        val detected = countryRepository.getCountryCode().getOrNull()
        val country = countries.firstOrNull { it.countryCode == detected }
        reduce {
            state.copy(
                countries = countries,
                country = state.country ?: country,
                callingCode = state.callingCode.ifEmpty {
                    country?.callingCodes?.firstOrNull().orEmpty()
                }
            )
        }
    }

    fun onAction(action: PhoneScreenAction) {
        when (action) {
            is PhoneScreenAction.OnCallingCodeChange -> onCallingCodeChange(action.value)

            is PhoneScreenAction.OnNumberChange -> onNumberChange(action.value)

            is PhoneScreenAction.OnSyncContactsChange -> onSyncContactsChange(action.enabled)

            is PhoneScreenAction.OnCountryPickerVisibilityChange -> onCountryPickerVisibilityChange(
                action.visible
            )

            is PhoneScreenAction.OnCountryQueryChange -> onCountryQueryChange(action.query)

            is PhoneScreenAction.OnCountrySelect -> onCountrySelect(action.country)

            PhoneScreenAction.OnSubmit -> onSubmit()
        }
    }

    private fun onCallingCodeChange(value: String) = intent {
        val code = value.filter(Char::isDigit).take(MAX_CALLING_CODE_LENGTH)
        reduce { state.copy(callingCode = code, error = null) }
        val country = countryRepository.getPhoneNumberInfo(code).getOrNull()?.country
            ?: state.countries.firstOrNull { code in it.callingCodes }
        reduce { state.copy(country = country) }
    }

    private fun onNumberChange(value: String) = intent {
        reduce {
            state.copy(number = value.filter(Char::isDigit).take(MAX_NUMBER_LENGTH), error = null)
        }
    }

    private fun onSyncContactsChange(enabled: Boolean) = intent {
        reduce { state.copy(syncContacts = enabled) }
    }

    private fun onCountryPickerVisibilityChange(visible: Boolean) = intent {
        reduce { state.copy(isCountryPickerVisible = visible, countryQuery = "") }
    }

    private fun onCountryQueryChange(query: String) = intent {
        reduce { state.copy(countryQuery = query) }
    }

    private fun onCountrySelect(country: CountryInfo) = intent {
        reduce {
            state.copy(
                country = country,
                callingCode = country.callingCodes.firstOrNull().orEmpty(),
                isCountryPickerVisible = false,
                countryQuery = ""
            )
        }
    }

    private fun onSubmit() = intent {
        if (!state.canSubmit) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        authRepository.setAuthenticationPhoneNumber("+${state.callingCode}${state.number}").fold(
            ifLeft = { error -> reduce { state.copy(isSubmitting = false, error = error) } },
            ifRight = {
                reduce { state.copy(isSubmitting = false) }
                postSideEffect(PhoneScreenSideEffect.Submitted)
            }
        )
    }

    private companion object {
        const val MAX_CALLING_CODE_LENGTH = 4
        const val MAX_NUMBER_LENGTH = 15
    }
}
