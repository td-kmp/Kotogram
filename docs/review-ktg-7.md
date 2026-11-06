# KTG-7 review (2026-11-06)

`/code-review` of the branch (5 commits after `origin/refactoring/KTG-7-multiaccount-koin` plus the uncommitted language-pack step). No fixes made yet. Severity order as the review gave it; checked = confirmed by a source beyond the reviewer's reading.

## Language packs

1. **`language_pack_database_path` is set too late** (`core/account/.../LanguagePackOptions.kt:22`). TDLib applies it only after a restart: "it should be set before call to setTdlibParameters" ([options](https://core.telegram.org/tdlib/options), checked). A fresh instance (first launch, every new sign-in) keeps the pack in memory only, while `Localization` reads the file database through `execute(getLanguagePackString)`, so the QR screen stays bundled until the next start. The German run on 2026-11-06 worked because that account's instance already had the option from an earlier start. Fix: send the option before `setTdlibParameters` (it is a pre-authentication request; whether TDLib applies it at init when queued is unchecked; Unigram sends its options first, unchecked).
2. **`tdLib.execute` isn't caught** (`Localization.kt:55`). On the web a dead worker throws `TdLibUnavailableException` out of `produceState` (`LanguagePack.web.kt`) and cancels the Recomposer; the rest of `core:account` uses `sendCatching`.
3. **Cache race** (`Localization.kt:35`). A `lookUp` that read 404 before the pack was saved writes its null after `onStringsChanged` emptied the cache, so the bundled string stays until the next pack update. Widest on the web (worker round-trip).
4. **Legacy Android language codes** (`Localization.kt:24`). `Locale.getDefault().language` gives `iw`, `in`, `ji`; Telegram's packs are `he`, `id`, `yi`. Hebrew and Indonesian never load.
5. **Every account's follower forwards the update** (`Localization.kt:44`). With N accounts, `updateLanguagePackStrings` clears the cache N times, and each clear re-reads every visible string with `runBlocking` on the main thread (`LanguagePack.nonWeb.kt:13`). Possible jank while TDLib writes the pack (unchecked).

Also from the review: `formatTelegram` should live in `extensions/StringExt.kt` (CLAUDE.md layout rule).

### Discussed: bundling packs at build time

A Gradle task that downloads the packs into `values-xx` was considered and not taken. Size is fine (only our keys), but it needs MTProto and the API keys in the build, it would distribute community translations (licence unknown), no reference client does it, a snapshot drifts from the live pack, and it saves only the first ~2.5 s on a new language. Alternatives: bundle English only (drop `values-ru` later); if the first launch matters, hold the splash for `synchronizeLanguagePack` with a 1–2 s timeout (own idea), or show "Continue in …" from `suggested_language_pack_id` as macOS and Telegram X do.

## The rest of the branch

6. **Debug `if (true) darkColorScheme()`** in `KotogramTheme.kt:12` (committed with the language step on purpose, to carry on elsewhere). In a light system theme the splash and web background are light and the status bar icons dark on a dark UI.
7. **`PhoneViewModel` can hang in "submitting"** (`PhoneViewModel.kt:41`). Send a number during QR confirmation, switch to QR before the restart ends, then back to phone: the new VM takes the stale number with `isSubmitting = true` and waits for a `WaitPhoneNumber` that never comes.
8. **System splash has no timeout** (`shared/src/androidMain/.../IsStarting.kt:9`). A shown account stuck in null or `WaitTdlibParameters` (lost Keystore key, failed `setTdlibParameters`) keeps the icon forever; before there was a spinner.
9. **`retire()` / `deleteData(id)` wait for `awaitFirstScreen`** up to `REPLACE_TIMEOUT` (`AccountManager.kt:284`). Killed within those 5 s, the `root/N` directory and the Keystore key stay forever (`withoutUnfinishedSignIns` only looks at registry entries); meanwhile `shown` points at a session no longer in `sessions`.
10. **Closing account keeps its chat list** (`RootNavigation.kt:56`). LoggingOut/Closing/Closed map to null and `mapNotNull` keeps the last root, so a remotely logged-out account shows a clickable `ChatListRoute` over a dead client; before, LoggingOut showed the splash.
11. **No back handler while root is null** (`RootNavigation.kt:69`). Only `SplashScreen` composes, so `onRootBack = cancelAdding` doesn't exist; back goes to the hidden account's `NavDisplay` or closes the activity. Latent: `add()` has no caller yet.
12. **QR placeholder restarts on every return** (`QrCode.kt:128`). QR → phone → QR builds a new `QrCode` with `placeholderSince = now`, hiding the known code for ~3 s + 1.2 s crossfade, against "coming back shows the same code" in multiaccount.md.
13. **Login forms lost vertical centring** (`CodeScreen.kt:82` and 6 more screens). The old `AuthScaffold` box centred; the `Column` now sits at the top in tall desktop and web windows. `QrScreen` centres explicitly, so it looks unintended.
14. **Extension placement** (`feature/auth/api/.../AuthRoute.kt:10`). `Navigator.navigateToAuth()` moved out of `extensions/NavigatorExt.kt`; chats still keeps its own there.
15. **Duplicated form layout.** The `Scaffold` + `fillMaxSize().padding().verticalScroll().wrapContentWidth().widthIn(max = 420.dp).padding(24.dp)` chain, title/subtitle styles and error text are pasted into 7 screens; one internal composable in auth impl's `component/` would hold them (and fix 13 once).

Also: `getString(StringResource)` in `core:resources` is unused and bypasses the pack.
