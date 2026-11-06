# Kotogram — strings from Telegram's language packs

Status: **first step implemented** (the QR screen's strings); the language list, switching and plurals are not. Open questions at the end.

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
core:resources
  langpack/macos-keys.txt   macOS's 9260 keys, one per line (licensing.md)
  generateLanguagePackKeys  Gradle: LanguagePackKeys, resource name -> pack key, for our names only
  LanguagePack              revision + suspend string(key); LocalLanguagePack, null = bundled only
  stringResource(res, args) the pack's string for res.key, else the bundled one of the same name
  formatTelegram            %1$@ %@ %1$d %d %% and ** of macOS's strings
core:account
  Localization              one per app: getLanguagePackString through TdLib.execute, a cache, a revision
  LanguagePackOptions       an AccountUpdates follower: the three options, updateLanguagePackStrings
shared
  App                       provides LocalLanguagePack from Localization
```

### Keys

- **Resource names are snake_case**: the macOS key lowercased with `.` as `_`, `Login.QR.Title` → `login_qr_title`. The rule maps all 9260 macOS keys apart (no two meet); the 59 Interface Builder ids (`1Xt-HY-uBw.title`) are left out of the list.
- **The pack key can't be read back** from a snake_case name, and TDLib's keys are case-sensitive, so `generateLanguagePackKeys` writes `LanguagePackKeys` from `langpack/macos-keys.txt` and the names in `values/strings.xml`. A name with no macOS key is ours and is never looked up.
- **The bundled English is macOS's own text**; other bundled languages (`values-ru`) are what shows before the pack has arrived.
- New macOS keys: rebuild `macos-keys.txt` from telegram-macos's `Telegram-Mac/en.lproj/Localizable.strings` (UTF-16).

### Where the pack lives

- **`core:account`**, the only module that talks to TDLib. `Localization` is a root singleton in `accountsModule`, like `AccountManager`: the pack belongs to the app, every client only shares its database.
- **Options**, as Telegram X and Unigram: `LanguagePackOptions` gives every account's client `language_pack_database_path`, `localization_target` = `macos` and `language_pack_id`. It waits until the client is past `authorizationStateWaitTdlibParameters`: the first request starts an instance, and that stays `AccountManager`'s. TDLib then downloads the whole pack once and keeps it.
- **Database:** `AccountStorage.languagePackDatabase`, `<accounts root>/langpack`; the root is `accounts-test` on the test DC, and `/tdlib/…` inside the worker's IndexedDB on the web.
- **Language:** the system language (`TdlibConfig.systemLanguageCode`) for now. A chosen one needs a store of our own once there is a screen to choose it (Telegram X keeps it in `Settings`, Unigram in `AppSettings`).
- **Reading:** `Localization.string(key)` runs `getLanguagePackString` through `TdLib.execute`, offline from the database, and caches the answer; null until the pack has it.
- **Updates:** `updateLanguagePackStrings` of our target and language empties the cache and raises `revision`. TDLib sends it after the first full download too (`is_diff || (new_is_full && is_first)`).

### Reading a string in Compose

- `stringResource(Res.string.x, vararg args)` in `core:resources` is the only entry: a detekt `ForbiddenImport` keeps Compose's own inside `core:resources`.
- It reads the pack through `LocalLanguagePack` by `LanguagePackKeys[res.key]`, and falls back to Compose's string of the same name.
- **Waiting, as Compose does for its own strings** (`rememberResourceState` in components-resources 1.12.1): on Android, JVM and iOS `runBlocking` in the first composition, since TDLib runs in the process and reads its database at once; on the web the bundled string first and the pack's when the worker answers.
- **Formatting:** macOS strings use `%1$@` (945), `%@` (844), `%d` (367), `%1$d` (69) and `%%` (1); `formatTelegram` numbers bare ones in order and drops `**` (352 strings bold something; an annotated variant is for later).

## Not in the first step

- A language list, switching (`synchronizeLanguagePack` before `language_pack_id`, as Telegram X) and the "Continue in …" suggestion (`suggested_language_pack_id`).
- Plurals: TDLib gives `languagePackStringValuePluralized`, but the forms need a CLDR rule by `plural_code`, and Compose's aren't public (unchecked).
- Bold and links in pack strings; layout direction by `LanguagePackInfo.is_rtl`.
- The rest of the login screens: their strings get macOS's keys when the screens are redrawn after macOS.

## Tests

`TelegramFormatTest` (core:resources). `AccountManagerTest` runs without the follower; `KoinGraphTest` resolves it on real TDLib. Nothing has run against the servers yet: whether the strings arrive and replace the bundled ones is to be seen by hand.

## Open questions

1. **Whose pack.** Decided 2026-11-06: `macos`, our texts being macOS's. What was checked:
   - The [API Terms of Service](https://core.telegram.org/api/terms) say nothing about language packs; they cover privacy, the name ("Telegram" only after "Unofficial"), the logo, advertising and AI training.
   - [`langpack.getLangPack`](https://core.telegram.org/method/langpack.getLangPack) describes `lang_pack` as a "platform identifier (i.e. android, tdesktop, etc)" and works without authorization; TDLib's `localization_target` lists the official targets as examples, with no restriction.
   - Unofficial clients do it: Nagram (GPL-3.0, a fork of Telegram for Android) sends `lang_pack = "android"` in its connection (`TMessagesProj/jni/tgnet/ConnectionsManager.cpp`); Nekogram has the same file, not opened. Both are forks, so they inherited it.
   - Own targets (`android_x`, `unigram`) belong to apps Telegram works with; no way for a third party to get one was found.

   So nothing forbids it and nothing allows it. The risk is that keys are the macOS app's: a removed key falls back to our bundled text, a reworded one shows its new text.
2. **Test DC.** Whether the test servers serve language packs at all wasn't checked; hence a database of its own.
3. **Pack size.** macOS's English source is 1.2 MB with 9356 keys; the Russian pack and its database weren't measured.
