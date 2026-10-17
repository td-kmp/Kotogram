package com.xbot.kotogram.core.domain.model

/** What TDLib reports about an [AccountId]. */
data class Account(
    val id: AccountId,
    /** `null` until TDLib reports the first state. */
    val authorizationState: AuthorizationState?,
    /** TDLib's `my_id` option: set while the account is authorized. */
    val userId: Long?
)
