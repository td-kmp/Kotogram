# Kotogram — accounts, TDLib clients and the login flow (design v2)

Status: **implemented** on `feature/KTG-2-td-client-api` (with the review changes below). v1 (`TdSession`, `TdAccounts`, an `AccountStatus` enum, an
`accounts.json` manifest) is replaced. Nothing below is implemented yet; §8 lists what changes in the code.

Principles, from the review of v1:

1. **TDLib is the source of truth.** No own status enums, flags or manifests where a TDLib state or a
   directory on disk already says it.
2. **A repository is an interface plus one implementation that depends on `TdClient`.** Nothing else in
   between.
3. **Repository methods return `Either<Error, T>`** (Arrow), preparing for Kotlin rich errors.
4. **Domain models mirror TDLib 1:1**: same types, same fields. They differ only where Kotlin has a better
   type (`Duration`, `Instant`, nullability, enums for closed sets).
5. **Every login step is an entry of the app's single Navigation 3 graph.** One shared ViewModel routes
   between them from TDLib's state.
6. No `Td` prefix in Kotogram. The test DC is a build flag (done: `TelegramApiConfig.USE_TEST_DC`).

Legend: **[V]** verified in this project. **[U]** to verify while implementing.

---

> **Review changes, as implemented:**
> - `AccountClient` became **`ClientHolder`**: an interface plus an internal `DefaultClientHolder`. It does
>   **not** implement `TdClient`, because `TdClient`'s contract (updates end at `Closed`) must stay true.
>   Repositories depend on `ClientHolder`.
> - The active account id lives in **DataStore** (`accounts.preferences_pb`), not in a plain file.
> - `AccountId` (a value class) is the Koin scope type and source **[V]**.

## 1. The one piece of own machinery: `AccountClient` (implemented as `ClientHolder`)

TDLib can't reuse a closed instance, but an account outlives its instances: the QR → phone switch logs out,
and so does a real log out. Every real client (Telegram X, Unigram) therefore keeps one object per account
that swaps instances. Kotogram does the same, **as a `TdClient`**, so repositories still depend on
`TdClient` only:

```kotlin
// core:tdlib — internal; provided in the account scope and bound as TdClient
@Scope(AccountId::class) @Scoped(binds = [TdClient::class])
internal class AccountClient(@Provided id: AccountId, environment: TdlibEnvironment, scope: Scope) : TdClient
```

| `TdClient` member | Behavior |
|---|---|
| `authorizationState` | continuous across instances |
| `updates` | continuous; a restart shows up as `Closed`, then the new instance's states, so caches reset there (Telegram X) |
| `send` | to the live instance; while one is being replaced, waits for the next |
| `close` | closes for good (removes the account); otherwise it is never called |

One rule, with no flags: **after `Closed`, start a new instance on the same directory, unless the client
was closed**, as Telegram X does. Every instance is started eagerly: `SetTdlibParameters` is the first
request, with no wait for `WaitTdlibParameters` (Telegram X, Unigram). The scope's close callback closes
the client: on account removal and on `stopKoin()` at exit.

**Why not one Koin scope per instance:** the account's ViewModels and the QR → phone continuation (§4)
would die on every restart. With `AccountClient` the account scope stays put, and the restart is
invisible to everything except the state flow.

## 2. Accounts

**On disk:** one TDLib directory per account, `<tdlib>/account-<n>` (database) and
`<files>/account-<n>`. The directory list **is** the account list. The only own data is **the active
account id**, one small file `<tdlib>/active`, because TDLib has no app-wide storage.

```kotlin
// core:domain
@JvmInline value class AccountId(val value: Int)   // Koin scope type of everything account-bound

data class Account(
    val id: AccountId,
    val authorizationState: AuthorizationState?,    // null until TDLib reports one
    val userId: Long?,                              // TDLib option my_id; null while unauthorized
)

interface AccountRepository {
    val accounts: StateFlow<List<Account>>          // directory order
    val active: StateFlow<Account?>                 // null only while loading

    suspend fun addAccount(): Either<AccountError, AccountId>   // new directory, made active
    suspend fun switchTo(id: AccountId): Either<AccountError, Unit>
}
```

`DefaultAccountRepository` (`core:tdlib`, `@Singleton`) opens one Koin scope per directory
(`createScope(id, source = accountId)`) and applies **two rules**, both read from TDLib states (Unigram's
and Telegram X's behavior):

1. **The active account goes `LoggingOut` after `Ready`** (a real log out, local or remote): switch to
   another `Ready` account, if there is one. The UI moves away first.
2. **A non-active account that waits for a phone number or a QR confirmation is removed**: its scope
   closes and its directories are deleted. This one rule covers:
   - an account after log out (TDLib wiped it, and it restarted at `WaitPhoneNumber`);
   - a cancelled "add account": switching back makes it non-active;
   - leftovers found at start.

`addAccount` creates `account-<max+1>` and makes it active, so the root shows its login. On success it
stays active. To cancel, switch back, and rule 2 removes it. **No `discard` / `logOut` / status APIs:**
log out is `AuthRepository.logOut()`, and everything else follows from TDLib states.

On first launch there are no directories, so `account-1` is created. When the only account logs out, it
restarts at `WaitPhoneNumber`, stays active (rule 2 doesn't apply to the active account), and the login
screen shows.

## 3. Domain models: 1:1 with TDLib (`core:domain`)

`AuthorizationState` moves to `core:domain`, because both the root and the auth feature read it, and
becomes a mirror of TDLib:

```kotlin
sealed interface AuthorizationState {
    data object WaitTdlibParameters : AuthorizationState
    data object WaitPhoneNumber : AuthorizationState
    data class WaitPremiumPurchase(val storeProductId: String, val premiumDayCount: Int,
        val supportEmailAddress: String, val supportEmailSubject: String) : AuthorizationState
    data class WaitEmailAddress(val allowAppleId: Boolean, val allowGoogleId: Boolean) : AuthorizationState
    data class WaitEmailCode(val allowAppleId: Boolean, val allowGoogleId: Boolean,
        val codeInfo: EmailAddressAuthenticationCodeInfo, val emailAddressResetState: EmailAddressResetState?) : AuthorizationState
    data class WaitCode(val codeInfo: AuthenticationCodeInfo) : AuthorizationState
    data class WaitOtherDeviceConfirmation(val link: String) : AuthorizationState
    data class WaitRegistration(val termsOfService: TermsOfService) : AuthorizationState
    data class WaitPassword(val passwordHint: String, val hasRecoveryEmailAddress: Boolean,
        val hasPassportData: Boolean, val recoveryEmailAddressPattern: String) : AuthorizationState
    data object Ready : AuthorizationState
    data object LoggingOut : AuthorizationState
    data object Closing : AuthorizationState
    data object Closed : AuthorizationState
}

data class AuthenticationCodeInfo(val phoneNumber: String, val type: AuthenticationCodeType,
    val nextType: AuthenticationCodeType?, val timeout: Duration)     // int32 seconds → Duration
sealed interface EmailAddressResetState {
    data class Available(val waitPeriod: Duration) : EmailAddressResetState
    data class Pending(val resetIn: Duration) : EmailAddressResetState
}
```

The same goes for `AuthenticationCodeType` (11 TDLib cases), `TermsOfService` (`FormattedText`),
`CountryInfo` and `PhoneNumberInfo`, which replace today's `Country`.

**Removed:**
- `AuthState`, with its merged `Initializing` and `Unsupported`: the UI handles `WaitPremiumPurchase`
  itself;
- `acceptsLoginRequest`;
- `CodeInfo` and `Country`.

Mappers stay 1:1: `fun TdApi.X.toDomain()`.

## 4. Repositories: `Either`, `TdClient` only

```kotlin
// feature:auth:domain
interface AuthRepository {
    val authorizationState: Flow<AuthorizationState>
    suspend fun setAuthenticationPhoneNumber(phoneNumber: String): Either<AuthError, Unit>
    suspend fun requestQrCodeAuthentication(): Either<AuthError, Unit>
    suspend fun checkAuthenticationCode(code: String): Either<AuthError, Unit>
    suspend fun resendAuthenticationCode(): Either<AuthError, Unit>
    // … one method per TDLib auth function, named like it
    suspend fun logOut(): Either<AuthError, Unit>
}

// feature:auth:data — @Scope(AccountId::class) @Scoped
internal class DefaultAuthRepository(
    private val client: TdClient,
    private val accounts: AccountRepository,
) : AuthRepository
```

- Method names follow TDLib functions (`setAuthenticationPhoneNumber`, …).
- `either { }` + `catch` turns `TdException` into `AuthError`. The current error mapping stays.
- **QR → phone, Unigram-style and TDLib-driven** (TDLib refuses a phone number during QR confirmation
  **[V]**):

  ```kotlin
  override suspend fun setAuthenticationPhoneNumber(phoneNumber: String) = either {
      if (client.authorizationState.value is AuthorizationStateWaitOtherDeviceConfirmation) {
          call { client.send(LogOut) }                  // AccountClient restarts the instance
          client.authorizationState.first { it is AuthorizationStateWaitPhoneNumber }
      }
      call { client.send(SetAuthenticationPhoneNumber(phoneNumber = phoneNumber)) }
  }
  ```

  The QR code stays valid until the number is submitted, and the caller gets the real result.
- `requestQrCodeAuthentication` passes
  `otherUserIds = accounts.accounts.value.mapNotNull { it.userId }` minus its own.

## 5. The login flow in the app's navigation graph

| Route | Module | Shown for |
|---|---|---|
| `AuthRoute` | `feature:auth:api` | entry of the flow: QR on desktop, phone number elsewhere (`LoginCapabilities`) |
| `PhoneNumberRoute` | auth presentation | desktop, after "log in by phone" |
| `CodeRoute`, `PasswordRoute`, `EmailAddressRoute`, `EmailCodeRoute`, `RegistrationRoute`, `PremiumPurchaseRoute` | auth presentation | the matching `AuthorizationState` |

- **One shared `AuthViewModel`.** Every step entry declares `metadata = parent(AuthRoute)` and gets the
  shared ViewModel from `LocalSharedViewModelStoreOwner`. The ViewModel store decorator is copied from
  Google's (AOSP) nav3 code, as AnilibriaRefresh did. Step-local state (inputs, countries) stays in
  small per-step ViewModels.
- **Routing.** `AuthViewModel` maps each **new** TDLib state to a step and posts
  `AuthScreenSideEffect.ShowStep(route)`. The visible entry runs
  `navigator.popUpTo(AuthRoute); navigator.navigate(route)`. Routing on transitions (not on the current
  state) keeps back working: back from `CodeRoute` shows the number again, TDLib accepts a new number
  from `WaitCode` **[V: td_api.tl]**, and the next code moves forward again.
- **Root.** `RootViewModel` derives the root key from `accounts.active`:

  | Active account's state | Root key |
  |---|---|
  | `null` (before TDLib reports one) | `SplashRoute` |
  | `Ready` | `ChatListRoute` |
  | `Wait*` | `AuthRoute` |
  | `LoggingOut`, `Closing`, `Closed`, `WaitTdlibParameters` | keep the current root; if the previous state was `Ready`, `SplashRoute` |

  So the restart inside the login flow doesn't flicker. That decision uses only TDLib's sequence of
  states.
- `core:navigation:api` gets `popUpTo(key: NavKey)`. `core:navigation:impl` gets the shared
  ViewModel-store decorator.

## 6. DI

```
root        DefaultAccountRepository (AccountRepository)  TdlibEnvironment  PlatformLoginCapabilities  RootViewModel
 └─ AccountId scope (one per account directory, source = AccountId)
       AccountClient (TdClient)  Default*Repository  *UseCase  AuthViewModel + step ViewModels  AccountViewModelStoreOwner
```

- Scopes are opened and closed only by `DefaultAccountRepository`. Composition only provides one
  (`UnboundKoinScope`, with a `SaveableStateHolder` keyed by account so each account keeps its back
  stack).
- **[U]** `AccountId` (a value class) as the Koin scope type and source; a data class worked **[V]**. If
  the value class doesn't, the fallback is `data class AccountKey(val id: AccountId)`.

## 7. What is deliberately left out

- **Pausing idle accounts** (Telegram X): `AccountClient` can close its instance on idle and reopen it
  on `send`. It comes with the account switcher.
- **Update handlers for caches** (`getAll<UpdateHandler>()` fed in order without a dispatcher hop): they
  come with the first cache (chats).
- **The existing-number check on "add account"**: it needs `getMe` phone numbers, which come with the
  account switcher.

## 8. Changes to the current code

| Today | After |
|---|---|
| `TdSession`, `TdAccounts`, `SessionRepository`/`ActiveSession`/`SessionStatus`, `Account`(dirs) | `AccountClient` (internal), `DefaultAccountRepository`, `AccountRepository`/`Account`/`AccountId` |
| `AuthState`, `CodeInfo`, `Country`, `PhoneNumberInfo` (feature) | `AuthorizationState`, `AuthenticationCodeInfo`, … 1:1 (`core:domain`), `CountryInfo`/`PhoneNumberInfo` 1:1 (feature) |
| repositories throw `AuthException` | `Either<AuthError, T>` (`arrow-core` 2.2.3, as AnilibriaRefresh) |
| auth flow: its own nested `NavDisplay` + `AuthStep` | step entries in the app graph + shared `AuthViewModel` |
| `AccountScope` / `@Scope(AccountScope::class)` | `@Scope(AccountId::class)` |
| `TdAccountsTest` | `AccountClientTest`: eager start, restart after `LogOut` from QR, `send` during a restart, close. `DefaultAccountRepositoryTest`: rules 1 and 2, add + switch back |
