package com.xbot.kotogram.feature.auth.domain

import com.xbot.kotogram.feature.auth.domain.model.EnteredPhoneNumber
import com.xbot.kotogram.feature.auth.domain.model.LoginFlow
import com.xbot.kotogram.feature.auth.domain.model.LoginMethod
import kotlinx.coroutines.flow.MutableStateFlow

// Outlives an account: a number entered during QR confirmation goes to the account that replaces it.
class LoginEntry(flow: LoginFlow) {
    val method = MutableStateFlow(
        if (flow == LoginFlow.QrCodeFirst) LoginMethod.QrCode else LoginMethod.PhoneNumber
    )

    val pendingPhoneNumber = MutableStateFlow<EnteredPhoneNumber?>(null)
}
