# How TDLib clients are built: a comparison

Seven implementations, read from source on 2026-10-17, against Kotogram's `tdlib-kmp` client and the
`TdAccounts`/`TdSession` session layer. Telegram X is covered in detail in
[telegram-x-sessions.md](telegram-x-sessions.md).

| Project | Kind | Source (last commit) |
|---|---|---|
| Telegram X | Android app, multi-account | `TGX-Android/Telegram-X` (2026-10-15) |
| Unigram | Windows app, multi-account | `UnigramDev/Unigram` (2026-10-13) |
| tdl | Node.js library | `eilvelia/tdl` (2026-07-23) |
| tdlight-java | JVM library | `tdlight-team/tdlight-java` (2026-10-13) |
| telegram-bot-api | official server, thousands of TDLib clients | `tdlib/telegram-bot-api` (2026-08-25) |
| tdlib-rs | Rust library | `FedericoBruzzone/tdlib-rs` (2026-04-12) |
| pytdbot | Python asyncio library | `pytdbot/client` (2026-10-10) |

---

## 1. Transport

| | Interface | Receive loop | Timeout | Stop |
|---|---|---|---|---|
| Telegram X | object JNI, batches of 1000 | 1 daemon thread per process | 100000 s | never |
| Unigram | `td_json` (fork with `request_id` instead of `@extra`) + fast JSON parsers | 1 background thread `TdReceive` | — | never |
| tdl | `td_json` | 1 native thread, events posted to the JS thread | 10 s | when no clients are left |
| tdlight-java | object JNI, batches of 100, grouped per client | 1 thread per `ClientFactory` | 2 s | when the factory closes and no clients are left |
| telegram-bot-api | C++ `ClientActor` (in-process actors) | TDLib's own scheduler | — | — |
| tdlib-rs | `td_json` | the caller runs `receive()` in a loop (e.g. `spawn_blocking`) | 2 s | the caller decides |
| pytdbot | `td_json` | 1 executor thread per `ClientManager` | — | on close it **sends a dummy request to wake `td_receive`** |
| **Kotogram (tdlib-kmp)** | `td_json` on all targets | 1 coroutine on its own thread per process | 1 s | when no clients and no pending requests are left |

**Conclusion.** Every implementation uses one receive loop per process (or per manager) and routes by
client id. Kotogram's model is the common one. **Unigram reaches desktop-class speed on `td_json`**:
it wins with a fast JSON parser, not a different interface. That supports keeping `td_json` as the
single transport.

## 2. Answers and updates

| | Matching | Updates to the app | Order |
|---|---|---|---|
| Telegram X | query id → handler map | synchronous on the receive thread | kept |
| Unigram | `request_id` → handler map | synchronous on the receive thread (caches) + event aggregator to UI | kept |
| tdl | `@extra` → per-client map | `EventEmitter` + async iterator that ends on `Closed` | kept |
| tdlight-java | query id → per-client map | per update, or **per batch** (`onUpdates(List)`) | kept |
| tdlib-rs | `@extra` → global oneshot map | returned from `receive()` | caller's |
| pytdbot | `@extra.id` → futures | **a task per update** (sequential queue optional) | **not kept by default** |
| **Kotogram** | `@extra` → global map | `Flow<Update>` per client, completes on `Closed` | kept per collector |

**Conclusion.** Kotogram's `updates` flow ending on `Closed` is the same as tdl's async iterator.
pytdbot's default shows what to avoid: parallel handling breaks TDLib's in-order rule. Kotogram's
"one cache collector" rule (`TdClient` KDoc) covers that.

## 3. Requests around close

| | Pending requests when `Closed` arrives |
|---|---|
| tdl | rejected client-side ("Client was closed") |
| tdlight-java | completed client-side with `Error(500, "Instance closed")` |
| telegram-bot-api | queued commands failed client-side |
| Telegram X, Unigram | left to TDLib |
| **Kotogram** | left to TDLib, which answers with 500 itself (verified in this project). This works because the pending map is global and doesn't depend on the client being registered |

## 4. Startup

| | `setTdlibParameters` |
|---|---|
| Telegram X, Unigram (apps) | **sent right after the client is created**, together with options and `GetApplicationConfig`. No wait for `WaitTdlibParameters` |
| tdl, tdlight-java, pytdbot, telegram-bot-api (libraries) | sent on `authorizationStateWaitTdlibParameters`; tdl and bot-api buffer requests made before init |
| **Kotogram** | `TdSession` sends it on `WaitTdlibParameters` |

**Conclusion.** Sending the parameters eagerly is an app optimization. It belongs in `TdSession` (the app
layer), not in `TdClient`: the library stays neutral, the same split tdlight makes between
`TelegramClient` and `SimpleTelegramClient`.

## 5. Accounts and sessions (the two apps)

| | Telegram X | Unigram | Kotogram |
|---|---|---|---|
| Account object | `Tdlib`, lives for the whole app | `Session` = **a DI container** (`container.Resolve<T>()`) | Koin `AccountScope` |
| TDLib instance | replaceable `ClientHolder` | replaceable client in `ClientService` (`Close(restart)`, `Delete(restart)`) | replaceable client in `TdSession` |
| Directory | `x_account<N>` | `LocalFolder/<id>` | `account-N` |
| Active account | persisted, usage order | `ActiveSession` and `PreviousSession` persisted | first account, not persisted |
| Log out | switch to the next account first → `LogOut` → erase data, keep the slot | `LoggingOut`: block that window → `Closed`: switch to previous/first/new, remove the session, delete its folder | `Closed`: close scope, delete dirs, open next/new |
| Abandoned "add account" | slot reused by the next add | an inactive session at `WaitPhoneNumber`/QR is sent `Destroy` when there are other sessions | not handled |
| Idle accounts | instance paused after a usage-based timeout | — | — |

## 6. QR ↔ phone switch (Unigram `Session.cs`)

Unigram makes the switch **one operation**:

1. `SetAuthenticationPhoneNumberAsync(fn)` (or `RequestQrCodeAuthentication(otherUserIds)`) stores
   the action and sends `LogOut`.
2. On `Closed`: `ClientService.TryInitialize()`, a new client in the same session.
3. On `WaitPhoneNumber`: it sends the stored action and completes the caller's task with its result.

So **the QR code stays valid while the user types the number**. The log out happens only on submit,
and the caller gets the result of `setAuthenticationPhoneNumber`. Kotogram logs out as soon as the user
picks "log in by phone", which drops the QR earlier than needed.

---

## 7. What this changes for Kotogram

Library (`tdlib-kmp` `TdClient`):

- **Keep:** one loop, `td_json` everywhere, global `@extra` map, `updates` ending on `Closed`, no auth
  automation in the core. Every implementation agrees on these, or splits the layers the same way.
- **Improve:** stop polling. Use a long `td_receive` timeout and stop the loop right after dispatching
  the last client's `Closed`, or wake it with a cheap request (pytdbot) when it must stop.
- **Later:** a fast JSON path (Unigram's lesson), since `td_json` stays.

App (`TdSession` / `TdAccounts` / auth):

1. Send `setTdlibParameters` eagerly from `TdSession` (Telegram X, Unigram).
2. QR → phone as one operation, Unigram-style: the log out happens on submit, then the stored
   `setAuthenticationPhoneNumber` runs, and its result goes back to the screen.
3. Persist the active and previous account. On log out, switch to the previous account first
   (Telegram X, Unigram).
4. Clean up an abandoned "add account": `Destroy` it (Unigram) or reuse its slot (Telegram X).
5. Pass `other_user_ids` to `requestQrCodeAuthentication` (Unigram, Telegram X).
6. If the number entered when adding an account is already open, switch to that account (Telegram X).
7. Pause idle accounts once several are shown (Telegram X).
