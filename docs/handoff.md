# Handoff (2026-11-04)

For the next agent or machine. Durable rules are in [CLAUDE.md](../CLAUDE.md). This file is status: replace it when it goes stale.

## Where things stand

**KTG-6 is in the working tree, uncommitted.** The developer built it and reports that it seems to work; which targets and tests they ran isn't recorded. After that build `PlatformTdlibEnvironment` and `PlatformAccountStorage` became provider functions in `CoreTdlibModule`, and extensions and `platform*` moved to their packages. Studio's analyzer sees no errors in the moved files, but those steps haven't been built. Run the full set before committing:

```
./gradlew :core:tdlib:jvmTest :shared:jvmTest
./gradlew compileKotlinJvm compileAndroidMain compileKotlinIosSimulatorArm64 compileKotlinWasmJs
./gradlew spotlessApply :detekt
```

Next task number after it: **KTG-7**.

What to watch, each one not confirmed on every target:

- `KoinGraphTest`: `CoreTdlibModule.tdlibEnvironment` must still let the test's `single<TdlibEnvironment>` override it, or the test opens the real accounts. Its first assertion checks that.
- `tdlibEnvironment(scope)` on Android: the scope passed to a provider function must reach `androidContext()`.
- `WebLocalStorage` and `PreferencesSerializer` from `commonMain`'s DataStore dependency on wasmJs.
- Test DSL under compile safety (`TestKoin.kt`).

## What KTG-6 changed

- **Modules.** `core:data` → `core:tdlib`, `core:domain` → `core:model`, `feature:x:presentation` → `feature:x:impl`. `AccountRepository` moved to `core:tdlib`. Features keep their `domain` and `data`, and a feature owns its entity.
- **Koin at the edges.** Entities carry annotations only. `AccountSessions` and `AccountLifetime` hide scopes; `KoinAccountSessions` and `KoinAccountLifetime` in `core:tdlib/di` are the adapters. Screens take the ViewModel as a parameter, and entries resolve it.
- **Navigation without Koin.** `FeatureNavigation` objects with Navigation 3's `entry<…>`, listed in `shared/root/AppNavigation.kt`. `koin-compose-navigation3` is gone.
- **Entities split.** `ManagedClientHolder : ClientHolder`, `MutableAccountStore : AccountStore`, one `Reducer` per entity in a `CompositeReducer`, `AccountPointerKeeper`, `MemoizingRowMapper`, and `sendCatching` and `ErrorExt` shared by features.
- **Packages.** Extensions in `extensions/<Receiver>Ext.kt` per module, `platform*` expect/actual in `di`.
- **Web storage is DataStore** through `WebLocalStorage`. Old `localStorage` keys aren't migrated, so an existing web profile logs in again.
- **Passkey code is deleted.** It could never work (CLAUDE.md).

## Left on purpose

- **`build-logic`**: 14 build files repeat about 40 lines each. Convention plugins can't be checked without running Gradle, so they wait for the first build.
- **`Scope` in `platformTdlibEnvironment`**: the last Koin type outside adapters' reach, because Android reads its `Context` from it. Other platforms ignore it.

## What exists

- **`core:tdlib`**, by entity: `client`, `store`, `account`, `environment`, `di`. See `architecture.md` §1.
- **No mirror.** TDLib's generated classes are the domain model everywhere above `data`. `core:model` exposes them through `api(libs.tdlib.client)`, and a detekt `ForbiddenImport` keeps `io.xbot.tdlib.client.*` out of `domain` and `impl`.
- **`feature:chats:{api,domain,data,impl}`.** The chat list is real: `DefaultChatRepository`, `ChatListViewModel`, a memoising `ChatListRowMapper`, `loadChats` paging. No avatars yet. The chat screen only shows its title: history is M2.
- **Web:** `web-app` (`wasmJs`), worker in `core/tdlib/worker`, `AccountStorage` as the platform seam, one tab at a time through `navigator.locks`.

**Checked before KTG-6, on the layout it replaced.** `jvmTest` passed with real TDLib. JVM, Android, iOS and web compiled. Headless Chrome: the web bundle started TDLib 1.8.69 in the worker, connected over WebSocket, reached `authorizationStateWaitOtherDeviceConfirmation` and showed the QR screen with a real token; the database is in IndexedDB.

**Not checked: nothing past the login screen has run anywhere.** The chat list has never seen a real account. No device, no logged-in account, no web production bundle. Do this by hand on a test-DC account: log in, see the list, log out, add an account, go back from its login. `-Ptelegram.test.dc=true` keeps away from real data, because the real data directory holds real accounts and the rules delete non-active unauthorized ones.

## Decisions

- **D1 decided and applied (2026-11-04):** generated DTOs are the domain model.
- **D2 decided:** the store lives in `core:tdlib`.
- **Accounts design accepted** and implemented.
- **Module layout decided (2026-11-04):** one low-level core module, `domain` and `data` per feature, `impl` instead of `presentation`.

Waiting on the developer: **D3, push.** Log in at my.telegram.org and check whether a third-party `api_id` can upload FCM/APNs credentials. Nobody could verify it from outside. It decides the whole notification design.

## Next steps, in order

1. Run the full KTG-6 set (above), then commit it. Consider `build-logic` right after.
2. Try it by hand with a real account, and get D3.
3. Finish M1: avatars (`FileStore`, `FileDownloads`, Coil fetcher, §7 of the design), folders, typing, scope mute settings. Measure cold-start update count, JSON decode cost and sort cost, and run TDLib on an iOS device.
4. M2: `ChatHistory` (design §6).
5. Web: the QR screen shows the link as text everywhere, not an image. `index.html` has no favicon.
6. Optional, one agent at most: a focused review of design §4.2, §6 and §7 against the TDLib contract (`td_api.tl`, `MessagesManager.cpp`). Of the four planned reviews, only mechanics finished ([06-review-mechanics.md](research/tdlib-survey/06-review-mechanics.md)).

## What is not done, or weak

- [01-landscape.md](research/tdlib-survey/01-landscape.md) is a scripted classification, not the verified catalog that was planned. Only the Kotlin clients in report 02 were read.
- §6's table "everything else a chat screen needs" comes from Pingogram and hasn't been reviewed against the design.
- The clones (66 GB) exist only on the first Mac, in a temporary folder. `inventory.tsv` lists every repository.

## Also true

- `docs/research/tdlib-clients.md`, `docs/design/sessions.md` and the research reports use older module names and are kept for the reasoning.
- The local `tdlib-kmp` clone is on `feature/web-target`, which diverges from `main`; only `main` is published.
