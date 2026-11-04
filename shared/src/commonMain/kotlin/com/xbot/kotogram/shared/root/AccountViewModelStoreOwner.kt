package com.xbot.kotogram.shared.root

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.core.tdlib.account.AccountLifetime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

/**
 * The parent of every screen's ViewModelStore while an account is shown: screens of one account
 * never reuse another account's ViewModels, and they are cleared when the account ends.
 * It lives as long as the account, not the composition, so it survives configuration changes.
 */
@Scope(AccountId::class)
@Scoped
internal class AccountViewModelStoreOwner(lifetime: AccountLifetime) : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()

    init {
        lifetime.addCloseable(
            AutoCloseable {
                // ViewModel.clear() is @MainThread, and an account ends on whichever thread ended it.
                CoroutineScope(Dispatchers.Main.immediate).launch { viewModelStore.clear() }
            }
        )
    }
}
