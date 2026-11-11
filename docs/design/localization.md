# Kotogram — strings from Telegram's language packs

Status: **English bundled; "Continue in …", switching and updates as macOS and iOS; the whole pack in memory; Android and iOS declare the pack's languages, and a language chosen for the app in the system's settings is followed** (2026-11-11). A language list in settings is not done. Open questions at the end.

## What the reference clients do

All three keep one bundled language and take every other from Telegram's servers at run time. None imports translations at build time.

- **Telegram for macOS** (`packages/Localization`, `packages/AuthUI/.../AuthController.swift`): `en.lproj/Localizable.strings` is bundled, and SwiftGen generates `strings().loginQRTitle` from it. `ru.lproj` in the repository is empty. `applyUILocalization` installs a downloaded pack as `primaryLanguage` (a custom pack's base as `secondaryLanguage`); `_NSLocalizedString` looks a key up in them in that order. `currentlySuggestedLocalization(extractKeys: ["Login.ContinueOnLanguage"])` gives the server's suggestion with that one string in its language; the button (centred at the bottom of the login window) shows only while no other account is signed in and the suggestion isn't the language in use. A tap runs `downloadAndApplyLocalization` under a modal progress. After login a modal (`SuggestionLocalizationViewController`) offers the system language once more if it isn't English.
- **Telegram for iOS** (`AuthorizationSequenceSplashController.swift`, `Localizations.swift`): the same. `help.getConfig` gives `suggested_lang_code`; `langpack.getStrings` gives `Login.ContinueWithLocalization` in that language; the intro shows it under "Start Messaging" unless it is English. A tap downloads the pack and its base, stores `LocalizationSettings` for the whole app (not per account) and puts the code into the connection's `lang_code`. The app's 20 `.lproj` folders hold only `InfoPlist.strings` (permission prompts) and Siri vocabulary: iOS's per-app language setting changes those, never the UI, which follows the chosen pack alone.
- **Telegram X** (target `android_x`): `Tdlib.updateLanguageParameters` sets `language_pack_database_path`, `localization_target` and `language_pack_id` on every client. `Lang.getStringImpl` uses the Android resource name as the pack key: an in-memory cache, then `Client.execute(GetLanguagePackString(...))`, then the pack's base, then the bundled English resource. `applyLanguage` calls `SynchronizeLanguagePack` (and for the base) before it sets `language_pack_id` and switches the UI.
- **Unigram** (target `unigram`, database `LocalPath/langpack`): `LocaleService.GetString` reads an in-memory dictionary, then `Client.Execute(GetLanguagePackString)`, then the bundled `LocaleFallback`. A switch sets `language_pack_id` on every client and awaits `SynchronizeLanguagePack`.

### How they declare languages to the platform (checked 2026-11-11, their `master`)

None of them ties the platform's per-app language to its pack; that tie is our design, on the platforms' documented APIs.

- **Telegram for Android** has no `android:localeConfig`, so Android's settings offer no language for it. It keeps its language in its own `SharedPreferences` (`LocaleController`, key `language`) and without one takes the system's. Its resources hold `ar`, `de`, `es`, `it`, `ko`, `nl`, `pt-rBR`, `ru`, `uk` beside English.
- **Telegram X** has none either. Its release build fetches a few built-in strings (the intro, the network status, login errors: `builtin-strings.txt`) for every language of `android_x` from translations.telegram.org into `values-xx` and sets `localeFilters` to them (`FetchLocalizedStringsTask`).
- **Telegram for iOS** has 19 `.lproj` folders with only `InfoPlist.strings` and Siri's vocabulary, `CFBundleDevelopmentRegion = en` and `CFBundleAllowMixedLocalizations`. That gives the App Store its list of languages and iOS its language row for the app; the interface ignores the row and follows the chosen pack.
- **The platforms.** Android 13 and later list an app's languages from `android:localeConfig` ([per-app language preferences](https://developer.android.com/guide/topics/resources/app-languages)); below 13 there is no system setting, and `AppCompatDelegate.setApplicationLocales` with `autoStoreLocales` keeps a choice, in an `AppCompatActivity` only. Android 14 can replace the list at run time (`LocaleManager.setOverrideLocaleConfig`). Google Play shows no list of an app's languages. The App Store takes its list from the bundle's `.lproj` folders, and iOS shows the language row for an app with more than one ([Apple's forum](https://developer.apple.com/forums/thread/836440), [QA1828](https://developer.apple.com/library/archive/qa/qa1828/_index.html)); only the user changes it, in Settings, and iOS restarts the app after it.
- **The `macos` pack's languages** ([translations.telegram.org](https://translations.telegram.org/languages/list/macos)): `en`, `be`, `ca`, `zh-hans`, `zh-hant`, `hr`, `cs`, `nl`, `fi`, `fr`, `de`, `he`, `it`, `kk`, `ko`, `ms`, `nb`, `pl`, `pt-br`, `ro`, `ru`, `sr`, `es`, `tr`, `uk`. `ios` and `android` have 33.

## What TDLib does (`td/telegram/LanguagePackManager.cpp`, `Td.cpp`, checked at c15d3f5)

- **Options before `setTdlibParameters`** wait in `pending_preauthentication_requests_`. Once the database is open, TDLib runs the queued `setOption` and `getOption` first, then creates its managers, then runs every other queued request (`Td.cpp:865-875`, `:957`). `LanguagePackManager` reads `language_pack_database_path` only in its constructor (`:208`), so a path sent later is ignored until the next start. Requests sent after `setTdlibParameters` and before its answer wait in `pending_set_parameters_requests_` and run after the managers exist (`Td.cpp:301`).
- **Setting `language_pack_id`** makes TDLib download a pack it doesn't have and later apply `langpack.getDifference` when the server announces a new version.
- **One database for every instance:** the strings live in SQLite at `language_pack_database_path`, and the in-memory copy is shared by path (`add_language_database`). `getDifference` is deduplicated on that shared object (`:423-430`), and `updateLanguagePackStrings` comes only from the instance that applied it (`:1473`): **once a process, not once a client**. It carries the changed strings; none means all of them, as after a full download of the current pack. It comes for the base pack too.
- **`getLanguagePackStrings(id, [])`** answers from the database when the pack is whole there, and otherwise downloads the whole pack and keeps it as whole (`:1109-1135`). `td_api.tl` says `synchronizeLanguagePack` isn't needed for the pack in use. An untranslated key is missing from the pack or marked `languagePackStringValueDeleted` (`tr` translates 5646 of 9060 keys, `de` 7766); either way the base or the bundle answers.
- **A pack's info** (`plural_code`, `is_rtl`, `base_language_pack_id`) is kept only for packs `getLocalizationTargetInfo(only_local = false)` listed (`!server2`, `save_server_language_pack_infos`). `getLanguagePackInfo` updates only entries already there (`on_get_language`, `:1060-1085`), so the app keeps the chosen pack's info itself.
- **Plural forms:** TDLib gives all six; the client picks one by CLDR for `plural_code` (`td_api.tl`).
- **The suggestion:** before authorization the server answers the `system_language_code` with `suggested_language_pack_id`. Checked on 2026-11-06: `ru`, `ru-RU` → `ru`; `de` → `de`; `pt-BR` → `pt-br`; `zh-Hant-TW` → `zh-hans`; `uz-UZ` → `en`, no pack.

## What the user sees (our design, as macOS and iOS)

1. **The app starts in English or in the language it has stored,** on every platform, as the reference clients do: only English is bundled, and nothing waits for the network behind the splash. Layout is left to right unless the chosen pack is RTL.
2. **The first sign-in offers the system language:** when the server's suggestion isn't the language in use and no account is signed in, one string of that pack, `getLanguagePackStrings(id, ["Login.ContinueOnLanguage"])` ("Продолжить на русском"), is a link on the first login screen; it goes for good at the first change of step, as macOS's `AuthView.hideLanguage()`. On a laptop it lies over the screen at the bottom, 15 from the edge, macOS's place. On touch screens it is a part of the screen, under its action (decided on 2026-11-11 over [the prototype](language-suggestion-prototype.html)): Telegram for iOS and Android show it under "Start Messaging" on their intro (`RMIntroViewController`, `IntroActivity` in portrait), a screen we don't have, and an overlay there met the screen's own action. The phone and QR screens take a `footer` slot for it under their action, at least as tall as their bottom inset; the link's row is its 50 with 8 under it, grows and fades in when the string arrives, and the action moves up by the difference. The link has no ripple (`TextButtonStyle { ripple(false) }`): Android's is a bare `TextView`.
   - **Android and iOS also declare the `macos` pack's 25 languages** (decided 2026-11-11): `res/xml/locales_config.xml` on Android, a `.lproj` folder each on iOS (with the app's name in `InfoPlist.strings`, as Telegram for iOS's hold only that kind of file). So the system's settings offer a language for the app and the App Store lists them.
   - **A language the user chooses there is followed** (own): the pack downloads behind what is on screen and replaces it when it is here, on Android 13 and later in the running app with no restart of the activity (`configChanges` has `locale`), on iOS after the restart iOS does. On Android without such a choice the system's own language changes nothing: it is only offered by the link. On iOS a change of the language iOS gives the app is followed, whether made in the app's row or for the whole device, since iOS doesn't tell them apart; the language at the first start is only offered by the link.
   - **A switch made in the app is told to Android 13 and later** (`LocaleManager.setApplicationLocales`), so the system's setting shows the same language. iOS lets only the user set it ([WWDC19 403](https://developer.apple.com/videos/play/wwdc2019/403/): an app that needs its own switch should open Settings), so there the row and the app may differ until the user changes the row.
   - **Tried and dropped the same day:** taking the system's language at the first start and holding the splash for its pack. On an emulator the pack came 5.8 to 6.3 s after the process started; the developer: reading a cache behind the splash is one thing, the network another. Bundling the sign-in screens' strings for every language (Telegram X's built-in strings; translations.telegram.org exports the `macos` pack as `.strings`) would show the system's language at once, and was put off as too much for now.
3. **A tap switches everything at once:** `getLanguagePackInfo`, then the whole pack and its base; then the choice is stored, every instance gets `language_pack_id`, and every screen changes. The button shows progress meanwhile, and stays if the switch fails.
4. **Later starts** show the splash until the stored pack is read from TDLib's database, so the first screen is already in its language. After 3 s without it the bundled English shows, and the pack follows (our choice).
5. **English again** clears `language_pack_id`: TDLib then downloads nothing, and the connection still says `en`.

## Design (ours, after Telegram X, Unigram, macOS and iOS)

`core:account` owns no subsystem: it gives every feature a few extension points, and the language is a feature. Compose reads the strings through a port in `core:resources`.

```
core:resources        Compose's side; doesn't depend on core:account
  Localization        @Immutable port: language (LanguagePackInfo, null for English), string(key)
  BundledLocalization English from the bundle; LocalLocalization's default, so previews need nothing
  LocalizationSource  StateFlow<Localization?>: null until the chosen language is ready
  ProvideLocalization LocalLocalization, LocalLayoutDirection and Compose's environment pinned to en
  StringResource      @Immutable sealed: String, Text, Plural (a PluralStringResource)
  stringResource, pluralStringResource, annotatedStringResource; Localization.getString outside composition
  ComposeResourcesInternals.kt   the one file that reaches into Compose resources' internals
  generateLanguagePackKeys       LanguagePackKeys: resource name -> pack key
core:account          extension points; nothing about languages
  TdlibOptions        Flow<Map<String, OptionValue>>: the first ones before setTdlibParameters, then each change
  AccountUpdates      followers of an account's updates
  AppClient           the app's requests through a live instance, a signed-in account's first
  AppStorage          tdlibDirectory (apart for the test DC) and the app's one DataStore
  AppCoroutineScope   the qualifier of the app's CoroutineScope
feature:language
  domain  LanguagePack(info, strings, baseStrings); LanguageState: Loading, Bundled, Loaded(pack)
          LanguageRepository: state, apply(id): Either; LanguageSuggestionRepository: suggestion (per account)
  data    DefaultLanguageRepository (single); ChosenLanguageStore (the chosen LanguagePackInfo as TDLib's JSON)
          PlatformLanguage: a language chosen in the system as a pack id, and sync after a switch;
          AndroidPlatformLanguage (LocaleManager, Android 13 and later), IosPlatformLanguage (the bundle's
          preferred localization); none below Android 13, on the JVM and the web
          per account: LanguageOptions : TdlibOptions, LanguagePackUpdates and SuggestedLanguagePack : AccountUpdates,
          DefaultLanguageSuggestionRepository
  impl    PackLocalization (the pack, then its base), DefaultLocalizationSource
feature:auth:impl     LanguageSuggestion, a component with its own ViewModel and store, over the flow's first step only
shared                App: the splash until the localization is ready, then ProvideLocalization; isStarting() waits too
```

| Where | Pattern | Backed by |
|---|---|---|
| `TdlibOptions`, `AccountUpdates`, `AppClient`, `AppStorage` | plug-ins bound in Koin, open–closed | our design; TDLib's queues checked above |
| `Localization`, `LocalizationSource` | ports and adapters | `LocalConfiguration`; AnilibriaRefresh's `ProvideAppLocale` |
| a snapshot in `staticCompositionLocalOf` | immutable value | `LocalConfiguration` |
| pack, then base, then bundle | lookup chain | Telegram X's `Lang.getStringImpl`, macOS's `primaryLanguage`/`secondaryLanguage` |
| `apply` in `appScope` | work that shouldn't be cancelled | Android's "Coroutines & patterns for work that shouldn't be cancelled" |
| `ComposeResourcesInternals.kt` | one place for internal API | — |
| `Localization` over its source | as Compose's fonts (`FontFamily.Resolver`, `FontLoadingStrategy`, `AndroidFont.TypefaceLoader` under Google Fonts), but one gate for the whole UI where fonts reflow each text | ui-text 1.12.1: CMP's `SkiaFontLoader` has no `Async` path, so remote fonts there are Compose resources' `Font(Res.font…)` and `preloadFont` |

### How it works

- **Options.** `LanguageOptions` gives `localization_target` = `macos`, `language_pack_database_path` = `<tdlibDirectory>/langpack` and `language_pack_id`, the stored choice (`OptionValueEmpty` for English: TDLib keeps the last one in its binlog). `AccountManager.setUp` sends every `TdlibOptions`' first options without waiting for their answers, then `setTdlibParameters`, then each change; `TdClient.send` keeps the order of its calls (tdlib-kmp alpha03). Checked on 2026-11-07 on the desktop: TDLib received `setOption` ×3, then `setTdlibParameters`.
- **Start.** `DefaultLanguageRepository` reads the stored info; without one it is `Bundled` at once. With one it releases `language_pack_id` first (every instance waits for it to start) and then asks `AppClient` for the whole pack and its base. That request waits in TDLib's queue and runs right after the managers exist.
- **Switch.** `apply` runs in `appScope` under the repository's mutex: `getLanguagePackInfo` (its id may differ from the one asked for), the pack and its base, the store, then the state; `LanguageOptions` sends the new `language_pack_id` to every instance. `apply("en")` clears the choice.
- **Where the choice lives.** `ChosenLanguageStore` is the app's one store of it, on every platform, as Telegram X keeps the chosen pack in its own settings (`Settings.KEY_LANGUAGE_CURRENT`: id, base, plural code, RTL) and sends `language_pack_id` to every client (`Tdlib.updateLanguageParameters`). TDLib can't be that store: `language_pack_id` lives in each instance's options (`OptionManager`'s `option_pmc_`), one per account, and a new sign-in starts on an empty database; it keeps a pack's info only after `getLocalizationTargetInfo(only_local = false)` (`!server2`), not after `getLanguagePackInfo` (`on_get_language` updates only listed entries).
- **The system's choice** (`PlatformLanguage`, Android 13 and later and iOS). It gives the pack of a language chosen for the app in the system's settings, `null` without one. After the start (from the store, as everywhere) the repository switches to each value that isn't the language in use, the way a tap does but with nobody waiting: the state changes when the pack is here. After a switch, by `apply` or by following, it calls `sync`.
  - **Android** (`AndroidPlatformLanguage`, [per-app language preferences](https://developer.android.com/guide/topics/resources/app-languages), framework API): `LocaleManager.getApplicationLocales`, matched to the declared languages (`LocaleConfig`) with `LocaleList.matchesLanguageAndScript` (`zh-HK` is `zh-Hant`, `pt-PT` is `pt-BR`); an empty list is no choice. Read again on every configuration change (`ComponentCallbacks` as a `callbackFlow`). `sync` is `LocaleManager.setApplicationLocales` unless the system already has that language. Below Android 13 there is no system setting, so there is nothing to follow or tell, and the store keeps the choice. AppCompat was dropped on 2026-11-11: below 13 it only kept a second copy of the choice (`autoStoreLocales`, a blocking read on the main thread), needed an `AppCompatActivity`, its theme and a workaround for reading its store before an activity attached. What goes with it: below 13 Material 3's own strings from Android resources and Compose's text locale stay in the system's language, and after an update from Android 12 the system shows no app language until the next switch (AppCompat's one-time handoff, `syncRequestedAndStoredLocales`).
  - **iOS** (`IosPlatformLanguage`): `NSBundle.mainBundle.preferredLocalizations`, the language iOS picked for the app among its `.lproj` folders, with Apple's matching; read once, since iOS restarts the app when it changes. iOS can't be told a switch made in the app, so following every start would undo it: the adapter follows only a localization that differs from the one at the last `sync`, kept in the app's `NSUserDefaults` (own); the first start only records it. A switch that fails isn't synced and is tried again at the next start. Reading `AppleLanguages` from the app's own defaults domain, the first take, relied on where iOS happens to keep a per-app choice, and was dropped.
  - **A pack's id** is the declared tag in lower case (`zh-Hans` is `zh-hans`), which holds for the 25 languages of `macos`.
- **Updates.** `LanguagePackUpdates` passes each `updateLanguagePackStrings` of the chosen pack or its base to the repository, under the same mutex: changed strings replace theirs, deleted ones go, none reads the pack again from the database. Every account listens, since any instance may be the one that sends it.
- **The button.** `SuggestedLanguagePack` follows `suggested_language_pack_id` of its instance; `DefaultLanguageSuggestionRepository` drops it while it is the stored choice and fetches its one string through that account's client.

### Measured on Android (2026-11-11, an Android 37 emulator on an M-series Mac, debug build)

The app's language set with `cmd locale set-app-locales`. Switching in the running app: `ru` in 0.34 s from TDLib's database, no restart of the process, and back to English at once. A restart with `ru` stored: the pack 2.7 s into the request, which waits for TDLib to start. A first download: the pack 5.8 to 6.3 s after the process started (1.6 to 2.4 s of it the download). A real phone is not measured.

### Measured (2026-11-07, desktop, M-series Mac)

`ru` the first time, downloaded by TDLib: 1.93 s for 9858 strings; from the database, 54 ms. A tap on "Weiter auf Deutsch" (`de-DE` system): 1.25 s for 7757 strings. Both are followed by TDLib's "all strings" update, and the pack is read once more from the database (54 ms, 32 ms). A slow Android phone is not measured yet.

### Keys

- **Resource names are snake_case**: the pack key lowercased with `.` as `_`, `Login.QR.Title` → `login_qr_title`. No two of the pack's keys meet under the rule.
- **The pack key can't be read back** from a snake_case name, and TDLib's keys are case-sensitive, so `strings.xml` keeps it beside the name: `<string name="login_qr_title" key="Login.QR.Title">`. Compose reads only `name` (`PrepareComposeResources.kt` in CMP 1.12.1). `generateLanguagePackKeys` writes `LanguagePackKeys` from it (plurals included) in files of 500, as CMP splits `Res` (`GeneratedResClassSpec.kt`, `MethodTooLargeException`). An entry without `key` is ours and is never looked up.
- **English only is bundled**: the whole `macos` pack as the server gives it. Screens use its strings; the few without a counterpart (`app_name`, `unsupported_title`, `unsupported_subtitle`, `chat_history_placeholder`) are ours. `voiceover_login_qrcode` keeps macOS's key `VoiceOver.Login.QRCode`, which the server doesn't have yet.
- **Plurals are `<plurals>`** with the English forms the pack has (305 with `one` and `other`, 44 with `other` only, 1 empty). Compose picks a form by the environment's language (`PluralRuleList.getInstance(environment.language, …)`), so `ProvideLocalization` pins that environment to `en`: an English string on a Russian device would read 21 as "one" otherwise. A downloaded pack's form is picked by its `plural_code` with Compose's own CLDR rules (`PluralRuleList`, `cldrPluralRuleLists`). Both are internal and stay in `ComposeResourcesInternals.kt` (`@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")`, as AnilibriaRefresh's `ProvideAppLocale`). Kotlin warns that this is unspecified, and a `when` over Compose's `PluralCategory` compiles to a missing class; a CMP update that renames them fails the build, and then the rules have to be copied instead.
- **The snapshot** is from 2026-11-06: `getLanguagePackStrings(language_pack_id = "en", keys = [])` with `localization_target = "macos"` on a fresh unauthorised TDLib. 9051 strings came, 350 of them plural; the 54 Interface Builder ids (`Vdr-fp-XzO.title`) are left out. The server's keys differ from the macOS repository's `en.lproj` (736 only on the server, 945 only in the repository): the pack is what TDLib will give at run time.

### Reading a string in Compose

- `stringResource`, `pluralStringResource` and `annotatedStringResource` in `core:resources` are the only entries: detekt's `ForbiddenImport` keeps Compose's `stringResource`, `pluralStringResource`, `getString` and `getPluralString` inside `core:resources`.
- They look the key up in `LocalLocalization` by `LanguagePackKeys[res.key]`, a plain map read, and fall back to Compose's own string of the same name. Nothing suspends and nothing collects a Flow per string: a new pack is a new `Localization`, and the whole tree recomposes once.
- **Markup**, as macOS's `parseMarkdownIntoAttributedString` (TGUIKit `Markdown.swift`): only `**bold**` (374 strings) and `[text](url)` (199, 93 of them with an empty url, an action of the screen such as `· [Edit]()`). The template is parsed first and the arguments go in after, so an argument's own text is never markup. A url with `://` becomes `LinkAnnotation.Url`, any other `LinkAnnotation.Clickable` with the url as its tag, for `onLinkClick`. `stringResource` keeps only the text.
- **Arguments:** `%@` (809), `%1$@` (516), `%d` (385), `%2$@` (297) and fewer of the rest; bare ones are numbered in order across the string, links included. 32 strings use `{name}` placeholders, which nothing fills yet.

### Dates

`relativeDateString(epochSeconds)` in `core:resources` writes a chat's date as Telegram for iOS's `stringForRelativeTimestamp` does: the time today, the short weekday for the six days before (and later this year), day and month this year, day, month and two-digit year before. The rule is common code (kotlinx-datetime); the words and order are the platform's own, from CLDR skeletons (`Hm`/`hm`/`jm`, `E`, `Md`, `yyMd`): ICU's `DateFormat.getInstanceForSkeleton` on Android (API 24), `NSDateFormatter.setLocalizedDateFormatFromTemplate` on iOS, `DateTimeFormatter.ofLocalizedPattern` on the JVM (it resolves only CLDR's own skeletons, so the year's pattern is `yMd` cut to two digits), `Intl.DateTimeFormat` on the web. The language is the app's (the language pack's `pluralCode`, English while it's bundled), the region the system's, so a British English system writes 13/10 and an American one 10/13; the hour cycle is the system's on Android (`DateFormat.is24HourFormat`), iOS (`j` in the current locale) and the web (`hourCycle`), the language's on the JVM. Weekdays get a capital first letter, as Telegram's own strings have it (CLDR's Russian is "пн"). Chat rows keep TDLib's `date` and format it while composing, so a change of language or hour cycle shows at once. `DateFormatsTest` checks Russian, American and British English on the JVM.

## Not done yet

- A language list in settings (`getLocalizationTargetInfo`, with `translated_string_count`), and macOS's after-login offer.
- Android 14's `setOverrideLocaleConfig` with the server's list of packs: the declared list is the pack's languages as of 2026-11-11, and a new one needs a release.
- Strings of the sign-in screens bundled per language, as Telegram X's built-in strings, so that the first start is in the system's language at once (put off on 2026-11-11).
- `UIPrefersShowingLanguageSettings` on iOS, which shows the language row even with one preferred language.
- A message when a switch fails: the button only comes back.
- Links `t.me/setlanguage/…` (`internalLinkTypeLanguagePack`): custom packs, which TDLib leaves out of the connection's `lang_code`.
- The pack's language in `TextStyle.localeList` (CJK glyphs) and in date and number formatting.
- Fonts on the web (unchecked): the project has none, and Compose on the web has no system fonts, so Arabic, CJK and Devanagari may not render there.

## Tests

`PluralFormTest`, `LocalizationTest` and `TelegramTextTest` (`core:resources`): CLDR forms for `en`, `ru`, `ar`, `pt`, the pack-key lookup, markup and arguments. `DefaultLanguageRepositoryTest` (`feature:language:data`, a fake `AppClient`): start, a slow TDLib, switch, failure, English, updates, and a fake platform (no choice in the system, a choice loading behind the bundled English, one already stored, a change, a choice taken back, a language without a pack, a followed or applied switch synced and a failed one not). `LanguagePacksTest`: a tag's pack id, and that `locales_config.xml`, the `.lproj` folders and the Xcode project's regions list the same languages. The matching is the platforms' own and runs only on a device. `LocalizationTest` (`feature:language:impl`): pack, base and bundle. `AccountManagerTest`: options before the parameters, and only changed options sent again. `AuthViewModelTest`: the button on the first sign-in only. `KoinGraphTest` (real TDLib): `language_pack_database_path` applied and the bundled localization ready.

## Open questions

1. **Whose pack.** Decided 2026-11-06: `macos`, our texts being macOS's. What was checked:
   - The [API Terms of Service](https://core.telegram.org/api/terms) say nothing about language packs; they cover privacy, the name ("Telegram" only after "Unofficial"), the logo, advertising and AI training.
   - [`langpack.getLangPack`](https://core.telegram.org/method/langpack.getLangPack) describes `lang_pack` as a "platform identifier (i.e. android, tdesktop, etc)" and works without authorization; TDLib's `localization_target` lists the official targets as examples, with no restriction.
   - Unofficial clients do it: Nagram (GPL-3.0, a fork of Telegram for Android) sends `lang_pack = "android"` in its connection (`TMessagesProj/jni/tgnet/ConnectionsManager.cpp`); Nekogram has the same file, not opened. Both are forks, so they inherited it.
   - Own targets (`android_x`, `unigram`) belong to apps Telegram works with; no way for a third party to get one was found.

   So nothing forbids it and nothing allows it. The risk is that keys are the macOS app's: a removed key falls back to our bundled text, a reworded one shows its new text.
2. **Test DC.** Whether the test servers serve language packs at all wasn't checked; hence a database of its own.
3. **Pack size.** The English pack is 1.48 MB as TDLib's JSON, 1.1 MB in `strings.xml`, 1.2 MB as TDLib's database; other languages weren't measured.
