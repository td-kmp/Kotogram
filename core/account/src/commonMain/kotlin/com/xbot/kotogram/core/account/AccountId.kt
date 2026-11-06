package com.xbot.kotogram.core.account

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class AccountId(val value: Int)
