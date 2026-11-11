package com.xbot.kotogram.feature.language.data.extensions

import com.xbot.kotogram.feature.language.domain.model.LanguageError
import io.xbot.tdlib.api.Error

internal fun Error.toLanguageError(): LanguageError = LanguageError.Unknown(message)
