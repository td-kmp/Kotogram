# Kotogram — multi-account on tdlib-kmp + Koin

Status: **implemented in KTG-7.** See [handoff.md](../handoff.md) for what has been built and run. It replaces [accounts.md](accounts.md) and the `core:tdlib` engine of [messaging-core.md](messaging-core.md), which stay for the reasoning.

Source: a sketch the developer brought (`multiaccount-koin`, written against tdlib-kmp 1.8.69-1.0.0-alpha02 and Koin 4.x, never compiled). Its API was checked against the published `TdClient`/`TdLib` (tag `v1.8.69-1.0.0-alpha02`) and the Koin 4.2.2 sources. This is the sketch's design plus the corrections listed below, which are our own.

## How it works

- **One `TdClient` per account on one shared `TdLib`.** `TdReceiver` routes updates by `@client_id`, so the library needs no changes. All accounts run at once: notifications and unread counts of every account keep working, and the UI shows one.
- **A Koin scope per account**, id `account-N`, qualifier `AccountScope`. The manager declares the account's `TdClient` and `AccountContext` in it. Scoped definitions see the root; the root doesn't see them, so a repository of the wrong account can't be taken by accident.
- **Followers before the first request.** `TdClient.updates` has no replay. The manager calls `getAll<AccountUpdates>()` and `start(...)` on each before it sends anything; they collect with `CoroutineStart.UNDISPATCHED`, so they are subscribed on return. Everything else in the scope stays lazy.
- **A sign-in fills in the registry.** `AuthorizationStateReady` → `GetMe`, and the user id is saved. A user already on the device is logged out of the new account, and the existing one becomes active.
- **UI:** `RootNavigation` in `shared` keys everything by `session.id` and provides the account's scope with `UnboundKoinScope`, so screens use plain `koinViewModel()` and know nothing about accounts. ViewModels live in the stores of navigation entries. Their provider is keyed by the position in the composition, so accounts stay apart; it clears them when the account leaves the composition, and not on rotation.

```
core:account
  AccountManager     start, add, cancelAdding, restartSignIn, switchTo, logOut, closeAll
  AccountSession     what the root sees: id, Koin scope, authorization state; AccountScope, AccountUpdates, AccountContext
  AccountRegistry    who is on the device, saved through AccountRegistryStore
  AccountStorage, DatabaseKeys, TdlibConfig
  di/                accountsModule, platform* expect/actual
shared
  RootNavigation     key(session.id), UnboundKoinScope, one AccountNavigation per shown account
```

A feature adds to one account's graph, `data` and `impl` each with their own module:

```kotlin
val chatsDataModule = module {
    scope(AccountScope) {
        scopedOf(::ChatStore) { bind<AccountUpdates>() }
        scopedOf(::DefaultChatRepository) { bind<ChatRepository>() }
    }
}

val chatsImplModule = module {
    scope(AccountScope) {
        viewModelOf(::ChatListViewModel)
        viewModel { parameters -> ChatViewModel(parameters.get(), get()) }
    }
}
```

## Differences from the sketch

| The sketch | Here | Why |
|---|---|---|
| Reuses the lowest free slot | Ids never repeat (`AccountRegistry.lastId`) | Logging out of the only account reused its id, so `key(session.id)` kept the old account's composition. On the web the old files also can't be deleted |
| `active`, `adding` and `accounts` through `stateIn` | One `shown`, published synchronously under the mutex | The screens never see a half-updated pair, and no account's UI flashes between two states |
| Closes the Koin scope at removal | Closes it a second later (`retire`), and the coroutines at once | Under `UnboundKoinScope`, a `koinViewModel()` call site that recomposes on a closed scope throws, even for an existing ViewModel: `currentKoinScope()` has nothing to fall back to. The screens leave within a frame of `shown`. Our own design, on a timer |
| `AccountSession` and `accountViewModel` in the UI, a `ViewModelStore` per session | The session is seen only by `shared`, without its client. Plain `koinViewModel()`, no store of our own | A screen could reach `LocalAccount.current.client` without an import, past detekt. Stores of navigation entries already keep accounts apart |
| `AccountsRoot` in the account module | In `shared` | `core:account` has no UI dependencies |
| Any `Closed` not caused by the app deletes the account | Only after `LoggingOut` | A failed web worker closes every client, which would erase all accounts. `follow` reads `updates`, because the `authorizationState` StateFlow can conflate `LoggingOut` away |
| `Close` for a cancelled sign-in | `LogOut` | Before authorization it only erases local data, and the web can't delete the worker's files itself |
| No QR login | `restartSignIn` | See below |
| `if (Ready) main else signIn` | Splash until TDLib reports, `routeOf` in `shared` | A signed-in account would flash the login on each cold start. One `AccountNavigation` replaces the root with `setRoot`, so popped entries clear their ViewModels |
| `runCatching` around requests | `sendCatching` | `runCatching` swallows `CancellationException` |
| An unreadable key throws | `getOrCreate` returns null; a signed-in account is removed, a new one waits | Throwing crashes the app, and a broken Keystore would otherwise open new accounts in a loop |
| JSON file per platform | DataStore everywhere | Already in the project, with a known web path (`WebLocalStorage`) |
| One set of directories and keys | `accounts` and `accounts-test`, keys named by them too | Test DC ids repeat production ones, and deleting one must not take the other's key |

### Leaving QR login

TDLib leaves QR login only through a log out, which ends the client (`AuthorizationStateClosed`), and a session is one client. So choosing the phone during QR confirmation restarts the sign-in in a new account:

1. `LoginEntry.method` becomes `PhoneNumber`. It's a root singleton, because it has to outlive the session.
2. `AuthViewModel` sees `WaitOtherDeviceConfirmation` with that method and calls `AccountManager.restartSignIn`. It reacts to the state and not to the click, so a QR request already on its way is covered too.
3. The new account opens as the phone screen. Going back to QR needs no restart: TDLib takes `requestQrCodeAuthentication` in `WaitPhoneNumber`.

This happens on the click rather than on submit as before, so nothing typed is lost and an invalid number shows its error in place. The cost is one instance restart, and a new QR code when coming back. `PhoneNumberRoute` is gone.

The other way, as in Unigram: TDLib gives no fresh QR code past a step (code, password, email, registration), so the entry under a step is always the phone screen, and choosing QR there restarts the sign-in too. Where QR exists at all is `LoginFlow`, chosen per platform: phones have only the phone number, desktop and web start with the QR code ([auth.md](auth.md)).

## Pitfalls

- Don't use Koin's `KoinScope { }` or `rememberKoinScope`: they close the scope with the composition, while the manager owns it. `UnboundKoinScope` only provides it.
- `koinViewModel()` only inside navigation entries: Koin's ViewModel key doesn't include the scope, so two accounts sharing one store would share the ViewModel.
- Koin's DSL isn't checked at compile time. `KoinGraphTest` resolves every definition of an account on real TDLib.
- `getAll` gives no order, so followers mustn't depend on each other's order.
- Web: `databaseDirectory` must be under `/tdlib`, the only part the worker persists. Files of an abandoned account stay there: ids never repeat, so they are only garbage.
- Android: `android:allowBackup` is off. A restored database can't be opened with a new key.

## Not ported

**Push** waits for D3 ([handoff.md](../handoff.md)). The sketch's version, with `pushReceiverId: Long?` on `AccountEntry`:

```kotlin
/** One token for the app: every account registers it and names the others, as Telegram's apps do. */
suspend fun registerDevice(token: DeviceToken) {
    val signedIn = registry.value.signedIn
    for (entry in signedIn) {
        val session = sessions.value[entry.id] ?: continue
        val others = signedIn.filter { it.id != entry.id }.mapNotNull { it.userId }
        val receiver = runCatching {
            session.client.send(RegisterDevice(deviceToken = token, otherUserIds = others))
        }.getOrNull() ?: continue
        mutex.withLock { setRegistry(registry.value.update(entry.id) { it.copy(pushReceiverId = receiver.id) }) }
    }
}

/** getPushReceiverId can be called synchronously, so any client's execute will do. Null: hand the payload to every account. */
suspend fun accountForPush(payload: String): AccountSession? {
    val any = sessions.value.values.firstOrNull() ?: return null
    val receiverId = runCatching { any.client.execute(GetPushReceiverId(payload = payload)).id }.getOrNull() ?: return null
    val entry = registry.value.accounts.firstOrNull { it.pushReceiverId == receiverId } ?: return null
    return sessions.value[entry.id]
}
```

In the service: `manager.accountForPush(payload)?.client?.send(ProcessPushNotification(payload))`, or every account's client when it returns null.

**The iOS Keychain** for `DatabaseKeys`: iOS has no database key for now, like desktop and web.
