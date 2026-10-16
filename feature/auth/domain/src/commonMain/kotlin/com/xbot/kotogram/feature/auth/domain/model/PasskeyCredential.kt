package com.xbot.kotogram.feature.auth.domain.model

/** A WebAuthn assertion produced by the platform authenticator. */
class PasskeyCredential(
    val credentialId: String,
    val clientDataJson: String,
    val authenticatorData: ByteArray,
    val signature: ByteArray,
    val userHandle: ByteArray
)
