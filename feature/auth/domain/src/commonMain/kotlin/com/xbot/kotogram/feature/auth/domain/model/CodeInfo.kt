package com.xbot.kotogram.feature.auth.domain.model

data class CodeInfo(
    val phoneNumber: String,
    val type: CodeType,
    val nextType: CodeType?,
    val timeoutSeconds: Int
)

/** How the login code was delivered. [length] is 0 when the code length is unknown. */
sealed interface CodeType {
    val length: Int

    data class TelegramMessage(override val length: Int) : CodeType

    data class Sms(override val length: Int) : CodeType

    data class SmsWord(val firstLetter: String) : CodeType {
        override val length: Int get() = 0
    }

    data class SmsPhrase(val firstWord: String) : CodeType {
        override val length: Int get() = 0
    }

    data class Call(override val length: Int) : CodeType

    data class FlashCall(val pattern: String) : CodeType {
        override val length: Int get() = 0
    }

    data class MissedCall(val phoneNumberPrefix: String, override val length: Int) : CodeType

    data class Fragment(val url: String, override val length: Int) : CodeType

    /** Firebase-verified SMS; this client can't pass device verification, so TDLib falls back. */
    data class Firebase(override val length: Int) : CodeType
}
