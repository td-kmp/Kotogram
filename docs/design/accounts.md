> **Superseded by [multiaccount.md](multiaccount.md) in KTG-7.** Kept for the reasoning: the classes and modules here no longer exist.

# Kotogram — accounts and scopes: review and design v3

Status: **implemented** on `feature/KTG-1-login` (2026-11-04), see §8. Replaces [sessions.md](sessions.md) §1, §2 and the root logic in §5 where they differ. Fits
[messaging-core.md](messaging-core.md) §3: it is the same `ClientHolder` loop, and it was implemented with it in M0.

Read from source on 2026-11-04: Unigram `Services/LifetimeService.cs`, `Services/Session.cs` (6c0d4f3); Pingogram `core/data/.../di/Accounts.kt`,
`docs/adr/0031-multiple-accounts.md`, `app/.../account/AccountRestart.kt` (99455eb); Telegram X via [research/telegram-x-sessions.md](../research/telegram-x-sessions.md) plus `TdlibManager.java`,
`TdlibAccount.java` (5a27890); Koin `Scope.kt`; `lifecycle-viewmodel` 2.11.0. **[O]** marks my own design.

---

## 1. Verdict on the code before this change

**The foundation is right.** One Koin scope per account is the same shape as Unigram's `SessionImpl` (a DI container per session) and Pingogram's `AccountComponent` (a Dagger
component per slot), and it is Koin's documented session scope. A holder that replaces a closed instance is what Telegram X and Unigram do. The directory as the account list and the ViewModel store in the
scope are good: Pingogram solves that last problem by restarting the activity in a new task (`AccountRestart.kt`), and Unigram by rebuilding navigation. Keep all of this.

**The account lifecycle around it is what's wrong.** Everything hangs on `keepTidy`: one collector watching a conflated list and inferring events from states.

| # | Defect | Failure | Evidence |
|---|---|---|---|
| 1 | The last account stays in the same scope after a log out | The next login, possibly another person, shares every `@Scoped` object with the previous user. Nothing visible today, since no scoped object holds user data yet. Every future one must remember to reset | `sessions.md` §2; `DefaultClientHolder.kt` restarts on every `Closed`. Unigram builds a new session instead |
| 2 | Hand-over is inferred from a conflated flow | `combine → mapLatest { GetOption } → stateIn`, then a collector that can sit in `remove()` for up to 3 s. `LoggingOut` can be skipped, or `Ready` never seen in `previous`. Then the user lands on a login screen although another account is signed in | `DefaultAccountRepository.kt` `accountOf`, `keepTidy` |
| 3 | Data can be deleted under a live database | `withTimeoutOrNull(3s)` result is ignored in `close()`, and `remove()` deletes the directories regardless | `DefaultClientHolder.kt:89-95`, `remove()` |
| 4 | Teardown runs off the main thread, and in the wrong order | `ViewModel.clear()` is `@MainThread` (`ViewModel.kt:168`), but the scope callback calls it from `Dispatchers.Default`. Koin runs callbacks in registration order (`Scope.kt:558`), so TDLib closes first and ViewModels outlive their client | `AccountViewModelStoreOwner.kt`, `remove()` |
| 5 | A closed client crashes a screen | `send` throws `IllegalStateException` from `check(...)`. `request { }` catches only `TdException`, so an in-flight call in a ViewModel crashes it. TDLib's own contract here is error 500 | `DefaultClientHolder.kt:75`, `AuthErrorMapper.kt:15` |
| 6 | The scope can close under composition | Cancelling "add account": `switchTo(A)` makes rule 2 close B's scope at once, while composition may still show B. A recomposition then asks a cleared store for a ViewModel, whose factory resolves from a closed scope: `ClosedScopeException` (`Scope.kt:374`). Narrow window, real crash | `keepTidy`, `RootNavigation.kt` |
| 7 | Ids are reused, and saved state is never removed | `maxOf + 1` reuses a removed highest id, and `SaveableStateProvider(accountId.value)` is never cleared. A new login can inherit the removed account's back stack, including a chat route of another user | `addAccount`, `RootNavigation.kt` |
| 8 | Zombie accounts | Rule 2 covers only `WaitPhoneNumber` and QR. An account abandoned at `WaitCode` or `WaitPassword` stays listed, with TDLib running, forever | `keepTidy` |
| 9 | An instance is started on a wiped database just to learn that it should be deleted | After logging out of a non-active account: new instance → `WaitPhoneNumber` → rule 2 → delete | `keepTidy` |
| 10 | Hand-over target is arbitrary; no same-user check; startup is unordered | `firstOrNull` instead of the previous account. Logging in the same user twice makes two sessions. All accounts start at once | `handOver`, `load` |

`RootViewModel.previous` is a smaller symptom of 2: it reconstructs history that a pure function of the current route doesn't need (§4.4).

## 2. What the others do

| | Telegram X | Unigram | Pingogram | Kotogram today |
|---|---|---|---|---|
| Account object | `Tdlib`, lives for the app | `SessionImpl`, DI container | `AccountComponent`, Dagger | Koin scope |
| List | config file with flags | folders + `UserId` in settings | `accounts` file | folders |
| **After a real log out** | Erase data, slot stays, state reset by hand | **Session destroyed, folder deleted; previous, any, or a new session takes over** (`Destroy`) | Slot removed; the last account stays on login with reset caches | Instance restarts; rule 2 deletes, or the last stays |
| Restart gate | `isLoggingOut` flag | `_loggingOut` vs `_continueOnLogOut` | `restartsAfterClose = { kept(slot) }` | none |
| Hand-over | Switch first, then `LogOut` | Block window on `LoggingOut`, switch on `Closed` | `remove`: switch, wait `Closed`, cancel, delete | Inferred from a list |
| Cancel "add" | Slot reused later | `Destroy` when non-active at phone or QR | `cancelAdding` returns to the recorded previous | Rule 2 |
| Same user twice | Number pre-check, switches | not found | Refuses at `Ready`, logs out | none |
| Previous account | Usage order | `PreviousSession` persisted | Only while adding | none |
| Who runs | Paused by usage | Authorized sessions only | All | All |
| Events observed | In order, on the TDLib thread | In order, per session | `collectLatest` per account, then reacts | Conflated list |

Unigram is the closest match and the simplest: **one session is one login's life.** Quill, an active Rust client, states the same rule for background accounts: "One that needs a sign-in, or whose session ended, is closed… signing in runs on screen"
(`src/ui/background_accounts/mod.rs`, header only, not read further).

---

## 3. The model

- **An account is one login's life**: one `AccountId`, one directory, one Koin scope. Everything user-specific lives in the scope, so nothing has to reset by hand.
- **Ids never repeat.** The next id is `max(directories, last_account_id) + 1`.
- **An instance is replaceable only inside a login.** Leaving QR login is a `LogOut` from a login step: same account, new instance.
- **A real log out ends the account.** No new instance. Scope closes, directories go. If none is left, a new account appears, showing the login.
- **`Closed` without `LoggingOut` restarts the instance.** Data is deleted only on proof that TDLib already destroyed it, or when we asked for it. An unexplained close must never cost a user their database.
- **Only the active account may be unauthorized.** A non-active account at any login step is destroyed. There is no way to park a half-finished login.
- **All authorized accounts run,** as in Telegram's apps and Pingogram (ADR 0031), so notifications work. Pausing stays deferred.
- **UI leaves first.** A scope closes only after composition stopped showing it.

### Lifecycle, derived in order from TDLib's states

The holder sees every state in order, so it derives this where nothing can be conflated:

```
Live ──LoggingOut after Ready, WaitTdlibParameters or nothing──► LoggingOut ──Closed──► Ended
  │                                                                                      ▲
  ├──LoggingOut after a login step (leaving QR login)──► Closed ──► new instance, Live   │
  ├──Closed with no LoggingOut (unexplained)───────────► new instance, Live              │
  └──destroy() (non-active, unauthorized) ───────────────────────────────────────────────┘
```

`LoggingOut` after `WaitTdlibParameters` covers a session revoked while the app was closed (Unigram's comment at `LifetimeService.Destroy` describes this path).

`AccountLifecycle { Live, LoggingOut, Ended }` is an own enum, against the "no own enums" rule. It stays because TDLib's state reaches everyone else through a conflated `StateFlow`, and these two facts must not be missed. It is sticky and only moves forward within a login's life.

---

## 4. Entities

### 4.1 `ClientHolder`

```kotlin
interface ClientHolder {
    val authorizationState: StateFlow<AuthorizationState?>
    val lifecycle: StateFlow<AccountLifecycle>
    /** After the end: throws TdException(500), as TDLib answers a closed instance. */
    suspend fun <R : TdObject> send(function: TdFunction<R>): R
    /** Ends an unauthorized account: TDLib's `destroy`. */
    suspend fun destroy()
    /** Stops for good and waits for TDLib. Returns false if TDLib didn't confirm in time. */
    suspend fun shutdown(): Boolean
}
```

```kotlin
// Inside messaging-core §3's loop. Reset per instance.
if (update is UpdateAuthorizationState) when (val state = update.authorizationState) {
    AuthorizationStateLoggingOut -> if (previous !is login step) lifecycle.value = LoggingOut
    else -> Unit
}
// After the stream ended at Closed:
if (lifecycle.value == LoggingOut || destroying) { lifecycle.value = Ended; return }
// otherwise the loop starts the next instance
```

Precedent for the gate: Pingogram's `restartsAfterClose`, Unigram's `_loggingOut`. Deriving it from the previous state instead of a call-site flag is **[O]**: `AuthRepository` keeps sending a plain `LogOut`, and its QR continuation stays as it is.

The scope callback still calls a blocking close, only for `stopKoin()` at exit. Normal teardown has already called `shutdown()`, so there it does nothing.

### 4.2 `DefaultAccountRepository`

```kotlin
interface AccountRepository {
    val accounts: StateFlow<List<Account>>
    val active: StateFlow<Account?>
    suspend fun addAccount(): Either<AccountError, AccountId>
    suspend fun switchTo(id: AccountId): Either<AccountError, Unit>   // to a Ready account
    fun onAccountShown(id: AccountId?)                                // from the root composable
}
```

Own data: the active and previous account and the last id, behind `AccountStorage`, in DataStore on every platform. On disk the directories stay the truth for which accounts exist; on the web TDLib's files are the worker's, so a preferences key holds the list too.

One small watcher per open account replaces `keepTidy`:

```kotlin
private fun CoroutineScope.watch(id: AccountId, holder: ClientHolder) {
    launch {                                  // edge-free: sticky and monotonic, so conflation can't lose it
        holder.lifecycle.collect { when (it) {
            Live -> Unit
            LoggingOut -> handOverIfActive(id)          // UI moves first, when another account is signed in
            Ended -> end(id)
        } }
    }
    launch {                                  // level-triggered: conflation is harmless
        combine(holder.authorizationState, activeId) { state, active -> id != active && state.isLoginStep() }
            .first { it }
        holder.destroy()
    }
}

private suspend fun end(id: AccountId) {
    mutex.withLock {
        ids.value -= id
        if (activeId.value == id) setActive(previousOrAnyReady(id) ?: createAccount())   // Unigram's Destroy
    }
    withTimeoutOrNull(UI_TIMEOUT) { shown.first { it != id } }     // composition may be stopped: don't wait forever
    val released = holder(id).shutdown()
    koin.accountScope(id)?.close()
    if (released) deleteDirectories(id)                            // otherwise the next start cleans it up
}
```

- **Hand-over target:** `previous_account` if `Ready`, else any `Ready`. Otherwise stay on the splash until `Ended` creates a new account.
- **Startup:** open the active account first, then the others in the same pass (Unigram orders active first the same way). Leftovers open like any account, reach a login step and are destroyed by the watcher, unless active. Unigram avoids that by storing a `UserId`; here it stays out, per "TDLib is the source of truth", at the cost of one short-lived instance in a rare case.
- **Cancel "add account":** `switchTo(previous)`. The watcher destroys the new account. No extra API.
- **`Account`** is derived from `AccountStore.state` (`authorizationState`, `options["my_id"]`), replacing the `GetOption` round trip (messaging-core §4).
- **Eager parts:** `open()` resolves `ClientHolder`, whose constructor takes the store. When background parts arrive (notifications), one `@Scoped AccountRuntime` with constructor dependencies is resolved instead. Not `getAll`: it has no order (05 §4.2), and Pingogram has the same explicit step, `component.updateCaches()`.

### 4.3 Same user twice (with the account switcher, M7)

- At `Ready` of an account being added: if another account has the same `my_id`, switch to that one and `LogOut` this one (Pingogram `followSignIn`).
- Before that, in `setAuthenticationPhoneNumber`: if the number belongs to another open account, switch and skip the code (Telegram X `hasAccountWithPhoneNumber`). It saves the user a code.

### 4.4 Root

```kotlin
private fun routeOf(state: AuthorizationState?, current: NavKey, sameAccount: Boolean): NavKey = when {
    state == Ready -> ChatListRoute
    state.isLoginStep() -> AuthRoute
    sameAccount && current == AuthRoute -> AuthRoute      // restart inside a login: no flicker
    else -> SplashRoute
}
```

Pure, no `previous`, directly testable.

```kotlin
// RootNavigation, inside the per-account provider
DisposableEffect(accountId) {
    viewModel.onAction(OnAccountShown(accountId))
    onDispose { viewModel.onAction(OnAccountHidden(accountId)) }
}
```

- `DisposableEffect` is the signal that composition has really left, and `end` waits for it **[O]**. Telegram X's `REFERENCE_TYPE_UI` is the same idea with a counter. There is a timeout, because a stopped Android activity doesn't recompose until it returns. That's safe: on return, the root recomposes first and removes the old content.
- `AccountViewModelStoreOwner` clears on Main: its scope callback launches on `Dispatchers.Main.immediate`. At JVM exit, the process is going away anyway.
- `saveableStateHolder.removeState(id)` when an account leaves the list. With ids never repeating this only frees memory.
- Keep per-account back stacks: `setRoot` already keeps a restored stack when the root matches (`KotogramNavigator.kt:43-47`).

---

## 5. Flows

| Situation | What happens |
|---|---|
| First launch | No directories → account 1, active, login shows |
| Add an account | New id, active, login shows; A keeps running |
| Back from that login | `switchTo(A)`; composition leaves B; `destroy`; `Ended`; scope closes; directories go |
| Log out, others signed in | `LoggingOut` → switch at once to previous; `Closed` → `Ended` → teardown after the UI left |
| Log out, last account | `LoggingOut` → splash; `Ended` → new account, active; old scope closes; login shows on a clean scope |
| Remote log out, non-active | Same, with no UI step. Worth a notice later |
| Revoked while the app was closed | `WaitTdlibParameters → LoggingOut → Closed` → `Ended`; next account or a new one |
| QR → phone | `LoggingOut` after a login step → `Closed` → new instance, same account and scope; `AuthRepository` continues |
| Switch accounts | `setActive`; `previous_account` updated; each keeps its stack and ViewModels |
| TDLib closes unexpectedly | New instance, data kept |
| Quit on desktop | `stopKoin()` → scope callbacks → blocking close, as today |

## 6. Changes

| File | Change |
|---|---|
| `ClientHolder`, `DefaultClientHolder` | `lifecycle`, `destroy`, `shutdown`; no restart after a real log out; error 500 after the end; remove `updates`; messaging-core §3's loop |
| `DefaultAccountRepository` | Watchers instead of `keepTidy`; `end`; `previous_account`, `last_account_id`; active-first startup; no `GetOption` |
| `AccountRepository` | `onAccountShown`; docs |
| `RootViewModel`, `RootNavigation` | Pure `routeOf`; shown effect; `removeState` |
| `AccountViewModelStoreOwner` | Clear on Main |
| `sessions.md`, `architecture.md` §2.4 | Point here; rule 2 and "the last account stays" are gone |


## 7. Left out on purpose

- Pausing idle accounts (Telegram X's table). It fits: `shutdown` without ending.
- A limit on accounts. TDLib has none; Telegram's Android app allows 3, or 4 with Premium (Pingogram ADR 0031, quoting DrKLO). Product decision.
- Database encryption keys per account (Pingogram has them). Worth a look with the passcode feature.
- Unigram's `IsAuthorized` without opening TDLib: see §4.2, startup.

## 8. As implemented

Differences from the sections above, and what is proven.

- **`TdLib` is bound by a provider function, never by a wrapper class.** `TdReceiver` runs one receive loop per `TdLib` object. A wrapper (`TdLib by NativeTdLib`) created twice started a second loop that stole the first one's events, and tests hung. `CoreTdlibModule.tdLib()` hands out `NativeTdLib` itself.
- **KTG-6 moved the controls and Koin out of sight (not compiled yet).** The interface in §4.1 is `ManagedClientHolder`, internal; repositories see `ClientHolder` (`send`, `authorizationState`). `DefaultAccountRepository` goes through `AccountSessions` and `AccountPointerKeeper` instead of Koin, and a scoped class registers its end with `AccountLifetime.addCloseable` instead of a `ScopeCallback`.
- **Web storage is DataStore (KTG-6, not run yet).** `WebLocalStorage`, because `createWithPath` means `sessionStorage` on the web. Pointers are common code; only the account list differs by platform. Old `localStorage` keys are not migrated.
- **`saveableStateHolder.removeState` is not done.** Ids never repeat, so this only leaves a little memory until the process ends.
- **`routeOf` has no direct test,** and the same-user checks (§4.3) wait for the switcher.

| Claim | Evidence |
|---|---|
| Nothing is lost before the first `send`; each lifecycle path in §3; hand-over to the previous account; a login abandoned at any step; ids never repeat; a scope stays open while shown; a shutdown TDLib doesn't confirm reports false | `AccountLifecycleTest`, 13 tests, scripted states through `FakeTdLib` |
| `LogOut` from QR really restarts the instance (the states TDLib sends fit §3) | `ClientHolderTest`, real TDLib and servers |
| `destroy` ends a real leftover account, and its directory goes | `DefaultAccountRepositoryTest`, real TDLib |
| The real annotated graph opens an account | `KoinGraphTest`, real TDLib, temporary directory |
| JVM, Android, iOS compile | `compileKotlin` on each. iOS and Android were not run |

**Unproven:** the state order for a real log out from `Ready` and for a session revoked while closed, because both need a logged-in account: only the fake covers them. The `DisposableEffect` signal and clearing ViewModels on Main have not been exercised in a running UI. Nothing was run on a device.
