package com.xbot.kotogram.core.domain.model

import kotlin.jvm.JvmInline

/**
 * An account on this device: one TDLib database. Also the type of its Koin scope: everything bound to
 * the account is `@Scope(AccountId::class)`, and the scope's source is this id.
 */
@JvmInline
value class AccountId(val value: Int)
