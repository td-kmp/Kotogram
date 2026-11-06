# Handoff (2026-11-05)

For the next agent or machine. Durable rules are in [CLAUDE.md](../CLAUDE.md). This file is status: replace it when it goes stale.

## Where things stand

**KTG-7 is in the working tree on `refactoring/KTG-7-multiaccount-koin`, uncommitted.** The developer brought a new multi-account design and asked to delete everything except the UI and rebuild on it. KTG-6 and everything before it are at `ba53a20` on `feature/KTG-1-login`, pushed.

Checked on the JVM only: every JVM module and `jvm-app` compile, all 42 tests pass (`KoinGraphTest` on real TDLib included), and spotless and detekt are clean. Android, iOS and web have not been compiled, and `AndroidDatabaseKeys` has never run. Before committing:

```
./gradlew compileAndroidMain compileKotlinIosSimulatorArm64 compileKotlinWasmJs
```

Next task number after it: **KTG-8**.

## What KTG-7 changed

- **Design:** [design/multiaccount.md](design/multiaccount.md), including where it differs from the sketch and why.
- **Modules.** `core:tdlib` and `core:model` → `core:account`, which has no UI dependencies. Features keep `{api,domain,data,impl}`. `core:navigation`, `core:designsystem` and `feature:x:api` didn't change.
- **Kept from the UI:** every screen, contract, component, resource, the design system and the navigation. ViewModels keep their logic and lose their annotations and `ObserveAuthorizationStateUseCase`.
- **Koin's DSL replaces annotations and the compiler plugin.** A client declared at runtime can't pass compile safety. `KoinGraphTest` resolves every definition of an account instead.
- **Accounts run at once**, one `TdClient` and one Koin scope each. `shared` provides the scope with `UnboundKoinScope`, and screens use plain `koinViewModel()`. `ClientHolder`, `AccountRepository`, `AccountSessions`, `AccountLifetime`, `UpdateLane` and `AccountPointerStore` are gone.
- **`AccountStore` → `ChatStore`** in `feature:chats:impl`, an `AccountUpdates` follower. Its reducers moved as they were. `options`, `connectionState` and the group maps had no reader, so they went with `SessionReducer` and `GroupReducer`.
- **QR → phone** is only a screen change; submitting the number during QR confirmation restarts the sign-in and the next account sends it, as Unigram does (`LoginEntry.pendingPhoneNumber`). `AccountManager` shows a replacing account only at its first screen, so the restart doesn't flash. `local.properties` has `telegram.test.dc=true` for IDE runs. `PhoneNumberRoute` is gone, and system back on the phone screen no longer returns to QR: its button does. QR from the phone screen past a step restarts it too, as Unigram does.
- **The login flow is a `NavDisplay` of its own** inside the `AuthRoute` entry, over a back stack `AuthViewModel` derives from TDLib (`AuthViewModelTest`). Its steps are `AuthStep` in `feature:auth:api`; the screens use a plain `Scaffold` each. The side effects, `AuthEntry` and `clearSteps` are gone.
- **Navigation is in Koin again**, as in AnilibriaRefresh: `navigation<Route>` and `polymorphic<NavKey>` in each impl module, `koinEntryProvider()` in `shared`. `FeatureNavigation` (KTG-6) is gone.
- **`LoginFlow`** replaces `LoginCapabilities`: `PhoneNumberOnly` on Android and iOS, `QrCodeFirst` on desktop and web, from one `platformLoginFlow` in `feature:auth:data`.
- **Start-up as in Telegram's apps:** a blank `SplashScreen`; Android holds its system splash (`core-splashscreen`) while `isStarting()`, i.e. until TDLib reports where the shown account stands, and the web page has the theme's background while the wasm loads.
- **Polish:** comments only on hacks and workarounds. Each screen keeps its state, actions and side effects in files of their own, and the splash screen moved to `shared/screen/splash`.
- **New:** a database key per account on Android (Keystore), `android:allowBackup="false"`, and data under `accounts/N/{db,files}`. Old `tdlib/account-N` directories aren't migrated, so an existing install logs in again.

## Left on purpose

- **No UI to add, switch or log out of an account.** There wasn't one before either. `AccountManager` has the operations and tests; the sign-in above the active account is wired in `RootNavigation` but nothing opens it.
- **Push** isn't ported (D3). **iOS** has no database key. See the design's "Not ported".
- **`build-logic`**: the build files still repeat about 40 lines each.

## What exists

- **`feature:auth:{api,domain,data,impl}`**: the login flow.
- **`feature:chats:{api,domain,data,impl}`**: the chat list is real: `ChatStore`, `DefaultChatRepository`, `ChatListViewModel`, a memoising `ChatListRowMapper`, `loadChats` paging. No avatars yet. The chat screen only shows its title: history is M2.
- **Web:** `web-app` (`wasmJs`), worker in `core/account/worker`, one tab at a time through `navigator.locks`.
- **Tests:** `AccountManagerTest` and `AccountRegistryTest` on a fake TDLib, `ReduceTest` and `UpdateCoverageTest` in `feature:chats:data`, `KoinGraphTest` on real TDLib.

**Not checked: nothing past the login screen has run anywhere,** and KTG-7 hasn't run at all. By hand on a test-DC account (`-Ptelegram.test.dc=true` keeps away from real data): log in, see the list, restart the app, QR → phone → QR on desktop (the same QR code), and a number submitted from QR.

## Decisions

- **D1:** generated DTOs are the domain model.
- **Multi-account (2026-11-05):** the design above; it replaces the accepted accounts design and the module layout of KTG-6.

Waiting on the developer: **D3, push.** Log in at my.telegram.org and check whether a third-party `api_id` can upload FCM/APNs credentials. Nobody could verify it from outside. It decides the whole notification design.

## Next steps, in order

1. Build and test (above), then commit.
2. A small account menu in the chat list (add, switch, log out), so multi-account can be tried by hand.
3. Try it with a real account, and get D3.
4. Finish M1: avatars (`FileStore`, `FileDownloads`, Coil fetcher, §7 of [messaging-core.md](design/messaging-core.md)), folders, typing, scope mute settings. Its entities use the old names: `FileStore` and the like become `AccountUpdates` followers.
5. M2: `ChatHistory` (design §6).
6. Web: the QR screen shows the link as text everywhere, not an image. `index.html` has no favicon.

## What is not done, or weak

- [01-landscape.md](research/tdlib-survey/01-landscape.md) is a scripted classification, not the verified catalog that was planned. Only the Kotlin clients in report 02 were read.
- §6's table "everything else a chat screen needs" comes from Pingogram and hasn't been reviewed against the design.
- The clones (66 GB) exist only on the first Mac, in a temporary folder. `inventory.tsv` lists every repository.

## Also true

- `docs/research/`, `docs/design/sessions.md`, `accounts.md` and `messaging-core.md` use older module and class names and are kept for the reasoning.
- The local `tdlib-kmp` clone is on `feature/web-target` and its `origin/main` is behind the published tag. Read `v1.8.69-1.0.0-alpha02` on GitHub.
