# Kotogram — messaging core: from a TDLib update to a pixel (design v1)

Status: **proposal; M0 and the start of M1 are implemented** (§14). Module names follow KTG-6 (`core:tdlib`, `core:model`, `feature:x:impl`); reducers are now one `Reducer` per entity in a `CompositeReducer`, not one dispatching `when`. It fills the gap left by [architecture.md](../architecture.md)
(modules, DI, navigation) and [accounts.md](accounts.md) (accounts, scopes, login): which entities exist between
`TdClient` and Compose, and how they relate.

Given: the published `tdlib-kmp` `1.8.69-1.0.0-alpha02` as it is. No library changes are proposed. §12 lists
library facts the design works around.

Evidence: [research/tdlib-survey/](../research/tdlib-survey/). Reports are cited by number (05 §3.2). Tags: **CS§n** TDLib contract sheet (04 §1),
**TGX#n** Telegram X hidden rules (03 §4), **UNI#n** Unigram hidden rules (04 §5), **A#n / B#n / N#n** Kotlin
client anti-patterns, best ideas and unsolved gaps (02 §4). **[O]** marks my own design with no direct
precedent; everything unmarked follows at least one client or an official contract.

---

## 1. The design in one screen

```
 NativeTdLib ── TdReceiver loop ── TdClient.updates        (one subscriber, registered before the first send)
                                        │
 ┌──────────────────────────────────────▼─ per account, one serial lane ───────────────────────────────┐
 │ ClientHolder: owns the instance, runs  collect { update -> store.apply(update) }                    │
 │                                        │                                                            │
 │ AccountStore   1. state = reduce(state, update)   ──► state: StateFlow<AccountState>   never misses │
 │                2. updates.tryEmit(update)         ──► updates: SharedFlow<Update>      from now on  │
 │                   UpdateFile also goes to FileStore (per-file flows, not in the snapshot)           │
 └──────────────────────────────────────┬──────────────────────────────┬───────────────────────────────┘
                                        │ selectors, identity distinct │ filtered by chat
                           repositories (read projections)      ChatHistory (one per open chat)
                                        │                              │
                           ViewModels: memoising row mappers on Dispatchers.Default
                                        │ instance-preserving rows
                           LazyColumn(key, contentType) ── strong skipping compares by ===
```

Five decisions carry the design:

1. **One ordered applier per account.** TDLib requires updates to be applied in the order received (CS§1.1).
   Telegram X, Unigram, nngGram and pixelGram keep that order; clients that fan out early lose it and then add race
   buffers for an order TDLib already guarantees (A2, A3).
2. **One immutable snapshot per account, a pure fold of the updates.** `AccountState = updates.fold(Empty, reduce)`.
   A screen reading a chat and its sender sees one instant, without `combine`. Nothing is written except by the
   applier. Structural sharing keeps unchanged entities the same instance, which is what Compose skipping needs.
3. **Two outputs with different promises.** `state` never misses anything. `updates` delivers only from the moment
   of subscription, for consumers whose lifetime begins with a subscribe-then-request step (a history window).
   Anything that must not miss an update is a reducer over the snapshot, not a subscriber.
4. **Messages are not in the snapshot.** TDLib keeps them; the app holds a window per open chat (`ChatHistory`) as
   a separate single-writer state machine.
5. **One mapping step, where the UI needs it.** DTO → row model in the ViewModel, memoised. See decision D1 in §13.

The combination of 1 and 2 on coroutines is **[O]**: N1 found nobody who has it. Each part has precedent: the single
applier (Telegram X, Unigram), reducers and persistent snapshots (Pingogram), derived lists off the writer (Pingogram).

---

## 2. Entities and lifetimes

| Entity | Responsibility | Lifetime | Thread | Owner | Module |
|---|---|---|---|---|---|
| `TdLib` (`NativeTdLib`) | the library seam; a fake or recording decorator goes here (§11) | process | library | Koin root | library |
| `DefaultAccountRepository` (exists) | accounts, active account, opens and closes account scopes | process | Default | Koin root | `core:tdlib` |
| Update lane | one `limitedParallelism(1)` dispatcher that `apply` runs on, for all accounts (§3) | process | — | Koin root | `core:tdlib` |
| `AppPresence`, `NetworkMonitor` | platform seams: foreground, connectivity | process | Main | Koin root, `expect` function | `core:tdlib` |
| `DefaultClientHolder` (changes) | one account's instances; the only subscriber of `TdClient.updates`; `send` | account | lane + any | account scope | `core:tdlib` |
| `DefaultAccountStore` | `apply(update)`; publishes `state` and `updates` | account | written on the lane, read anywhere | account scope | `core:tdlib` |
| `AccountState` + `reduce` | the snapshot and the pure fold | value | any | store | `core:tdlib` |
| `FileStore` | per-file `StateFlow<File>` for observed files (§7) | account | written on the lane | account scope | `core:tdlib` |
| `FileDownloads` | the single arbiter of `DownloadFile`/`CancelDownloadFile` per file (§7) | account | serial | account scope | `core:tdlib` |
| `OpenChats` | balanced `openChat`/`closeChat`, delayed close, "is this chat open" | account | any | account scope | `core:tdlib` |
| `InstanceSetup` | per-instance setup: parameters, options, `online`, network type (§8) | instance | Default | account scope | `core:tdlib` |
| `Default…Repository` | read projections over `state`; commands via `ClientHolder`, returning `Either` | account | any | account scope | the owning `feature:x:data` |
| `ChatHistory` + `ChatHistoryFactory` | the message window of one open chat (§6) | screen | own serial lane | ViewModel | interface in `feature:chats:domain`, implementation in `feature:chats:data` |
| `MessageViews` | visible items → `viewMessages` (§6) | screen | Main → any | ViewModel | `feature:chats:impl` |
| `ChatListViewModel`, `ChatViewModel` | Orbit state; memoising mappers (§5) | screen | mapping on Default | account `ViewModelStoreOwner` | `feature:chats:impl` |
| `TdImageFetcher` + `Keyer` | Coil 3 bridge from `TdImage` to pixels (§7) | process | Coil, bounded | process `ImageLoader` built in `shared` | `feature:files:data`, or `core:tdlib` if every feature needs it: undecided; `TdImage` in `core:model` |
| `NotificationController` | snapshot's notification groups → platform renderer (§9) | account | serial | account scope | `core:tdlib` + platform |

### Relations

```
Koin root
 ├─ DefaultAccountRepository ──creates/closes──► account scope (AccountId), one per account directory
 ├─ TdLib, AppPresence, NetworkMonitor, ImageLoader
 └─ account scope
     ├─ DefaultClientHolder ──apply──► DefaultAccountStore ──► AccountState (value)
     │        │                              ├─► FileStore
     │        └─ InstanceSetup (setup per instance)
     ├─ FileDownloads ──send──► ClientHolder
     │        └──reads──► FileStore
     ├─ OpenChats ──send──► ClientHolder
     ├─ NotificationController ──reads──► AccountState.notifications
     ├─ Default…Repository ──reads──► AccountStore.state        ──send──► ClientHolder
     └─ ViewModels
          ├─ read repositories; own memoising row mappers
          └─ ChatViewModel owns ChatHistory ──subscribes──► AccountStore.updates ──send──► ClientHolder
```

Direction is one-way: the holder pushes into the store, and the store knows nothing about the client. That is why the
reducer can be tested without TDLib, and why a late resolve in Koin can't lose updates: the store is a constructor
parameter of the holder, so it exists before the first `send`.

Only `core:tdlib` sees `TdClient`. `ClientHolder.updates` is removed from the interface: consumers get
`AccountStore.updates`, which is after-apply, so a consumer reacting to an update always finds `state` at least that
new. `TdClient.updatesOf<T>()` must not leave `core:tdlib` (A2).

---

## 3. `ClientHolder`: the only subscriber

Replaces `instances.flatMapConcat { it.updates }`, which never advances past the first instance because
`TdClient.updates` never completes (05 §1.3), and which also loses the startup burst because `MutableSharedFlow`
with replay 0 drops everything while nobody is subscribed (05 §2.1).

The code is `DefaultClientHolder.kt`: `runInstances` loops, and for each instance `runInstance` does this:

```kotlin
supervisorScope {                                   // a failed setup must not cancel the collector
    val setup = launch(Dispatchers.Default, CoroutineStart.LAZY) { setUp(client) }
    client.updates
        // Documented: emissions inside or right after this action reach this subscription.
        .onSubscription { instance.value = client; setup.start() }
        .transformWhile { update -> emit(update); !update.closesInstance() }
        .collect { update -> /* lifecycle, see accounts.md */ store.apply(update) }
    setup.cancel()                                  // or supervisorScope waits for it forever
}
// finally: withContext(NonCancellable) { client.close() }. Never leak a live instance.
```

- `SharedFlow.onSubscription` gives the guarantee in its KDoc (kotlinx-coroutines `Share.kt`): the subscription exists
  before the action runs. **The instance is published to `send` inside that action**, so no caller can reach it first.
- `setup` will outlive setup once it follows connectivity and foreground (§8), so it is cancelled when the stream ends. It catches
  `TdException` itself: in a `supervisorScope` an uncaught child failure goes to the crash handler. Today it only sends the parameters.
- `Closed` is read **from the stream**, in order, never from the conflated `authorizationState` (B1). The next instance
  starts only after the old one's last update was applied.
- `shutdown()` cancels the loop first, so it can't start an instance after the account's directories are deleted (accounts.md).
- `apply` catches an exception from `reduce`, reports it and skips that update: a reducer bug must not take the account
  down.
- `apply` runs on **one lane shared by all accounts** (a root `Dispatchers.Default.limitedParallelism(1)`), like Telegram
  X's one thread. N lanes could saturate a 2-core device during cold start, ahead of the row mappers (06 §1.7). It never
  suspends and never does IO.
- `authorizationState` stays as it is: `TdClient.authorizationState` is never missed, and existing routing uses it.
- `send` keeps its contract: it goes to the live instance. The race between an answer and the updates before it is
  handled where it matters (§4.3, §6).

**Batching is a later lever.** The first version applies one update at a time. If cold-start measurement (N12) shows
selectors waking too often, drain with `produceIn` + `tryReceive` and publish one snapshot per batch, behind the same
`apply` shape. Measure first: nobody has numbers for the JSON path.

---

## 4. `AccountStore`, `AccountState`, `reduce`

### 4.1 Shape

```kotlin
// A plain class: StateFlow compares with equals, and generated equals over DTOs is deep and has a ByteArray hole.
class AccountState(
    val options: PersistentMap<String, OptionValue>,
    val connectionState: ConnectionState?,
    val chats: PersistentMap<Long, Chat>,
    val users: PersistentMap<Long, User>,
    val basicGroups: PersistentMap<Long, BasicGroup>,
    val supergroups: PersistentMap<Long, Supergroup>,
    val secretChats: PersistentMap<Int, SecretChat>,
    val chatLists: PersistentMap<ChatList, ChatListOrder>,
    val chatActions: PersistentMap<Long, ChatActions>,
    val chatFolders: List<ChatFolderInfo>,
    val unreadCounts: PersistentMap<ChatList, UnreadCounts>,
    val scopeNotificationSettings: PersistentMap<NotificationSettingsScope, ScopeNotificationSettings>,
    val notifications: NotificationsState,
)

interface AccountStore {
    val state: StateFlow<AccountState>
    val updates: SharedFlow<Update>
    val files: FileStore
}
```

`kotlinx-collections-immutable` persistent maps give an O(log n) copy per update and keep unchanged entries the same
instance. `Chat.copy(...)` on a change replaces one instance.

`updates` is `MutableSharedFlow<Update>(extraBufferCapacity = Int.MAX_VALUE)`, like the library's own. With the default
capacity, `tryEmit` returns false whenever a subscriber isn't parked at that instant, and the update is silently lost
(06 §2.1).

### 4.2 Reducer rules

- `fun AccountState.reduce(update: Update): AccountState` is pure and returns `this` when nothing changed (B2). It lives
  in per-entity files (`ChatReducer.kt`, `UserReducer.kt`, …) with one dispatching `when`, so the single writer does
  not become one god class (A24).
- **A new update type in a TDLib bump is a decision, not a silent skip.** `UpdateCoverageTest` compares `Update`'s subtypes with
  a checked-in list and fails, the same effect Telegram X gets from generated `assert` stubs (03 C9). A compile-time `when`
  with no `else` would need 170 listed names in production code.
- `AuthorizationStateClosed` returns `Empty`. TDLib re-sends what the app needs on the next instance (TGX#3).
- A field update for an unknown chat or user is ignored and logged. TDLib guarantees the order (CS§1.2), so this means
  a bug or a dropped decode (§12). Reads repair on demand with `GetChat` (§4.3), not the reducer: it stays pure.
- Positions: `updateChatLastMessage` and `updateChatDraftMessage` carry the full `positions` array, `updateChatPosition`
  one; `order == 0` removes that one position, a missing list in the array removes that list (CS§1.3, TGX#5, A5, A6).
- `updateChatLastMessage.lastMessage == null` is stored as null. `ChatHistory` reads it (§6).
- Order is `(position.order, chat.id)` descending. `is_pinned` and `source` are display only (CS§1.3, A7). Membership
  (`chat_lists`) and position are different things (TL `chat.chat_lists`).
- `chatActions` needs no timers: TDLib cancels after 5.5 s (02 §5).

### 4.3 Reading

Selectors over `state`, always with **identity** comparison:

```kotlin
fun chat(id: Long): Flow<Chat?> = store.state
    .map { it.chats[id] }
    .distinctUntilChanged { a, b -> a === b }
```

Never a structural `distinctUntilChanged()` on DTOs: `equals` is deep, and `Minithumbnail.data: ByteArray` makes two equal
chats unequal (05 §3.2).

Every publish resumes every selector, so **a screen has one selector, not one per row**: `chat(id)` above is for a
screen's header, never for list items (06 §2.2). Readers `conflate()`.

`ChatListOrder` holds `chatId → order` in a persistent map and sorts lazily, once per instance that someone reads, so
a cold-start burst of position updates doesn't sort at all until a reader needs it (N10). Sorting happens on the reader's
dispatcher, not on the lane (the weakness 05 §6.5 and nngGram point to). `by lazy` is thread-safe on JVM and Native. If
M1's measurement shows sort cost, keep the order incrementally in `reduce` instead (06 §2.4).

**An answer can overtake the updates before it.** TDLib delivers both in one stream, but the library completes an answer
directly from the receive loop while updates wait in a buffer (02 §6, TGX#2 in weakened form). Rules:

- Don't read `state` right after `send` to see its effect. Wait: `store.state.first { id in it.chats }`, as `createPrivateChat`
  or a restored route needs. TDLib guarantees the update exists (CS§1.2), but **always with a timeout**: a dropped decode or a
  restart between `send` and the wait would hang it (06 §2.5).
- A miss on read path (restored back stack with a chat id, TDLib not yet told us): one `GetChat`, then wait as above.
- `loadChats`' `Ok` only ends a loading flag. The rows arrive through `state`.

---

## 5. Chat list, row models, memoisation

```
state ─► ChatListRepository.entries(list): Flow<List<ChatListEntry>>   ids sorted; entry = chat + sender + actions
      ─► ChatListViewModel: mapper on Default, reuses the previous row object when its inputs are identical
      ─► Orbit state (rows: List<ChatListRow>)
      ─► LazyColumn(items(rows, key = { it.chatId }, contentType = …))
```

- **The row model is a plain class of display values**, previewable, no DTOs. Every good client has one, including
  those that keep TDLib types in the UI (02 §4.5), so a domain mirror would not remove this step.
- **Memoise by input identity** (B8). A status change of one user yields a new `users` map, but every other row's
  inputs are the same instances, so those row objects are reused, and strong skipping's `===` skips them (05 §3.4). Without
  reuse, every row recomposes (A8).
- The memo is a map by chat id. **Prune it to the current ids on every emission**, or it grows forever and keeps rows of a
  previous login. An emptied snapshot after `Closed` prunes it for free (06 §3.2). Orbit's `repeatOnSubscription` restarting
  is harmless: same inputs, same row instances, no emission (06 §3.1).
- Per the Android docs, no `ImmutableList`/`@Immutable` on these models: under strong skipping it replaces a cheap `===`
  with per-element `equals` in lazy lists (05 §3.4). Rows are plain classes, so identity is their equality.
- Paging state is per list in the ViewModel: `isLoading`, `hasLoadedPage`, `isComplete`. Page while the last visible
  index is near the end. `loadChats` 404 is the end, never a count (CS§1.3, UNI#11, B27). Orbit doesn't order concurrent
  intents, so derive these flags inside the one long-lived intent that collects the entries (06 §3.3).
- A row reads no global map (A9). Avatars go through Coil with a `File` (§7); progress never enters list state (B21).

Known simplification [O]: a chat TDLib mentions from far below the loaded part sorts to the bottom of the list, and
later pages insert above it. Unigram and Telegram X hide such rows with a window. Here the row only shifts at the very
end of the list, so no window is needed until that proves wrong.

---

## 6. `ChatHistory`: the window of one open chat

Own state machine, owned by the `ChatViewModel`. Paging 3 doesn't fit: no in-place updates, and no serious client pages
history with it (05 §5, A17).

Wiring (06 §4): the interface is in `feature:chats:domain` because `impl` can't see `data`, and the implementation is in
`feature:chats:data`. The ViewModel gets it from a `@Scoped ChatHistoryFactory` with `create(chatId)`: a plain class keeps the graph
under compile safety, while `@InjectedParam` is skipped by it. The lane is `viewModelScope` on its own `limitedParallelism(1)`,
so it dies in `onCleared`. An account switch clears the account's ViewModel store, so histories reload on return.

```kotlin
class HistoryWindow(
    val messages: List<Message>,      // newest first, consecutive: no gaps inside (B4)
    val reachedOldest: Boolean,
    val reachedNewest: Boolean?,      // null: unknown (last message became unknown), Unigram's tri-state
    val windowId: Long,               // bumps when the window is replaced by a jump
    val anchor: ScrollAnchor?,        // delivered together with messages (B6)
)
```

Rules, each from TDLib's contract or a client that hit the bug:

| Rule | Source |
|---|---|
| **Subscribe, then request**: `store.updates.onSubscription { launch { requestFirstPage() } }`. A child on the history lane, not inline: inline would block the collector, and nothing would be recorded | §3's guarantee; A2; 06 §4.4 |
| **While a request is in flight, record live updates; merge the page; replay them.** Set-state updates converge in either order. This is the answer to the answer-overtakes-update race | B3 |
| Page merges only extend the ends; they never overwrite an entry that a newer update set | B4, A14 |
| Local first (`only_local = true`), then remote. A short page is not the end; only an empty remote page is | CS§1.4, TGX#13, UNI#12 |
| `reachedNewest` is `newest.id >= chat.lastMessage.id`, re-checked on `updateChatLastMessage`. Never assumed from a local page | N5, TGX C4 |
| `lastMessage == null` makes `reachedNewest = null`: the newest slice is stale, and TDLib may add messages with no `updateNewMessage`. Reload on return to the bottom | CS§1.6, UNI#3, N4 (no Kotlin client does this) |
| A live message enters only when `reachedNewest == true`. Otherwise it is dropped, and paging brings it | UNI#4, TGX#20, A15 |
| Own outgoing message while detached: reload the newest slice | TGX#20, Unigram |
| Center on a message: `offset = -n`, `limit > n`; `limit ≤ 100` | CS§1.4, TGX#14 |
| A stale answer is ignored: the lane's `loadId`, not only cancellation, because an answer may be in flight | TGX#15, 02 §4.5 |
| Deletes: `from_cache = true` is ignored for an open chat's window; everything else removes the row, even with `is_permanent = false` | 02 §5, A16 |
| `updateMessageSendSucceeded`: re-key by the new id, tolerate the new id already being present; keep the row's `presentationKey` | TGX#17, UNI#6, B9 |
| Unsent messages sort by date, then id | UNI#5 |
| Albums fold by `media_album_id` into one item keyed by album id, growing as pages arrive. No timeout: TDLib has no end signal | B10, 02 §5 |
| The far end is trimmed past a limit, turning its reached flag false | Unigram |
| At most 30 `getChatHistory` per 30 s: no speculative preloading loops | tdlib/td#743 |
| Replies resolved through `getRepliedMessage` per visible item, cached in the window; no N+1 per page | CS§1.4, 02 A18 |

Around the window:

- **`OpenChats`**: `openChat` when the screen starts, `closeChat` when it stops, with TGX's 1 s delayed close (TGX#12). It lives in
  the account scope and launches on its own scope, so a close survives `onCleared` (B26, 06 §4.3). TDLib itself counts opens
  (MM:17104), so the tracker exists for delayed close and for notifications, not for TDLib's sake.
- **`MessageViews`**: from `LazyListState.layoutInfo`, compare both ends per frame, batch `viewMessages` every 300 ms, only
  while open and foreground, `messageSourceChatHistory` (CS§1.5, B19).
- **Message rows**: mapper on Default, memoised by `(message, sender)` identity; `AnnotatedString` built there. Reverse
  layout, index 0 newest (TGX#35). Text measurement stays in the frame: Compose can't move it (N9); reduce with prefetch.
- **Stable key**: `presentationKey`, the temporary id kept after send succeeds (B9, three independent clients). It is the one own field
  this design adds to a message; it lives in the window, not the model. Otherwise `animateItem` shows a double bubble.
- **Scroll position**: `chat.client_data` is TDLib's own per-chat app storage, persistent with the message database (CS§1.2)
  [O: clients use their own settings store; this follows "TDLib is the source of truth"]. `td_api.tl` has no update for it, so
  the snapshot's value goes stale after `setChatClientData`: read it with `GetChat` when opening. It has no topic key.
- **Draft echo**: an `updateChatDraftMessage` for the open chat doesn't overwrite what the user is typing (UNI#8).

Everything else a chat screen needs (M2 detail, from Pingogram, not yet reviewed against this design):

| Need | Home |
|---|---|
| New window and scroll anchor together; first frame without a jump | `HistoryWindow.anchor` + `windowId` (B6); `requestScrollToItem` from layout, list hidden until anchored (B7). CMP 1.12.1 availability unverified |
| Unread separator | Synthetic item computed once from `last_read_inbox_message_id` at open (TGX C4), frozen for the window |
| Date headers | Synthetic items in the row mapper, keyed by day |
| Jump and "back to where I was" | `windowId` bump; a stack of return ids remapped on send-succeeded (TGX#17) |
| Header: title, typing, online count, permissions | One selector over `state` for this chat |
| Pinned message, reply targets | Own small requests in the ViewModel, refreshed by the relevant updates |
| Reactions, interaction info | `ChatHistory` set-state updates; memo makes it one bubble |
| Download progress ring | The one per-item subscription: `FileStore.observe` in the composable. It must not pass through screen state (B21) |

---

## 7. Files and images

`updateFile` goes to `FileStore`, on the lane, **not into `AccountState`**: progress at 10/s would wake every selector, and
`File` objects don't need a consistent cut with chats (A9, 02 §6: every good client goes per-file here).

- `FileStore.observe(fileId): Flow<File>`: one `MutableStateFlow` per **observed** file (B14), acquired when collection starts
  and released when it ends. **An explicit count under one lock, with eviction re-checking the count under that lock.** Releasing on
  `subscriptionCount == 0` alone races a new subscriber, who would hold a flow that never updates again (06 §5.1). Offline
  `GetFile` on first observation; an answer never overwrites a newer update (B16). `file.id` is a session handle (CS§1.8), so all
  of this resets on `Closed`.
- `FileDownloads` is the only caller of `DownloadFile`. TDLib keeps the last request's parameters per file, so without an
  arbiter a prefetch can downgrade a visible image (TGX#22, N6). It holds consumers per file with a priority ladder
  (B15), re-issues only when the effective priority or range changes, cancels with `only_if_pending` after a debounce when the
  last consumer leaves, adopts downloads it did not start, and treats completion idempotently (TGX#23-24, UNI#9).
- `TdImageFetcher` + `Keyer` (Coil 3): data is `TdImage(accountId, fileId, uniqueId, minithumbnail)`, primitives in `core:model`, so
  a composable never holds a DTO. **Key is `remote.unique_id`**, stable across restarts and shared between accounts ("the same for
  the same file even for different users", CS§1.8), so one process-wide `ImageLoader` is correct (Pingogram's singleton bug is N7).
  It can be empty: fall back to `(accountId, fileId)`. No disk cache: TDLib owns the files. Subscribe, then request, then wait for
  `is_downloading_completed`, then hand Coil a path through okio (B17). Decode bounded with `limitedParallelism` (B18). A closed
  account scope is a fetch error, not a crash (06 §5.2).
- Minithumbnail is JPEG bytes in the message: placeholder at once, then the preview size, then full (TGX#25).
- Only `TdImageFetcher` and the player read `local.path`, because the web target has no paths (CS§1.8). A vanished file produces
  no update: on read failure call `DeleteFile`/re-download (UNI#9).

---

## 8. Lifecycle

`InstanceSetup.run(client)` runs once per instance, started from `onSubscription`, in one coroutine off the lane, and is
cancelled when the instance's stream ends (§3):

1. `SetTdlibParameters` first (Telegram X, Unigram; already done).
2. Per-instance options, re-sent every time (TGX#28): `notification_group_count_max`, `online`, localization, `use_storage_optimizer`.
3. `SetNetworkType` from `NetworkMonitor`, and again on every connectivity change even when the type is the same (CS§1.10);
   not `networkTypeNone` on a doubtful signal (UNI#16).

- `online` is true only for the **active** account while the app is in the foreground (Telegram X, Unigram). Without it TDLib backs off
  reconnects (02 §3.2).
- `AppPresence`: `ProcessLifecycleOwner` on Android, scene phase on iOS, window focus on desktop. Whether Nucleus drives CMP lifecycle
  states is unverified (05 §5).
- Pausing idle accounts (TGX table, sessions §7) stays deferred. It fits: `ClientHolder` closes the instance, `reduce` empties,
  and the next `send` starts one.
- Exit on JVM: `stopKoin()` closes TDLib; `close()` returns on `Closed` while the receive loop may still be in `td_receive`. Unchanged
  by this design (05 §2.7).

## 9. Notifications and push (later milestone)

- `notifications` is part of `AccountState`, because `updateActiveNotifications` "comes once" and must not be missed (CS§1.9). The
  platform renderer reads the snapshot through one serial queue (B25). Never built from `updateNewMessage` (TGX flow c).
- Android: FCM → `getPushReceiverId` (synchronous, no instance needed) → account scope → `processPushNotification` → wait for
  `updateHavePendingNotifications`; 401 means a log out follows (TGX#26-27, CS§1.9). Same process: two processes can't share a TDLib
  database (UNI#15, A25).
- iOS: an NSE can't reasonably run TDLib; design unverified (04 §2).
- **Blocker to check first**: whether my.telegram.org accepts FCM/APNs credentials for a third-party `api_id` today (D3).

## 10. Errors

- `ClientHolder.send` throws `TdException`; a repository wraps it with `either { request { … } }`, as today.
- `request` must also catch `SerializationException`: the library surfaces an undecodable answer that way (05 §2.4).
- `code == 429`: parse `retry after N` into a typed error. No generic retry loops: neither Telegram X nor Unigram has one (TGX C10).
- `loadChats` 404 and an empty remote history page are results, not errors.

## 11. Testing

The design makes the important parts deterministic. N11: nobody tests interleavings; here it is straightforward.

| Level | How |
|---|---|
| `reduce` | Pure. Fold recorded or scripted updates; assert the snapshot and identity of unchanged entries |
| `ChatHistory` | Fake `ClientHolder` + scripted updates. **Permute answer and update order** for each rule in §6 |
| `ClientHolder` | `FakeTdLib` at the JSON seam through `TdClient.create(tdLib)`: nothing lost before first `send`, restart order |
| Recording | A `TdLib` decorator in debug builds writes real JSON events for replay. It also re-parses each event and logs what fails to decode, since the library drops those silently (§12) [O] |
| Compose | Recomposition counters: a user status change recomposes one row (B23). RPC budgets on fakes: page once, no N+1 (B24) |
| Contract | Today's `ClientHolderTest` keeps running against real TDLib |

## 12. Facts about the library the design respects (no change requested)

| Fact | Source | Handled by |
|---|---|---|
| `updates` has replay 0 and never completes | `TdClient.kt:34` | §3 |
| No `execute` on the companion; no-arg `create` is a `nonWebMain` extension; `TdLib` is an interface | 05 §1 | M0 |
| An update that fails to decode is dropped silently; an answer surfaces `SerializationException` | `TdReceiver.kt:83,80` | §10, §11, reducer tolerance |
| Each event is parsed twice (tree, then decode), with a mutex per event | `TdReceiver.kt:75-79` | Measure at M1 (N12) |
| `close()` returns on `Closed`, before the receive loop stops | `TdClient.kt:50-55` | §8 |
| One loop per process: one account's storm delays another | `TdReceiver.kt` | Accepted, same as Telegram X |
| One receive loop **per `TdLib` object**: a second wrapper object means a second loop stealing events | `TdReceiver.kt:103-108`; found as a hang in tests | `CoreTdlibModule.tdLib()` hands out `NativeTdLib` itself. A recording decorator (§11) must be one object per process |

---

## 13. Decisions for the developer

**D1. The model above `data`. Decided 2026-11-04: option A.** "Own copies are too expensive." Generated DTOs are the domain model, composables get row models. The table stays as the record.

| Option | Cost (05 §3.2) | What it gives |
|---|---|---|
| **A. Generated DTOs are the domain model; composables get row models** (recommended) | none; a detekt `ForbiddenImport` keeps `io.xbot.tdlib.client.*` out of presentation and domain | "1:1, no `Td` prefix" holds by definition. Instance identity flows from store to row. Telegram X and Unigram do this; Kotlin helpers (`Duration`, `Instant`) become extensions |
| B. Generated mirror | 0.5–1k lines of generator, about 8.5k generated lines | Isolation and `Duration`/`Instant`; a second mapping step with a per-entity memo to preserve identity |
| C. Hand-written mirror (the current rule) | about 8.5k lines for M1–M4, re-synced on each TDLib bump; 23 types changed in 1.8.68 → 1.8.69 | The same as B, by hand. Mategram: 77% of its main code, and it still drops `positions` (A5) |
| D. Hand-written subset | Pingogram: about 12k domain and 22k data lines for a nearly full client | Readable UI; not 1:1 |

Neither mirror buys Compose stability: `core:model` isn't compiled with the Compose compiler either (05 §3.4). Row models are needed
in all four (02 §4.5), so with B or C the path is DTO → mirror → row. The iOS framework doesn't decide this: `shared` has no `export()`, so
no DTO reaches the Swift header under any option, and the DTOs are in the binary anyway (06 §7.2).

Consequences of A, honestly: two rows of `architecture.md` §1.1 change (`feature:x:domain` and `feature:x:impl`, whose ViewModels
map DTOs), and `core:model` gets `api(libs.tdlib.client)`, so the client artifact is on every presentation classpath. "Composables get
row models" is then a convention plus review, not something the build enforces (06 §6).

Under B, C or D, the `ChatHistory` window and the file seam carry the mirror type instead, which is a real but contained change.

**D2. Where the store lives. Decided: `core:tdlib`,** with `DefaultAccountRepository`. Repositories over it live in features (KTG-6).
The holder → store direction allows splitting the store out with one interface.

**D3. Push.** Log in at my.telegram.org and check whether the app settings accept FCM/APNs credentials. If not, Android needs a
foreground service and iOS has no background delivery.

---

## 14. Milestones

| # | Scope | Done when |
|---|---|---|
| M0 ✅ | Fix the three compile breaks (05 §1.3). `ClientHolder` per §3, `AccountStore`, empty `reduce` skeleton. A `@Singleton` class delegating to `NativeTdLib`, so tests can swap `TdLib`. `TestKoin.kt:21` constructs the holder by hand and must change. `request` catches `SerializationException` (§10). Update stale docs (05 §7) | `ClientHolderTest` passes; test: nothing lost before first `send`, restart order, no instance started after `close()` |
| M1, started | Chat list: reducers, `ChatListOrder`, row mapper, `loadChats` are in. **Left:** avatars with minithumbnail, folders, typing, scope mute settings | Real account on all three platforms. **Measure cold-start update count and decode time (N12), sort cost (N10).** iOS on a device (N2) |
| M2 | `ChatHistory` for text: all §6 rules, `OpenChats`, `MessageViews` | Permutation tests pass; scrolling recomposition budget |
| M3 | `FileStore`, `FileDownloads`, Coil fetcher, photos | Prefetch can't downgrade a visible image; account switch shows the right images |
| M4 | Sending: pending, succeeded, failed, retries; drafts | Stable key; no double bubble |
| M5 | Rich content: albums, video, voice, stickers, replies, reactions | |
| M6 | Notifications; push per D3 | |
| M7 | Account switcher UI; pausing idle accounts | |

## 15. Open and unverified

- **Review coverage is partial.** This version includes one adversarial review, of mechanics (coroutines, Koin, Orbit, module rules: 06).
  The planned reviews against Telegram X's rules, the TDLib contract (§4.2, §6, §7) and the Kotlin clients (§5, §6) were cut off and not
  done.
- **iOS is the largest unknown**: no Compose client runs TDLib on iOS (N2). M1 checks it on a device, early on purpose.
- Cold-start update counts and JSON decode cost are unmeasured (N12); "thousands" is folklore. §3's batching waits for that number.
- `LazyLayoutCacheWindow` and `requestScrollToItem` availability in CMP 1.12.1 (02 §7).
- Beyond M0 nothing here was compiled or run. Client consequences in the research are inferred from reading code.
