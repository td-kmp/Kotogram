# Telegram X: TDLib sessions, login and account switching

What Telegram X (TGX), the reference TDLib client, does with TDLib instances, login and accounts.
Kotogram reproduces the behavior, not the code, behind its own coroutine API (`docs/architecture.md`).

Source: `TGX-Android/Telegram-X`, `main` @ 2026-10-15 (commit "Fix answering call from notification
without microphone permission"). TDLib wrapper: `TGX-Android/tdlib`, `src/main/java/org/drinkless/tdlib/Client.java`.
Paths below are relative to `app/src/main/java/org/thunderdog/challegram/telegram/`.

---

## 1. The TDLib client wrapper (`Client.java`)

- **One receive thread per process** ("TDLib thread", daemon), started by the first `Client.create`.
  It never stops, and its receive timeout is 100000 s, so it never polls.
- **Batch receive of ready objects.** `nativeClientReceive(clientIds, eventIds, events, timeout)` returns
  up to **1000 `TdApi.Object`s per JNI call**, through TDLib's object JNI interface, **not JSON**.
- **Routing:** answers go by query id (`ConcurrentHashMap<Long, Handler>`), updates by client id.
  After `AuthorizationStateClosed` the client's update handler is removed: no more updates.
- The client **starts at once**: its constructor sends `getOption("version")`.
- **Handlers run synchronously on the receive thread.** TGX applies updates to its caches right
  there (`@TdlibThread`), without a thread hop.

## 2. One account = one long-lived `Tdlib`, with a replaceable `ClientHolder` (`Tdlib.java`)

- `Tdlib` (per account) lives as long as the app does. Its TDLib instance (`ClientHolder`, :256) is
  created on demand (`clientHolderUnsafe`, :1763) and replaced after every `AuthorizationStateClosed`
  (`updateAuthState`, :1196): it resets the account's in-memory state, then creates a new client
  unless the account is paused.
- **Eager startup** (`ClientHolder.init`, :317): right after `Client.create`, TGX sends
  `setTdlibParameters` **without waiting for `authorizationStateWaitTdlibParameters`**, plus a
  `SetAlarm(0)` (or `GetProxies`) and `GetApplicationConfig`. If parameters take longer than 1 s,
  the app shows "optimizing" (`setIsOptimizing`).
- **Per-account parameters** (:6172): `databaseDirectory = getTdlibDirectory(accountId)`
  (`…/x_account<N>`, TdlibManager :2386), `filesDirectory` likewise, `useTestDc = isDebugInstance()`.
  Instance modes are `NORMAL`, `DEBUG` (test DC) and `SERVICE`.

## 3. Pausing idle accounts (`Tdlib.java` :943–1050)

- Every account keeps its `Tdlib`, but an **idle account's TDLib instance is closed** ("paused") after
  a timeout. Nothing is lost: the next request wakes it up (`wakeUp`, `clientHolder()` creates a new
  client).
- **A paused account must be idle:** authorization finished (or not started), no keep-alive flag,
  device registered for pushes, and **reference count 0**. References are taken by UI, jobs, sync,
  notifications, live location, calls and requests (`REFERENCE_TYPE_*`, :729).
- **Timeout by usage** (`getPauseTimeout`):

  | Situation | Timeout |
  |---|---|
  | no UI (background) | 5 s |
  | single account | 15 min |
  | among the 3 most recently used | 15 min |
  | among the 5 most recently used | 7 min |
  | among the 10 most recently used | 2 min |
  | otherwise | 5 s |

## 4. Login (`TdlibUi.addAccount` :1446, `TdlibManager.newAccount` :1461, `PhoneController`)

- **Adding an account reuses a slot.** An existing **unauthorized** account (not the current one, not
  logging out) is reused if there is one; otherwise a new account id (slot) is appended. The phone
  screen then opens for that account's `Tdlib`.
- **Existing number** (`PhoneController` :1113): when adding an account, if the entered number
  already belongs to one of the accounts, TGX **switches to that account instead of logging in
  again** (`SWITCH_REASON_EXISTING_NUMBER`).
- **No QR request on mobile.** TGX handles `authorizationStateWaitOtherDeviceConfirmation` but never
  calls `requestQrCodeAuthentication`. TDLib only allows `setAuthenticationPhoneNumber` and
  `requestQrCodeAuthentication` from `WaitPhoneNumber` or later steps, **not from
  `WaitOtherDeviceConfirmation`** (td_api.tl). So leaving QR login takes a log out, as Kotogram does.
- **Multi-account parameters.** `requestQrCodeAuthentication` and `registerDevice` take
  `other_user_ids`, "user identifiers of other users currently using the application". TGX passes them
  for push registration (`TdlibSettingsManager.setRegisteredDevice`).

## 5. Switching accounts (`TdlibManager.changePreferredAccountId` :1287)

- **Only to an authorized, non-service account.** TGX waits for `awaitInitialization` first, then
  switches on the UI thread.
- The preferred account id is **persisted** (`savePreferredAccountId`), and both the old and the new
  account are `markAsUsed()`. That usage order drives the pause timeouts above.
- Listeners get `notifyAccountSwitched(account, myUser, reason, oldAccount)`. The reasons are
  `UNAUTHORIZED`, `NAVIGATION`, `USER_CLICK`, `CHAT_OPEN`, `CHAT_FOCUS` and `EXISTING_NUMBER`.

## 6. Log out (`Tdlib.signOut` :1147, `updateAuthState`)

1. **Switch first.** `switchToNextAuthorizedAccount()` moves the UI to another authorized account
   before anything else.
2. Send `LogOut` while holding a job reference (`JOB_ID_SIGN_OUT`), so the instance isn't paused
   meanwhile.
3. On `AuthorizationStateClosed` while logging out, TGX **erases the account's TDLib database and
   files** and creates a fresh client. **The account slot stays**, now unauthorized, and
   `newAccount` reuses it later.
4. Logged-out accounts that still hold files are cleaned up in the background
   (`cleanupLoggedOutAccounts`).

---

## 7. Kotogram today vs TGX

| Topic | TGX | Kotogram (`feature/KTG-2`) | Verdict |
|---|---|---|---|
| Receive loop | one thread per process | one loop per process (`TdReceiver`) | same model |
| Wire format | object JNI, batches of 1000 | JSON (`td_json`) + kotlinx decode per event | **not adopted**: tdlib-kmp stays on `td_json` on every platform, one implementation for all targets (decision 2026-10-17). Speed comes from a fast JSON path, as Unigram shows ([tdlib-clients.md](tdlib-clients.md)) |
| Update handling | synchronous on the TDLib thread | `SharedFlow` per client, collectors on other dispatchers | fine for UI; caches should collect without hopping (§8) |
| Account object | long-lived `Tdlib`, replaceable client | Koin `AccountScope` + `TdSession`, client replaced while unauthorized | same model |
| Startup | `setTdlibParameters` sent eagerly | waits for `WaitTdlibParameters` | **adopt eager** |
| Idle accounts | paused after a usage-based timeout, woken on demand | one active account, no pausing | **adopt** when several accounts are shown |
| Add account | reuses an unauthorized slot | new `account-N` | **adopt reuse** |
| Existing number | switches to the existing account | no check | **adopt** |
| `other_user_ids` | passed | not passed | **adopt** |
| Switch | only to authorized accounts; persisted; usage order | always the first account; not persisted | **adopt** |
| Log out | switch first → `LogOut` → erase → slot stays | `LoggingOut` → splash → delete dirs → open next/new | **adopt switch-first**; keeping the slot is optional, since reusing it on add has the same effect |
| Process exit | doesn't close TDLib (Android kills the process) | closes on `stopKoin()` | keep ours: the desktop JVM crashes if TDLib is in `td_receive` at exit |

## 8. To reproduce in Kotogram (behind the new API)

1. **`TdSession` eager startup:** send `SetTdlibParameters` right after `TdClient.create()`, with no
   `GetAuthorizationState` kick and no wait for `WaitTdlibParameters`.
2. **`TdAccounts`:**
   - `addAccount()` reuses an unauthorized account;
   - `switchTo(id)` only to authorized accounts;
   - the preferred account is persisted;
   - each account gets a usage timestamp.
3. **Log out:** switch to the next authorized account first, then `LogOut`; erase on `Closed`.
4. **Existing number:** if `setPhoneNumber` gets a number that belongs to another open account,
   switch to it (auth data needs the accounts' phone numbers).
5. **`other_user_ids`:** pass the user ids of the other authorized accounts to
   `requestQrCodeAuthentication`, and later to `registerDevice`.
6. **Pausing:**
   - `TdSession.pause()` closes the instance without marking it logged out, and the next `send`
     reopens it;
   - a reference count kept by the UI and background jobs;
   - TGX's timeout table.

   Needed once more than one account is shown.
7. **tdlib-kmp:** stays on `td_json` everywhere (no object/JNI path). The cache collector should run
   without a dispatcher hop. See [tdlib-clients.md](tdlib-clients.md) §7.
