# The chat list's bar in the window's title band on macOS (WIP)

Written on 2026-11-13 on the first Mac (macOS 26 or later, Nucleus `3.0.0-dev-202611061347` from the developer's fork,
branch `feature/native-view-overlay`). What the desktop does, what was measured, and what Nucleus needed. Work in
progress: the developer goes on with it at home.

## 1. The setup

- `jvm-app` puts `App()` in `KotogramWindowScaffold` (`core:designsystem:theme`, jvmMain): Nucleus's `WindowScaffold`
  with `TitleBarPlacement.Overlay(autoHideInFullscreen = false, passThroughToContent = true)` and a transparent
  `MaterialTitleBar` 52 high. The content is edge to edge: it starts at the window's top, under the band. A press in
  the band is hit-tested against the content first; a press nothing consumes drags the window (Nucleus's
  `titleBarHitTestHandler`). Nucleus still draws the window controls on Windows and Linux.
- `SearchScaffold` (`core:designsystem:components`) reads the band from `LocalWindowChromeInsets` through
  `platform/windowChrome()` (null off the JVM): its row is the band's height at the window's top, inset by the
  controls (the traffic lights' 80 on macOS, the caption buttons on Windows).
- Not tried yet: a drag and a double click on the bar's empty part, Windows, Linux.

## 2. Measured against AppKit

A throwaway AppKit app (Swift, `NSWindow` with `.fullSizeContentView`, `toolbarStyle = .unified`, the title "Чаты",
SF Symbols `camera` and `square.and.pencil` as toolbar items, the second `.prominent` with `.systemBlue`), the same
700 × 550 window, both captured by window id at 2× and compared pixel by pixel:

| | AppKit | Kotogram |
|---|---|---|
| Toolbar band | 52 | 52 |
| First traffic light, left edge | 19 | 13 (§4) |
| Title "Чаты" | from 109, 21 px high, 73 px wide | from 109, 21 px high, 72 px wide |
| New chat (prominent) | x 1312–1383, y 16–87 px | the same pixels |
| Camera | 1222–1296 px | 1223–1296 px |
| Buttons | 36, 8 apart, 8 from the right edge | the same |
| Camera's glyph | 20.5 wide (SF Symbol) | 16 wide (ours) |

The laptop's `SearchScaffoldStyle` holds these numbers: buttons of 36 without the 48 touch target, gaps of 8,
padding of 8, the title inset of 20 after the controls' inset.

## 3. Glass buttons in the band got no press (fixed in the fork, not yet confirmed)

- A `NativeView(placedAsOverlay = true)` (the AppKit glass `NSButton` of `GlassIconButton` and `GlassButton`) is
  mounted above the Compose surface so that AppKit hit-tests it and runs the control's own mouse tracking: the glass
  press animation needs that.
- In the title band the topmost view is AppKit's `NSTitlebarView`, and Nucleus adds `NucleusTaoPassthroughView`
  to it over the whole band (`decorated-window-tao/src/main/native/macos/NucleusTaoMetal.m`, `ensureTaoPassthroughView`).
  AppKit gives the press to that view, which forwards `mouseDown:` to the window's content view, that is Compose.
  The overlay button below it gets the hover (tracking areas don't depend on hit-testing) but no press: no press
  animation, and most likely no click either (the bar's actions are no-ops, so it doesn't show).
- The fix, uncommitted in the fork: a `hitTest:` on `NucleusTaoPassthroughView` that gives the point to an overlay
  first. Overlays sit in `NucleusTaoNativeClipView`s, siblings of the content view in the frame view; that class lives
  in another dylib (`native_view.m`), hence `NSClassFromString`:

```objc
- (NSView *)hitTest:(NSPoint)point {
    NSView *frameView = self.window.contentView.superview;
    Class clipClass = NSClassFromString(@"NucleusTaoNativeClipView");
    if (frameView != nil && clipClass != nil && !self.isHidden) {
        NSPoint inWindow = [self.superview convertPoint:point toView:nil];
        NSPoint inFrame = [frameView convertPoint:inWindow fromView:nil];
        for (NSView *sub in frameView.subviews.reverseObjectEnumerator) {
            if (sub.isHidden || ![sub isKindOfClass:clipClass]) continue;
            NSView *hit = [sub hitTest:inFrame];
            if (hit != nil) return hit;
        }
    }
    return [super hitTest:point];
}
```

- Published to Maven Local under the same version and checked loaded: the process mapped the new
  `libnucleus_tao_metal.dylib` from `~/Library/Caches/nucleus/native/darwin-aarch64/<hash>-<size>/`, which has the
  class name in it. Nobody has pressed a button since. If the press still doesn't animate, something else takes it.
- Without the fork's change the options are worse: the band's buttons drawn in Compose (not AppKit's glass), the
  `NSButton` moved into `NSTitlebarView` by hand (fighting the frame Nucleus gives its clip view), or a real
  `NSToolbar` (native, and the traffic lights' inset of §4 with it, but the bar is no longer `SearchScaffold`).

## 4. The traffic lights sit 6 left of AppKit's

`NucleusTaoMetal.m` places them at `min(height / 2, 20) + extraInset`, and `extraInset` is `kToolbarExtraInset` (6)
only when `window.toolbar != nil`, as AppKit does for a window with a toolbar. Ours has none: the first light starts
at 13 instead of 19. A fix belongs in the fork (a parameter that asks for the toolbar inset); not done.

## 5. Building the fork on this Mac

- Rust: `brew install rustup` (keg-only), then `PATH=/opt/homebrew/opt/rustup/bin:$PATH`; the toolchain and both
  macOS targets were already in `~/.rustup`. `~/.zshrc` is untouched.
- JNI headers: the shell's `JAVA_HOME` (Android Studio's JBR) has no `include`; use a full JDK, e.g. the JDK 25 in
  `~/.gradle/jdks/eclipse_adoptium-25-aarch64-os_x.2/jdk-25.0.5+8/Contents/Home`.
- The version comes from `GITHUB_REF`:
  `GITHUB_REF=refs/tags/v3.0.0-dev-202611061347 ./gradlew :decorated-window-tao:publishToMavenLocal --no-configuration-cache`.
  Kotogram takes the jar from `~/.m2` directly; restart the app, no Gradle cache to clear.

## 6. Open

- Press a glass button in the band (§3) and drag by the bar's empty part.
- The traffic lights' inset (§4); SF Symbols for the toolbar's icons on the Mac (the camera's glyph is 16 against
  20.5); AppKit's darker toolbar band with its hairline, or ours continuous with the list.
- File ▸ New Message, ⌘N, in the menu bar (the HIG: every toolbar item is also a menu command).
- Windows and Linux; the iPhone's pull-down growth and a shared capsule for several glass buttons.
