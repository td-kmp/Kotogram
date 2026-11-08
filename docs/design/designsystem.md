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
- `currentPlatform.isApple` picks the font (see Platforms).

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

`config/detekt/baseline.xml` lists the other auth screens' `OutlinedTextField`s. A screen's `TextFieldState` lives in its ViewModel (decided 2026-11-08 with the phone screen), which edits it and follows it with `snapshotFlow`.

### Platforms

What differs by platform goes through one `expect` file, `theme/PlatformInfo` (own design, after the patterns below):
- `currentPlatform: PlatformInfo(isApple, isDesktop)` for the facts the theme branches on: iOS `(true, false)`, Android `(false, false)`, web `(false, true)`, JVM `(os.name starts with "Mac", true)`.
- `platformComponentOverrides()`, the native components `KotogramTheme` provides. Android and the web provide none.
- `platformDynamicColorScheme`, Android's dynamic colours.
- `platformUiMediaScope()` and `platformMediaQueryInfo()`, the environment the theme provides ([media queries](#media-queries)).

A component with a native variant is written as Material 3's component overrides (`@ExperimentalMaterial3ComponentOverrideApi`, e.g. `NavigationBar`): the public composable builds a `…OverrideScope` of its parameters and calls `Local…Override.current`; `Default…Override` is the Material version, and the native ones (`component/platform` in iosMain and jvmMain) implement `…Override`. Ours are internal and `staticCompositionLocalOf` (M3 uses `compositionLocalOf`). Overrides: `ProminentButton` (iOS 26 and macOS 26 glass), `ProgressIndicator` (iOS, macOS) and `Menu` (macOS). The JVM provides them only on macOS inside a Nucleus window, so tests and screenshots get the Material versions.

### Media queries

`KotogramTheme` provides two locals, apart as in androidx:

**`LocalUiMediaScope`** (`media/`): the window and the input. `media/MediaQuery.kt` is androidx-main's `androidx.compose.ui.MediaQuery` (Compose UI 1.13) under our package: `WindowPosture` with its `WindowFold`s and no book posture (aosp/4249142), no `platformFeatures` (the unmerged aosp/4304334 declares them on Android only). Left out: `computedMediaQuery`, as its `computedStateOf` is 1.13's runtime. CMP 1.12.1 provides the official scope only on Android; CMP 1.13 has it on iOS and desktop (1.13.0-beta01) and the web (1.13.10), and Nucleus#671 keeps us on 1.12.1. The platform scopes are the official ones, copied:
- Android: androidx's `UiMediaScopeImpl` (`MediaQuery.android.kt`). `rememberUiMediaScope` attaches what `AndroidComposeView` attaches to it: posture from `WindowInfoTracker`, pointer and keyboard from `InputManager`, IME, dock; camera, microphone, TV and car from `PackageManager`.
- JVM: CMP's `DesktopMediaScope` (CMP #3341): a fine pointer, a physical keyboard, a microphone, no camera.
- iOS: CMP's `IosUiMediaScope` (CMP #3340) without what only its scene sees: the pointer stays coarse, where CMP takes the type of the last touch. Camera and microphone are taken as present: CMP asks `AVCaptureDevice`, which would have the app link AVFoundation, as the framework is static. No posture: CMP's is a TODO for the iPhone Duo too (CMP-10776, targeted at 1.14.0), and iOS 27.1's `UIHingeInteraction` is newer than the iOS 26.4 SDK that Kotlin 2.4.20's UIKit is built from. JetBrains' prototype (branch `svastven/reserved-regions`) reads only the occlusion regions, as cutouts, through a Swift helper.
- Web: CMP's `WebMediaScope` (CMP #3525): CSS `any-pointer`/`any-hover`, viewport segments and `device-posture` for folds, the virtual keyboard, `enumerateDevices`. Its `MediaQueryListener` is folded into one class without its fallbacks for browsers that can't run Kotlin/Wasm.

Moving to the official API is a change of imports and the deletion of `media/`.

**`LocalMediaQueryInfo`** (`style/`): the form factor, `MediaQueryInfo(isLaptop, isTv, isAuto)`, which `UiMediaScope` doesn't describe. It is Material 3's `mediaQueryInfo()` of androidx-main, the switch of its adaptive styles (`ComponentStyles.kt`), and stays after the move. `platformMediaQueryInfo()`:
- Android: M3's own code: `PackageManager`'s `FEATURE_PC`, `FEATURE_LEANBACK` and `FEATURE_AUTOMOTIVE`.
- JVM: a laptop. iOS: a laptop when an iPad app runs on a Mac (`NSProcessInfo.iOSAppOnMac`).
- Web: a fine pointer that hovers, as `WebMediaScope` takes a physical keyboard (own heuristic). CMP's M3 has no answer off Android: its `mediaQueryInfo()` returns all false (CMP-10800 asks whether a desktop is a laptop).

Own design: M3 reads `mediaQueryInfo()` from an `expect`; here it is a local the theme provides, so tests and previews set it as they set `LocalUiMediaScope`.

Two questions go to them, apart:
- **Sizes**: Telegram for macOS's on a laptop, Telegram for iOS's otherwise ([adaptive styles](#adaptive-styles)). Not the pointer: it reports the finest one attached, so a trackpad would turn an iPad into a laptop; M3 takes a laptop from `FEATURE_PC` too.
- **Layout**: `isLaptop`, the window and the posture, per screen (the phone screen: [auth](auth.md)). The book posture, a half-opened vertical fold, is `WindowPosture.isBook` in auth's extensions.

### Adaptive styles

`style/ComponentStyles` is Material 3's stand-in for the Style API (material3's `ComponentStyles.kt` of androidx-main; its `Checkbox`, `RadioButton`, `SearchBar`, `Card` and navigation bars resolve through it). CMP 1.12.1's `Style` has no properties of a component's own: those (`CommonStyleScope`, `stylePropertyOf`, `StyleResolver`) are androidx-main's foundation, on which M3's next styles build (aosp/4392084; `optimizedUiForLaptop/Tv/Auto` in aosp/4400908). Copied: `StyleResolver`, `ComponentStyle`, `resolve`, `AdaptiveStyleScope` with `laptop {}`, `tv {}` and `auto {}`. Public, as features style their screens; M3's private `StyleResolverImpl` is `DefaultStyleResolver`. Not copied until a style needs them: `ComponentState` and its scopes (`disabled {}`, `checked {}`, `pressed {}`…), and the theme's defaults (`ComponentProperties`).

A styled component or screen:
- **Where:** a screen's style is `XScreenStyle.kt` next to it, in its package; a component's is at the bottom of its file.
- **What:** every value of its look is in the style: sizes, spacing, text styles, colours, shapes, animation specs. No `XDefaults` or `XTokens` objects (`TextFieldDefaults`, `CodeFieldDefaults` and the screens' tokens are gone). Constants of behaviour stay outside (`MAX_DIGITS`, keyboard options).
- **Shape:** `fun interface XStyle : ComponentStyle<XStyleScope>` whose `companion object : XStyle` is the empty style and holds `Default` (touch screens) and `Adaptive = Default then XStyle { laptop { … } }`, with a top-level `infix fun XStyle.then`. `XStyleScope(theme, mediaQueryInfo)` has a property and a setter per value, `AdaptiveStyleScope<XStyleScope>` and `StyleResolver by DefaultStyleResolver()`. As M3's styled components in progress (`ButtonStyle` and `Button(onClick, style, …)` in aosp/4375293, progress indicators in aosp/4348772): the empty companion, `then` passing it through, and the component's style applied over its default.
- **Parameters:** a component takes state and content, and one `style: XStyle = XStyle` for its look, applied over `XStyle.Adaptive` (or `Default` where a laptop doesn't differ): `ProgressIndicator(style = ProgressIndicatorStyle { color(theme.colors.onPrimary) })` changes only the colour. No `colors`, `shape`, `contentPadding` or `textStyle` parameters.
- **Theme:** styles read `theme`, a `KotogramTheme.Values` (colours, typography, shapes, spacing), as M3's read `MaterialTheme.Values`.
- **Use:** `val style = rememberStyle(::XStyleScope, XStyle.Adaptive)` (own design: M3 resolves on every composition), then `context(style) { … }`: the parts of the component or screen take it as a context parameter, `context(style: XStyleScope)`, instead of passing it on. Remembered per theme and `MediaQueryInfo`, so the parts skip.
- **Layout** stays in the composable: `style.mediaQueryInfo.isLaptop`, the window and the posture decide what goes where.

Styled: every component (`Button`, `TextButton`, `ProminentButton`, `ProgressIndicator`, the fields, `ListItem`, `Menu`, `OptionPicker`, `QrCode`, `RichText`) and the phone, QR and language-suggestion screens. When CMP has the foundation's style properties, they move to M3's newer styles (aosp/4400909 moves `Checkbox`): a rewrite of the styles, not of the screens' layout.

### Previews

`@KotogramPreview` shows touch screens and `@KotogramLaptopPreview` a laptop (700 × 550, the desktop window), light and dark, each with a `PreviewWrapper` that sets `ProvideFormFactor` under `KotogramTheme`. Studio wraps all of a function's previews in the first `PreviewWrapper` of its annotation graph (`getPreviewWrapperProviderFqn`), so the laptop one is a function of its own (`XScreenLaptopPreview`, calling `XScreenPreview`). `ProvideFormFactor(laptop)` sets `MediaQueryInfo` and a pointer and keyboard to match over the device's `UiMediaScope`, as the `mediaQuery` guide overrides the scope for previews. Values in `@Preview(device = "spec:…")` (aosp/4342259) need ui-tooling 1.14 and a Studio that passes `tools:device`: none does yet.

### Press feedback

Android, the web and desktop other than macOS keep Material 3's ripple. iOS and macOS (`currentPlatform.isApple`) have one indication for everything instead, Calf's `CupertinoIndication` (`theme/DimIndication`): what the component draws drops to an alpha of 0.33 at once, holds for at least 100 ms and comes back in 250 ms. Nothing happens on hover. The values are Calf's ("matching iOS"); Apple's own aren't checked.

- It dims only what is drawn, so it ignores Material's 48 dp touch area around a button.
- On Apple, `KotogramTheme` provides it as `LocalIndication` and switches Material's ripple off (`LocalRippleConfiguration` `null`). `Button` and `TextButton` add it over Material's buttons. Material components that hardcode `ripple()` and that we don't wrap (`Switch`, `IconButton`, `Card`) show no press at all.
- Our components take `KotogramTheme.indication`, not `LocalIndication`: any nested `MaterialTheme` resets `LocalIndication` to its ripple.

### Desktop window

`KotogramWindow` (jvmMain, `window/KotogramWindow`) is Nucleus's `DecoratedWindow` with Kotogram's colours on its frame and title bar (Nucleus's public `rememberMaterialWindowStyle` and `rememberMaterialTitleBarStyle`; the title bar takes `background` instead of Material's `surface`, so it is one with the screens under it, as Telegram's), and `KotogramTheme` around its content. Every Nucleus window has a `ComposeScene` of its own, so composition locals don't cross into it: `MaterialDecoratedWindow` re-provided only Material's colours, typography and shapes, and its nested `MaterialTheme` dropped everything else the theme provides. detekt keeps `MaterialDecoratedWindow` out of the apps; `MaterialTitleBar` stays, since it only reads the title bar style.

## Components

- **`TextField`, `SecureTextField`** (`component/TextField`): `TextFieldState` only, M3's state-based parameters without the look's (`textStyle`, `shape`, `colors`, `contentPadding`: the style has them), plus `title` and `suggestion`.
  - Laptop: Telegram for macOS's input, 40 high, `spacing.small` inside, `shapes.medium`; text `bodyLarge`, placeholder, suggestion and supporting text `bodyMedium`, title `bodySmall`; a 28-point icon slot 6 from the edge and 4 from the text (`Auth_PasswordEntry`); no focus ring.
  - Touch screens (own): Telegram for iOS's 17-point type (15 above and below), 50 high as its buttons, 16 and 12 inside, a 32-point icon slot 8 from the edge. The focus ring of Android and iOS, `primary`, red with `isError` even without focus, as M3's indicator.
  - Font scale: text is in sp. The container is at least its height and grows with its text, and the title, suggestion and supporting text wrap within the field's width (`TextFieldTest`, both form factors). The width stays M3's 280 dp minimum, so screens should give fields their full width.
- **`Button`, `TextButton`** (`component/Button`, `ButtonStyle` and `TextButtonStyle` over one `ButtonStyleScope`): a pill whose height is the button's, not the screen's. On a laptop Telegram for macOS's sign-in button (`Auth_NextView`): at least 36 high, 15 on each side, 14 medium, an 18 spinner. On touch screens Telegram for iOS's (`SolidRoundedButtonNode` at its sign-in height): 50 high, 17 semibold, a 22 spinner. The text button is a link in the regular weight, 14 or Telegram for iOS's 16, as high as the filled one so a screen can swap them in place. Neither has Material's hover elevation.
  - `ButtonState` is `Enabled`, `Disabled` or `Loading`; `ButtonState.of(enabled, loading)` builds it from a screen's flags.
  - A loading button keeps its colours and width, with a spinner in place of its content.
  - Colours are M3's filled and text buttons' (`FilledButtonTokens`, `TextButtonTokens`), in the style.
- **`ProminentButton`** (`component/ProminentButton`): a screen's main action, with a `text`, a `ButtonState` and a `ButtonStyle`. It is the filled `Button` everywhere except on iOS 26 and macOS 26, where it is the system's glass button.
  - Size, font and colours come from Compose: an invisible filled `Button` with the same text sizes the native one (without Material's 48 dp touch target, which the box adds as `Button` does), and each platform hands its native button the style's text style in points and its colours (the container colour tints the glass), as Calf's native pickers take their colours.
  - iOS 26 (`component/platform/GlassProminentButtonOverride`): UIKit's `prominentGlassButtonConfiguration` in a `UIKitView` with CMP's experimental `placedAsOverlay`, since the glass refracts only what lies under it. UIKit runs the press (it swells and stretches), the click and the spinner. `overrideUserInterfaceStyle` follows the theme, as Calf does for its native pickers.
  - macOS 26 (`native/MacGlassButton`, `native/ObjC` in jvmMain): AppKit's `NSButton` with `NSBezelStyleGlass`, `NSControlSizeExtraLarge` and `NSTintProminencePrimary`, made through the Objective-C runtime with JNA, in Nucleus's `NativeView` with `placedAsOverlay`. AppKit then delivers its events itself: hover, the press (it glows and swells by a few points), the click through target-action, and an `NSProgressIndicator` while loading. It doesn't take focus, so it draws no focus ring.
  - `placedAsOverlay` on macOS is our change to Nucleus (local `dev` build, [handoff](../handoff.md)). Under the surface, as Nucleus mounts views by default, AppKit's controls can't work: Nucleus calls `mouseDown:` on them directly, `NSButton`'s tracking loop then takes the mouse-up Compose still needs, and the glass never lights; Nucleus 3.0 stopped forwarding the mouse to `NSControl` for that reason.
  - Both overlays cover whatever Compose draws over them, so the glass shows only while its `LocalLifecycleOwner` is `RESUMED`: NavDisplay holds a screen at `STARTED` during a transition and under a dialog, and the new screen's button appears once it settles.
  - AppKit draws the glass only when the process's executable claims macOS SDK 26 or later; no JDK does (checked up to 27: SDK 13.3–15.2). Nucleus's `macOsSdkVersion` (default `26.0`) patches it with `vtool`: `nucleusPatchMacJvm` for `run`, the `.app` launcher when packaging. `hotRun` and the toolchain's own `java` give no glass.
  - Calf's `AdaptiveButton` imitates the glass in Compose (Kyant's Backdrop) instead; its native glass is only in screen-level views (FAB, bars) on the view controller.
- **`ProgressIndicator`** (`component/ProgressIndicator`): an indeterminate spinner as large as its modifier makes it, 20 without a size, the same on every form factor. Its style has the colour (`primary`) and the stroke (2), which only Material's spinner draws. `Button`'s loading state and the chat list use it; detekt keeps Material's `CircularProgressIndicator` out of screens.
  - iOS: UIKit's `UIActivityIndicatorView`, medium up to 32 and large above, in the given colour.
  - macOS: AppKit's `NSProgressIndicator`, small up to 32 and regular above. It can't take a colour: it is light when the colour is lighter than the theme's background, dark otherwise.
  - Both are overlays (UIKit interop's and Nucleus's `placedAsOverlay`), so a spinner over a Compose surface shows that surface, and like the glass button they show only while their screen is `RESUMED`. Elsewhere it is Material 3's spinner with a stroke of 2.
- **`Menu`** (`component/Menu`): a list of choices anchored to the layout it sits in, as `DropdownMenu`, with the selected one checked.
  - macOS (in a Nucleus window): AppKit's `NSMenu` (`native/MacMenu`) with its top left corner at the click, as Telegram for macOS opens its menus (seen in the app; the code that places them isn't checked), or under the anchor when the pointer isn't over it (opened by keyboard). `popUpMenuPositioningItem:` with no item; the pointer is the window's `mouseLocationOutsideOfEventStream`. AppKit draws it (glass on macOS 26), keeps it on screen, scrolls it with arrows and selects by typing. Telegram for macOS draws its own menu window by default (TGUIKit's `AppMenuController`; `NSMenu` only behind the developer setting "Native Context Menu").
  - Elsewhere Material 3's `DropdownMenu` (`DefaultMenuOverride`) in `MenuStyle`: M3's baseline on touch screens (items 48 high, 12 at the sides) and its laptop values from the unmerged adaptive menu styles (aosp/4424517) on a laptop: items 44 high with 10 at the sides, inside 10 at the sides; selection and hover rounded with `shapes.small`. It is at most the window's height less M3's 48 margins (`DropdownMenuPositionProvider` centres a taller menu over the whole window), opens scrolled to centre the selected item, and selects by typing as `NSMenu` does (letters within a second make one prefix, leading emoji skipped; the found item takes focus, Enter picks it). Our own design: M3 has no type-select. Typing matches the characters the layout produces, so a Russian layout won't find English names.
- **`IconButton`, `GlassIconButton`** (`component/IconButton`, `component/GlassIconButton`): an icon in a circle, 44 on touch screens (UIKit's bar buttons) and 32 on a laptop, with no container until `IconButtonStyle` gives one. `IconButton` is Compose everywhere. `GlassIconButton` is for a bar over content: UIKit's `glassButtonConfiguration` on iOS 26 (`IosGlassIconButtonOverride`), AppKit's `NSButton` with `NSBezelStyleGlass` and `NSControlBorderShapeCircle` on macOS 26 (`MacGlassIconButtonOverride`, `native/MacGlassIconButton`), `IconButton` elsewhere.
  - The native buttons draw the icon from its vector: `rememberIconBitmap` (`component/platform/NativeIcon`) draws it at the screen's scale. UIKit gets a template image and tints it with the configuration; AppKit gets the icon in its colour, since `contentTintColor` tints only borderless buttons (`NSButton.h`).
  - Overlays as the prominent button: they show only while their screen is `RESUMED`, and keep Material's 48 touch target.
- **`TopBar`** (`component/TopBar`): a title between a navigation slot and actions, with no background of its own. `TopBarStyle.centeredTitle` defaults to the platform's: centred on Apple's, as UIKit's navigation bar, at the start elsewhere, as Material's. Touch screens: 56 high, 16 from the edges, 17 semibold (UIKit's bar in a sheet); a laptop: 44, 10, `titleMedium` (own). The catalog's own bar is one.
- **`SearchField`** (`component/SearchField`): a capsule, 48 on touch screens (iOS 26's toolbar search) and 32 on a laptop, with a magnifier and, while it has text, a clear button: the design system's `TextField` without an indicator. On iOS 26 UIKit's `UISearchTextField` in a `UIGlassEffect` capsule, with a glass cancel button beside it while searching, both in one `UIGlassContainerEffect` so they merge (`GlassSearchView`). Where it sits is the screen's call.
- **`FastScroller`** (`component/FastScroller`): a handle that takes a sectioned `LazyColumn` to a section (`FastScrollerSection`: the title and the index of its header). UIKit has an index only inside `UITableView` and `UICollectionView` (SDK headers), so both looks are Compose: `SectionIndex` on iOS (UITableView's: 11 semibold, 14 a line, the tint colour) and `ScrollThumb` elsewhere (own, after Android's contacts: a thumb that widens and shows the section in a bubble while dragged). Both tick with `HapticFeedbackType.SegmentTick`, which CMP sends to `UISelectionFeedbackGenerator` on iOS.
- **`BottomSheet`** (`component/BottomSheet`): a modal sheet shown while it is composed, in `background`. iOS (`PageSheetOverride`): UIKit's page sheet at the large detent with the content in a `ComposeUIViewController` of its own, as Calf's `AdaptiveBottomSheet` does; the screen's composition locals come through `currentCompositionLocalContext`, the sheet's own platform locals over them, and a swipe down calls `onDismissRequest`. Not run on iOS yet. Elsewhere Material 3's `ModalBottomSheet`, expanded.
- **`Checkbox`, `Switch`** (`component/Checkbox`, `component/Switch`): controlled toggles.
  - macOS: AppKit's checkbox and `NSSwitch` (`native/MacToggle`) at their fitting size; the switch takes the system accent, since `NSSwitch` has nothing but `state`.
  - iOS: `UISwitch` with the style's track colour. UIKit has a checkbox only in Mac Catalyst (`UISwitchStyleCheckbox`, `UISwitch.h`), so Compose draws iOS's selection circle, 22.
  - Elsewhere Compose: Material 3's 18 rounded square and 52 × 32 switch on touch screens, a 14 square and a 34 × 20 switch on a laptop (own).
  - A change the caller doesn't take goes back: every toggle of a native control recomposes it, and an effect sets the control to `checked`. With `onCheckedChange = null`, as when a row toggles it, a toggle keeps its enabled look; iOS hands the touch to Compose, while Nucleus gives a click on the control to the control.
- **`OptionPicker`** (`component/OptionPicker`): one choice out of many, each shown by an `OptionLabel` (text, a leading emoji or symbol, a second line, a trailing value). On a laptop (`MediaQueryInfo.isLaptop`) a `Menu` with the label on one line. On touch screens, a phone's browser included, a `BottomSheet` in `surface` made of the parts above: a `TopBar` with the title and a `GlassIconButton` (`Cross`) to close it, a `SearchField` (`searchAtBottom`: at the bottom on Apple's platforms, as iOS 26, at the top elsewhere) and `ListItem` rows (17 and 15, a 26 flag: Telegram for iOS's country list) under sticky headers by `optionSection`, with the platform's `FastScroller`.
  - It opens three rows above the selected option, which has a check; a search lists the matches alone, the match in semibold.
  - The phone screen picks its country with it: sections by the localised name's first letter, the English name under it when it differs, the title `Login.countryLabel` capitalised (macOS's pack has no title for it: its login picks the country from a menu).
- **`LottieImage`** (`component/LottieImage`): a Lottie animation by resource path or by a TDLib sticker's `.tgs` bytes and a cache key; compottie's cache is keyed by that name, not by `JsonString`'s md5 of the whole JSON, so a cached animation is neither read nor inflated again and shows in the first frame; it tells Telegram's `.tgs` (gzip, inflated per platform: `GZIPInputStream`, zlib on iOS, `DecompressionStream` on the web; compottie has no gzip), `.lottie` and JSON apart by their first bytes. `colors` repaints shapes by key path, as Telegram for macOS's `LottieColor`. The sign-in steps play Telegram's default stickers from `files` (iOS's `Intro*`, macOS's `keychain` and `code_note`), repacked from `.tgs` into `.lottie`, whose deflate compottie reads itself.
- **`rememberNavTransitions`** (`animation/NavTransitions`): `push`, `pop` and `predictivePop` for every `NavDisplay` (root, sign-in steps, catalog). Android: the framework's `activity_open_*`/`activity_close_*` (96dp slide, 450 ms, `fast_out_extra_slow_in` as M3's emphasized curve, an 83 ms fade); the back gesture approaches `DefaultCrossActivityBackAnimation` (the closing screen shrinks to 0.9 against the far edge, then leaves 96dp further and fades), which scales the entering screen too, something a one-way `ContentTransform` can't. iOS: Compose Multiplatform's own `NavDisplay.ios.kt`, UINavigationController's push copied 1:1. Desktop and web: Telegram for macOS's `NavigationViewController` (a full-width slide, the old screen a third aside under a backdrop, 0.4 s); its curve, a `CASpringAnimation` cut at the duration, is approached by an ease-out.
- **`rememberPhotoPickerLauncher`** (`component/PhotoPicker`): the system's picker of one image, which hands back a JPEG of at most 2048 a side at quality 70 (`inputChatPhotoStatic` takes JPEG; the size and quality are Telegram for iOS's sign-up: `TGPhotoEditorResultImageAvatarMaxSize`, `compressImageToJPEG(quality: 0.7)`; Telegram for macOS sends the file as it is). Android: the Photo Picker (`PickVisualMedia`), decoded upright by `ImageDecoder` from API 28. iOS: `PHPickerViewController` in the compatible representation, redrawn upright by `UIGraphicsImageRenderer`. macOS: `NSOpenPanel` run modal, since a sheet takes a block JNA can't make. Web: a hidden `<input type=file>`. Android, iOS and web are adapted from Calf's `FilePickerLauncher` (Apache-2.0). The JVM and the web re-encode with Skia, which ignores EXIF orientation. Null on other desktops.
- **`PhoneNumberField`** (`component/PhoneNumberField`): Telegram for macOS's `Auth_PhoneInput` (`PhoneInputUI`, used by sign-in and by changing the number), a grey card, `shapes.medium`, `surfaceVariant`: 80 high on a laptop; on touch screens the same card in Telegram for iOS's rows of 57 with 20-point text (its country row is 67 with the notch over the number; own: the card, 16 in from the sides). It owns its heights; its width is 280 by default and the caller's modifier overrides it (the phone screen gives touch screens the full width up to 430).
  - Top half: the country in 15 regular and a `ChevronRight` in `outline` (`grayIcon`); a click opens `countryMenu`, the slot for the caller's `OptionPicker`. A hairline 10 in from each side splits the halves, `outline` at 0.12: over `grayBackground` that is Telegram's day `border` (`EAEAEA`). Telegram tints both with `border`, which its dark palettes set to `grayBackground` itself, so at night it shows neither; we fix that.
  - Bottom half: one `TextFieldState` with the digits of calling code and number, 14 medium monospaced. Telegram has two fields; one keeps the code and the number in step without moving focus or digits between fields. The output shows `+`, the calling code (`callingCodeLength`), a space and the number on `numberPattern`.
  - `numberPattern` is TDLib's `phoneNumberInfo.formattedPhoneNumber` (`getPhoneNumberInfo`, digits typed and `-` for those expected); what is not typed yet is drawn behind the text in `onSurfaceVariant`, as macOS's placeholder. Without a pattern it shows `numberPlaceholder`.
  - Digits only, at most 15 (E.164).
- **`ListItem`** (`component/ListItem`): M3's slots (overline, headline, supporting, leading, trailing) with the insets of macOS's rows (`GeneralRowItem`: 12 and 14), headline `bodyLarge`, supporting `bodyMedium`, and M3's list colours (`ListTokens`), all in `ListItemStyle`.
  - `position` (`Single`, `First`, `Middle`, `Last`) groups rows as macOS's settings do: rounded ends and a divider that starts under the text.
  - Without a position the row is plain, as in the lists of chats and countries.
- **`RichText`** (`component/RichText`): TDLib's `FormattedText` as text. `core:resources` parses it next to the language pack's markup (`FormattedText.toAnnotatedString`, `FormattedTextStyles`, `FormattedTextTest`); `RichTextStyle` styles the entities from the theme and takes the surrounding text style.
  - Links, e-mail addresses and phone numbers open.
  - Mentions, hashtags, cashtags and bot commands go to `onEntityClick`.
  - Custom emoji, spoilers' animation and media timestamps aren't drawn yet.
- **`CodeField`** (`component/CodeField`): the sign-in code, after macOS's `CodeEntryControl` (`TGUIKit`). It takes a `TextFieldState` and the code's `length`.
  - Cells on a laptop: 40 × 40, 8 apart, `shapes.medium`, digits 24 medium with tabular figures (macOS uses SF Mono). On touch screens Telegram for iOS's `CodeInputView`: 43 × 51, 15 apart, a radius of 15, digits 23 regular. The container colour of text fields; the cell to fill has a 1-point `primary` border while the field has focus, on every platform.
  - Font scale: cells grow with the system's font scale (Android goes up to 2×, non-linearly from Android 14), never under their size. When the row would be wider than its room, the cells narrow and keep their height.
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
- **Mobile:** the switch next to the theme's shows the pages as on touch screens (`ProvideFormFactor`); `-Dcatalog.mobile=true` starts with it, as `-Dcatalog.dark` does for the theme.
- **Screenshot tests:** `CatalogScreenshotTest` (Roborazzi 1.77.0) captures the home screen and every page, light and dark, and the pages of the fields at twice the font scale and on touch screens (`_mobile`). It runs on a frozen clock, moved 6 s on, so the QR code has faded in.
  - `./gradlew :catalog-app:recordRoborazziJvm` writes the goldens to `catalog-app/screenshots/`; `verifyRoborazziJvm` compares, allowing 1% of pixels to differ.
  - The goldens are recorded on macOS with SF Pro: on Linux the fonts differ, so CI would need goldens of its own.
- The catalog's texts are plain English: it is a developer's tool, not part of the app.

## Not done

- The feature screens still use their own dp values and the M3 roles they had. Moving them onto `KotogramTheme` and macOS's layouts is the next step.
- No theme choice in settings: the system's dark mode decides.
