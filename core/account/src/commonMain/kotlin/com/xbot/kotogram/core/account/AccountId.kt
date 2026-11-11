package com.xbot.kotogram.core.account

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class AccountId(
    val value: Int,
)
