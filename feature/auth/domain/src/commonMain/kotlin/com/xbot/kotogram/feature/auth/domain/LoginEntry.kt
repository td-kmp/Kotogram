package com.xbot.kotogram.feature.auth.domain

import com.xbot.kotogram.feature.auth.domain.model.LoginFlow
import com.xbot.kotogram.feature.auth.domain.model.LoginMethod
import kotlinx.coroutines.flow.MutableStateFlow

// Outlives an account: leaving QR login takes a new one.
class LoginEntry(flow: LoginFlow) {
    val method = MutableStateFlow(
        if (flow == LoginFlow.QrCodeFirst) LoginMethod.QrCode else LoginMethod.PhoneNumber
    )
}
