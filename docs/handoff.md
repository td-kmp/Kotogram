# Handoff (2026-11-10)

For the next agent or machine. Durable rules are in [CLAUDE.md](../CLAUDE.md). This file is status: replace it when it goes stale.

## Where things stand

**KTG-7 is on `refactoring/KTG-7-multiaccount-koin`, committed and pushed; no PR yet.** The developer brought a new multi-account design and asked to delete everything except the UI and rebuild on it. KTG-6 and everything before it are at `ba53a20` on `feature/KTG-1-login`, pushed.

Checked on 2026-11-07: all 85 JVM tests pass (`KoinGraphTest` on real TDLib included), detekt is clean, and `shared` compiles for Android, the iOS simulator and the web (`compileAndroidMain compileKotlinIosSimulatorArm64 compileKotlinWasmJs`). `jvm-app` ran on the desktop: the login screen offered "Продолжить на русском" for a `ru-RU` system, and a stored `ru` started the next run in Russian. `AndroidDatabaseKeys` has never run.

Checked on 2026-11-08: `core:designsystem` builds for Android, the JVM, the iOS simulator and the iPhone; the catalog's 56 screenshot tests match; the catalog ran on the desktop with AppKit's glass icon button, checkbox and switch. The country picker ran on an Android phone (debug) before its drag handle went; the APK after that is built, not installed. A release build is installed on an iPhone Air and hasn't been looked at.

Checked on 2026-11-10, on a second Mac and **not committed**: native overlays on macOS stay through screen transitions and are cut with their screen, on recordings of the catalog's Transitions page (`-Dcatalog.page=transitions -Dcatalog.autoplay=true`). Besides that: `:core:designsystem:jvmTest` passes; `shared`, `jvm-app` and `feature:auth:impl` compile for the JVM and `core:designsystem` for the web; Android and iOS weren't compiled, and nothing but the catalog ran. `detekt` fails on 12 findings, none in a file this change touched (`MediaQuery*`, `MediaScope*`, `TextField.kt`, `ListItem.kt`, `PhoneNumberField.kt`, `EventTargetListener.kt`). The catalog's screenshots match except `network-image` (both themes), which this change doesn't touch either; `home` was recorded again for the new card.

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
- **QR code** in `core:designsystem` (`component/QrCode`): qrose draws it with shapes of our own (`QrCodeShapes`) that give Telegram for macOS's `QRCode.swift` look (modules with rounded and inner corners, a 9-module cutout for a future icon) and the finders of its matrix animation (compottie, `files/qr_placeholder.lottie`); qrose turns the finder for each corner. `QrCodeGrid` puts both on whole pixels, so the animation's finders and the code's coincide. The code fades in over the animation in 1.2 s, and a new code over the last in 0.2 s, as macOS's image view. Licensing: [licensing.md](licensing.md).
- **Design system** ([design/designsystem.md](design/designsystem.md)):
  - `KotogramTheme.colors`, `.typography` and `.shapes` read M3's `MaterialTheme`; `.spacing` is our own CompositionLocal.
  - Colours are Telegram for macOS's Day Classic and Night Accent as M3 schemes, dynamic on Android 12 and later. Sizes and radii are macOS's.
  - Fonts: the system's SF Pro on Apple platforms, Inter (bundled) elsewhere.
- **`KotogramIcons`** in `core:designsystem`: 51 line icons for sign-in, the chat list, chats, settings and profiles ([design/designsystem.md](design/designsystem.md#icons)). A detekt `ForbiddenImport/designSystem` keeps screens on the design system; feature screens moved from `MaterialTheme` to `KotogramTheme`.
- **`Button`/`TextButton`** (`ButtonState`: enabled, disabled, loading), **`ListItem`** (M3's slots, macOS's insets, grouped positions) and **`RichText`** (TDLib's `FormattedText`, parsed in `core:resources`) in `core:designsystem`. The auth and chat screens use them; `CodeField` has a `supportingText` for its error.
- **`ProminentButton`**: the main action of each auth screen. It is UIKit's prominent glass button on iOS 26 and later and the filled `Button` elsewhere ([design/designsystem.md](design/designsystem.md#components)). It compiles for iOS but hasn't run on a device or simulator: this Mac has no iOS simulator runtime. On macOS 26 it is AppKit's glass `NSButton` above the Compose surface (Nucleus's `NativeView` with our `placedAsOverlay`); in the catalog, real clicks press it, light it, click it and show its spinner.
- **Native overlays stay through transitions on macOS** ([design/designsystem.md](design/designsystem.md#native-overlays), 2026-11-10, uncommitted):
  - Nucleus's `NativeView(placedAsOverlay = true)` now mounts the view in a clip view framed to its Compose ancestors' clip, reported after every frame's layout and committed with that frame.
  - The desktop's transitions clip the screen underneath to the part still seen, in their specs alone. A NavDisplay made to fill its place can't be clipped that way: the root's, inside a `Surface`, takes `wrapContentSize`.
  - The macOS overrides no longer hide while their screen isn't `RESUMED` (`isSettledInFront` is iOS's alone now).
  - `TransitionsPage` in the catalog. Compose Multiplatform has no answer of its own: it leaves overlays unclipped on purpose, up to `jb-main`.
- **`CodeField`** in `core:designsystem`: the sign-in code as Telegram for macOS's `CodeEntryControl`, on a `TextFieldState` ([design/designsystem.md](design/designsystem.md#components)). `CodeScreen` still uses `OutlinedTextField`.
- **`catalog-app`**: the design system's catalog on the desktop, with Roborazzi screenshot tests of every page (`recordRoborazziJvm`, `verifyRoborazziJvm`; [design/designsystem.md](design/designsystem.md#catalog)).
- **Text fields** in `core:designsystem` (`component/TextField`): `TextField` and `SecureTextField` take only a `TextFieldState`. They have M3's state-based parameters, plus `title` and `suggestion` above the field. On a laptop they look like Telegram for macOS's inputs: 40 high, radius 10, supporting text 10 below and red with `isError`; on touch screens they are 50 high with 17-point text and a focus ring. The auth screens still use M3's `OutlinedTextField`.
- **Start-up as in Telegram's apps:** a blank `SplashScreen`; Android holds its system splash (`core-splashscreen`) while `isStarting()`, i.e. until TDLib reports where the shown account stands, and the web page has the theme's background while the wasm loads.
- **Language packs as a feature** ([design/localization.md](design/localization.md)): `core:account` lost its language classes and got extension points instead (`TdlibOptions`, `AppClient`, `AppStorage`, `AppCoroutineScope`); `feature:language:{domain,data,impl}` keeps the whole chosen pack in memory; `core:resources` reads it through the `Localization` port, without `core:account`, `runBlocking` or a Flow per string; plurals are `<plurals>`; "Continue in …" is part of `AuthScreen`.
- **Media queries and adaptive styles** ([design/designsystem.md](design/designsystem.md#media-queries)): `media/` is androidx-main's `UiMediaScope` (`WindowPosture`, no `platformFeatures`) with the official scopes copied per platform, Google's on Android and CMP 1.13's elsewhere; `MediaQueryInfo` and `style/ComponentStyles` are Material 3's form factor and style stand-in. Every component and the phone, QR and language-suggestion screens have `Default`/`Adaptive` styles with `laptop {}`, every value of their look in the style, passed to their parts as a context parameter; components take one `style` parameter over their default and otherwise only state, as M3's styled `Button` (aosp/4375293). Buttons own their height (50 on touch screens, 36 on a laptop). On touch screens the fields are larger, the phone field a card as on the desktop. `@KotogramLaptopPreview` and the catalog's Mobile switch show either look. iOS reports no fold: iOS 27.1's hinge API isn't in Kotlin 2.4.20's UIKit.
- **Native controls and the country picker** ([design/designsystem.md](design/designsystem.md#components)): `IconButton` and `GlassIconButton` (UIKit's and AppKit's glass on iOS 26 and macOS 26), `TopBar`, `SearchField` (glass on iOS 26), `FastScroller` (`SectionIndex` on iOS, `ScrollThumb` elsewhere), `BottomSheet` (UIKit's page sheet on iOS; `dismiss()` hides it with its animation), `Checkbox` and `Switch` (AppKit's on macOS, `UISwitch` on iOS), and `OptionPicker` made of them, which the phone screen picks its country with; `KotogramIcons.Cross`. The catalog has a page for each, its own bar is `TopBar`, and it is in the window frame's colour.
- **Kotlin's experimental KEEP features are on everywhere** (root `build.gradle.kts`): collection literals, context-sensitive resolution, explicit context arguments, name-based destructuring (`only-syntax`) and the return value checker (`check`). Context parameters, explicit backing fields, nested type aliases, `@all`, guards and the rest are stable in 2.4. Local type aliases and companion blocks stay off: they mark binaries pre-release, which a release compiler won't read from another module.
- **Desktop window as Telegram for macOS's** (`AppDelegate.swift`, `ApplicationContext.swift`): 700×550 at the centre on start, never smaller than 380×550, no maximum (Nucleus's `minimumSize`). macOS also restores the last frame and, without one, takes 800×650 after the first sign-in; neither is done.
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
- **`feature:language:{domain,data,impl}`**: the app's language, "Continue in …" through `feature:auth:impl`.
- **`feature:media:{domain,data}`**: TDLib files for images: `FileRepository` (`downloadFile`, waits on `updateFile`; `readFilePart`), and each account's Coil `ImageLoader` with `TdlibFileFetcher` and `TdlibFileKeyer`; `NetworkImage` in `core:designsystem`. Nothing shows a TDLib image yet: chat avatars are next.
- **Tests:** `AccountManagerTest` and `AccountRegistryTest` on a fake TDLib, `ReduceTest` and `UpdateCoverageTest` in `feature:chats:data`, `DefaultLanguageRepositoryTest` on a fake `AppClient`, `PluralFormTest` and the `Localization` tests, `KoinGraphTest` on real TDLib.

**Not checked: nothing past the login screen has run anywhere,** and KTG-7 hasn't run at all. By hand on a test-DC account (`-Ptelegram.test.dc=true` keeps away from real data): log in, see the list, restart the app, QR → phone → QR on desktop (the same QR code), and a number submitted from QR.

## Decisions

- **D1:** generated DTOs are the domain model.
- **Multi-account (2026-11-05):** the design above; it replaces the accepted accounts design and the module layout of KTG-6.

Waiting on the developer: **D3, push.** Log in at my.telegram.org and check whether a third-party `api_id` can upload FCM/APNs credentials. Nobody could verify it from outside. It decides the whole notification design.

## Next steps, in order

1. Look at the release build on the iPhone: the country picker (page sheet, glass search at the bottom, the A–Я index), `GlassIconButton`, `UISwitch` and `ProminentButton`'s glass. Then a PR for KTG-7.
2. A small account menu in the chat list (add, switch, log out), so multi-account can be tried by hand.
3. Try it with a real account, and get D3.
4. Finish M1: avatars (`FileStore`, `FileDownloads`, Coil fetcher, §7 of [messaging-core.md](design/messaging-core.md)), folders, typing, scope mute settings. Its entities use the old names: `FileStore` and the like become `AccountUpdates` followers.
5. M2: `ChatHistory` (design §6).
6. Web: `index.html` has no favicon.
7. Languages: a list in settings, and a message when a switch fails ([design/localization.md](design/localization.md), "Not done yet"). Measure the pack's load on a slow Android phone (`LanguageRepository` logs it).
8. The other auth screens onto `TextField` and `CodeField`, with each `TextFieldState` in the ViewModel as on the phone screen.
9. Nucleus's `placedAsOverlay`: open a GitHub discussion (their rule for features), then a PR to `nucleus-3.0` (Windows and Linux ignore the flag for now; the old `NativeView` signature is kept as a hidden overload). The clip and the transaction taken after the layout are part of it, or a second PR; the second changes the views under the surface too, which then move with their frame, not one later.
10. The Nucleus work is pushed to the fork `github.com/xephosbot/Nucleus` (public, as every fork of a public repository is), branch `feature/native-view-overlay`, three commits over `nucleus-3.0@50f64808` (the transaction's order, the overlay, and the test that reproduces the first). A pull request to NucleusFramework/Nucleus opens from there. `xephosbot/Nucleus-private` is an earlier private copy of the same branch that GitHub doesn't count as a fork: it can be deleted. The first Mac's `~/AndroidStudioProjects/Nucleus` still has its own uncommitted `placedAsOverlay`: this branch replaces it.
11. Look at the transitions by eye on macOS. On the display's frames the native controls and Compose's picture move together; what is left to see is the veil, which dims the screen underneath but not its controls.
12. iOS: clip the overlays as on macOS and stop hiding them (`isSettledInFront`); `IosNavTransitions` needs the clip of `MacNavTransitions`. Native popups on the desktop (`NativePopupLayers`), so a sheet or a menu drawn in the scene isn't under the controls.

## What is not done, or weak

- [01-landscape.md](research/tdlib-survey/01-landscape.md) is a scripted classification, not the verified catalog that was planned. Only the Kotlin clients in report 02 were read.
- §6's table "everything else a chat screen needs" comes from Pingogram and hasn't been reviewed against the design.
- The clones (66 GB) exist only on the first Mac, in a temporary folder. `inventory.tsv` lists every repository.

## Also true

- **APK size** (2026-11-08): debug 120 MB, R8 release 94.6 MB, of which TDLib for four ABIs is 89.6 MB (already stripped) and the dex 3.9 MB. arm64-v8a alone would be about 28 MB: ABI splits or an App Bundle.
- `ios-app.xcscheme` has an uncommitted change from 2026-11-07 22:17 that drops Archive, Test and Profile from the shared scheme; it was left out of the commit.
- Builds run with `--no-daemon -Pkotlin.compiler.execution.strategy=in-process`: the developer's 16 GB Mac swaps with Android Studio and leftover daemons.
- `docs/research/`, `docs/design/sessions.md`, `accounts.md` and `messaging-core.md` use older module and class names and are kept for the reasoning.
- **Nucleus on the second Mac** (2026-11-10): `~/StudioProjects/Nucleus`, branch `feature/native-view-overlay` cut from `nucleus-3.0` at `50f64808` and pushed to the fork `xephosbot/Nucleus` (remote `xephosbot`): `placedAsOverlay` written again from issue #920 (the first Mac's isn't pushed), with the clip and a headful case (`./gradlew :decorated-window-tao:taoHeadfulTest -Dnucleus.tao.headful.filter=#920`). No Rust is needed: the release `3.0.0-dev-202611061347` on Maven Central is that commit's code (only an example's build file differs), so its jars give every native library (unzipped into `decorated-window-tao`, `decorated-window-core` and `energy-manager` under `src/main/resources/nucleus/native/`), `clang` rebuilds `libnucleus_tao_macos_native_view.dylib` with `build.sh`'s flags, and `CI=true GITHUB_REF=refs/tags/v3.0.0-dev-202611061347 ./gradlew :decorated-window-tao:publishToMavenLocal --no-configuration-cache` puts that one module over the release's in `mavenLocal()`. The working tree's catalog says `nucleus = "3.0.0-dev-202611061347"` for it (and `tdlib` alpha02); the committed `dev` and alpha03 are the first Mac's.
- **Nucleus is `dev`, a local build that isn't published** (2026-11-07): branch `feature/native-view-overlay` of `~/AndroidStudioProjects/Nucleus`, cut from `nucleus-3.0` at `50f64808`, not committed yet. It adds `NativeView(placedAsOverlay = true)` on macOS. The macOS natives come from that commit's CI artifact `natives-macos` (`gh run download 42389053280 -n natives-macos` into the clone; there is no Rust here), with `libnucleus_tao_macos_native_view.dylib` rebuilt by `clang` as its `build.sh` does. Publish with `GITHUB_REF=refs/tags/vdev CI=true ./gradlew publishAllToMavenLocal --no-configuration-cache` (the version comes only from `GITHUB_REF`; `CI` keeps the prebuilt natives), with `sdk.dir` in its `local.properties`. `settings.gradle.kts` reads `dev.nucleusframework` from `mavenLocal()` for plugins and libraries.
- **`tdlib-kmp` is `1.8.69-1.0.0-alpha03`, a local build that isn't published** (2026-11-07): branch `bugfix/td-receiver` of the local clone, cut from the tag `v1.8.69-1.0.0-alpha02` and not committed yet, `./gradlew publishToMavenLocal` there. `settings.gradle.kts` reads `io.github.xephosbot` from `mavenLocal()` first, so another machine has to build it the same way. Its API is alpha02's; inside, requests reach TDLib in the order `send` was called, an answer is decoded by its request and not in the receive loop, and `TdObject`'s serializer is built once (0.7 ms an event before). Once alpha03 is on Maven Central, drop `mavenLocal()`.
