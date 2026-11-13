# The `online` option and the network type: a stale chat list after the background

Found on 2026-11-13 on the SM-G990E (Android 16) with a release build: back from nine minutes in the background,
the chat list showed nothing new for about two minutes. This note is what was read and measured, and what is built (§5).

Sources: the phone's own logs over adb; TDLib in `td-pack/td` (1.8.68 there, one version behind the library);
Telegram X `main` as fetched on 2026-11-13 (`BaseActivity.java`, `telegram/Tdlib.java`, `telegram/TdlibManager.java`,
`core/WatchDog.java`); Telegram for Android's `ApplicationLoader.java` and Telegram-iOS's
`submodules/Reachability/Sources/Reachability.swift`, both fetched the same day;
Compose Multiplatform 1.12.1's source jars; the Nucleus fork's `feature/native-view-overlay`.

## 1. What happened

| Time | Event | Seen in |
|---|---|---|
| 01:23:36 | the app leaves the screen | `wm_on_stop_called` in the events log |
| 01:23:41 | Android destroys its three TCP sockets | `InetDiagMessage: Destroyed live tcp sockets for uids={…}`; TDLib: `Session connection … was closed: [PosixError : Software caused connection abort : 103]` on every session |
| 01:26:56 | a reconnect in the background fails | TDLib: `Connection timed out : 110` |
| 01:32:49 | the app is back, its network is allowed at once | `Firewall rule changed: <uid>-background-allow` in `dumpsys netpolicy` |
| 01:34:54 | TDLib's first traffic | `GetConfigActor`, then new sessions |

- Android 16 keeps a background app off the network: `dumpsys netpolicy` shows `blocked=APP_BACKGROUND` for the
  uid, with battery saver and data saver off. Nothing arrives in the background without a push or a foreground
  service, whatever the app does.
- The process and its coroutines lived through it: the same pid, no `FZ :` line from Samsung's
  `FreecessController` for the uid, and TDLib's thread logged at 01:26:56.

## 2. Why TDLib waits

- `ConnectionCreator::client_loop` (`td/telegram/net/ConnectionCreator.cpp`): while `online` is false, each
  attempt adds to `ClientInfo::Backoff`, which doubles to `MAX_BACKOFF`: 300 s on Android and iOS, 16 s elsewhere
  (`ConnectionCreator.h`). While `online` is true the backoff isn't consulted.
- `setOption("online", …)` goes through `OptionManager` to `StateManager::on_online` and
  `ConnectionCreator::on_online`, which clears the backoff and the flood controls and runs `client_loop` at once.
  It also sets the user's status for others (`OnlineManager`). The default is false (`OnlineManager::is_online_`).
- `setNetworkType` goes through `StateManager::on_network` to `ConnectionCreator::on_network`: the same reset.
  `td_api.tl`: "forces all network connections to reopen … so it must be called whenever the network is changed,
  even if the network type remains the same".
- Kotogram calls neither: no lifecycle or connectivity code in the sources.

## 3. What Telegram X does

- `BaseActivity.onResume` calls `setOnline(true)` and `onPause` calls `setOnline(false)`, for the activity's
  current `Tdlib` only. `setTdlib` (an account switch) takes it from the old account and gives it to the new one.
- `Tdlib.setOnline` sends `SetOption("online", OptionValueBoolean)`; a client that starts while its account is
  online is sent it again.
- `TdlibManager.setNetworkType` sends `SetNetworkType` to every account, and `onConnectionAwake` sends it again
  to each account that isn't connected (`resendNetworkTypeIfNeeded`).

## 4. Where the signal can come from

`androidx.lifecycle`'s `RESUMED`, read at the root of `App()`:

| Platform | `RESUMED` means | Read in |
|---|---|---|
| Android | the activity is resumed, Telegram X's signal | |
| iOS | the view appeared and the scene is in the foreground and active | `ComposeContainerLifecycleDelegate.ios.kt` (the rule that combines the three wasn't read) |
| Web | the page has focus (`focus`, `blur`); `visibilitychange` is start and stop | `ComposeWindowInternal.web.kt` |
| Desktop | always: a scene's default owner is set to `RESUMED`, and Nucleus's tao window doesn't replace it | `PlatformContext.skiko.kt`; no lifecycle code in `decorated-window-tao` |

So the desktop needs the window's own state. Nucleus has `DecoratedWindowState.isActive`, which stays true while
focus is anywhere in the window's tree, and its scene hosts set `WindowInfo.isWindowFocused`. Not confirmed: that
`state` is reachable in the content lambda `jvm-app` gives `KotogramWindow`.

`LocalWindowInfo.isWindowFocused` isn't the signal on Android: the window loses focus to the app's own dialogs.
`MainActivity` handles `orientation|screenLayout|screenSize|smallestScreenSize|locale|layoutDirection`, so a
switch of the dark theme still recreates it: a pause and a resume in a row.

## 5. What is built (2026-11-13)

The author's own on top of Telegram X's behaviour.

- **`feature:connection`**, not `core:account`, which runs accounts and owns no subsystem
  ([architecture](../architecture.md)). `domain`: `ConnectionRepository.setInForeground`, for the root's
  lifecycle effect. `data`: `DefaultConnectionRepository` (a `single`, holds `inForeground` and the shared
  network flow), and in `scope(AccountScope)`:
  - `OnlineOptions : TdlibOptions`: `"online"` is true while the app is in front and the account is
    `shown.adding ?: shown.active` (one account online, as Telegram X).
  - `NetworkTypeUpdates : AccountUpdates`: `setNetworkType` on every change of the network (the shared flow
    replays the last one to a new client, as Telegram X's `modifyClient`), and again when the app comes to the
    front to a client whose state isn't `connectionStateReady` (Telegram X's `resendNetworkTypeIfNeeded`).
- **The network**, native as Telegram's apps:
  - Android: `registerDefaultNetworkCallback`, as Telegram for Android (`ApplicationLoader`); Telegram X still
    reads `CONNECTIVITY_ACTION` (`WatchDog`). Wi-Fi and Ethernet are `networkTypeWiFi`, cellular without
    `NET_CAPABILITY_NOT_ROAMING` is roaming, anything else mobile: Telegram X's mapping.
  - iOS (`IosNetworkMonitor`) and macOS (`core:platform:macos`'s `MacNetworkMonitor` through
    NucleusNativeAccess): `nw_path_monitor`, as Telegram's `Reachability` (`submodules/Reachability`, built for
    iOS 12 and macOS 14): satisfied and cellular is mobile, satisfied otherwise Wi-Fi, else none. A report goes
    out only when the path's status or interfaces change. Telegram keeps only changes of the type; TDLib asks
    for every change of the network.
  - Windows (`core:platform:windows`, `mingwX64`): Network List Manager's `INetworkListManagerEvents`
    (`ConnectivityChanged`), as Telegram Desktop: Qt's `QNetworkInformation` there, and lib_base's own NLM sink
    before Qt 6.2 (`base_network_reachability_win.cpp`). Disconnected is none, anything else other: NLM tells no
    transport. COM runs on one thread of its own, with a static sink whose `AddRef` and `Release` don't count.
  - Linux (`core:platform:linux`, `linuxX64`): rtnetlink's link, address and route groups wake it, the default
    route in `/proc/net/route` and `/proc/net/ipv6_route` decides, as GLib's `GNetworkMonitorNetlink` does, one of
    the backends of Qt's `QNetworkInformation` that Telegram Desktop uses. The route's device is Wi-Fi with
    `wireless` or `phy80211` in `/sys/class/net/<name>`, mobile with `DEVTYPE=wwan`, other otherwise (own rule).
    NetworkManager over D-Bus, Qt's first backend, needs libdbus, which the Kotlin/Native sysroot doesn't have.
  - Web: `navigator.onLine` and the window's `online` and `offline` events, as Telegram Web A
    (`useBrowserOnline`) and Web K (`apiManagerProxy`): online is other, offline none.
  - The JVM picks by `os.name` and `os.arch`: macOS on arm64, Windows and Linux on x64; any other desktop
    reports nothing. A monitor that fails is logged and stops; TDLib then keeps its default.
- **Checked on 2026-11-13:** `feature:connection:data` compiles for the JVM, Android, the iOS simulator and wasm;
  the three native modules compile, link (the DLL and the `.so` cross-linked on the Mac) and are bundled in their
  jars. On this Mac a throwaway JVM test went through `platformNetworkMonitor()` and FFM to `nw_path_monitor`: the
  first type in 33 ms (`networkTypeWiFi`, the default route on `en0`), three collectors at once and five restarts
  alike, no second report on a steady network, and the repository's replay. Watched live: Wi-Fi off gave
  `networkTypeNone` and on `networkTypeWiFi` within a second each; joining the iPhone's hotspot gave a `none` and
  a `wiFi` 0.3 s apart (the hotspot is Wi-Fi to macOS, not cellular). `spotlessApply` and `detekt` pass. Nothing
  ran on Windows, Linux, a phone or a browser, and no TDLib client was given the reports.
- **Wired:** `feature:connection:impl`'s `ConnectionEffect()` sits at the root of `App()` and writes
  `rememberAppForeground()`, a platform's own signal as a `State`, to `setInForeground` through `snapshotFlow`,
  and false when it leaves the composition. Not a recomposition: a window out of sight renders no frames, and
  composition runs on frames (Nucleus's macOS host drops every frame while the window is minimized or occluded,
  `TaoComposeSceneHost.renderFrameSuspending`), so a value read in composition stayed true in the background. Android: the activity's lifecycle at `RESUMED` (`LocalLifecycleOwner` above
  the navigation is `MainActivity`), Telegram X's `onResume` and `onPause`. iOS: `UIApplication`'s state and its
  did-become-active, will-resign-active and did-enter-background notifications, as Telegram-iOS's `AppDelegate`.
  Desktop: the window's focus, `LocalWindowInfo.isWindowFocused`, as Telegram Desktop's `hasActiveWindow`.
  `KotogramWindow` provides its own `KotogramWindowInfo` (Nucleus's `WindowInfo` by delegation, plus `isMinimized`,
  `isMaximized`, `isFullscreen`) whose focus is the window state's active and not minimized: Nucleus's own stayed
  true with the window minimized on macOS (`windowDidMiniaturize` goes into `isMinimized`, not into the focus).
  Telegram Desktop's idle timeout (`offlineIdleTimeout`) isn't taken. Web: `focus` and
  `blur` of the window and `document.hasFocus()`, as Web A's `useBackgroundMode`. `shared` starts
  `connectionDataModule`.
- **Checked:** `KoinGraphTest` (real TDLib, this Mac's network monitor) sets the app in front and TDLib answers
  `getOption online` with true; nothing is logged against `setOption online` or `setNetworkType`, so options
  sent before `setTdlibParameters` are taken. On this Mac (2026-11-13), `jvm-app` with `OnlineOptions`'s log:
  minimizing the window over Accessibility gave false and restoring it true, twice, with the app staying active
  throughout; another app in front gave false, and back true. Read in composition, the minimize gave nothing.
  Not run on a phone, Windows, Linux or a browser.
- **Open:** whether several Kotlin/Native libraries (`appkit`'s and a platform module's) live well in one JVM.
  Then the phone: the chat list fresh right after the background (§6).

## 6. Seeing it again

Read-only over adb; the phone's screen isn't touched.

- `adb logcat -b events -d | grep com.xbot.kotogram`: `wm_on_stop_called` and `wm_on_resume_called` bound the
  background, `am_proc_start` tells whether the process was restarted.
- `adb logcat -d --pid=$(adb shell pidof com.xbot.kotogram) | grep ' TDLib '`: at verbosity 2 the closed sessions
  and their errors are there, successful connects aren't.
- `adb logcat -d | grep 'Destroyed live tcp sockets for uids'`, and `adb shell dumpsys netpolicy`: the `UID=`
  line has `blocked=APP_BACKGROUND`, the `Firewall rule changed: <uid>-background-allow` lines have the times.

Aside from the same log: TDLib's own `contacts.getBirthdays` failed six times in a minute with `FLOOD_WAIT_97xx`.
Not looked into.
