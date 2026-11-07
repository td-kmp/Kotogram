# Kotogram — Architecture

Status: **accepted.** Accounts and their Koin scopes are in [design/multiaccount.md](design/multiaccount.md). It fixes the module layout, DI, navigation and naming rules that every
feature follows. The naming and navigation rules are borrowed from the user's AnilibriaRefresh project (`AGENTS.md`,
`shared-ui/navigation`) and adapted to Kotogram.

Stack: Kotlin 2.4.20, CMP 1.12.1, Koin 4.2.2 (DSL), Navigation 3 (runtime 1.2.0, JetBrains `navigation3-ui`
1.2.0-rc01), Orbit MVI 12, TDLib via `tdlib-kmp`. Targets: Android, iOS, JVM desktop, web (`wasmJs`).

Legend: **[V]** verified against sources (library sources and docs, reference projects). **[U]** unverified, check before relying on it.

---

## 0. Decisions (TL;DR)

1. **A feature has four modules**: `api` (navigation keys and `navigateTo…` extensions), `domain` (its repository
   interfaces and types), `data` and `impl` (screens, ViewModels, navigation). A feature owns its entity. Another feature
   may use its `api` and `domain`, never its `data` or `impl`.
2. **One assembly point**, `:shared`: `initKoin`, the root `NavDisplay` and the iOS
   framework. The platform shells `:android-app`, `:jvm-app` and `:web-app` only start Koin and host the UI.
3. **DI is Koin's DSL.** `module { scope(AccountScope) { … } }`, one per module. A client declared in a scope at runtime
   can't pass the compiler plugin's compile safety, so there is no plugin and no annotations. `KoinGraphTest` resolves
   every definition of an account on real TDLib instead.
4. **Each Telegram account is a Koin scope** owned by `AccountManager`, with its own `TdClient`, on one shared `TdLib`. All
   accounts run at once and the UI shows one. See [design/multiaccount.md](design/multiaccount.md).
5. **Navigation is Navigation 3 with Koin's `navigation<Route>`, as in AnilibriaRefresh**: each feature's
   `impl` module registers its keys with `polymorphic<NavKey>` and its screens with `navigation<Route> { … }`,
   and `shared` builds the graph with `koinEntryProvider()`. One `Navigator` (`LocalNavigator`) per account
   keeps a saved back stack.
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
:catalog-app                 Desktop catalog of core:designsystem, Roborazzi screenshot tests
:shared                      ASSEMBLY POINT: initKoin, root App composable with the account scopes, iOS framework
ios-app/                     Xcode project, thin Swift host (not a Gradle module)

:core:account                AccountManager, AccountSession, extension points for features, platform storage, web worker; no UI
:core:navigation:api         NavKey, Navigator, LocalNavigator, navigation helpers
:core:navigation:impl        rememberNavigator, NavigationState, deep links
:core:designsystem           KotogramTheme, tokens, components used by 2+ features
:core:resources              every string, StringResource, the Localization port, markup and plurals, localising extensions of TDLib's classes

:feature:<name>:api
:feature:<name>:domain
:feature:<name>:data
:feature:<name>:impl
```

`core:account` exposes TDLib's generated classes through `api(libs.tdlib.client)`, which are the domain model everywhere.
Its source sets split on where TDLib runs: `nonWebMain` (in the process) and `webMain` (in a Web Worker,
`core/account/worker`).

### 1.1 Dependency rules

| Module | May depend on | Must not contain |
|---|---|---|
| `core:account` | `tdlib-client`, Koin, DataStore | Compose, anything feature-specific |
| `feature:x:api` | `core:navigation:api`, serialization | screens, ViewModels, logic |
| `feature:x:domain` | `tdlib-client` (the DTOs), other features' `domain`, coroutines, Arrow | `core:account`, Compose, `io.xbot.tdlib.client.*` |
| `feature:x:data` | its `domain`, `core:account`, Koin | Compose, ViewModels |
| `feature:x:impl` | its `api` and `domain`, other features' `api` and `domain`, `core:designsystem`, `core:resources`, `core:navigation:api`, `core:account` for `AccountScope` and `AppCoroutineScope` in `di` | any `data`, other `impl`, `io.xbot.tdlib.client.*` |
| other `core:*` | other `core:*`, no cycles | anything feature-specific |
| `shared` | everything | business logic |
| `android-app`, `jvm-app`, `web-app` | `shared` | DI declarations, UI |
| `catalog-app` | `core:designsystem` | features, `shared`, TDLib |

- `impl` never sees `data`. They meet only in the Koin graph started by `shared`. A detekt `ForbiddenImport` keeps `io.xbot.tdlib.client.*` out of `domain` and `impl`.
- A feature owns its entity: `feature:chats` owns chats for everyone. Nothing flows into core just because two features want it.

---

## 2. Dependency injection (Koin)

- `shared`'s `initKoin(config)` calls `startKoin` with `accountsModule` and every feature's module, then `config`, so a shell or a test can override definitions, and then `AccountManager.start()`.
- **One `val <feature><Layer>Module` per module**, in its `di` package:

```kotlin
// feature:chats:data
val chatsDataModule = module {
    scope(AccountScope) {
        scopedOf(::ChatStore) { bind<AccountUpdates>() }
        scopedOf(::DefaultChatRepository) { bind<ChatRepository>() }
    }
}

// feature:chats:impl
val chatsImplModule = module {
    scope(AccountScope) {
        viewModelOf(::ChatListViewModel)
        viewModel { parameters -> ChatViewModel(parameters.get(), get()) }
    }
}
```

- **Everything bound to one account goes in `scope(AccountScope)`**: repositories, caches, ViewModels. It sees the account's `TdClient` and `AccountContext`, declared by the manager, and anything from the root. The root doesn't see it.
- **A cache or counter that follows updates binds `AccountUpdates`**: the manager creates it before the client's first request, because updates have no replay.
- **Options every instance needs bind `TdlibOptions`** in `scope(AccountScope)`: the manager sends their first values before `setTdlibParameters`, which TDLib needs for some of them, and each change after. One option has one owner.
- **App-wide parts** are root singles: requests that need no particular account go through `AppClient`, app-wide files and preferences through `AppStorage`, and work that outlives screens runs in `get(AppCoroutineScope)`.
- **Entries resolve ViewModels with plain `koinViewModel()`**, from the account scope `shared` provides with `UnboundKoinScope`. Only inside navigation entries: a store of its own per entry is what keeps accounts apart, because Koin's ViewModel key doesn't include the scope. Elsewhere it needs its own `rememberViewModelStoreOwner()`.
- **Implementations stay `internal`.** A module's definitions see them, and only the `val` is public.
- **Platform values** come from `platform*` expect/actual functions in `core:account`'s `di` package.
- **Nothing checks the graph at compile time.** `KoinGraphTest` resolves every definition of an account. Add a parameter's value there when a new ViewModel takes one.

---

## 3. Navigation (Navigation 3)

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

```kotlin
inline fun <reified T : Any> Module.polymorphic(builder: PolymorphicModuleBuilder<T>.() -> Unit)
```

Copied from AnilibriaRefresh: each call is one `PolymorphicSerializerConfig`, and `koinNavSerializersModule()`
in `core:navigation:impl` collects them into the `SerializersModule` that saves the back stack.

### 3.2 A feature

`feature:chats:api`:

```kotlin
@Serializable
data class ChatRoute(val chatId: Long) : NavKey

// extensions/NavigatorExt.kt
fun Navigator.navigateToChat(chatId: Long) = navigate(ChatRoute(chatId))
```

`feature:chats:impl`, `di/ChatsImplModule.kt`:

```kotlin
val chatsImplModule = module {
    polymorphic<NavKey> {
        subclass(ChatListRoute::class)
        subclass(ChatRoute::class)
    }

    navigation<ChatListRoute> {
        val navigator = LocalNavigator.current
        val lifecycleOwner = LocalLifecycleOwner.current
        ChatListScreen(
            viewModel = koinViewModel(),
            onChatClick = { chatId -> lifecycleOwner.dropUnlessResumed { navigator.navigateToChat(chatId) }.invoke() }
        )
    }

    scope(AccountScope) {
        viewModelOf(::ChatListViewModel)
    }
}
```

`navigation<Route>` is a root singleton, and `koinEntryProvider()` collects every one from the account scope
`shared` provides, so an entry's `koinViewModel()` resolves from the account.

- Other features navigate here **only** through `navigateTo…` extensions from `api`.
- **Navigation callbacks are wrapped in `dropUnlessResumed`**, so a double tap during a transition
  doesn't push twice.
- Screens never touch the `Navigator`; only entries do.

### 3.3 `core:navigation:impl` and the app

- `rememberNavigator(startRoute, serializersModule)` returns the `KotogramNavigator`. Its back stack is
  `rememberNavBackStack(SavedStateConfiguration { serializersModule = … }, …)`, so it survives
  configuration changes and process death. On iOS and desktop that call **requires** a `SerializersModule`
  registering every `NavKey` subtype, and throws otherwise **[V]**.
- `koinNavSerializersModule()` builds the `SerializersModule` from every `polymorphic<NavKey>` in Koin. Only keys
  that can reach the back stack are registered.
- `shared` provides `LocalNavigator` and renders
  `NavDisplay(backStack = navigator.backStack, onBack = navigator::navigateBack, entryProvider = koinEntryProvider())`,
  with `rememberSaveableStateHolderNavEntryDecorator()` and the ViewModel-store entry decorator.
- **The root is state-driven.** `RootNavigation` shows the active account, and splash, auth and main follow its
  authorization state (`routeOf`). Auth → main replaces the back stack's root with
  `setRoot`, so popped entries clear their ViewModels. An account switch disposes the whole subtree.
- **A flow driven by an outside state has a `NavDisplay` of its own, over a derived back stack.** The login
  flow is one entry, `AuthRoute`; inside it `AuthScreen` shows the stack `AuthViewModel` holds, worked out from
  TDLib's state: the entry screen, and the current step on top unless the user went back from it. As in the
  recipes (`multiplestacks`, `conditional`), the state holder changes only on actions and the display only shows
  it. There are no navigation side effects, and nothing to restore: the stack follows TDLib after process
  death too, so its keys (`AuthStep`) are neither `NavKey`s nor saved. Back handlers are LIFO, so a handler
  around the account's `NavDisplay` is composed before it. Own design from Navigation 3's pieces: there is no
  recipe with a nested `NavDisplay`.
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
| Error | `{Feature}Error` | mapped from TDLib's `Error` in data |

A ViewModel may call a repository directly. Use cases exist only where they add logic or combine
repositories.

## 5. Data layer

- `Default{Noun}Repository` is `internal`, in `feature:x:data`, and scoped to the account. Commands go through
  `TdClient.sendCatching`, reads through a follower's state, such as `ChatStore.state`.
- There is no mapping layer: TDLib's generated classes are the model.
- A follower of updates is **one** collector, because TDLib requires in-order processing: `ChatStore` folds everything it
  needs into one snapshot. Two followers see the updates in order but not in step with each other.

---

## 6. Presentation layer

### 6.1 Package layout (`com.xbot.kotogram.feature.<feature>.impl`)

```
di/
  <Feature>ImplModule.kt              # keys, navigation<Route> entries and ViewModels
navigation/
  <Feature>Routes.kt                  # the feature's own keys, when it has some
screen/
  <screen>/                           # one folder per screen, always (also for one-screen features)
    <Screen>Screen.kt                 # stateful XScreen + stateless XScreenContent + previews
    <Screen>Components.kt             # optional: pieces used only by this screen
    <Screen>ViewModel.kt
    <Screen>ScreenState.kt
    <Screen>ScreenAction.kt
    <Screen>ScreenSideEffect.kt       # only when the screen has one-off events
component/                            # optional: composables shared by 2+ screens of this feature
```

`:shared` follows the same layout for its own screen: `navigation/SplashRoute.kt`, `screen/splash/SplashScreen.kt`;
`root/` holds the account host.

- **TDLib work stays where `TdClient` is visible**: `core:account` runs the accounts and owns no subsystem; a feature's `data` owns its entity, an app-wide one too (`feature:language`). What every instance needs reaches it through `TdlibOptions` or an `AccountUpdates` follower, as the language pack's options and updates do; files for images and video will too. A UI module reads such a part through the feature's `domain` or a port in `core:*` (`LocalizationSource`), never through an adapter in `shared`.
- Strings live only in `core:resources`, as in AnilibriaRefresh. State carries text as its `StringResource`; a feature's own types (`AuthError`) get theirs in the feature's `impl` `extensions`, since `core` sees no feature.
- No `ui/`, `util/` or `utils/` folders. Helpers live next to the screen that uses them.
- Platform code (`expect`/`actual`) mirrors the same package path in `androidMain`/`iosMain`/`jvmMain`.
- Everything is `internal` except the Koin module.

### 6.2 Where a composable lives

1. Used by one screen: `screen/<screen>/`, `private` or `internal`.
2. Used by 2+ screens of the feature: `component/`, `internal`.
3. Used by 2+ features: `:core:designsystem`, `public`.

### 6.3 Screens

```kotlin
// Stateful
@Composable
internal fun PhoneScreen(
    viewModel: PhoneViewModel,
    onQrLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
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

- `XScreen` (or `XPane` for an adaptive pane) takes the ViewModel as a required parameter, which the entry
  resolves with `koinViewModel()`. It collects state and side effects, receives navigation callbacks and
  never touches the `Navigator` or Koin.
- `XScreenContent` takes `state`, `onAction` and the navigation callbacks. It has no ViewModel and is
  previewable.
- A screen with nothing to hold (`PremiumPurchaseScreen`, `SplashScreen`) is only a stateless `XScreen` with
  its preview. A flow host (`AuthScreen`) has no `XScreenContent`: its `NavDisplay` entries resolve ViewModels.
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
internal class PhoneViewModel(
    private val authRepository: AuthRepository,
) : ViewModel(), OrbitContainerHost<PhoneScreenState, PhoneScreenState, PhoneScreenSideEffect> {

    override val container = orbitContainer(initialState = PhoneScreenState()) { … }

    fun onAction(action: PhoneScreenAction) {
        when (action) {
            is PhoneScreenAction.OnCountrySelect -> onCountrySelect(action.country)
            PhoneScreenAction.OnSubmit -> onSubmit()
        }
    }
}
```

- Its public surface is `container` and a single `fun onAction(action)`.
- A component with logic of its own gets its own ViewModel, `{Component}ViewModel` with `{Component}State` and `{Component}Action`, in a store of its own: `koinViewModel(viewModelStoreOwner = rememberViewModelStoreOwner())` (lifecycle 2.11), cleared when the component leaves the composition. `AuthScreen` hosts `LanguageSuggestion` so.
- One-off events (snackbar, a navigation request) are side effects, not state. Use `Nothing` when a
  screen has none.
- No Compose types and no `Navigator`, except a screen's `TextFieldState`: the ViewModel owns it in its `SavedStateHandle` (`saveable(TextFieldState.Saver)`, lifecycle-viewmodel-compose; `saved` needs a `KSerializer`, which `TextFieldState` lacks), edits it and follows it with `snapshotFlow` (decided 2026-11-08, `PhoneViewModel.phoneNumber`). A ViewModel of an account is declared in `scope(AccountScope)`.

---

## 7. Naming reference

| Thing | Pattern | Example |
|---|---|---|
| Navigation key | `{Feature}Route`, `{Screen}Route` (`@Serializable`, implements `NavKey`) | `ChatRoute(chatId)` |
| Navigation helper | `Navigator.navigateTo{Target}()` in `api/extensions` | `navigateToChat(chatId)` |
| Flow-internal step (not a NavKey) | `{Flow}Step` | `AuthStep.Code` |
| Stateful composable | `{Screen}Screen` or `{Screen}Pane` | `PhoneScreen` |
| Stateless composable | `{Screen}ScreenContent` | `PhoneScreenContent` |
| Screen-private pieces | `{Screen}Components.kt` | `PhoneComponents.kt` |
| State | `{Screen}ScreenState` | `PhoneScreenState` |
| Action | `{Screen}ScreenAction`, sealed, entries `On{Something}` | `PhoneScreenAction.OnCountrySelect` |
| Side effect | `{Screen}ScreenSideEffect` | `PhoneScreenSideEffect.Submitted` |
| ViewModel | `{Screen}ViewModel` (or `{Feature}ViewModel` when one serves several screens) | `PhoneViewModel` |
| Navigation callback | `on[Target]Click`: name the target, not the layout role | `onChatClick`, `onQrLoginClick`, `onBackClick` |
| Use case | `{Verb}{Noun}UseCase` | `ObserveAuthStateUseCase` |
| Repository | `{Noun}Repository`, impl `Default{Noun}Repository` | `DefaultAuthRepository` |
| Koin module | `{feature}{Layer}Module` | `authDataModule` |
| A feature's screens | `navigation<Route>` in `{feature}ImplModule` | `authImplModule` |
| Scope | a qualifier | `AccountScope` |
| Boolean state | `is…`, `can…`, `has…` | `isSubmitting`, `canSubmit` |

## 8. Code style

Androidx habits, applied:

- **Abstraction first.** Interface, then `Default…` in its own file. Read from the top, not from the implementation.
- **Narrow read interfaces.** Expose a `StateFlow`, keep the `MutableStateFlow` private.
- **Delegation over inheritance chains.** `ChatListRowMapper : RowMapper<…> by MemoizingRowMapper(…)`,
  `ChatsReducer : Reducer by CompositeReducer(…)`.
- **One responsibility per class.** A new entity is one more reducer in the list, not one more branch.
- **Extensions live in each module's `extensions` package, one `<Receiver>Ext.kt` per receiver type** (`ErrorExt`, `TdSenderExt`,
  `NavigatorExt`), never `util/`. A private member extension that is an implementation detail of one class stays in that class.
- **`platform*` lives in each module's `di` package**: the `expect` declaration and its `actual`s. A platform class it builds
  (`DirectoryAccountStorage`) stays with its entity.

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
  `koin-core/definitions.md` (onClose), `koin-compose/compose-scopes.md`:
  https://github.com/InsertKoinIO/koin/tree/main/docs/reference
- `androidx.navigation3:navigation3-runtime:1.2.0` sources, `RememberNavBackStack.kt`, `EntryProvider.kt`.
- `androidx.datastore:datastore-preferences-core:1.3.0-rc01` wasmJs sources, `PreferenceDataStoreFactory.web.kt`, `WebStorage.web.kt`.
- `koin-compose-navigation3:4.2.2` sources: `navigation<T>` is a root singleton `EntryProviderInstaller`, `koinEntryProvider()` collects them.
- AnilibriaRefresh: `AGENTS.md`, `shared-ui/navigation/{api,impl}`, `shared-ui/src/.../di/{StartKoin,KoinNavigation}.kt`,
  `shared-ui/feature/title/{api,impl}`.
