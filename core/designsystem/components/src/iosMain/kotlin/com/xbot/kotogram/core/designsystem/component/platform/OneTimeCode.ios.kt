package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.input.PlatformImeOptions
import platform.UIKit.UITextContentTypeOneTimeCode

@OptIn(ExperimentalComposeUiApi::class)
internal actual val OneTimeCodeImeOptions: PlatformImeOptions? =
    PlatformImeOptions { textContentType(UITextContentTypeOneTimeCode) }
