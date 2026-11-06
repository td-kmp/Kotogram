# Kotogram — Auth flow UI/UX spec (KTG-1)

> Module names here predate KTG-6: `presentation` is now `impl`. Passkey parts are out of the code: third-party clients can't use them (see CLAUDE.md).

> **Where QR login is offered is the client's decision.** TDLib's `requestQrCodeAuthentication` checks only the authorization state and a bot token, not the platform. The server's `qr_login_code` and `qr_login_camera` can't be read through TDLib: `ConfigManager::process_app_config` drops them, so `getApplicationConfig` never returns them (checked live on 2026-11-05: 10 keys, none of these, for desktop, Android and iPhone device models). Hence `LoginFlow` from `platformLoginFlow`: `QrCodeFirst` on desktop and web, `PhoneNumberOnly` on phones, which matches the official apps, because the code has to be scanned by another device. If the server refuses, the QR screen shows the error and keeps its phone button.
>
> History: Telegram Desktop read `qr_login_code` from 2019-11 (`e6c86b19d`) and has ignored it since 2021-08 ("Always start with QR-code login", `ae40ea933`). Telegram for macOS, on its own MTProto, still shows QR unless it is `disabled`. TDLib passed the key through `getApplicationConfig` until `dc8ffea5b8` (2025-06-16, "Add more ignored config options") and `00f352a052` (2025-06-17, `qr_login_camera`); Unigram still reads it and so only gets its own defaults. On 2026-11-05 the server issued QR tokens to our `api_id` for desktop, Android and iPhone device models alike.

Status: design spec for implementation. Target: `feature/KTG-1-login`. Stack: Kotlin 2.4.20, CMP 1.12.1, `org.jetbrains.compose.material3:material3:1.12.0-alpha03`, Calf 0.14.0, Nucleus 2.5.18 (Tao backend).

Mockups: [auth-mockups.html](auth-mockups.html) — every screen per platform, dark/light (open in a browser).

Legend: **[V]** = verified against sources/binaries in this session (Gradle cache, Maven Central, GitHub tag, K/N 2.4.20 platform klib). **[U]** = unverified, check on device or in the IDE before relying on it.

Reference screenshots (read in this session):
- Desktop QR: dark `#131A22`-ish window, white rounded QR tile (~360 px, radius ~16) with the Telegram logo in the middle, title "Быстрый вход по QR-коду", 3 numbered steps with blue filled number circles, 2 blue text links. Top-right: two 36 dp round dark buttons (shield/proxy, close).
- Desktop phone: 96 dp logo, "Telegram" title, gray subtitle, one rounded card (`#232F3E`-ish, radius ~12) with a country row (flag + "Uzbekistan") and a phone row ("+998" bold + gray dash mask `-- --- -- --`), the two links below.
- iOS (iOS 26, Telegram 12.x): pure black, round glass X top-left (~44 pt), ☎️ emoji ~100 pt, "Телефон" bold ~28 pt, centered 17 pt subtitle, full-bleed hairline-separated rows: country (flag + name in **accent blue** + gray chevron), phone ("+998" | vertical divider | gray mask `00 000 00 00`) with a small notch/arrow on the divider under the flag pointing up to the country row, "Синхронизировать контакты" + green switch, bottom full-width pill "Продолжить" (disabled = `#1C1C1E` container, gray label). Passkey variant subtitle: "Введите свой номер телефона или **используйте ключ доступа ›**".

---

## 0. Key decisions (TL;DR)

1. **Three visual "skins", one stateless screen tree.** All screens live in `commonMain` and read a `KotogramPlatformStyle` (Material / Cupertino / Desktop) from a CompositionLocal. Only the handful of controls whose *native* look matters go through `expect`/`actual` (`AuthNavButton`, `AuthPrimaryButton`, `CountryPickerHost`, `AuthSystemAlert`). Everything else is shared Compose.
2. **iOS 26 Liquid Glass = real UIKit glass for the persistent chrome**, not per-screen. The close/back button and the bottom "Продолжить" button are native `UIButton`s with `UIButtonConfiguration.glassButtonConfiguration()` / `prominentGlassButtonConfiguration()` hosted via `UIKitView(… properties = UIKitInteropProperties(placedAsOverlay = true))`. They live in an `AuthChrome` layer **outside** the animated Nav3 content, so step transitions never have to animate a native view (interop views ignore Compose `graphicsLayer` alpha/scale/clip). iOS < 26 falls back to Compose (`CupertinoButton` from Calf). Calf's `AdaptiveSwitch` (→ Calf `LiquidGlassSwitch` on 26+) and `AdaptiveBottomSheet` (→ native `UISheetPresentationController`) are used as-is; `AdaptiveAlertDialog` (→ `UIAlertController`) for blocking errors.
3. **Calf's own `AdaptiveButton`/`AdaptiveIconButton` are NOT used for primary chrome** on iOS 26: they render a Compose-drawn kyant-backdrop glass (`LiquidGlassButton`) that only refracts something if a `LocalBackdrop` + `Modifier.layerBackdrop` is wired, forces 48 dp height + capsule shape and is `@ExperimentalCalfUiApi`. Native glass is more faithful and cheaper.
4. **Android: Material 3 Expressive** (`MaterialExpressiveTheme`, `MotionScheme.expressive()`, `ButtonDefaults.shapes()` morphing buttons, `LoadingIndicator`, `SegmentedListItem` for the country/phone group, dynamic color on API 31+).
5. **Desktop: Telegram-Desktop look on Nucleus `MaterialDecoratedWindow`**, dark navy palette, centered 360 dp column, round title-bar buttons. Do **not** adopt `compose-macos-ui` / Nucleus glass for auth (see §3.4).
6. **QR: `io.github.alexzhirkevich:qrose:1.3.1`** (built against CMP 1.12.1, has android/desktop/ios artifacts, native logo support). **[V]**
7. New deps: `qrose 1.3.1`, `components-resources 1.12.1` (logo/flags/icons as vector resources), optional `io.github.kyant0:backdrop:2.0.1` (desktop glass title-bar buttons; already transitively present on iOS via Calf but not on the compile classpath), optional `compottie 2.3.3` for animated stickers.

---

## 1. Visual language & tokens

### 1.1 Per-platform language

| | Android | iOS | Desktop (JVM, Nucleus) |
|---|---|---|---|
| Base | M3 Expressive | iOS 26 HIG (Liquid Glass chrome, grouped-less "inset rows" like Telegram iOS) | Telegram Desktop dark/light |
| Color | Dynamic color (API 31+) else seed scheme from Telegram blue | Fixed: system black/white backgrounds, accent `#3E88F7` (dark) / `#007AFF` (light) | Fixed navy dark / white light |
| Shape | Expressive: buttons morph `shapes()`, pill 56 dp primary, 28 dp sheet corners, segmented list items (large outer / small inner corners) | Capsule buttons (glass), hairline separators, 10 pt sheet corners (system) | 12 dp card, 16 dp QR tile, 36 dp round icon buttons, 10 dp button radius |
| Type | M3 Expressive scale (`headlineMediumEmphasized` for titles) | System font (SF Pro via `FontFamily.Default` on iOS) with iOS sizes | System font; Desktop scale (smaller) |
| Motion | `MotionScheme.expressive()` springs, shared-axis X between steps | iOS push (slide + parallax), spring 0.35 s | 200 ms fade-through + 24 dp slide |
| Hero | Animated sticker/emoji 120 dp | Emoji/sticker 100 pt | Logo 96 dp (phone) / QR 280 dp (qr) |

### 1.2 Design tokens (`KotogramTheme`)

Put in `shared-ui/src/commonMain/kotlin/com/xbot/kotogram/ui/theme/` (or wherever the design-system module ends up). The ColorScheme stays M3 (so all M3 components theme correctly), plus a small `KotogramColors` extension for things M3 has no slot for.

```kotlin
// commonMain
enum class KotogramPlatformStyle { Material, Cupertino, Desktop }
val LocalPlatformStyle = staticCompositionLocalOf { KotogramPlatformStyle.Material }
expect val defaultPlatformStyle: KotogramPlatformStyle   // android=Material, ios=Cupertino, jvm=Desktop

@Immutable
data class KotogramColors(
    val accent: Color,            // links, country name, selected
    val onAccent: Color,
    val background: Color,        // screen bg
    val groupedRow: Color,        // desktop card / iOS row bg (iOS = background)
    val separator: Color,         // hairlines
    val placeholder: Color,       // phone mask, hints
    val secondaryText: Color,
    val buttonDisabled: Color,
    val onButtonDisabled: Color,
    val switchOn: Color,
    val destructive: Color,
    val qrTile: Color,            // always white
    val qrDark: Color,            // always black
)
val LocalKotogramColors = staticCompositionLocalOf<KotogramColors> { error("no colors") }

@Immutable
data class KotogramDimens(
    val screenHorizontal: Dp,     // 16 iOS/Android, 0 desktop (column is centered)
    val contentMaxWidth: Dp,      // 480 mobile, 360 desktop
    val rowHeight: Dp,            // 52 iOS, 56 Android, 48 desktop
    val primaryButtonHeight: Dp,  // 52 iOS, 56 Android, 44 desktop
    val heroSize: Dp,             // 100 iOS, 120 Android, 96 desktop
    val navButtonSize: Dp,        // 44 iOS, 48 Android, 36 desktop
)
```

Color values:

| token | iOS dark | iOS light | Desktop dark | Desktop light | Android |
|---|---|---|---|---|---|
| accent | `#3E88F7` | `#007AFF` | `#5EB5F7` | `#3390EC` | `colorScheme.primary` |
| background | `#000000` | `#FFFFFF` | `#131A22` | `#FFFFFF` | `colorScheme.surface` |
| groupedRow | `#000000` | `#FFFFFF` | `#232E3C` | `#F1F3F5` | `surfaceContainerHigh` |
| separator | `#38383A` | `#C6C6C8` | `#2B3846` | `#E4E7EB` | `outlineVariant` |
| placeholder | `#5A5A5E` | `#C4C4C6` | `#6C7883` | `#A2ACB4` | `onSurfaceVariant` @ 0.6 |
| secondaryText | `#8E8E93` | `#8A8A8E` | `#8D9BA8` | `#707579` | `onSurfaceVariant` |
| buttonDisabled | `#1C1C1E` | `#E9E9EB` | `#2B3846` | `#E4E7EB` | M3 default disabled |
| onButtonDisabled | `#8E8E93` | `#A2A2A7` | `#6C7883` | `#A2ACB4` | M3 default |
| switchOn | `#34C759` | `#34C759` | accent | accent | `primary` |
| destructive | `#FF453A` | `#FF3B30` | `#EF5B5B` | `#E53935` | `error` |

Android scheme: `if (Build.VERSION.SDK_INT >= 31) dynamicDark/LightColorScheme(ctx) else` a seed-generated scheme from `#3390EC` (pre-generate light/dark with Material Theme Builder and hard-code; M3 has no runtime seed API in CMP **[V: only dynamic*ColorScheme in androidMain]**). Use `MaterialExpressiveTheme(colorScheme, motionScheme = MotionScheme.expressive(), shapes, typography)` **[V]** on Android; plain `MaterialTheme` elsewhere (iOS/Desktop still need M3 slots for text fields etc., mapped from the table above).

Typography (sp; iOS values are pt → use sp 1:1):

| role | iOS | Android (M3 slot) | Desktop |
|---|---|---|---|
| screenTitle | 28 / Bold / -0.4 tracking | `headlineMediumEmphasized` | 22 / Medium |
| subtitle | 17 / Regular / centered / lh 22 | `bodyLarge` | 15 / Regular |
| row | 17 / Regular | `bodyLarge` | 15 |
| phoneInput | 20 / Regular (tabular) | `headlineSmall` | 17 |
| codeCell | 24 / Semibold, monospaced digits | `headlineMedium` | 22 |
| button | 17 / Semibold | `labelLarge` (M3 button default) | 15 / Medium |
| link | 17 / Regular accent | `bodyLarge` primary | 15 / Regular accent |
| footnote | 13 / Regular | `bodySmall` | 13 |

Use `TextStyle(fontFeatureSettings = "tnum")` for phone/code digits so the mask doesn't jitter.

---

## 2. Screens × platforms

### 2.0 Shared structure

```
AuthRoot (commonMain)
 ├─ AuthBackground                 // solid color per token; desktop draws into Nucleus window
 ├─ NavDisplay(auth back stack)    // owned by the nav agent; each entry = a stateless screen below
 │    └─ <Step>Screen(state, callbacks)
 └─ AuthChrome (persistent, NOT animated with the steps)
      ├─ AuthNavButton (top-start)  X on first step, ‹ back afterwards       [expect/actual]
      ├─ (desktop) title-bar actions: proxy + close/back                     [jvmMain via MaterialTitleBar]
      └─ AuthPrimaryButton (bottom, imePadding)  label/enabled/loading per step   [expect/actual]
```

Each step exposes to the chrome a `AuthChromeState(navIcon: Close|Back|None, primary: PrimaryAction?)`, where `PrimaryAction(label: String, enabled: Boolean, loading: Boolean, onClick)`. The screen composable emits it through a callback/`CompositionLocal` slot so the ViewModel side doesn't need to know about chrome. On desktop the primary button is rendered **inline** inside the content (Telegram Desktop has none on the phone screen; Enter submits) — `AuthChrome` skips it when `LocalPlatformStyle == Desktop`.

Keyboard: `AuthPrimaryButton` sits above the IME (`Modifier.windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))` + 16 dp). Hardware Enter / IME action `Next/Done` triggers the same `onClick`.

Accessibility: hero emoji `contentDescription = null`; buttons ≥ 44 pt/48 dp; links are `Role.Button`; code cells expose one merged semantics node with the full value.

### 2.1 QR login — **Desktop only** (default start screen)

TDLib: `authorizationStateWaitOtherDeviceConfirmation(link)`. Start it with `requestQrCodeAuthentication` when the flow starts on desktop. Link rotates (~30 s) — just re-render.

Layout (centered column, width 360 dp, vertically centered):
- QR tile: 280 dp square, `RoundedCornerShape(16.dp)`, white bg, 16 dp inner padding, `Image(painter = rememberQrCodePainter(link) {…})` (see §4). Telegram/Kotogram logo in the center (circle, 20% of size).
- 24 dp gap → title "Быстрый вход по QR-коду" (screenTitle desktop, centered).
- 20 dp → steps list, each `Row(verticalAlignment = Top)`: 22 dp accent circle with white number (13 sp, Medium) + 12 dp + text (15 sp), 14 dp between items. Width-constrained to 360 dp, start-aligned.
  1. "Запустите Telegram на телефоне"
  2. "Откройте Настройки > Устройства > Подключить устройство"
  3. "Для подтверждения направьте камеру телефона на этот экран"
- 24 dp → `TextButton`-like links (no container; hover = underline + 8% accent background, pointer cursor via `Modifier.pointerHoverIcon(PointerIcon.Hand)`):
  - "Вход по номеру телефона" → `onPhoneLoginClick`
  - "Вход с ключом доступа" → `onPasskeyClick` (hidden when `passkeyAvailable == false`)

States: `Loading` (tile shows `CircularProgressIndicator(24.dp)` centered on white, QR area keeps size), `Ready(link)`, `Refreshing` (crossfade old → new QR, 250 ms), `Error(message)` (tile shows "Не удалось загрузить QR-код" + "Повторить" link). When `link` changes use `Crossfade(targetState = link)`.

Components: plain Compose (`Image`, `Text`, `Box`) + M3 `CircularProgressIndicator`, `TextButton(colors = textButtonColors(contentColor = accent))`. No Calf.

### 2.2 Phone number

Copy:
- title mobile: "Телефон"; desktop: "Telegram" (matches screenshot) — keep string resource `auth_phone_title_desktop`. (Consider "Kotogram" — see §4 trademark note.)
- subtitle (no passkey): "Проверьте код страны и введите свой номер телефона."
- subtitle (passkey available, mobile): "Введите свой номер телефона или " + link "используйте ключ доступа ›" — build with `buildAnnotatedString { …; withLink(LinkAnnotation.Clickable("passkey", TextLinkStyles(SpanStyle(color = accent))) { onPasskeyClick() }) { append("используйте ключ доступа") }; append(" ›") }` (the `›` also inside the link span).
- switch: "Синхронизировать контакты"
- button: "Продолжить"
- country placeholder when unknown: "Выберите страну"; invalid code: "Неверный код страны"

Suggested UI state (adapt to the ViewModel's own type):
```kotlin
@Immutable data class PhoneUiState(
    val country: CountryUi?,              // flag emoji, localized name, callingCode "998"
    val phoneDigits: String,              // national part, digits only
    val mask: String?,                    // "00 000 00 00" from TDLib getPhoneNumberInfo / country pattern; null = unknown
    val syncContacts: Boolean,
    val passkeyAvailable: Boolean,
    val isValid: Boolean,                 // drives button enabled
    val isSubmitting: Boolean,
    val error: AuthErrorUi?,              // inline (invalid number) or blocking (flood/banned)
)
@Immutable data class CountryUi(val iso2: String, val flag: String, val name: String, val callingCode: String)
```
Callbacks: `onCountryClick()`, `onCallingCodeChange(String)`, `onPhoneChange(String)`, `onSyncContactsChange(Boolean)`, `onSubmit()`, `onPasskeyClick()`, `onQrLoginClick()` (desktop), `onClose()`.

#### iOS layout (matches IMG_1776/1777)
- `AuthNavButton(Close)` top-start: 44 pt, 16 pt from leading edge, aligned to safe-area top + 8 pt.
- Hero: ☎️ (U+260E U+FE0F) at 100 sp in `Text` (iOS renders Apple Color Emoji natively), top ~22% of height (use `Spacer(Modifier.weight(0.6f))` above and fixed spacing below so it collapses with the keyboard: when IME visible, animate hero to 64 sp and hide it if height < 600 pt).
- 24 pt → title, 12 pt → subtitle (max 2 lines, centered, horizontal padding 40 pt).
- 32 pt → **input group** (full width, no card):
  - top hairline (0.33 dp `separator`, inset 16 start/end)
  - Country row 56 pt: `Row(Modifier.adaptiveClickable(onClick = onCountryClick))` **[V Calf `Modifier.adaptiveClickable` = scale 0.96 press on iOS, ripple elsewhere]**: 16 pt padding, flag (22 sp emoji), 12 pt, name in `accent` 17 sp, `Spacer(weight)`, chevron `›` (SF-like, 13 pt semibold, `secondaryText`).
  - Divider with notch: custom `Canvas` hairline that draws a 10 pt wide × 6 pt high upward-pointing triangle cut-out whose x-center = flag center (≈ 16 + 11 pt). Implement as `Modifier.drawBehind { path: moveTo(0,0) → lineTo(cx-6,0) → lineTo(cx,-6) → lineTo(cx+6,0) → lineTo(w,0) }` stroked with `separator`. (Telegram draws the notch pointing **up** into the country row.)
  - Phone row 56 pt: calling code field `+998` (BasicTextField, editable, digits only, max 4, width wraps content, min 56 pt, 17→20 sp) | vertical hairline 24 pt tall | phone field with **inline mask**: `BasicTextField(state = phoneState, inputTransformation = DigitsOnly.then(MaxLength(maskDigits)), outputTransformation = MaskFormatting(mask), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done), decorator = { inner -> Box { MaskGhost(mask, typedCount); inner() } })`. `MaskGhost` draws the *remaining* part of the mask (`00 000 00 00` minus typed digits) in `placeholder` color, offset by the typed text width (measure with `rememberTextMeasurer`). Autofocus the phone field on enter (`FocusRequester` + `LaunchedEffect(Unit)`).
  - bottom hairline.
- 16 pt → "Синхронизировать контакты" row: `Text` + `Spacer(weight)` + `AdaptiveSwitch(checked, onCheckedChange)` **[V Calf: iOS 26 → `LiquidGlassSwitch` (green `#34C759`, ignores `colors`), iOS < 26 → `CupertinoSwitch`, Android/desktop → M3 `Switch`]**.
- Bottom: `AuthPrimaryButton("Продолжить", enabled = isValid, loading = isSubmitting)` — full width minus 16 pt margins, 52 pt, capsule.

#### Android layout (M3 Expressive)
- Top: `FilledTonalIconButton(onClick = onClose, shapes = IconButtonDefaults.shapes())` **[V]** with close icon, 48 dp, in a transparent `TopAppBar`-less Box (no app bar).
- Hero: 120 dp animated sticker (§2.10) or the emoji; title `headlineMediumEmphasized`, subtitle `bodyLarge onSurfaceVariant`.
- Input group as **segmented list** (Expressive): 2 items in a `Column(verticalArrangement = spacedBy(2.dp))`:
  - `SegmentedListItem(onClick = onCountryClick, shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2), leadingContent = { flag }, trailingContent = { chevron icon }) { Text(name, color = primary) }` **[V names; exact `segmentedShapes` params U — check signature at ListItemDefaults.kt:444; `@ExperimentalMaterial3ExpressiveApi`]**
  - second item: same container color/shape (index 1), content = `+998` code field | `VerticalDivider` | masked phone field (same BasicTextField + ghost mask as iOS; color `onSurface`, mask `onSurfaceVariant @0.6`).
  - Alternative if segmented items fight the text fields: a `Surface(shape = RoundedCornerShape(28.dp), color = surfaceContainerHigh)` with two rows and an `HorizontalDivider` (no notch on Android).
- Switch row: `ListItem(headlineContent = { Text("Синхронизировать контакты") }, trailingContent = { AdaptiveSwitch(...) })` (→ M3 `Switch` with check `thumbContent`).
- Bottom: `AuthPrimaryButton` → M3 `Button(onClick, shapes = ButtonDefaults.shapes(), modifier = fillMaxWidth().height(56.dp))` **[V `Button(onClick, shapes: ButtonShapes, …)` overload exists]**; when loading, replace label with `LoadingIndicator(Modifier.size(24.dp), color = onPrimary)` **[V]** and keep width.
- Edge-to-edge: `Scaffold`-less; use `safeDrawing` insets.

#### Desktop layout (matches Screenshot 22.13.19)
- Column width 360 dp, centered both axes (`Box(contentAlignment = Center)` + `verticalScroll`).
- Logo 96 dp (vector resource `logo.xml`), 20 dp → "Telegram"/app name (22 sp Medium), 10 dp → subtitle (15 sp `secondaryText`, single line, may wrap).
- 24 dp → card `Surface(shape = RoundedCornerShape(12.dp), color = groupedRow)`, width 320 dp:
  - country row 48 dp: flag + English/localized name (white, 15 sp), whole row clickable (hover bg +4% white), opens desktop country picker.
  - phone row 48 dp: "+998" bold 17 sp + 12 dp + masked field with `-- --- -- --` dash-ghost (desktop uses dashes, not zeros — `maskGhostChar = '–'` per platform).
  - No divider (screenshot shows none); 0 dp gap, 12 dp internal padding.
- 32 dp → links "Вход по QR-коду" (`onQrLoginClick`) and "Вход с ключом доступа" (if available), 12 dp apart.
- Submit with Enter; while submitting show 16 dp `CircularProgressIndicator` at the card's trailing end of the phone row. Inline error replaces subtitle text in `destructive` with a 300 ms horizontal shake on the card.

### 2.3 Code entry (`authorizationStateWaitCode(code_info)`)

Copy:
- title: formatted phone "+998 90 123 45 67" (iOS/Android); desktop: same.
- subtitle by `code_info.type`:
  - TelegramMessage: "Мы отправили код в приложение Telegram на другом вашем устройстве."
  - Sms / SmsWord / SmsPhrase: "Мы отправили SMS с кодом на ваш номер." (Word: "…со словом", Phrase: "…с фразой")
  - Call: "Мы звоним вам, чтобы продиктовать код."
  - FlashCall / MissedCall: "Мы позвоним вам. Введите последние {length} цифр номера, с которого поступит звонок." (MissedCall shows prefix `+998 71 2••• ••`)
  - Fragment: "Мы отправили код в Fragment." + link "Открыть Fragment"
  - FirebaseAndroid/Ios: treated as Sms in UI.
- resend (from `code_info.next_type` + `timeout`): "Отправить код по SMS через 0:59" (disabled, `secondaryText`) → "Отправить код по SMS" (accent link). If `next_type == null`: "Не получили код?" → help alert.
- wrong number link: "Неверный номер?" → `onEditPhone()` (goes back).
- errors: "Неверный код" (inline, cells turn destructive + shake + clear), "Слишком много попыток. Повторите позже." (blocking alert), "Срок действия кода истёк" (inline + auto-resend link).

UI: **code cells** shared component `CodeInput(length, value, onValueChange, error)` in commonMain: single hidden `BasicTextField(state, inputTransformation = DigitsOnly + MaxLength(length), keyboardOptions(KeyboardType.NumberPassword, imeAction = Done), modifier = alpha(0f))` overlaid by `length` cells (iOS: 44×52 pt, 10 pt radius, `groupedRow`/`#1C1C1E` fill, 2 pt accent border on the focused cell; Android: 48×56 dp `RoundedCornerShape(16.dp)` `surfaceContainerHighest`, expressive spring scale-in of each digit; Desktop: 40×48 dp bottom-underline style). Auto-submit when `value.length == length`. For SmsWord/SmsPhrase render a normal text field instead of cells.
- Paste/SMS autofill: iOS `KeyboardOptions(keyboardType = NumberPassword)` + set `textContentType = oneTimeCode` **[U — CMP 1.12 has `PlatformImeOptions` for iOS content type; verify, else accept clipboard paste only]**; Android: SMS Retriever API is out of scope; long-press paste works via BasicTextField.
- Hero: 🔑/💬 animated sticker (Telegram uses a "monkey/envelope" TGS) at 100 pt; collapses with IME.
- Chrome: back chevron instead of X; primary button "Продолжить" enabled when complete (iOS/Android) — although auto-submit, keep button for a11y.
- Resend timer: `LaunchedEffect(timeout) { while (remaining > 0) { delay(1000); remaining-- } }` lives in the screen (pure UI), seeded from state.

### 2.4 2FA password (`authorizationStateWaitPassword(password_hint, has_recovery_email_address, recovery_email_address_pattern)`)

Copy:
- title: "Облачный пароль" (iOS/Android), desktop: "Введите пароль"
- subtitle: "Вы включили двухэтапную аутентификацию. Ваш аккаунт защищён дополнительным паролем."
- field placeholder: hint if present → "Подсказка: {hint}", else "Пароль"
- link: "Забыли пароль?"
- errors: "Неверный пароль" (inline + shake)
- forgot (has email): alert "Мы отправим код восстановления на {pattern}" → `requestAuthenticationPasswordRecovery` → **Email recovery code screen** (reuse §2.6 code UI, title "Проверьте почту", subtitle "Мы отправили код на {pattern}.")
- forgot (no email): `AuthSystemAlert` destructive: title "Сброс аккаунта", text "Восстановить доступ без пароля невозможно. Вы можете сбросить аккаунт — все чаты и контакты будут удалены.", confirm "Сбросить аккаунт" (destructive), dismiss "Отмена". **[U: TDLib call for reset during auth — `deleteAccount(reason, password = "")` is what official clients use; confirm with domain agent]**

UI: hero 🔐 / monkey sticker. Field:
- iOS: borderless 52 pt row with hairlines (same as phone group), `BasicSecureTextField(state, textObfuscationMode = …)` with eye toggle trailing.
- Android: `OutlinedSecureTextField(state, label = { Text(hint ?: "Пароль") }, …)` **[V]**, trailing visibility icon toggling `TextObfuscationMode.RevealLastTyped/Visible`.
- Desktop: card field like §2.2, Enter submits.

### 2.5 Registration (`authorizationStateWaitRegistration(terms_of_service)`)

Copy: title "Ваше имя" (desktop "Регистрация"), subtitle "Введите имя и добавьте фотографию профиля.", fields "Имя" / "Фамилия (необязательно)", footer "Регистрируясь, вы принимаете **Условия использования**." (link → ToS sheet), button "Продолжить"/"Зарегистрироваться" (desktop).
UI: optional avatar picker circle (88 dp, camera icon) using **calf-file-picker** (`rememberFilePickerLauncher(type = FilePickerFileType.Image)` **[U — exact Calf file-picker signature not re-verified in this pass]**) — can ship in a later ticket; keep slot. Fields: iOS hairline rows, Android `OutlinedTextField(state)` ×2 (capitalization Words, `ImeAction.Next`), desktop card with 2 rows + divider.
ToS: if `terms_of_service.show_popup` show the ToS **before** submit via `CountryPickerHost`-style sheet (`AdaptiveBottomSheet`) with formatted text, buttons "Принять" / "Отклонить" (decline → alert "Без принятия условий регистрация невозможна").

### 2.6 Email address / email code

`authorizationStateWaitEmailAddress(allow_apple_id, allow_google_id)`:
- title "Укажите почту", subtitle "Почта нужна для входа в аккаунт, если у вас не будет доступа к SMS.", field "Электронная почта" (KeyboardType.Email, autocorrect off), button "Продолжить".
- if `allow_apple_id` and iOS: secondary button "Войти через Apple" (native `ASAuthorizationAppleIDButton` via UIKitView **[U, out of scope for KTG-1 — keep slot hidden]**); `allow_google_id` on Android: "Войти через Google" (Credential Manager; out of scope, slot).

`authorizationStateWaitEmailCode(code_info, email_address_pattern, reset_available_period / next_phone_number_authorization_date)`:
- title "Проверьте почту", subtitle "Мы отправили код на {pattern}.", `CodeInput(length = code_info.length)`, link "Сбросить почту" when reset is available, otherwise footnote "Сбросить почту можно через {duration}". **[U: exact TDLib 1.8.68 field names for reset — domain agent owns]**

### 2.7 Country picker

Data: TDLib `getCountries` (name localized, english_name, calling_codes, is_hidden). UI state: `query`, `sections: List<Pair<Char, List<CountryUi>>>` (alphabetical; pinned "Текущая" (by SIM/locale) at top), `onQueryChange`, `onSelect(CountryUi)`, `onDismiss`. Filtering may be done in the composable (pure) or in VM — keep the composable fed with the filtered list.

Row: flag 22 sp · name (17) · `Spacer` · "+998" (`secondaryText`). Search matches name, english_name, iso2, and calling code (strip `+`).

| platform | container | search | list |
|---|---|---|---|
| iOS | `AdaptiveBottomSheet(onDismissRequest, adaptiveSheetState = rememberAdaptiveSheetState(skipPartiallyExpanded = true))` **[V → native `UISheetPresentationController`, page sheet, `largeDetent()` only when `skipPartiallyExpanded`, system grabber; on iOS 26 the sheet itself gets the system glass edge]** | Compose `BasicTextField` styled as iOS search field (36 pt, 10 pt radius, `#1C1C1E`/`#EEEEF0`, magnifier icon, "Поиск") pinned at top; header row with title "Выберите страну" + `AdaptiveIconButton`-like close (Compose, no native in sheet) | `LazyColumn` with sticky letter headers (13 pt, `secondaryText`), hairline separators inset 54 pt, section index on the right is optional |
| Android | Full-screen Nav3 entry (Telegram Android uses a full screen) with `TopSearchBar`/`SearchBar` **[V names exist; `@ExperimentalMaterial3Api`]**, or simply `TopAppBar` + `OutlinedTextField`. | M3 `SearchBar` with `rememberSearchBarState()` **[V]** | `LazyColumn` of `ListItem`s, sticky headers in `primary` `labelLarge` |
| Desktop | Nav3 dialog-style overlay: centered `Surface(RoundedCornerShape(12.dp), width 360, height 520)` over `#000 @ 0.5` scrim; Esc closes | 40 dp search field at top, autofocus | `LazyColumn`, hover highlight, ↑/↓ + Enter keyboard navigation (`onPreviewKeyEvent`) |

`CountryPickerHost` is `expect` because iOS needs Calf's sheet, Android/desktop are navigation destinations — alternatively make the picker a Nav3 entry on all platforms and let the iOS **scene strategy** render it via `AdaptiveBottomSheet`. Recommend the latter if the nav agent supports custom `SceneStrategy`; otherwise use the expect host.

### 2.8 Loading / connection / errors

- **Initial** (`authorizationStateWaitTdlibParameters`, `Closed`, network `connectionStateConnecting`): no spinner screen; show the phone/QR screen immediately with the primary button in loading state. A connection banner sits under the nav button: "Соединение…" / "Ожидание сети…" / "Обновление…" (iOS: 13 pt `secondaryText` with `AdaptiveCircularProgressIndicator` **[V → `CupertinoActivityIndicator` on iOS, M3 elsewhere]** 14 pt; Android: `LinearWavyProgressIndicator` **[V]** under the status bar; desktop: centered text in title bar via `MaterialTitleBar` content).
- **Submitting**: primary button shows spinner, fields disabled (alpha 0.6), back still works (cancels).
- **Inline errors** (invalid phone/code/password): message replaces subtitle (destructive color), affected field shakes: `Animatable(0f).animateTo(0f, keyframes { durationMillis = 400; -12f at 50; 12f at 100; -8f at 150; 8f at 200; -4f at 250; 0f at 300 })` applied as `offset { IntOffset(x.roundToPx(), 0) }`; haptic `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.Reject)` **[U: `Reject` exists in CMP 1.12 — else `LongPress`]**.
- **Blocking errors** (FLOOD_WAIT, PHONE_NUMBER_BANNED, network fatal): `AuthSystemAlert` = Calf `AdaptiveAlertDialog(onConfirm, onDismiss, confirmText = "OK", dismissText = null, title, text)` **[V → `UIAlertController` on iOS, M3 `AlertDialog` elsewhere]**. Copy: "Слишком много попыток" / "Пожалуйста, повторите через {duration}."; "Номер заблокирован" / "Этот номер заблокирован в Telegram." + dismiss "Помощь" (opens mailto/URL).
- **Passkey unavailable/cancelled**: silent return; failure → alert "Не удалось войти с ключом доступа".

### 2.9 Transitions between steps

Provide from presentation a `AuthTransitions` object the nav agent plugs into `NavDisplay(transitionSpec, popTransitionSpec, predictivePopTransitionSpec)` **[U: exact Nav3 parameter names per version in use]**:

- iOS: push = new slides in from `+width`, old slides to `-width*0.3` and dims (`fadeOut(targetAlpha = 0.7f)`); pop = reverse; `tween(350, easing = CubicBezierEasing(0.2f, 0.9f, 0.1f, 1f))`. Interactive edge-swipe back = Nav3 predictive back / CMP iOS back gesture **[U]**.
- Android: Material shared-axis X: `slideInHorizontally { it / 10 } + fadeIn(tween(210, delayMillis = 90))` togetherWith `slideOutHorizontally { -it / 10 } + fadeOut(tween(90))`; use `MaterialTheme.motionScheme.defaultSpatialSpec()` for slides **[V `MotionScheme` exists; method name U]**; predictive back scales the outgoing page to 0.9 with 28 dp corners.
- Desktop: fade-through 200 ms + 24 dp vertical slide; QR ↔ phone uses `Crossfade` (they're peers, not push/pop).
- Inside screens: hero emoji/title use `AnimatedContent` when the subtitle swaps to an error; chrome button label changes use `AnimatedContent(label) { fadeIn togetherWith fadeOut }` (Compose fallback only; native button just sets title).
- Chrome nav icon X → ‹ : on iOS native, swap `UIImage` (`xmark` → `chevron.backward`) with `UIView.transitionWithView(… .TransitionCrossDissolve, 0.2)`; Android `AnimatedContent` with rotate+fade.

### 2.10 Hero illustrations

MVP: system emoji in `Text` (☎️ phone, 💬 code, 🔐 password, 👋 registration, ✉️ email). iOS & macOS render Apple Color Emoji; Android Noto; Windows Segoe (flat). Later: Telegram-style animated stickers via **Compottie** (`io.github.alexzhirkevich:compottie:2.3.3`, CMP 1.12.1 compatible **[V]**) — TGS files are gzipped Lottie JSON, so un-gzip at build time and ship `.json` in `composeResources/files/`, then `rememberLottieComposition { LottieCompositionSpec.JsonString(Res.readBytes("files/phone.json").decodeToString()) }` **[U: compottie API names]**. Use our own Lottie art (Telegram's stickers are their assets).

---

## 3. Liquid Glass

### 3.1 Options evaluated (iOS)

| option | fidelity | cost / risk | verdict |
|---|---|---|---|
| **A. Native UIKit** `UIButton` + `UIButtonConfiguration.glassButtonConfiguration()` / `prominentGlassButtonConfiguration()` / `UIGlassEffect` in `UIVisualEffectView`, embedded with `UIKitView(placedAsOverlay = true)` | Real system glass, real refraction of the Compose Metal layer underneath, system press/morph animations, Dynamic Type, a11y | Interop views don't follow Compose `graphicsLayer` (alpha/scale/clip) → must not be inside animated content; one native view per button | **Chosen** for chrome (nav button + primary button) |
| B. Calf `AdaptiveButton` / `AdaptiveIconButton` | Compose-drawn approximation (`drawBackdrop` with `vibrancy() + blur(2.dp) + lens(12.dp, 24.dp)`) | Needs `LocalBackdrop` provided + `Modifier.layerBackdrop` on the background or it samples an empty layer; fixed 48 dp height, capsule, `@ExperimentalCalfUiApi`; ignores most M3 `colors` | Not for chrome. OK for in-content small glass if ever needed |
| C. kyant backdrop directly (`io.github.kyant0:backdrop:2.0.1`) | Same approximation, full control, works on Android/desktop too | Our own maintenance; not the system material | Use only on **desktop** title-bar buttons (optional) |
| D. Calf `AdaptiveTopBar` with `iosLeadingItems = listOf(UIKitUIBarButtonItem.systemItem(UIKitUIBarButtonSystemItem.Close, onClick = …))` | Real native `UINavigationBar` → system glass bar buttons on 26 **[V: Calf adds a `UINavigationBar` subview to `LocalUIViewController.current.view` and passes `isLiquidGlassEnabled = isIOS26OrAbove()`]** | Adds a whole nav bar pinned to the root VC view (not to our layout), polls frame height for several frames, `@ExperimentalCalfUiApi` + `@ExperimentalMaterial3Api`; can't host the bottom button | Viable fallback for the close button only if option A has issues |

Verified facts behind this:
- K/N 2.4.20 `platform.UIKit` exposes `UIButtonConfiguration.glassButtonConfiguration()`, `prominentGlassButtonConfiguration()`, `clearGlassButtonConfiguration()`, `prominentClearGlassButtonConfiguration()`, `UIGlassEffect.effectWithStyle(UIGlassEffectStyle.UIGlassEffectStyleRegular | …Clear)`, `UIGlassEffect.interactive`, `.tintColor`, `UIGlassContainerEffect.spacing` **[V via klib dump]**. **No Swift needed.**
- CMP 1.12.1: `androidx.compose.ui.viewinterop.UIKitView(factory, modifier, update, onRelease, onReset, properties)` and `UIKitInteropProperties(interactionMode, isNativeAccessibilityEnabled, placedAsOverlay)` **[V via klib]**. `placedAsOverlay = true` puts the native view above the Compose canvas so the glass samples Compose pixels **[U on device: confirm the glass refracts the Metal layer — it should, CA backdrop filters sample sublayers below]**.
- Calf's `isIOS26OrAbove()` is `internal` → write our own.

### 3.2 iOS implementation sketch (Kotlin only)

```kotlin
// iosMain — util
internal val isIos26: Boolean by lazy {
    UIDevice.currentDevice.systemVersion.substringBefore('.').toIntOrNull()?.let { it >= 26 } == true
}

// commonMain
enum class AuthNavIcon { Close, Back }
@Composable expect fun AuthNavButton(icon: AuthNavIcon, onClick: () -> Unit, modifier: Modifier = Modifier)
@Composable expect fun AuthPrimaryButton(
    text: String, enabled: Boolean, loading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier,
)
```

```kotlin
// iosMain — AuthNavButton.ios.kt
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun AuthNavButton(icon: AuthNavIcon, onClick: () -> Unit, modifier: Modifier) {
    if (!isIos26) return LegacyRoundIconButton(icon, onClick, modifier)   // Compose fallback, §3.3
    val onClickState = rememberUpdatedState(onClick)
    val target = remember { ClickTarget { onClickState.value() } }       // NSObject with @ObjCAction fun invoke()
    UIKitView(
        factory = {
            UIButton.buttonWithType(UIButtonTypeSystem).apply {
                configuration = UIButtonConfiguration.glassButtonConfiguration().apply {
                    image = UIImage.systemImageNamed(icon.sfSymbol)        // "xmark" / "chevron.backward"
                    preferredSymbolConfigurationForImage =
                        UIImageSymbolConfiguration.configurationWithPointSize(17.0, UIImageSymbolWeightSemibold)
                    cornerStyle = UIButtonConfigurationCornerStyleCapsule
                    baseForegroundColor = UIColor.labelColor
                }
                addTarget(target, NSSelectorFromString("invoke"), UIControlEventTouchUpInside)
                accessibilityLabel = if (icon == AuthNavIcon.Close) "Закрыть" else "Назад"
            }
        },
        update = { button ->
            UIView.transitionWithView(button, 0.2, UIViewAnimationOptionTransitionCrossDissolve, {
                button.configuration = button.configuration?.apply { image = UIImage.systemImageNamed(icon.sfSymbol) }
            }, null)
        },
        modifier = modifier.size(44.dp),
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true,
            placedAsOverlay = true,
        ),
    )
}

private class ClickTarget(private val block: () -> Unit) : NSObject() {
    @ObjCAction fun invoke() = block()
}
```

```kotlin
// iosMain — AuthPrimaryButton.ios.kt (26+ branch)
UIKitView(
    factory = {
        UIButton.buttonWithType(UIButtonTypeSystem).apply {
            addTarget(target, NSSelectorFromString("invoke"), UIControlEventTouchUpInside)
        }
    },
    update = { b ->
        val cfg = if (enabled) UIButtonConfiguration.prominentGlassButtonConfiguration()   // tinted with accent
                  else UIButtonConfiguration.glassButtonConfiguration()                 // neutral glass, gray label
        cfg.title = text
        cfg.showsActivityIndicator = loading
        cfg.cornerStyle = UIButtonConfigurationCornerStyleCapsule
        cfg.baseBackgroundColor = accentUiColor            // tint for prominent glass
        cfg.baseForegroundColor = if (enabled) UIColor.whiteColor else UIColor.secondaryLabelColor
        b.configuration = cfg
        b.enabled = enabled && !loading
    },
    modifier = modifier.fillMaxWidth().height(52.dp),
    properties = UIKitInteropProperties(isNativeAccessibilityEnabled = true, placedAsOverlay = true),
)
```
Font: set `cfg.titleTextAttributesTransformer` to SF 17 semibold if the default (body) differs **[U]**. Property names (`showsActivityIndicator`, `cornerStyle`, `baseBackgroundColor`) are standard `UIButtonConfiguration` API (iOS 15+) **[U: exact K/N casing, check in IDE autocompletion]**.

Rules for interop:
- Native chrome lives in `AuthChrome`, a sibling **above** `NavDisplay`, never inside an `AnimatedContent`/`graphicsLayer`.
- Hide native chrome while a Calf sheet/alert is up? Not needed: sheets are presented modally over the whole VC.
- When the IME animates, `imePadding()` moves the `UIKitView`; CMP syncs interop frames in the same CATransaction **[U: verify no lag on device]**.
- Dark/light: `UIColor.labelColor` / system configs adapt automatically; pass `accentUiColor` from theme (`UIColor.colorWithRed(…)`).

Optional: wrap both chrome buttons' area in a single `UIVisualEffectView(effect = UIGlassContainerEffect())` only if we ever place glass elements adjacent (merging blobs). Not needed for auth.

### 3.3 iOS < 26 fallback

- `AuthNavButton`: Compose 44 pt circle, `background(Color(0x33787880))` (iOS tertiary fill), `xmark` drawn as 17 pt vector, `Modifier.adaptiveClickable(shape = CircleShape)` **[V]** (Calf scale-press). Visually equals iOS 18 Telegram.
- `AuthPrimaryButton`: Calf `CupertinoButton(onClick, enabled, colors = CupertinoButtonDefaults.filledButtonColors(containerColor = accent, contentColor = White, disabledContainerColor = buttonDisabled, disabledContentColor = onButtonDisabled), shape = RoundedCornerShape(50), modifier = fillMaxWidth().height(52.dp))` **[V: public in Calf commonMain; `filledButtonColors` params — verify names at CupertinoButton.kt:138]**, loading → `CupertinoActivityIndicator(color = White)` **[V]**.
- Switch: Calf `AdaptiveSwitch` already falls back to `CupertinoSwitch`.

### 3.4 Desktop (macOS) glass — recommendation: **no system glass for auth**

- Nucleus 2.5.18 offers `Modifier.windowGlassRegion(kind = WindowGlassRegionKind.Sidebar|ContentList|Inspector, cornerRadius)` **[V]** — it hosts an `NSSplitViewItem` material (wallpaper-tinted sidebar material, like System Settings), macOS + Tao only, no-op elsewhere; and `DecoratedWindowScope.WindowsBackdrop(style = Mica/MicaAlt/Acrylic, tint, tier)` **[V]** for Windows 11. Neither is Liquid Glass for buttons; both are full-pane materials. Useful later for the **chat list sidebar**, not for the centered auth screen (Telegram Desktop's auth is an opaque navy page — matching the screenshot).
- `dev.nucleusframework:compose-macos-ui:1.1.0` (NucleusFramework/compose-macos-26-ui) **[V on Maven Central; deps CMP 1.12.0, `io.github.fletchmckee.liquid:liquid:1.1.1`]** is a full non-Material design system with its own `MacosTheme`, typography (Manrope) and components. Adopting it for one flow would split the design system. **Skip for KTG-1**; revisit for a macOS-native skin later.
- For the two round title-bar buttons (proxy, close) use a plain Compose 36 dp circle with `groupedRow` fill + 1 dp `#FFFFFF14` stroke (matches the screenshot exactly). Optional polish: kyant `drawBackdrop(backdrop = LocalTitleBarBackdrop, shape = { CircleShape }, effects = { vibrancy(); blur(8.dp.toPx()) })` **[V API]** — not worth it on an opaque background.

Desktop window composition (jvm-app `Main.kt`, already present):
```kotlin
MaterialDecoratedWindow(onCloseRequest = ::exitApplication, title = "Kotogram", minimumSize = DpSize(380.dp, 560.dp)) {
    MaterialTitleBar(gradientStartColor = Color.Unspecified) { state ->
        // traffic lights are native (Tao). Right side only during auth:
        AuthTitleBarActions(onProxyClick, onCloseOrBack)   // Row(Modifier.align(Alignment.End)) { RoundIcon(shield), RoundIcon(x) }
    }
    App()
}
```
`MaterialTitleBar(modifier, gradientStartColor, style, controlButtonsDirection, layoutPolicy, backgroundContent, content: TitleBarScope.(DecoratedWindowState) -> Unit)` **[V]**. The title-bar background must equal `background` token (`#131A22` dark) so the window looks seamless — set via `TitleBarStyle` colors or `backgroundContent = { Box(Modifier.fillMaxSize().background(bg)) }`. The auth flow's "close" (X) on desktop = go back to QR / exit auth sub-step, not quit app. Expose `AuthTitleBarActions` from presentation `jvmMain` and let `App()` provide it through a slot/CompositionLocal (`LocalTitleBarActions`) since the title bar is declared in `jvm-app`.

### 3.5 Android

No glass. M3 Expressive surfaces; optional subtle blur under the bottom button is not needed.

---

## 4. QR rendering

Library: **`io.github.alexzhirkevich:qrose:1.3.1`** **[V]** — Maven Central, Gradle module metadata lists `qrose-android`, `qrose-desktop` (jvm), `qrose-iosarm64`, `qrose-iossimulatorarm64` (+ js/wasm/macos); depends on `org.jetbrains.compose.ui:ui:1.12.1`, Kotlin stdlib 2.4.0. Returns a `Painter`, so it's just an `Image`.

```kotlin
@Composable
fun AuthQrCode(link: String, logo: Painter, modifier: Modifier = Modifier) {
    val painter = rememberQrCodePainter(link) {                 // [V] (data, vararg keys, options: QrOptionsBuilderScope.() -> Unit)
        errorCorrectionLevel = QrErrorCorrectionLevel.High        // [V] H (30%) — needed with a center logo
        shapes {
            darkPixel = QrPixelShape.roundCorners(radius = 0.5f)  // [V] Telegram-like rounded dots
            ball = QrBallShape.roundCorners(radius = 0.3f)        // [V]
            frame = QrFrameShape.roundCorners(corner = 0.35f)     // [V]
        }
        colors {
            dark = QrBrush.solid(Color.Black)                     // [V]
            light = QrBrush.solid(Color.White)
        }
        logo {
            painter = logo                                        // [V] QrLogoBuilderScope.painter/size/padding/shape
            size = 0.22f
            padding = QrLogoPadding.Natural(0.1f)                 // [V] clears modules around the logo
            shape = QrLogoShape.circle()                          // [V]
        }
    }
    Box(modifier.size(280.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).padding(16.dp)) {
        Image(painter, contentDescription = "QR-код для входа", modifier = Modifier.fillMaxSize())
    }
}
```
- Always black-on-white even in dark theme (scanners).
- Logo: vector resource `composeResources/drawable/ic_qr_logo.xml` — accent circle + white paper-plane; load with `painterResource(Res.drawable.ic_qr_logo)` (needs `components-resources`). Keep it ≤ 22% with ECC H.
- **Trademark note:** Telegram's API Terms ask third-party apps not to present themselves with Telegram's name/logo. Using a Kotogram mark in the QR center and on the desktop phone screen (instead of "Telegram"+paper plane) is the safer default; the screenshot's Telegram branding is the official app's.
- Link changes: wrap in `Crossfade(link, animationSpec = tween(250))`. Size on small windows: `Modifier.sizeIn(maxWidth = 280.dp).aspectRatio(1f)`.

---

## 5. Source-set structure (presentation module)

Assume `feature/auth/presentation` KMP module (android-kmp-library + jvm + iosArm64/iosSimulatorArm64), package `com.xbot.kotogram.feature.auth.presentation` — adjust to the nav/VM agent's naming. Depends on the design-system/theme module (today `shared-ui`).

```
commonMain/
  ui/AuthRoot.kt                 // AuthChrome + content slot (NavDisplay passed in by nav agent)
  ui/chrome/AuthChrome.kt        // layout of nav + primary buttons, insets, desktop skip
  ui/chrome/AuthChromeState.kt   // AuthNavIcon, PrimaryAction, LocalAuthChrome
  ui/chrome/AuthPlatformControls.kt  // expect AuthNavButton, AuthPrimaryButton, AuthSystemAlert, isPasskeySupported? (no, VM)
  ui/qr/QrLoginScreen.kt         // desktop-only by usage, but common code (testable in previews)
  ui/qr/AuthQrCode.kt
  ui/phone/PhoneScreen.kt        // branches on LocalPlatformStyle for layout variants
  ui/phone/PhoneInputGroup.kt    // country row, notch divider, code + masked phone field
  ui/phone/MaskedPhoneField.kt   // InputTransformation/OutputTransformation + ghost mask
  ui/code/CodeScreen.kt, ui/code/CodeInput.kt, ui/code/ResendTimer.kt
  ui/password/PasswordScreen.kt
  ui/registration/RegistrationScreen.kt, TermsOfServiceSheetContent.kt
  ui/email/EmailAddressScreen.kt, EmailCodeScreen.kt
  ui/country/CountryPickerContent.kt   // search + list; host decides container
  ui/common/AuthHeader.kt (hero+title+subtitle), Shake.kt, ConnectionBanner.kt, AuthLink.kt
  ui/AuthTransitions.kt          // transition specs per LocalPlatformStyle
  composeResources/values/strings.xml (ru default + values-en later), drawable/*.xml
androidMain/
  ui/chrome/AuthPlatformControls.android.kt   // M3 Expressive Button/FilledTonalIconButton/LoadingIndicator; AdaptiveAlertDialog
iosMain/
  ui/chrome/AuthPlatformControls.ios.kt       // UIKit glass (26+) / Compose Cupertino fallback; isIos26
  ui/country/CountryPickerHost.ios.kt         // AdaptiveBottomSheet (if not done via SceneStrategy)
jvmMain/
  ui/chrome/AuthPlatformControls.jvm.kt       // desktop round buttons; primary rendered inline
  ui/desktop/AuthTitleBarActions.kt           // used by jvm-app MaterialTitleBar
```
Theme: `KotogramTheme` (shared-ui) gets `expect fun platformColorScheme(dark: Boolean): ColorScheme` (android = dynamic/seed, ios/jvm = token-mapped), provides `LocalPlatformStyle`, `LocalKotogramColors`, `LocalKotogramDimens`, and uses `MaterialExpressiveTheme` on Android only (`expect @Composable fun PlatformMaterialTheme(...)`).

Statelessness contract: every `*Screen(state: XUiState, onX: () -> Unit, …, modifier: Modifier = Modifier)` — no ViewModel, no TDLib types, strings via resources. Purely UI-local state allowed: focus, `TextFieldState` (hoist as `state.phoneField: TextFieldState`? → prefer VM-owned `TextFieldState` or `value/onValueChange` String pair; recommend **String + onChange** to keep Orbit state the source of truth, and create a local `rememberTextFieldState` synced via `LaunchedEffect`/`snapshotFlow` only if mask transformations need it), resend countdown, shake animatable, sheet visibility.

Previews: `@Preview` in commonMain (CMP 1.12 `ui-tooling-preview`) per screen × light/dark × `LocalPlatformStyle` value.

---

## 6. New dependencies (all verified on Maven Central this session)

```toml
[versions]
qrose = "1.3.1"
kyant-backdrop = "2.0.1"      # optional (desktop glass buttons / direct iOS use)
compottie = "2.3.3"           # optional (animated hero stickers)

[libraries]
qrose = { group = "io.github.alexzhirkevich", name = "qrose", version.ref = "qrose" }
compose-components-resources = { group = "org.jetbrains.compose.components", name = "components-resources", version.ref = "compose-multiplatform" }   # 1.12.1 exists
kyant-backdrop = { group = "io.github.kyant0", name = "backdrop", version.ref = "kyant-backdrop" }               # android/desktop/iosArm64/iosSimArm64 artifacts, deps CMP 1.12.0
compottie = { group = "io.github.alexzhirkevich", name = "compottie", version.ref = "compottie" }               # deps foundation 1.12.1
```
- `qrose 1.3.1`: deps `qrose-core`, `qrose-encoder-matrix` 1.3.1, `compose ui 1.12.1`. **[V]**
- `components-resources 1.12.1`: required for `Res.drawable`/`Res.string` (logo, icons, Russian strings); also enable `compose.resources { packageOfResClass = "…auth.presentation.generated" }` in the module.
- Icons: no material-icons dependency in the project; ship needed Material Symbols (close, arrow_back, chevron_right, visibility, visibility_off, search, shield/proxy, photo_camera) as vector XML in `composeResources/drawable`. On iOS native buttons use SF Symbols by name (`xmark`, `chevron.backward`); Calf's `SFSymbol` object has constants **[V]**.
- Nothing new needed for UIKit glass (K/N 2.4.20 platform libs) or Calf (already present).
- Not recommended now: `dev.nucleusframework:compose-macos-ui:1.1.0` (exists **[V]**, see §3.4).

---

## 7. Calf 0.14.0 inventory (calf-ui) — what each renders

From `calf-ui-0.14.0-sources.jar` (+ GitHub tag `v0.14.0` = `dcc2cf9`); `materialMain` = Android + desktop, `iosMain` = iOS. iOS uses `kyant backdrop`/`shapes` as **implementation** deps (not on our compile classpath).

| composable (package `com.mohamedrejeb.calf.ui.*`) | signature (abridged) | iOS | Android / Desktop |
|---|---|---|---|
| `button.AdaptiveButton` | `(onClick, modifier, enabled, shape, colors: ButtonColors, liquidGlassColors: LiquidGlassButtonColors?, contentPadding, interactionSource, content: RowScope.() -> Unit)` | 26+: Compose `LiquidGlassButton` (kyant drawBackdrop, 48 dp capsule, ignores `shape`/`contentPadding`); <26: Compose `CupertinoButton` (capsule, filled) | M3 `Button` |
| `button.AdaptiveIconButton` | `(onClick, modifier, enabled, colors: IconButtonColors, liquidGlassColors?, interactionSource, content)` | 26+: `LiquidGlassButton` 48 dp plain; <26: `CupertinoButton` 44 dp plain | M3 `IconButton` |
| `button.LiquidGlassButton` (iosMain only, `@ExperimentalCalfUiApi`) + `LiquidGlassButtonDefaults.{filled,bordered,plain}ButtonColors()` | uses `LocalBackdrop.current ?: rememberLayerBackdrop()` | Compose glass | — |
| `button.CupertinoButton` / `CupertinoButtonDefaults` (commonMain, public) | `(onClick, modifier, enabled, colors: CupertinoButtonColors, shape, contentPadding, interactionSource, content)` | Compose | usable everywhere |
| `toggle.AdaptiveSwitch` | `(checked, onCheckedChange, modifier, thumbContent, enabled, colors: SwitchColors, interactionSource)` | 26+: Compose `LiquidGlassSwitch` (self-contained backdrop, green, ignores `colors`); <26: `CupertinoSwitch` | M3 `Switch` |
| `toggle.CupertinoSwitch` (common) | same shape as M3 Switch | Compose | usable everywhere |
| `sheet.AdaptiveBottomSheet` + `rememberAdaptiveSheetState(skipPartiallyExpanded, confirmValueChange)` | `(onDismissRequest, modifier, adaptiveSheetState, sheetMaxWidth, shape, containerColor, contentColor, tonalElevation, scrimColor, dragHandle, contentWindowInsets, properties, content: ColumnScope.() -> Unit)` | **Native** `UISheetPresentationController` page sheet hosting Compose; detents large(+medium); grabber = `dragHandle != null` | M3 `ModalBottomSheet` |
| `dialog.AdaptiveAlertDialog` | `(onConfirm, onDismiss, confirmText, dismissText?, title, text, material*…, iosDialogStyle, iosConfirmButtonStyle, iosDismissButtonStyle, iosConfirmButtonIsPreferred, properties, modifier)` | **Native** `UIAlertController` | M3 `AlertDialog` |
| `dialog.AdaptiveBasicAlertDialog` (`@ExperimentalCalfUiApi`) | `(onDismissRequest, modifier, iosProperties, properties, materialContent)` | native alert from `AlertDialogIosProperties` (supports text fields) | M3 `BasicAlertDialog` |
| `progress.AdaptiveCircularProgressIndicator` | `(modifier, color, strokeWidth, trackColor, strokeCap, gapSize)` | Compose `CupertinoActivityIndicator` | M3 `CircularProgressIndicator` |
| `navigation.AdaptiveTopBar` (`@ExperimentalCalfUiApi`, `@ExperimentalMaterial3Api`) | `(modifier, title, navigationIcon, actions, expandedHeight, windowInsets, colors, scrollBehavior, iosTitle, iosLeadingItems: List<UIKitUIBarButtonItem>, iosTrailingItems, iosConfiguration)` | **Native** `UINavigationBar` added to root VC view (glass items on 26) | M3 `TopAppBar` |
| `navigation.AdaptiveNavigationBar`, `AdaptiveScaffold` | — | native `UITabBar` / scaffold padding | M3 |
| `toolbar.AdaptiveToolbar` (`@ExperimentalCalfUiApi`) | `(expanded, modifier, leadingContent, trailingContent, content, iosItems)` | native `UIToolbar` | M3 toolbar |
| `gesture.Modifier.adaptiveClickable` | `(interactionSource, indication, enabled, onClickLabel, role, shape, onClick)` | scale 0.96 press, no ripple | `clickable` with ripple |
| `dropdown.AdaptiveDropDown`, `fab.AdaptiveExpandableFAB`, `slider.AdaptiveSlider` (26+: `LiquidGlassSlider`), `datepicker.*`, `timepicker.*` | — | native/glass | M3 |
| `utils.LocalBackdrop` (iosMain) | `staticCompositionLocalOf<Backdrop?>` | feed Calf glass buttons | — |
| `uikit.UIKitImage` (`SystemName`, `Vector`, `Bitmap`), `sf.symbols.SFSymbol` constants | — | for bar items | — |

Used in auth: `AdaptiveSwitch`, `AdaptiveBottomSheet`, `AdaptiveAlertDialog`, `AdaptiveCircularProgressIndicator`, `Modifier.adaptiveClickable`, `CupertinoButton` (iOS < 26 fallback), `CupertinoActivityIndicator`. Not used: `AdaptiveButton`, `AdaptiveIconButton`, `AdaptiveTopBar` (fallback only).

---

## 8. Open items / to verify

1. On-device check that `UIKitView(placedAsOverlay = true)` + glass `UIButton` refracts the Compose layer and tracks IME animation without lag (iOS 26 device + iOS 18 simulator for fallback).
2. iOS one-time-code autofill from Compose text field (`PlatformImeOptions`) — optional.
3. Exact Nav3 transition parameter names in the version the nav agent uses; whether a custom `SceneStrategy` can host the iOS country sheet.
4. TDLib 1.8.68 passkey auth API and email-reset field names (domain agent).
5. `ListItemDefaults.segmentedShapes(...)` parameter list and `@ExperimentalMaterial3ExpressiveApi` opt-ins on Android components.
6. `HapticFeedbackType.Reject` availability in CMP 1.12.
