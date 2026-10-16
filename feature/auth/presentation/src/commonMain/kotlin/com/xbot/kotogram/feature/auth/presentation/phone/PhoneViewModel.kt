package com.xbot.kotogram.feature.auth.presentation.phone

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.domain.model.AuthException
import com.xbot.kotogram.feature.auth.domain.model.Country
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.auth.domain.repository.CountryRepository
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

class PhoneViewModel(
    private val countryRepository: CountryRepository,
    private val authRepository: AuthRepository
) : ViewModel(),
    OrbitContainerHost<PhoneState, PhoneState, PhoneSideEffect> {

    override val container = orbitContainer<PhoneState, PhoneSideEffect>(PhoneState()) {
        val countries = runCatching { countryRepository.countries() }.getOrDefault(emptyList())
        val detected = countryRepository.detectedCountryCode()
        val country = countries.firstOrNull { it.code == detected }
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

    fun onCallingCodeChange(value: String) = intent {
        val code = value.filter(Char::isDigit).take(MAX_CALLING_CODE_LENGTH)
        reduce { state.copy(callingCode = code, error = null) }
        val country = runCatching { countryRepository.phoneNumberInfo(code).country }.getOrNull()
            ?: state.countries.firstOrNull { code in it.callingCodes }
        reduce { state.copy(country = country) }
    }

    fun onNumberChange(value: String) = intent {
        reduce {
            state.copy(number = value.filter(Char::isDigit).take(MAX_NUMBER_LENGTH), error = null)
        }
    }

    fun onSyncContactsChange(enabled: Boolean) = intent {
        reduce { state.copy(syncContacts = enabled) }
    }

    fun onCountryPickerVisibilityChange(visible: Boolean) = intent {
        reduce { state.copy(isCountryPickerVisible = visible, countryQuery = "") }
    }

    fun onCountryQueryChange(query: String) = intent {
        reduce { state.copy(countryQuery = query) }
    }

    fun onCountrySelect(country: Country) = intent {
        reduce {
            state.copy(
                country = country,
                callingCode = country.callingCodes.firstOrNull().orEmpty(),
                isCountryPickerVisible = false,
                countryQuery = ""
            )
        }
    }

    fun onSubmit() = intent {
        if (!state.canSubmit) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        try {
            authRepository.setPhoneNumber("+${state.callingCode}${state.number}")
            postSideEffect(PhoneSideEffect.Submitted)
        } catch (e: AuthException) {
            reduce { state.copy(error = e.error) }
        } finally {
            reduce { state.copy(isSubmitting = false) }
        }
    }

    private companion object {
        const val MAX_CALLING_CODE_LENGTH = 4
        const val MAX_NUMBER_LENGTH = 15
    }
}
