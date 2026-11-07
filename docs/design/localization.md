# Kotogram — strings from Telegram's language packs

Status: **English bundled; "Continue in …", switching and updates as macOS and iOS; the whole pack in memory** (2026-11-07). A language list in settings is not done. Open questions at the end.

## What the reference clients do

All three keep one bundled language and take every other from Telegram's servers at run time. None imports translations at build time.

- **Telegram for macOS** (`packages/Localization`, `packages/AuthUI/.../AuthController.swift`): `en.lproj/Localizable.strings` is bundled, and SwiftGen generates `strings().loginQRTitle` from it. `ru.lproj` in the repository is empty. `applyUILocalization` installs a downloaded pack as `primaryLanguage` (a custom pack's base as `secondaryLanguage`); `_NSLocalizedString` looks a key up in them in that order. `currentlySuggestedLocalization(extractKeys: ["Login.ContinueOnLanguage"])` gives the server's suggestion with that one string in its language; the button (centred at the bottom of the login window) shows only while no other account is signed in and the suggestion isn't the language in use. A tap runs `downloadAndApplyLocalization` under a modal progress. After login a modal (`SuggestionLocalizationViewController`) offers the system language once more if it isn't English.
- **Telegram for iOS** (`AuthorizationSequenceSplashController.swift`, `Localizations.swift`): the same. `help.getConfig` gives `suggested_lang_code`; `langpack.getStrings` gives `Login.ContinueWithLocalization` in that language; the intro shows it under "Start Messaging" unless it is English. A tap downloads the pack and its base, stores `LocalizationSettings` for the whole app (not per account) and puts the code into the connection's `lang_code`. The app's 20 `.lproj` folders hold only `InfoPlist.strings` (permission prompts) and Siri vocabulary: iOS's per-app language setting changes those, never the UI, which follows the chosen pack alone.
- **Telegram X** (target `android_x`): `Tdlib.updateLanguageParameters` sets `language_pack_database_path`, `localization_target` and `language_pack_id` on every client. `Lang.getStringImpl` uses the Android resource name as the pack key: an in-memory cache, then `Client.execute(GetLanguagePackString(...))`, then the pack's base, then the bundled English resource. `applyLanguage` calls `SynchronizeLanguagePack` (and for the base) before it sets `language_pack_id` and switches the UI.
- **Unigram** (target `unigram`, database `LocalPath/langpack`): `LocaleService.GetString` reads an in-memory dictionary, then `Client.Execute(GetLanguagePackString)`, then the bundled `LocaleFallback`. A switch sets `language_pack_id` on every client and awaits `SynchronizeLanguagePack`.

## What TDLib does (`td/telegram/LanguagePackManager.cpp`, `Td.cpp`, checked at c15d3f5)

- **Options before `setTdlibParameters`** wait in `pending_preauthentication_requests_`. Once the database is open, TDLib runs the queued `setOption` and `getOption` first, then creates its managers, then runs every other queued request (`Td.cpp:865-875`, `:957`). `LanguagePackManager` reads `language_pack_database_path` only in its constructor (`:208`), so a path sent later is ignored until the next start. Requests sent after `setTdlibParameters` and before its answer wait in `pending_set_parameters_requests_` and run after the managers exist (`Td.cpp:301`).
- **Setting `language_pack_id`** makes TDLib download a pack it doesn't have and later apply `langpack.getDifference` when the server announces a new version.
- **One database for every instance:** the strings live in SQLite at `language_pack_database_path`, and the in-memory copy is shared by path (`add_language_database`). `getDifference` is deduplicated on that shared object (`:423-430`), and `updateLanguagePackStrings` comes only from the instance that applied it (`:1473`): **once a process, not once a client**. It carries the changed strings; none means all of them, as after a full download of the current pack. It comes for the base pack too.
- **`getLanguagePackStrings(id, [])`** answers from the database when the pack is whole there, and otherwise downloads the whole pack and keeps it as whole (`:1109-1135`). `td_api.tl` says `synchronizeLanguagePack` isn't needed for the pack in use. An untranslated key is missing from the pack or marked `languagePackStringValueDeleted` (`tr` translates 5646 of 9060 keys, `de` 7766); either way the base or the bundle answers.
- **A pack's info** (`plural_code`, `is_rtl`, `base_language_pack_id`) is kept only for packs `getLocalizationTargetInfo(only_local = false)` listed (`!server2`, `save_server_language_pack_infos`). `getLanguagePackInfo` updates only entries already there (`on_get_language`, `:1060-1085`), so the app keeps the chosen pack's info itself.
- **Plural forms:** TDLib gives all six; the client picks one by CLDR for `plural_code` (`td_api.tl`).
- **The suggestion:** before authorization the server answers the `system_language_code` with `suggested_language_pack_id`. Checked on 2026-11-06: `ru`, `ru-RU` → `ru`; `de` → `de`; `pt-BR` → `pt-br`; `zh-Hant-TW` → `zh-hans`; `uz-UZ` → `en`, no pack.

## What the user sees (our design, as macOS and iOS)

1. **The app is English to the system:** only English is bundled and declared (`developmentRegion = en` on iOS, no `localeConfig` on Android), so no system setting offers another language. Layout is left to right unless the chosen pack is RTL.
2. **The first sign-in offers the system language:** when the server's suggestion isn't the language in use and no account is signed in, one string of that pack, `getLanguagePackStrings(id, ["Login.ContinueOnLanguage"])` ("Продолжить на русском"), is a button at the bottom of the login screens.
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
          per account: LanguageOptions : TdlibOptions, LanguagePackUpdates and SuggestedLanguagePack : AccountUpdates,
          DefaultLanguageSuggestionRepository
  impl    PackLocalization (the pack, then its base), DefaultLocalizationSource
feature:auth:impl     AuthScreenState.languageSuggestion, ContinueOnLanguageButton; "first sign-in only" is AuthViewModel's
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
- **Updates.** `LanguagePackUpdates` passes each `updateLanguagePackStrings` of the chosen pack or its base to the repository, under the same mutex: changed strings replace theirs, deleted ones go, none reads the pack again from the database. Every account listens, since any instance may be the one that sends it.
- **The button.** `SuggestedLanguagePack` follows `suggested_language_pack_id` of its instance; `DefaultLanguageSuggestionRepository` drops it while it is the stored choice and fetches its one string through that account's client.

### Measured (2026-11-07, desktop, M-series Mac)

`ru` the first time, downloaded by TDLib: 1.93 s for 9858 strings; from the database, 54 ms. A tap on "Weiter auf Deutsch" (`de-DE` system): 1.25 s for 7757 strings. Both are followed by TDLib's "all strings" update, and the pack is read once more from the database (54 ms, 32 ms). A slow Android phone is not measured yet.

### Keys

- **Resource names are snake_case**: the pack key lowercased with `.` as `_`, `Login.QR.Title` → `login_qr_title`. No two of the pack's keys meet under the rule.
- **The pack key can't be read back** from a snake_case name, and TDLib's keys are case-sensitive, so `strings.xml` keeps it beside the name: `<string name="login_qr_title" key="Login.QR.Title">`. Compose reads only `name` (`PrepareComposeResources.kt` in CMP 1.12.1). `generateLanguagePackKeys` writes `LanguagePackKeys` from it (plurals included) in files of 500, as CMP splits `Res` (`GeneratedResClassSpec.kt`, `MethodTooLargeException`). An entry without `key` is ours and is never looked up.
- **English only is bundled**: the whole `macos` pack as the server gives it. Screens use its strings; the few without a counterpart (`sync_contacts`, `unsupported_title`, `unsupported_subtitle`, `chat_history_placeholder`) are ours. `voiceover_login_qrcode` keeps macOS's key `VoiceOver.Login.QRCode`, which the server doesn't have yet.
- **Plurals are `<plurals>`** with the English forms the pack has (305 with `one` and `other`, 44 with `other` only, 1 empty). Compose picks a form by the environment's language (`PluralRuleList.getInstance(environment.language, …)`), so `ProvideLocalization` pins that environment to `en`: an English string on a Russian device would read 21 as "one" otherwise. A downloaded pack's form is picked by its `plural_code` with Compose's own CLDR rules (`PluralRuleList`, `cldrPluralRuleLists`). Both are internal and stay in `ComposeResourcesInternals.kt` (`@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")`, as AnilibriaRefresh's `ProvideAppLocale`). Kotlin warns that this is unspecified, and a `when` over Compose's `PluralCategory` compiles to a missing class; a CMP update that renames them fails the build, and then the rules have to be copied instead.
- **The snapshot** is from 2026-11-06: `getLanguagePackStrings(language_pack_id = "en", keys = [])` with `localization_target = "macos"` on a fresh unauthorised TDLib. 9051 strings came, 350 of them plural; the 54 Interface Builder ids (`Vdr-fp-XzO.title`) are left out. The server's keys differ from the macOS repository's `en.lproj` (736 only on the server, 945 only in the repository): the pack is what TDLib will give at run time.

### Reading a string in Compose

- `stringResource`, `pluralStringResource` and `annotatedStringResource` in `core:resources` are the only entries: detekt's `ForbiddenImport` keeps Compose's `stringResource`, `pluralStringResource`, `getString` and `getPluralString` inside `core:resources`.
- They look the key up in `LocalLocalization` by `LanguagePackKeys[res.key]`, a plain map read, and fall back to Compose's own string of the same name. Nothing suspends and nothing collects a Flow per string: a new pack is a new `Localization`, and the whole tree recomposes once.
- **Markup**, as macOS's `parseMarkdownIntoAttributedString` (TGUIKit `Markdown.swift`): only `**bold**` (374 strings) and `[text](url)` (199, 93 of them with an empty url, an action of the screen such as `· [Edit]()`). The template is parsed first and the arguments go in after, so an argument's own text is never markup. A url with `://` becomes `LinkAnnotation.Url`, any other `LinkAnnotation.Clickable` with the url as its tag, for `onLinkClick`. `stringResource` keeps only the text.
- **Arguments:** `%@` (809), `%1$@` (516), `%d` (385), `%2$@` (297) and fewer of the rest; bare ones are numbered in order across the string, links included. 32 strings use `{name}` placeholders, which nothing fills yet.

## Not done yet

- A language list in settings (`getLocalizationTargetInfo`, with `translated_string_count`), and macOS's after-login offer.
- A message when a switch fails: the button only comes back.
- Links `t.me/setlanguage/…` (`internalLinkTypeLanguagePack`): custom packs, which TDLib leaves out of the connection's `lang_code`.
- The pack's language in `TextStyle.localeList` (CJK glyphs) and in date and number formatting.
- Fonts on the web (unchecked): the project has none, and Compose on the web has no system fonts, so Arabic, CJK and Devanagari may not render there.

## Tests

`PluralFormTest`, `LocalizationTest` and `TelegramTextTest` (`core:resources`): CLDR forms for `en`, `ru`, `ar`, `pt`, the pack-key lookup, markup and arguments. `DefaultLanguageRepositoryTest` (`feature:language:data`, a fake `AppClient`): start, a slow TDLib, switch, failure, English, updates. `LocalizationTest` (`feature:language:impl`): pack, base and bundle. `AccountManagerTest`: options before the parameters, and only changed options sent again. `AuthViewModelTest`: the button on the first sign-in only. `KoinGraphTest` (real TDLib): `language_pack_database_path` applied and the bundled localization ready.

## Open questions

1. **Whose pack.** Decided 2026-11-06: `macos`, our texts being macOS's. What was checked:
   - The [API Terms of Service](https://core.telegram.org/api/terms) say nothing about language packs; they cover privacy, the name ("Telegram" only after "Unofficial"), the logo, advertising and AI training.
   - [`langpack.getLangPack`](https://core.telegram.org/method/langpack.getLangPack) describes `lang_pack` as a "platform identifier (i.e. android, tdesktop, etc)" and works without authorization; TDLib's `localization_target` lists the official targets as examples, with no restriction.
   - Unofficial clients do it: Nagram (GPL-3.0, a fork of Telegram for Android) sends `lang_pack = "android"` in its connection (`TMessagesProj/jni/tgnet/ConnectionsManager.cpp`); Nekogram has the same file, not opened. Both are forks, so they inherited it.
   - Own targets (`android_x`, `unigram`) belong to apps Telegram works with; no way for a third party to get one was found.

   So nothing forbids it and nothing allows it. The risk is that keys are the macOS app's: a removed key falls back to our bundled text, a reworded one shows its new text.
2. **Test DC.** Whether the test servers serve language packs at all wasn't checked; hence a database of its own.
3. **Pack size.** The English pack is 1.48 MB as TDLib's JSON, 1.1 MB in `strings.xml`, 1.2 MB as TDLib's database; other languages weren't measured.
