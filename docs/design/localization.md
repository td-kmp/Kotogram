# Kotogram — strings from Telegram's language packs

Status: **design, not implemented.** Open questions at the end; the first one decides whether this can ship.

## What the reference clients do

All three keep one bundled language and take every other from Telegram's servers at run time. None imports translations at build time.

- **Telegram for macOS** (`packages/Localization`): `en.lproj/Localizable.strings` is bundled, and SwiftGen generates `strings().loginQRTitle` from it. `ru.lproj` in the repository is empty. `applyUILocalization` installs a downloaded pack as `primaryLanguage` (a custom pack's base as `secondaryLanguage`); `_NSLocalizedString` looks a key up in them in that order. How TelegramCore downloads the pack wasn't read.
- **Telegram X** (target `android_x`): `Tdlib.updateLanguageParameters` sets `language_pack_database_path`, `localization_target` and `language_pack_id` on every client. `Lang.getStringImpl` uses the Android resource name as the pack key: an in-memory cache, then `Client.execute(GetLanguagePackString(...))`, then the pack's base, then the bundled English resource. `applyLanguage` calls `SynchronizeLanguagePack` (and for the base) before it sets `language_pack_id` and switches the UI.
- **Unigram** (target `unigram`, database `LocalPath/langpack`): `LocaleService.GetString` reads an in-memory dictionary, then `Client.Execute(GetLanguagePackString)`, then the bundled `LocaleFallback`. A switch sets `language_pack_id` on every client and awaits `SynchronizeLanguagePack`.

What TDLib does itself (`td/telegram/LanguagePackManager.cpp`):

- Setting `language_pack_id` runs `inc_generation()` → `on_language_pack_version_changed(...)`. A pack missing from the database (`version == -1`) is downloaded whole (`langpack.getLangPack`) and saved; later only `langpack.getDifference` runs, when the server announces a new `language_pack_version`. Clients never ask for the whole pack themselves.
- Strings are kept in SQLite at `language_pack_database_path`, one database for every client; without the option they live in memory only and come again on each start.
- `getLanguagePackString` reads only that database (404 if absent). `getLanguagePackStrings(id, keys)` answers from it when the pack is full, otherwise asks the server: the whole pack for an empty `keys`, `langpack.getStrings` for given ones.
- `setOption`, `getLanguagePackStrings` and `synchronizeLanguagePack` are pre-authentication requests (`Td::is_preauthentication_request`): sent before `setTdlibParameters`, they wait in `pending_preauthentication_requests_`.
- Changes come as `updateLanguagePackStrings(localization_target, language_pack_id, strings)`; empty `strings` means all of them.

What the user sees there: on a cold start the pack is already in the database, so the right language shows from the first frame. A switch waits for the pack before the UI changes. Only the very first launch is English, with an offer to switch: macOS's `continueOn` on the login screen; in TDLib the server's suggestion is `suggested_language_pack_id`.

## Design (our own, after Telegram X and Unigram)

```
core:account
  Localization          the chosen pack: strings, apply(id), suggestion; one per app, not per account
  AccountManager.open   sets the three options on every new client, before its parameters
  AppSettingsStore      language_pack_id in the same DataStore as the registry
core:resources
  LanguagePack          a read interface: get(key) -> value; LocalLanguagePack, null = bundled only
  stringResource(...)   pack first, bundled second; the only entry to strings
  TelegramFormat        %@ %1$@ %d %1$d %% → arguments
shared
  App                   provides LocalLanguagePack and the layout direction; waits for the pack on start
```

### Where the pack lives

- **`core:account`**, because only it talks to `TdClient`. `Localization` is a root singleton in `accountsModule`, like `AccountManager`: the pack is the app's, every client only shares its database.
- **Options.** `AccountManager.open` sends `SetOption` for `language_pack_database_path`, `localization_target` and, if one is chosen, `language_pack_id` right after `TdClient.create`. TDLib queues them until `setTdlibParameters`. Every client gets them, as in Telegram X and Unigram.
- **Database path:** `<app data>/langpack` next to `accounts`; on the web `/tdlib/langpack`, inside the worker's IndexedDB like the accounts. Test DC gets `langpack-test`, as accounts get `accounts-test` (see question 2).
- **The chosen pack** is our own state, `language_pack_id` in the registry's DataStore. TDLib's option is per client, and the app needs the value before any client runs. Telegram X (`Settings`) and Unigram (`AppSettings`) keep it the same way. `null` means none chosen: bundled strings in the system language.
- **Loading.** `strings: StateFlow<LanguagePack?>` is filled by one `getLanguagePackStrings(id, emptyList())` through any running client: from the database when the pack is full, else downloaded. A pack with a `base_language_pack_id` is merged over its base. `tdlib-kmp`'s `TdLib.execute` is `suspend` (the web runs TDLib in a worker), so lookups during composition read this map, not TDLib.
- **Updates.** `updateLanguagePackStrings` of the chosen pack patches the map; with empty `strings` it reloads. Every client may deliver it; applying it twice changes nothing.
- **Switching** (`apply(id)`), as Telegram X: `synchronizeLanguagePack(id)` and its base, `language_pack_id` on every client, save, reload the map. Compose recomposes every string at once.
- **Suggestion.** `suggested_language_pack_id` (an `updateOption` of any client) and its `LanguagePackInfo` are exposed for a later "Continue in …" on the login screen.

### Reading a string

- **Keys.** A resource name is the pack key with `.` written as `__`: `Login.QR.Title` → `Login__QR__Title`. macOS's 9356 keys never contain `__`, so the mapping goes both ways; besides 59 Interface Builder ids (`1Xt-HY-uBw.title`), every key is letters, digits, `.` and `_`. Strings Telegram doesn't have keep ordinary names and simply never match. The bundled English is macOS's own text.
- **`stringResource(Res.string.x, vararg args)`** in `core:resources` reads `LocalLanguagePack.current?.get(key)`, else CMP's `stringResource`. CMP itself hides a `suspend` loader the same way (`rememberResourceState`: `runBlocking` on Android, JVM and iOS; a default first and the value later on the web); `runBlocking` is out of common code here, so the map is the synchronous part. The `StringResource` from AnilibriaRefresh resolves through it too.
- **One entry.** A detekt `ForbiddenImport` keeps `org.jetbrains.compose.resources.stringResource` and `getString` inside `core:resources`.
- **Formatting.** macOS strings use `%1$@` (945), `%@` (844), `%d` (367), `%1$d` (69) and `%%` (1). CMP formats only `%1$s`/`%1$d`, and internally, so `TelegramFormat` replaces them, numbering bare `%@`/`%d` in order.
- **Bold.** 352 strings carry `**…**` (macOS: `detectBoldColorInString`). The plain `stringResource` drops the markers; `annotatedStringResource` makes them bold.

### Start-up

`Localization` loads the chosen pack before the first frame: offline from the database, so quickly (not measured). Android's `isStarting()` waits for it as it waits for TDLib's state; desktop and web show the blank start screen meanwhile. With no pack chosen nothing waits. Layout direction follows `LanguagePackInfo.is_rtl`.

## Not in the first step

- A language list and settings screen (`getLocalizationTargetInfo`).
- Plurals: TDLib gives `languagePackStringValuePluralized` with its forms and `plural_code` names the rule, but CMP's CLDR rules aren't public API (unchecked), so a rule table of our own is needed.
- Custom and beta packs (`addCustomServerLanguagePack`).

## Tests

`TelegramFormat` and the key mapping are plain functions. `Localization` runs on the `FakeTdLib` of `AccountManagerTest`: options reach every client, a switch syncs before it applies, an update patches the map.

## Open questions

1. **Whose pack.** Our texts are macOS's, so the natural target is `macos`. Telegram X and Unigram use targets of their own; whether a third-party client may read an official app's pack wasn't checked. Without an answer the design waits.
2. **Test DC.** Whether the test servers serve language packs at all wasn't checked; hence a database of its own.
3. **Pack size.** macOS's English source is 1.2 MB with 9356 keys; the Russian pack and its database weren't measured.
