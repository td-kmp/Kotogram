# Design system

`core:designsystem` follows Telegram for macOS. Values come from [telegram-macos](https://github.com/TelegramMessenger/telegram-macos) (read 2026-11-07); paths below are its files.

## Theme

`KotogramTheme { }` sets Material 3's `MaterialTheme` and Kotogram's spacing. `KotogramTheme.colors`, `.typography` and `.shapes` read `MaterialTheme`'s `ColorScheme`, `Typography` and `Shapes`, so M3's components and ours share one scheme. `.spacing` reads `LocalKotogramSpacing`: M3 has no spacing. This is the "extend Material 3" pattern of Android's [custom design systems](https://developer.android.com/develop/ui/compose/designsystems/custom) guide, with the theme API of AnilibriaRefresh.

- **Colours:** M3 `ColorScheme`s filled with macOS's two default palettes, chosen by `isSystemInDarkTheme()`. `ThemeSettings.defaultTheme` takes Day Classic for day and Night Accent for dark (`ColorPalette.swift`). On Android 12 and later, `dynamicColor` (on by default) takes the system's `dynamicLightColorScheme`/`dynamicDarkColorScheme` instead. Previews use the palettes.
- **Typography:** M3's roles with macOS's sizes.
- **Shapes:** M3's, with `medium` = 10 (`cornerRadius = 10` in `Auth_PasswordEntry`, `Auth_PhoneInput`, `Auth_Token`). Buttons keep M3's full corners, as `Auth_NextView` (`cornerRadius = height / 2`).

### Colour roles

Which M3 role takes which palette field is our own mapping.

| M3 role | macOS field | Day Classic | Night Accent |
|---|---|---|---|
| `primary`, `primaryContainer`, `surfaceTint` | `accent` | `2481CC` | `2EA6FF` |
| `onPrimary`, `onPrimaryContainer`, `onSecondary`, `onTertiary`, `onError` | `underSelectedColor` | `FFFFFF` | `FFFFFF` |
| `secondary` | `link` | `2481CC` | `62BCF9` |
| `tertiary` | `greenUI` | `63DA6E` | `49AD51` |
| `surface`, `surfaceContainer`, `surfaceContainerLow` | `background` | `FFFFFF` | `18222D` |
| `onBackground`, `onSurface`, `on…Container` of the grey containers | `text` | `000000` | `FFFFFF` |
| `onSurfaceVariant` | `grayText` | `999999` | `B1C3D5` |
| `surfaceVariant`, `surfaceContainerHigh`, `surfaceContainerHighest`, `secondaryContainer`, `tertiaryContainer` | `grayBackground` | `F4F4F4` | `213040` |
| `outline` | `grayIcon` | `9E9E9E` | `B1C3D5` |
| `outlineVariant` | `border` | `EAEAEA` | `213040` |
| `background`, `surfaceDim` | `listBackground` | `EFEFF3` | `101820` |
| `error` | `redUI` | `FF3B30` | `EF5B5B` |

`background` is Telegram's `listBackground`, the backdrop of its sign-in and settings windows; `surface` is its `background`, the lists and chats on it, so the chat list and the chat paint `surface`. `surfaceBright` and `surfaceContainerLowest` are Telegram's `background` by day; at night they are `grayBackground` and `listBackground`, so they stay brighter and darker than `surface`. The `inverse…` roles take the other palette's `background`, `text` and `accent`. Roles not listed are M3's.

### Typography

| M3 role | Size, weight | macOS |
|---|---|---|
| `headlineSmall` | 22 medium | `Auth_Insets.headerFont` |
| `titleLarge` | 18 medium | `FontSize.huge`; `Auth_Token`'s title |
| `titleMedium` | 15 medium | `FontSize.header` |
| `titleSmall`, `labelLarge` | 14 medium | `FontSize.title`; `Auth_NextView`'s button |
| `bodyLarge` | 14 | `FontSize.title`; text in fields |
| `bodyMedium` | 13 | `FontSize.text`; placeholders, `LoginErrorStateView` |
| `bodySmall` | 12 | `FontSize.short` |
| `labelMedium` | 12 medium | `FontSize.short` |
| `labelSmall` | 11 medium | `FontSize.small` |

`display…`, `headlineLarge` and `headlineMedium` keep M3's sizes in our font: macOS has nothing that large. Line height and letter spacing are the font's own, as AppKit draws them.

### Fonts

- **Apple** (iOS, and the desktop app on macOS): the system's SF Pro. Apple's licence doesn't allow bundling it. Compose maps `FontFamily.Default` to `.AppleSystemUIFont` on Apple platforms (`PlatformFont.skiko.kt`: ".AppleSystem* aliases is the only legal way to get default SF and NY fonts").
- **Everywhere else** (Android, the web, the desktop app on Windows and Linux): Inter 4.1 in `core:resources` (`font/inter_regular`, `inter_medium`, `inter_semibold`). Inter is the closest free face to SF Pro. macOS's `.bold` is semibold.
- `platformSystemFont` picks the font: true on iOS, and on a JVM whose `os.name` starts with "Mac".

### Spacing

| `KotogramSpacing` | dp | macOS |
|---|---|---|
| `extraSmall` | 5 | `GeneralRowItem`: section header and footer to their rows |
| `small` | 10 | `Auth_Insets.betweenError`; inside fields (`Auth_PasswordEntry`); title to description |
| `medium` | 15 | "Continue in …" from the bottom (`AuthController`) |
| `large` | 20 | `Auth_Insets.betweenHeader`; logo to title |
| `extraLarge` | 40 | `Auth_Insets.betweenNextView` |

### Icons

`KotogramIcons` (`icon/`) are 51 line icons as `ImageVector`s, drawn from lines, circles, ellipses, arcs and rounded rectangles on Compose's 24-dp canvas. They follow Telegram for macOS's weights: a 1.33 line, round caps and joins, and glyphs about 18 across, so `Icon`, `IconButton` and the fields' 28-dp icon slots draw them as macOS draws its own.

- **Fitted** (`CommonIcons`, `AuthIcons`): Back, Close, Clear, Search, Eye and EyeOff are generic glyphs whose sizes and strokes were fitted by least squares to macOS's @2x `Icon_ChatNavigationBack`, `Icon_ChatSearchCancel`, `Icon_SearchClear`, `Icon_SearchField`, `Icon_Eye_On` and `Icon_Eye_Off`.
- **Drawn for Kotogram**: every other icon, in the same weights and sizes but our own shapes. None was traced or fitted to a Telegram drawing.

| File | Icons |
|---|---|
| `CommonIcons` | Back, Close, Clear, Search, ChevronDown, ChevronRight, More, Check, DoubleCheck |
| `AuthIcons` | Eye, EyeOff, QrCode, Smartphone |
| `ChatListIcons` | Compose, Edit, Pin, Archive, Folder, Bookmark |
| `StatusIcons` | Bell, BellOff, Clock, Lock |
| `MessageInputIcons` | Attach, Send, Microphone, Smile, Camera |
| `MessageActionIcons` | Reply, Forward, Copy, Delete, ArrowDown |
| `ChatHeaderIcons` | Call, Video, Info |
| `SettingsIcons` | Settings, Globe, Appearance, Storage, Devices, Help, Logout, Shield |
| `ProfileIcons` | Person, PersonAdd, AtSign, Share, Block, Plus, Message |

The shapes come from `extensions/PathBuilderExt.kt` (`circle`, `ellipse`, `roundRect`, `arc`, `polyline`, `polygon`, `line`, `cross`, `circleWithBite`, `gear`), `ImageVectorBuilderExt.kt` (`stroke`, `fill`), `OffsetExt.kt` (`Offset.polar`) and `icon/IconGeometry.kt` (`halfPlane`, for the clipped gaps of EyeOff and BellOff). A new icon goes into the file of its screen, is listed in `KotogramIcons`, and is added to the catalog's Icons page.

### Lint

`ForbiddenImport/designSystem` in `config/detekt/detekt.yml` keeps everything outside `core:designsystem` on the design system. It forbids these imports:
- Material 3's `MaterialTheme`, text fields, buttons and list items, and their `*Defaults`;
- foundation's `BasicTextField` and `BasicSecureTextField`;
- Material icons;
- building vectors (`path`, `group`, `PathBuilder`, `addPathNodes`);
- `painterResource`, `vectorResource` and `imageResource`.

`config/detekt/baseline.xml` lists the auth screens' seven `OutlinedTextField`s, which wait for the decision on where a `TextFieldState` lives.

### Press feedback

Android, the web and desktop other than macOS keep Material 3's ripple. iOS and macOS (`platformPressHighlight`) have one indication for everything instead, Calf's `CupertinoIndication` (`theme/DimIndication`): what the component draws drops to an alpha of 0.33 at once, holds for at least 100 ms and comes back in 250 ms. Nothing happens on hover. The values are Calf's ("matching iOS"); Apple's own aren't checked.

- It dims only what is drawn, so it ignores Material's 48 dp touch area around a button.
- On Apple, `KotogramTheme` provides it as `LocalIndication` and switches Material's ripple off (`LocalRippleConfiguration` `null`). `Button` and `TextButton` add it over Material's buttons. Material components that hardcode `ripple()` and that we don't wrap (`Switch`, `IconButton`, `Card`) show no press at all.
- Our components take `KotogramTheme.indication`, not `LocalIndication`: any nested `MaterialTheme` resets `LocalIndication` to its ripple.

### Desktop window

`KotogramWindow` (jvmMain, `window/KotogramWindow`) is Nucleus's `DecoratedWindow` with Kotogram's colours on its frame and title bar (Nucleus's public `rememberMaterialWindowStyle` and `rememberMaterialTitleBarStyle`; the title bar takes `background` instead of Material's `surface`, so it is one with the screens under it, as Telegram's), and `KotogramTheme` around its content. Every Nucleus window has a `ComposeScene` of its own, so composition locals don't cross into it: `MaterialDecoratedWindow` re-provided only Material's colours, typography and shapes, and its nested `MaterialTheme` dropped everything else the theme provides. detekt keeps `MaterialDecoratedWindow` out of the apps; `MaterialTitleBar` stays, since it only reads the title bar style.

## Components

- **`TextField`, `SecureTextField`** (`component/TextField`): `TextFieldState` only, M3's state-based parameters plus `title` and `suggestion`.
  - Sizes: 40 high, `spacing.small` inside, `shapes.medium`.
  - Text styles: text `bodyLarge`; placeholder, suggestion and supporting text `bodyMedium`; title `bodySmall`.
  - Icons: a 28-point icon slot 6 from the edge and 4 from the text (`Auth_PasswordEntry`).
  - Focus ring: on by default only on Android and iOS (`platformFocusRing`). It turns red with `isError`, even without focus, as M3's indicator does.
  - Font scale: text is in sp. The container is at least 40 high and grows with its text, and the title, suggestion and supporting text wrap within the field's width (`TextFieldTest`). The width stays M3's 280 dp minimum, so screens should give fields their full width.
- **`Button`, `TextButton`** (`component/Button`): Telegram for macOS's sign-in button (`Auth_NextView`), a pill at least 36 high with 15 on each side and 14 medium. The text button is a link in the regular weight.
  - `ButtonState` is `Enabled`, `Disabled` or `Loading`; `ButtonState.of(enabled, loading)` builds it from a screen's flags.
  - A loading button keeps its colours and width, with a spinner in place of its content.
  - Colours are M3's `ButtonColors`.
- **`ProminentButton`** (`component/ProminentButton`): a screen's main action, with a `text`, a `ButtonState`, `ButtonColors` and content padding. It is the filled `Button` everywhere except on iOS 26 and macOS 26, where it is the system's glass button.
  - Size, font and colours come from Compose: an invisible filled `Button` with the same text sizes the native one (without Material's 48 dp touch target, which the box adds as `Button` does), and each platform hands its native button the theme's `labelLarge` in points and the `ButtonColors` (the container colour tints the glass), as Calf's native pickers take their colours.
  - iOS 26 (`component/platform/PlatformProminentButton.ios`): UIKit's `prominentGlassButtonConfiguration` in a `UIKitView` with CMP's experimental `placedAsOverlay`, since the glass refracts only what lies under it. UIKit runs the press (it swells and stretches), the click and the spinner. `overrideUserInterfaceStyle` follows the theme, as Calf does for its native pickers.
  - macOS 26 (`native/MacGlassButton`, `native/ObjC` in jvmMain): AppKit's `NSButton` with `NSBezelStyleGlass`, `NSControlSizeExtraLarge` and `NSTintProminencePrimary`, made through the Objective-C runtime with JNA, in Nucleus's `NativeView` with `placedAsOverlay`. AppKit then delivers its events itself: hover, the press (it glows and swells by a few points), the click through target-action, and an `NSProgressIndicator` while loading. It doesn't take focus, so it draws no focus ring.
  - `placedAsOverlay` on macOS is our change to Nucleus (local `dev` build, [handoff](../handoff.md)). Under the surface, as Nucleus mounts views by default, AppKit's controls can't work: Nucleus calls `mouseDown:` on them directly, `NSButton`'s tracking loop then takes the mouse-up Compose still needs, and the glass never lights; Nucleus 3.0 stopped forwarding the mouse to `NSControl` for that reason.
  - Both overlays cover whatever Compose draws over them, so the glass shows only while its `LocalLifecycleOwner` is `RESUMED`: NavDisplay holds a screen at `STARTED` during a transition and under a dialog, and the new screen's button appears once it settles.
  - AppKit draws the glass only when the process's executable claims macOS SDK 26 or later; no JDK does (checked up to 27: SDK 13.3–15.2). Nucleus's `macOsSdkVersion` (default `26.0`) patches it with `vtool`: `nucleusPatchMacJvm` for `run`, the `.app` launcher when packaging. `hotRun` and the toolchain's own `java` give no glass.
  - Calf's `AdaptiveButton` imitates the glass in Compose (Kyant's Backdrop) instead; its native glass is only in screen-level views (FAB, bars) on the view controller.
- **`ProgressIndicator`** (`component/ProgressIndicator`): an indeterminate spinner, 20 by default, in `primary` or a given colour. `Button`'s loading state and the chat list use it; detekt keeps Material's `CircularProgressIndicator` out of screens.
  - iOS: UIKit's `UIActivityIndicatorView`, medium up to 32 and large above, in the given colour.
  - macOS: AppKit's `NSProgressIndicator`, small up to 32 and regular above. It can't take a colour: it is light when the colour is lighter than the theme's background, dark otherwise.
  - Both are overlays (UIKit interop's and Nucleus's `placedAsOverlay`), so a spinner over a Compose surface shows that surface, and like the glass button they show only while their screen is `RESUMED`. Elsewhere it is Material 3's spinner with a stroke of 2.
- **`ListItem`** (`component/ListItem`): M3's slots (overline, headline, supporting, leading, trailing) with the insets of macOS's rows (`GeneralRowItem`: 12 and 14), headline `bodyLarge`, supporting `bodyMedium`, and M3's `ListItemColors`.
  - `position` (`Single`, `First`, `Middle`, `Last`) groups rows as macOS's settings do: rounded ends and a divider that starts under the text.
  - Without a position the row is plain, as in the lists of chats and countries.
- **`RichText`** (`component/RichText`): TDLib's `FormattedText` as text. `core:resources` parses it next to the language pack's markup (`FormattedText.toAnnotatedString`, `FormattedTextStyles`, `FormattedTextTest`); `RichTextDefaults.styles()` styles it from the theme.
  - Links, e-mail addresses and phone numbers open.
  - Mentions, hashtags, cashtags and bot commands go to `onEntityClick`.
  - Custom emoji, spoilers' animation and media timestamps aren't drawn yet.
- **`CodeField`** (`component/CodeField`): the sign-in code, after macOS's `CodeEntryControl` (`TGUIKit`). It takes a `TextFieldState` and the code's `length`.
  - Cells: 40 × 40, 8 apart, `shapes.medium`, the container colour of text fields. The cell to fill has a 1-point `primary` border while the field has focus, on every platform.
  - Font scale: a cell is 40/24 of its digit's size, never under 40, so it grows with the system's font scale (Android goes up to 2×, non-linearly from Android 14). When the row would be wider than its room, the cells narrow and keep their height.
  - Digits: 24 medium with tabular figures. macOS uses SF Mono.
  - Animation: a digit fades in and rises 5 over 0.2 s while its scale springs from 0.1. macOS's spring (mass 5, stiffness 900, damping 88) is `dampingRatio` 0.656, `stiffness` 180. A removed digit fades and shrinks over 0.2 s.
  - `isError`: the field shakes 3 to each side, 8 legs of 40 ms (`shake(beep:)`), and the marked cell turns `error`; macOS only shakes. `enabled = false` is macOS's locked state.
  - Input: digits only, at the end only (our own rule; macOS lets you click a cell). A whole code pasted over a full field replaces it, as macOS's `insertAll` does. `ContentType.SmsOtpCode` asks for SMS autofill.
  - Completion: the caller watches `state.text`; macOS submits at the last digit. `CodeFieldTest` covers the input rules.
  - `supportingText`: the error under the cells, centred, 10 below and red with `isError`, as macOS's `Auth_CodeEntry` puts `LoginErrorStateView` under its control. Only the cells shake.
- **`QrCode`**: see [handoff](../handoff.md).

## Catalog

`catalog-app` shows the design system on the desktop, as Material 3's catalog does: one grid, foundations first and then components; a card opens its page of examples.
- **Run:** `./gradlew :catalog-app:run`.
- **New page:** a `CatalogPage` in `pages/`, listed in `CatalogPages`.
- **Screenshot tests:** `CatalogScreenshotTest` (Roborazzi 1.77.0) captures the home screen and every page, light and dark, and the pages of the fields at twice the font scale. It runs on a frozen clock, moved 6 s on, so the QR code has faded in.
  - `./gradlew :catalog-app:recordRoborazziJvm` writes the goldens to `catalog-app/screenshots/`; `verifyRoborazziJvm` compares, allowing 1% of pixels to differ.
  - The goldens are recorded on macOS with SF Pro: on Linux the fonts differ, so CI would need goldens of its own.
- The catalog's texts are plain English: it is a developer's tool, not part of the app.

## Not done

- The feature screens still use their own dp values and the M3 roles they had. Moving them onto `KotogramTheme` and macOS's layouts is the next step.
- No theme choice in settings: the system's dark mode decides.
