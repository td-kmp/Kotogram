# The `online` option and the network type: a stale chat list after the background

Found on 2026-11-13 on the SM-G990E (Android 16) with a release build: back from nine minutes in the background,
the chat list showed nothing new for about two minutes. **Nothing is built yet.** This note is what was read and
measured, and a design to start from.

Sources: the phone's own logs over adb; TDLib in `td-pack/td` (1.8.68 there, one version behind the library);
Telegram X `main` as fetched on 2026-11-13 (`BaseActivity.java`, `telegram/Tdlib.java`, `telegram/TdlibManager.java`);
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

## 5. A design to start from

The author's own, not built and not reviewed.

- `core:account`: `OnlineStatus` (`isOnline: StateFlow<Boolean>`), `MutableOnlineStatus`, `DefaultOnlineStatus`,
  a `single` in `accountsModule`.
- `OnlineOptions : TdlibOptions` in `scope(AccountScope)`: `"online"` is true while the status is and its session
  is the one in front, `shown.adding ?: shown.active`, as one account is online in Telegram X.
  `AccountManager.sendOptions` already sends every change of an owner's map to its client, the first before
  `setTdlibParameters`. Its `combine` waits for every owner, so the flow has to emit at once.
- `shared`: `App(isActive: Boolean = isResumed())` writes the status in a `DisposableEffect`; `jvm-app` passes
  the window's state.
- `setNetworkType` is the other half: on each change of the default network, to every client, and again on
  resume to the clients that aren't connected. It is also what brings back at once the accounts that aren't in
  front. It needs a platform observer (`ConnectivityManager`'s default network callback on Android).
- Open: whether TDLib takes `setOption online` before `setTdlibParameters` (the options sent there now are
  answered only after it); a test over `FakeTdLib` in `core/account/src/jvmTest`.

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
