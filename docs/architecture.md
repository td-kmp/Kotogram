# Kotogram — Architecture

Status: **accepted.** Accounts and scopes are in [design/accounts.md](design/accounts.md), the path from an update to a pixel in [design/messaging-core.md](design/messaging-core.md). It fixes the module layout, DI, navigation and naming rules that every
feature follows. Where the current code disagrees, the code is migrated (§10). The naming and
navigation rules are borrowed from the user's AnilibriaRefresh project (`AGENTS.md`,
`shared-ui/navigation`) and adapted to Kotogram's layered features.

Stack: Kotlin 2.4.20, CMP 1.12.1, Koin 4.2.2 + Koin Compiler Plugin 1.2.1 (annotations + compiler
DSL), `koin-compose-navigation3`, Navigation 3 (runtime 1.2.0, JetBrains `navigation3-ui`
1.2.0-rc01), Orbit MVI 12, TDLib via `tdlib-kmp`. Targets: Android, iOS, JVM desktop, web (`wasmJs`).

Legend: **[V]** verified against sources in this session (library sources and docs, reference
projects). **[U]** unverified, check before relying on it.

---

## 0. Decisions (TL;DR)

1. **A feature has four modules**: `api` (navigation keys and `navigateTo…` extensions, what other
   features may see), `domain`, `data` and `presentation`. A feature sees other features only through
   their `api`.
2. **One assembly point**, `:shared`: the `@KoinApplication`, the root `NavDisplay` and the iOS
   framework. The platform shells `:android-app`, `:jvm-app` and `:web-app` only start Koin and host the UI.
3. **DI is Koin with the compiler plugin.** Annotations (`@Module @Configuration @ComponentScan`,
   `@Singleton`, `@Factory`, `@KoinViewModel`, `@Scope`/`@Scoped`) declare everything other code
   depends on. The DSL is used only for leaf contributions: navigation entries and NavKey
   serializers. Compile safety validates the whole graph at `startKoin` (§2.5).
4. **Lifetimes are Koin scopes.** Each Telegram account is a Koin scope (`AccountId`), the
   documented "session scope" pattern **[V]**. A scoped class that owns a resource closes it from a
   `ScopeCallback` on its own scope. `stopKoin()` closes every scope, root included, so this also
   runs at exit **[V, Koin sources + test]**.
5. **Navigation is Navigation 3 through `koin-compose-navigation3`**: features declare
   `navigation<Route> { … }` entries in their Koin module and the app uses `koinEntryProvider()`
   **[V]**. One global `Navigator` (`LocalNavigator`) keeps a saved back stack.
6. **Naming follows AnilibriaRefresh**:
   - `XRoute` is a navigation key;
   - `XScreen` is the stateful composable, `XScreenContent` the stateless one;
   - the screen contract is `XScreenState`, `XScreenAction` and `XScreenSideEffect`, and the ViewModel
     exposes a single `onAction`;
   - navigation callbacks are named `on[Target]Click`.

---

## 1. Modules

```
:android-app                 Android shell: Application, Activity
:jvm-app                     Desktop shell: main(), Nucleus window
:web-app                     Web shell: main(), ComposeViewport, index.html
:shared                      ASSEMBLY POINT: @KoinApplication, root App composable, iOS framework
ios-app/                     Xcode project, thin Swift host (not a Gradle module)

:core:domain                 cross-feature interfaces; exposes TDLib's generated classes as the model
:core:data                   ClientHolder, AccountStore, accounts, TDLib environment, the web worker
:core:navigation:api         NavKey, Navigator, LocalNavigator, navigation helpers
:core:navigation:impl        rememberNavigator, NavigationState, deep links
:core:designsystem           KotogramTheme, tokens, components used by 2+ features

:feature:<name>:api
:feature:<name>:domain
:feature:<name>:data
:feature:<name>:presentation
```

`core:data` is laid out by entity, under `com.xbot.kotogram.core.data`:

```
account/      DefaultAccountRepository, AccountStorage, scope lookups
client/       ClientHolder, AccountLifecycle, TdLibProvider, UpdateLane, TdlibLog
environment/  TdlibEnvironment, account directories
store/        AccountStore, AccountState, ChatListOrder, reducers
di/           CoreDataModule
```

Its source sets split on where TDLib runs: `nonWebMain` (in the process) and `webMain` (in a Web Worker,
`core/data/worker`).

### 1.1 Dependency rules

| Module | May depend on | Must not contain |
|---|---|---|
| `feature:x:api` | `core:navigation:api`, `core:domain` (keys carrying domain types), serialization | screens, ViewModels, logic |
| `feature:x:domain` | `core:domain`, coroutines, `koin-annotations` | Compose, `io.xbot.tdlib.client.*` |
| `feature:x:data` | its `domain`, `core:domain`, `core:data` | Compose, ViewModels |
| `feature:x:presentation` | its `api` and `domain`, other features' **`api` only**, `core:designsystem`, `core:navigation:api` | `data`, `io.xbot.tdlib.client.*` |
| `core:*` | other `core:*`, no cycles | anything feature-specific |
| `shared` | everything | business logic |
| `android-app`, `jvm-app`, `web-app` | `shared` | DI declarations, UI |

- `presentation` never sees `data`. They meet only in the Koin graph started by `shared`.
- A model or interface needed by two features moves to `core:domain`. It is never imported from
  another feature.
- `api` never depends on `presentation` or `data`. Only `shared` wires everything together.

---

## 2. Dependency injection (Koin)

### 2.1 Setup

- Every module with annotations or compiler DSL applies `alias(libs.plugins.koin.compiler)` and
  `koinCompiler { compileSafety = true }`. That is how AnilibriaRefresh is set up; the plugin's
  compatibility list includes Kotlin 2.4.20 **[V]**.
- `shared`: `@KoinApplication internal object KotogramApp` and `fun initKoin(config)` →
  `startKoin<KotogramApp> { includes(config); modules(<feature>FeatureModule, …) }`. Modules annotated
  `@Configuration` are included automatically **[V]**. The DSL feature modules are listed by hand, as in
  AnilibriaRefresh's `StartKoin.kt`.

### 2.2 Annotation layers (`domain`, `data`, `core:*`)

- **One module class per Gradle module**, in its `di` package:
  `@Module @Configuration @ComponentScan("com.xbot.kotogram.<feature>.<layer>") class <Feature><Layer>Module`.
  Example: `AuthDataModule`.
- **Classes are annotated directly**:
  - `@Factory` on use cases;
  - `@Singleton` on app-wide services;
  - `@Scope(AccountId::class) @Scoped` on anything bound to one account (repositories, caches);
  - `@KoinViewModel` on ViewModels, which live in `presentation`.
- **Implementations stay `internal`.** `@Singleton`/`@Scoped` on an internal class binds it to the
  interface it implements.

### 2.3 DSL layer (`presentation` feature module)

`val <feature>FeatureModule = module { … }` in `presentation/di/<Feature>FeatureModule.kt`. It holds:

- `polymorphic<NavKey> { subclass(XRoute::class) }`, the back-stack serializers (§3.3);
- `navigation<XRoute> { key -> … }`, the navigation entries (§3.2).

Everything else in the feature uses annotations.

### 2.4 Account scope and lifetimes

```
root (app)            @Singleton: AccountRepository, AccountStorage, TdlibEnvironment, TdLibProvider, UpdateLane
 └─ AccountId scope   one Koin scope per account: ClientHolder, AccountStore, repositories, ViewModels
```

An account is one login's life: see [design/accounts.md](design/accounts.md). Scopes are opened and closed only by
`DefaultAccountRepository`; composition only provides one (`UnboundKoinScope`) and reports when it stopped showing it.

### 2.5 Compile safety: rules learned in this migration

Koin's compile safety checks the full graph only at `startKoin<KotogramApp>()` in `shared`. Each
rule below comes from a case where the check was silently skipped or gave a false error **[V]**:

1. **No `expect`/`actual` `@Module`.** Its definitions are "incomplete" to the consumer, and the
   whole check is then **skipped** (`SKIPPED: some dependency modules have incomplete definitions`).
   Keep the definition in common code and make only a plain function `expect`
   (`platformTdlibEnvironment`).
2. **Define with classes, not provider functions, across modules.** A `@Singleton fun` (top-level or
   in a `@Module` class) consumed from another module was validated against the returned type's
   constructor. Example: `TdlibEnvironment` became an interface with a `@Singleton` class
   implementation.
3. **DSL modules from other Gradle modules are invisible to the check.** `modules(someModule)` lives
   in a lambda body, which isn't ABI. So anything other code depends on is annotated; DSL is only
   for leaf contributions.
4. **`@Provided` goes on the parameter, not the class,** when the type is in another module.
5. **`shared` compiles non-incrementally.** Incremental compilation reused a stale result after
   an upstream definition was removed, and the error appeared only on a full recompile. The
   aggregator module is small, so `incremental = false` is set there.
6. **Even test DSL is validated.** A runtime-declared instance in a test is fetched with
   `getOrNull` (nullable requests are not checked).

---

## 3. Navigation (Navigation 3 + Koin)

### 3.1 `core:navigation:api`

```kotlin
interface NavKey : androidx.navigation3.runtime.NavKey

interface Navigator {
    val backStack: List<NavKey>
    val currentDestination: NavKey?
    fun navigate(key: NavKey)
    fun navigateBack()
}

val LocalNavigator = compositionLocalOf<Navigator> { error("No navigator provided") }
```

Copied in shape from AnilibriaRefresh. `TopLevelNavKey` (tab title and icons) and `ExternalUriNavKey`
(t.me links opened outside the back stack) are added when the main screen gets tabs and links.

### 3.2 A feature

`feature:chats:api`:

```kotlin
@Serializable
data class ChatRoute(val chatId: Long) : NavKey

fun Navigator.navigateToChat(chatId: Long) = navigate(ChatRoute(chatId))
```

`feature:chats:presentation`, `di/ChatsFeatureModule.kt`:

```kotlin
val chatsFeatureModule = module {
    polymorphic<NavKey> { subclass(ChatListRoute::class); subclass(ChatRoute::class) }

    navigation<ChatListRoute> {
        val navigator = LocalNavigator.current
        val lifecycleOwner = LocalLifecycleOwner.current
        ChatListScreen(
            onChatClick = { chatId -> lifecycleOwner.dropUnlessResumed { navigator.navigateToChat(chatId) }.invoke() }
        )
    }
}
```

- Other features navigate here **only** through `navigateTo…` extensions from `api`.
- **Navigation callbacks are wrapped in `dropUnlessResumed`**, so a double tap during a transition
  doesn't push twice.
- Screens never touch the `Navigator`; only `navigation<…>` entries do.

### 3.3 `core:navigation:impl` and the app

- `rememberNavigator(startRoute, serializersModule)` returns the `KotogramNavigator`. Its back stack is
  `rememberNavBackStack(SavedStateConfiguration { serializersModule = … }, …)`, so it survives
  configuration changes and process death. On iOS and desktop that call **requires** a `SerializersModule`
  registering every `NavKey` subtype, and throws otherwise **[V]**.
- The `SerializersModule` is built from all `polymorphic<NavKey>` contributions in Koin
  (`getAll<PolymorphicSerializerConfig<NavKey>>()`), as AnilibriaRefresh's `KoinNavigation.kt` does
  **[V]**. Only keys that can reach the back stack are registered.
- `shared` provides `LocalNavigator` and renders
  `NavDisplay(backStack = navigator.backStack, onBack = navigator::navigateBack, entryProvider = koinEntryProvider<NavKey>())`
  **[V]**, with `rememberSaveableStateHolderNavEntryDecorator()` and the ViewModel-store entry decorator.
- **The root is state-driven.** Splash, auth and main are chosen from the session state
  (`RootViewModel`). On a session change, such as auth → main or an account switch, the navigator's back
  stack is replaced.
- **Flow-internal navigation stays local.** The auth flow's back stack is a function of TDLib's
  authorization state; it keeps its own nested `NavDisplay` and doesn't go through `Navigator`.
- **Later:**
  - tabs get one back stack per top-level route (AnilibriaRefresh's `NavigationState`);
  - deep links use `UriDeepLinkMatcher` (`DeepLinkHandler.kt` there);
  - a `requiresLogin`-style interceptor, when needed.

---

## 4. Domain layer

| Kind | Name | Rules |
|---|---|---|
| Model | `Chat`, `AuthorizationState` | TDLib's generated classes (`io.xbot.tdlib.api`), not copies. Own types only for what TDLib doesn't have: `AccountId`, `ChatListEntry`, errors |
| Repository | `{Noun}Repository` | interface; `Flow` to observe, `suspend` for one-shot calls |
| Use case | `{Verb}{Noun}UseCase` | concrete `@Factory` class, no interface and no `Default` prefix; `operator fun invoke`; input as a nested `data class Params` when there is more than one value |
| Error | `{Feature}Error` | mapped from `TdException` in data |

A ViewModel may call a repository directly. Use cases exist only where they add logic or combine
repositories.

## 5. Data layer

- `Default{Noun}Repository` is `internal` and annotated (`@Scope(AccountId::class) @Scoped`). Commands go through
  `ClientHolder`, reads through `AccountStore.state`.
- There is no mapping layer: TDLib's generated classes are the model.
- Each account has **one** collector of `TdClient.updates`, inside `ClientHolder`, because TDLib requires in-order
  processing. It feeds `AccountStore`. Everyone else reads the store. See
  [design/messaging-core.md](design/messaging-core.md).

---

## 6. Presentation layer

### 6.1 Package layout (`com.xbot.kotogram.feature.<feature>.presentation`)

```
di/
  <Feature>FeatureModule.kt           # DSL: polymorphic<NavKey> + navigation<Route> entries
screen/
  <screen>/                           # one folder per screen, always (also for one-screen features)
    <Screen>Screen.kt                 # stateful XScreen + stateless XScreenContent + previews
    <Screen>Components.kt             # optional: pieces used only by this screen
    <Screen>ViewModel.kt
    <Screen>Contract.kt               # <Screen>ScreenState + <Screen>ScreenAction + <Screen>ScreenSideEffect
                                      # (a screen with only a state: <Screen>ScreenState.kt, as ktlint names it)
component/                            # optional: composables shared by 2+ screens of this feature
```

- No `ui/`, `util/` or `utils/` folders. Helpers live next to the screen that uses them.
- Platform code (`expect`/`actual`) mirrors the same package path in `androidMain`/`iosMain`/`jvmMain`.
- Everything is `internal` except `val <feature>FeatureModule`.

### 6.2 Where a composable lives

1. Used by one screen: `screen/<screen>/`, `private` or `internal`.
2. Used by 2+ screens of the feature: `component/`, `internal`.
3. Used by 2+ features: `:core:designsystem`, `public`.

### 6.3 Screens

```kotlin
// Stateful
@Composable
internal fun PhoneScreen(
    onQrLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhoneViewModel = koinViewModel(),
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { /* one-off events */ }
    PhoneScreenContent(state = state, onAction = viewModel::onAction, onQrLoginClick = onQrLoginClick, modifier = modifier)
}

// Stateless
@Composable
private fun PhoneScreenContent(
    state: PhoneScreenState,
    onAction: (PhoneScreenAction) -> Unit,
    onQrLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

- `XScreen` (or `XPane` for an adaptive pane) takes the ViewModel as a defaulted parameter. It collects
  state and side effects, receives navigation callbacks and never touches the `Navigator`.
- `XScreenContent` takes `state`, `onAction` and the navigation callbacks. It has no ViewModel and is
  previewable.
- Every screen has a `private` `XScreenPreview` under its `XScreenContent`, annotated `@KotogramPreview`
  (`:core:designsystem`: `@PreviewLightDark` plus a `@PreviewWrapper` with `KotogramTheme` and a `Surface`, so
  the preview calls only the content). States come from a `private class XScreenStateProvider :
  PreviewParameterProvider<XScreenState>` in the same file, one value per state worth seeing (empty, filled,
  in progress, error). Presentation modules add `androidRuntimeClasspath(libs.compose.ui.tooling)`.
- Adaptive layout decisions are made by the caller (the nav entry), not by the screen.
- Parameter order follows the Compose API guidelines: required parameters, then
  `modifier: Modifier = Modifier`, then optional parameters, then the trailing content lambda.
- Lazy lists set `key` and `contentType`.

### 6.4 ViewModels (Orbit MVI)

```kotlin
@KoinViewModel
internal class PhoneViewModel(
    private val authRepository: AuthRepository,
) : ViewModel(), OrbitContainerHost<PhoneScreenState, PhoneScreenState, PhoneScreenSideEffect> {

    override val container = orbitContainer(initialState = PhoneScreenState()) { … }

    fun onAction(action: PhoneScreenAction) {
        when (action) {
            is PhoneScreenAction.OnNumberChange -> onNumberChange(action.number)
            PhoneScreenAction.OnSubmit -> onSubmit()
        }
    }
}
```

- Its public surface is `container` and a single `fun onAction(action)`.
- One-off events (snackbar, a navigation request) are side effects, not state. Use `Nothing` when a
  screen has none.
- No Compose types and no `Navigator`. Account-bound ViewModels add `@Scope(AccountId::class)`.

---

## 7. Naming reference

| Thing | Pattern | Example |
|---|---|---|
| Navigation key | `{Feature}Route`, `{Screen}Route` (`@Serializable`, implements `NavKey`) | `ChatRoute(chatId)` |
| Navigation helper | `Navigator.navigateTo{Target}()` in `api` | `navigateToChat(chatId)` |
| Flow-internal step (not a NavKey) | `{Flow}Step` | `AuthStep.Code` (today `AuthRoute`) |
| Stateful composable | `{Screen}Screen` or `{Screen}Pane` | `PhoneScreen` |
| Stateless composable | `{Screen}ScreenContent` | `PhoneScreenContent` |
| Screen-private pieces | `{Screen}Components.kt` | `PhoneComponents.kt` |
| State | `{Screen}ScreenState` | `PhoneScreenState` |
| Action | `{Screen}ScreenAction`, sealed, entries `On{Something}` | `PhoneScreenAction.OnNumberChange` |
| Side effect | `{Screen}ScreenSideEffect` | `PhoneScreenSideEffect.ShowError` |
| ViewModel | `{Screen}ViewModel` (or `{Feature}ViewModel` when one serves several screens) | `PhoneViewModel` |
| Navigation callback | `on[Target]Click`: name the target, not the layout role | `onChatClick`, `onQrLoginClick`, `onBackClick` |
| Use case | `{Verb}{Noun}UseCase` | `ObserveAuthStateUseCase` |
| Repository | `{Noun}Repository`, impl `Default{Noun}Repository` | `DefaultAuthRepository` |
| Koin, annotated | `{Feature}{Layer}Module` | `AuthDataModule` |
| Koin, DSL | `{feature}FeatureModule` (`val`) | `authFeatureModule` |
| Scope | the scope's source type | `AccountId` |
| Boolean state | `is…`, `can…`, `has…` | `isSubmitting`, `canSubmit` |

## 8. Code style

- Prefer `internal`. Feature implementation details, repository implementations and helpers **must**
  be `internal`.
- Common dependencies go in the top-level `dependencies { }` inside `kotlin { }`; platform ones in
  `sourceSets.<platform>Main.dependencies`. Use `api(...)` only when the types appear in the module's
  public API.
- Before committing, run `./gradlew spotlessApply`, then `:detekt`.

## 9. Status

Current status and open items are in [handoff.md](handoff.md).

---

## Sources

- Koin docs (`docs/reference`): `koin-core/scopes.md` (session scope, close, onClose),
  `koin-core/definitions.md` (onClose), `koin-annotations/scope.md`,
  `koin-annotations/annotations-inventory.md` (@Configuration, @Scope + @KoinViewModel),
  `koin-compose/navigation3.md`, `koin-compose/compose-scopes.md` (KoinScope, UnboundKoinScope):
  https://github.com/InsertKoinIO/koin/tree/main/docs/reference
- Koin Compiler Plugin README (setup, Kotlin 2.3.20 → 2.4.20 compatibility):
  https://github.com/InsertKoinIO/koin-compiler-plugin
- `androidx.navigation3:navigation3-runtime:1.2.0` sources, `RememberNavBackStack.kt`.
- AnilibriaRefresh: `AGENTS.md`, `shared-ui/navigation/{api,impl}`, `shared-ui/src/.../di/{StartKoin,KoinNavigation}.kt`,
  `shared-ui/feature/title/{api,impl}`.
