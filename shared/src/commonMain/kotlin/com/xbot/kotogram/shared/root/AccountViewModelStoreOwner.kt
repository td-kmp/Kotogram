package com.xbot.kotogram.shared.root

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.xbot.kotogram.core.domain.model.AccountId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped
import org.koin.core.scope.Scope as KoinScope
import org.koin.core.scope.ScopeCallback

/**
 * The parent of every screen's ViewModelStore while an account is shown: screens of one account
 * never reuse another account's ViewModels, and they are cleared when the account's scope closes.
 * It lives in the account scope, not in composition, so it survives configuration changes.
 */
@Scope(AccountId::class)
@Scoped
internal class AccountViewModelStoreOwner(koinScope: KoinScope) : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()

    init {
        koinScope.registerCallback(
            object : ScopeCallback {
                // ViewModel.clear() is @MainThread, and a scope closes on whichever thread closed it.
                override fun onScopeClose(scope: KoinScope) {
                    CoroutineScope(Dispatchers.Main.immediate).launch { viewModelStore.clear() }
                }
            }
        )
    }
}
