# Telegram X: from TDLib update to pixel (everything except sessions)

Read on 2026-11-03 from the shallow clones. Sessions, login, pausing and log out are in
`docs/research/telegram-x-sessions.md` and are not repeated; where this report touches them it only
adds what the cache and UI layers need.

Sources and path prefixes used below:

- **TGX** = `TGX-Android__Telegram-X/app/src/main/java/org/thunderdog/challegram/` (commit 5a27890, 2026-11-03)
- **TDJ** = `TGX-Android__tdlib/src/main/java/org/drinkless/tdlib/` (commit 84d3ba9)
- **UTILS** = `TGX-Android__tdlib-utils/src/main/kotlin/tgx/td/` (commit 823297e)
- **vkryl** `ReferenceList` is a git submodule that is empty in the clone; the same file (same author and
  license header) was read from `clones/yukari-sys__Yukagram/app/src/main/java/me/vkryl/core/reference/ReferenceList.java`.

Legend: **[V]** read in source at the cited lines, **[I]** inferred from the code, **[U]** not verified.
Line numbers are from these commits.

---

## 1. Entity map

### 1.1 Entities, by layer

| Entity | File | Responsibility | Lifetime | Thread it runs on | Owned by | Notifies |
|---|---|---|---|---|---|---|
| `Client` | TDJ `Client.java` | JNI handle for one TDLib instance; static receive loop for all instances; routes answers by query id and updates by client id | process (statics), instance until `Closed` | one daemon "TDLib thread" per process | `Tdlib.ClientHolder` | `ResultHandler`s, synchronously on the TDLib thread |
| `TdlibManager` | TGX `telegram/TdlibManager.java` | all accounts; device token; network type; UI state; push and sync tasks; global listeners; players | process (singleton) | any; most state read on UI | process | `TdlibListenersGlobal` |
| `TdlibAccount` | `telegram/TdlibAccount.java` | account slot; lazily creates its `Tdlib` | process | — | `TdlibManager` | — |
| `Tdlib` | `telegram/Tdlib.java` (12.4k lines) | the account: replaceable `ClientHolder`, request API, `processUpdate` switch, chat cache (`chats`, `chatLists`), options, auth state, counters, open-chat refcount, conditional executors | account (never destroyed) | writes on TDLib thread; reads any thread under `dataLock` | `TdlibAccount` | `TdlibListeners`, `TdlibCache`, `TdlibChatList`, `TdlibFilesManager`, `TdlibNotificationManager`, `ImageLoader` |
| `Tdlib.ClientHolder` | `Tdlib.java:257` | one TDLib instance of the account: creates `Client`, sends `SetTdlibParameters` eagerly, forwards every update to `tdlib.processUpdate` | one TDLib instance (replaced after `Closed`) | TDLib thread | `Tdlib.client` | `Tdlib.processUpdate` |
| `TdlibCache` | `telegram/TdlibCache.java` | users, user full infos, basic groups, supergroups, secret chats, communities, calls; per-id listener maps; "last seen" refresh timers | account; cleared on restart | writes on TDLib thread under `dataLock`; status timers on main looper | `Tdlib` | `UserDataChangeListener`, `UserStatusChangeListener`, group/supergroup/secret chat listeners |
| `TdlibOptions` | (via `Tdlib.options`) | the `updateOption` values | account; reset on restart | TDLib thread | `Tdlib` | `TdlibOptionListener` |
| `TdlibListeners` | `telegram/TdlibListeners.java` | the fan-out: typed listener lists (global and per chat id / list key / file id / user id); applies pending `TdApi.Message` copies | account | callbacks run on the TDLib thread, listeners hop themselves | `Tdlib` | every UI listener |
| `TdlibChatList` | `telegram/TdlibChatList.java` | the ordered chat list for one `TdApi.ChatList` (main, archive, folder): sorted `ArrayList<Entry>`, `LoadChats` paging state | account (created on first use, cleared on restart) | TDLib thread (all mutations); `synchronized(list)` for cross-thread reads | `Tdlib.chatLists` | `ChatListListener`s via `TdlibListeners.updateChat{Added,Removed,Moved,Changed}` |
| `TdlibChatListSlice` | `telegram/TdlibChatListSlice.java` | a screen's view of a `TdlibChatList`: filter, display window (`displayCount`/`maxSize`), optional re-sort, `bringToTop` | screen | TDLib thread | `ChatsController` | the controller's `ChatListListener` |
| `TdlibFilesManager` | `telegram/TdlibFilesManager.java` | download operations with per-source chunks (priority, offset, limit), pending operations, file listeners, auto-download rules, data saver | account | any, `synchronized(this)` / `synchronized(activeDownloadReferences)` | `Tdlib` | `FileListener`, `SimpleListener`, `FileUpdateListener` |
| `TdlibStatusManager` | `telegram/TdlibStatusManager.java` | typing / chat actions per chat and topic (`ChatState`), chat subtitle text, "online" of a user | account | UI thread | `Tdlib` | `ChatStateListener` (`Helper` objects in `TGChat` and headers) |
| `TdlibMessageViewer` | `telegram/TdlibMessageViewer.java` | collects visible messages per `Viewport` and sends `ViewMessages` with a `MessageSource` | account; `Viewport` per list | UI thread | `Tdlib` / controllers | — |
| `TdlibNotificationManager` + `NotificationQueue` | `telegram/TdlibNotificationManager.java` | TDLib notification groups → Android notifications; sounds | account; queue per process | own `SerialQueue` thread ("notification thread") | `Tdlib`, `TdlibManager` | Android `NotificationManager` |
| `ImageLoader`, `ImageActor`, `ImageReader`, `ImageCache` | `loader/` | image request dedupe (`watchers`), download trigger, decode, memory cache | process (singletons, per-account keys) | `ImageThread` (requests) and `ImageReaderThread` (decode) | process | `Watcher` → `ImageReceiver` |
| `ImageReceiver`, `DoubleImageReceiver`, `AvatarReceiver`, `ComplexReceiver` | `loader/` | per-view image slots: hold `ImageFile`, bitmap, fade animator, draw | view | UI thread | the custom `View` | `view.invalidate(rect)` |
| `TGChat` | `data/TGChat.java` | chat-list row model: a **copy** of `TdApi.Chat`, pre-laid-out title/text/time `Text` objects, counters, typing helper | screen (list item) | built on TDLib thread (initial chunk) or UI thread (live adds); updated on UI | `ChatsAdapter` | attached `ChatView`s via `MultipleViewProvider` |
| `TGMessage` (+ 20 subclasses) | `data/TGMessage*.java` (10k lines base) | message-bubble model: layout, text, media wrappers, albums (`combinedMessages`), reactions, send state | chat screen | built and laid out on the TDLib thread; mutated on UI | `MessagesAdapter` | attached `MessageView`s via `MultipleViewProvider` |
| `MessagesLoader` | `component/chat/MessagesLoader.java` | history windows: `GetChatHistory`/`GetMessageThreadHistory`/`SearchChatMessages`, local-first, chunk sizes, album completion, unread anchor search, `contextId` guard | chat screen | request on UI, result + parsing on TDLib thread, display via `UI.post` | `MessagesManager` | `MessagesManager.displayMessages` |
| `MessagesManager` | `component/chat/MessagesManager.java` | the open chat: `openChat`/`closeChat`, subscriptions, live update merge, scroll anchor, view-tracking, pinned messages, threads/topics | chat screen | UI thread (listener callbacks hop) | `MessagesController` | `MessagesAdapter`, `MessagesController` |
| `ChatsController` / `ChatsAdapter` / `ChatView` | `ui/ChatsController.java`, `component/dialogs/` | chat list screen: owns the slice, hops every callback to UI, `RecyclerView` adapter with `TGChat`s, custom-drawn row | screen | UI thread | navigation stack | — |
| `MessagesController` / `MessagesAdapter` / `MessageView` | `ui/MessagesController.java`, `component/chat/` | chat screen | screen | UI thread | navigation stack | — |

### 1.2 Relations

```
                    Android process
 ┌───────────────────────────────────────────────────────────────────────────────────┐
 │  TDJ Client (static): one daemon "TDLib thread", receive(≤1000 events, 100000 s)   │
 │        answers → handlers[queryId]            updates → updateHandlers[clientId]   │
 └───────────────┬───────────────────────────────────────────┬───────────────────────┘
                 │ (sync, same thread)                       │
   TdlibManager ─┼─ TdlibAccount[N] ── Tdlib (per account) ──┘
                 │                      │
                 │   ┌──────────────────┴──────────────────────────────────────────┐
                 │   │ ClientHolder.onResult → Tdlib.processUpdate(update)   @TdlibThread
                 │   │   ├─ chats: HashMap<Long, TdApi.Chat>  (dataLock, mutated in place)
                 │   │   ├─ chatLists: key → TdlibChatList  ──slice()──► TdlibChatListSlice (per screen)
                 │   │   ├─ TdlibCache: users/groups/supergroups/secret chats (dataLock, copyTo in place)
                 │   │   ├─ TdlibOptions, counters, authorizationState, connectionState
                 │   │   ├─ TdlibFilesManager ◄── updateFile ──► ImageLoader / GifBridge / player
                 │   │   ├─ TdlibNotificationManager ──► NotificationQueue (own thread)
                 │   │   ├─ ui() = TdlibUi(Handler, main looper): user status, chat actions, counters
                 │   │   └─ TdlibListeners (weak ReferenceList / ReferenceLongMap, per chat id, per list key, per file)
                 │   └─────────────────────┬────────────────────────────────────────┘
                 │                         │ callbacks on the TDLib thread; each listener hops to UI itself
                 │        ┌────────────────┼────────────────────────────┐
                 │   ChatsController   MessagesManager            TGMessage / TGChat (data+layout objects)
                 │   (ChatListListener,(MessageListener,          └─ MultipleViewProvider → attached Views
                 │    ChatListener …)   MessageEditListener …)       → invalidate()/requestLayout()
                 │        │                 │
                 │   ChatsAdapter ─ TGChat ─ ChatView.onDraw     MessagesAdapter ─ TGMessage ─ MessageView.onDraw
                 │                   │  AvatarReceiver            │  ImageReceiver / DoubleImageReceiver
                 │                   └───────────► ImageLoader (ImageThread) → DownloadFile / ImageReader → bitmap
                 └───────────────────────────────────────────────────────────────────────────────────┘
```

Who owns whom: `TdlibManager` → `TdlibAccount` → `Tdlib` → {`ClientHolder`, `TdlibCache`, `TdlibListeners`,
`TdlibChatList`s, `TdlibFilesManager`, `TdlibStatusManager`, `TdlibNotificationManager`, `TdlibMessageViewer`}
(`Tdlib.java:427-462` [V]). Screens own slices, adapters, `TGChat`/`TGMessage` objects and receivers; they
register themselves as weak listeners on the account's `TdlibListeners`/`TdlibCache` and unsubscribe on
destroy.

---

## 2. Concerns

### C1. Update pipeline

**Receive and decode.** `Client.create` starts one daemon thread named "TDLib thread" the first time it
is called (`TDJ Client.java:162-174` [V]). The loop calls `nativeClientReceive(clientIds, eventIds,
events, 100000.0)` and processes up to `MAX_EVENTS = 1000` already-decoded `TdApi.Object`s per JNI call
(`:190-199, :235-238` [V]). Decoding is done by the JNI layer into Java objects; there is no JSON.

**Routing.** `processResult` picks `updateHandlers.get(clientId)` when `id == 0`, otherwise
`handlers.remove(id)`; after `AuthorizationStateClosed` the client's update handler and exception
handler are removed and `clientCount` decremented (`:201-233` [V]). Answers and updates share one map
space and one thread, so a query's answer arrives in the same stream, after earlier answers and
updates. The `Interceptor` comment states the invariant explicitly: "The answer comes on the thread of
results after answers to earlier queries" (`:104-107` [V]) and TGX uses it as a **barrier**: an
intercepted query is replaced by `SetAlarm(0)` so the fake answer keeps its ordering.

**Fan-out.** `ClientHolder.onResult` (`TGX Tdlib.java:381-399` [V]) calls `tdlib.processUpdate(this,
update)` synchronously; if `Log.needMeasureLaunchSpeed()` it logs any update that took > 100 ms.
`processUpdate` is one `switch (update.getConstructor())` with ~197 cases (`:9854-10420` [V]). Each case
first mutates the account caches under `dataLock`, then calls `listeners.updateX(...)`, which iterates
weak listener lists **on the same thread** (`TdlibListeners.java:562-612` [V]). Nothing in the pipeline
posts to another thread until a listener does.

**"Run on the TDLib thread" without a handler.** TGX has no Looper on the TDLib thread, so it posts
work there by sending `SetAlarm(timeout)` and running the runnable in its result handler
(`Tdlib.java:302-312` [V]; `Tdlib.runOnTdlibThread(runnable, timeoutSeconds, acquireReference)`
`:1824-1840` [V] also takes a job reference so the instance is not paused meanwhile). `SetAlarm(0)` is
also used as an ordering barrier: `openChat` with an `after` callback sends `SetAlarm(0)` when the chat
is already open so `after` runs after the real `OpenChat` answer (`:4668-4672` [V]).

**Thread detection.** `inTdlibThread()` compares against the thread captured in `updateTdlibThread()`
and falls back to the thread *name* "TDLib thread", annotated `FIXME[tdlib]` twice (`:2257-2275` [V]).
`clientExecute` (blocking request with a latch) throws if called from the TDLib thread (`:2300-2302`
[V]) and only `SetAlarm`, `Close`, `GetAuthorizationState`, `GetCurrentState` may run before
initialization; everything else waits in `awaitInitialization` (`:2319-2334` [V]).

**Ordering guarantees.** Everything is in TDLib's order because there is one thread and synchronous
handlers. **Batching:** only at the JNI boundary (1000 objects per call); nothing coalesces updates in
Java. **Backpressure:** none; a slow handler delays the whole stream (hence the > 100 ms log).

**Fake updates.** `ClientHolder.sendFakeUpdate(update)` runs `processUpdate` on the TDLib thread via
`SetAlarm(0)` (`:367-369` [V]); used to re-inject a re-read option (`reloadOption`, `:6714-6720` [V]).

### C2. Entity caches

**Chats** live in `Tdlib`: `HashMap<Long, TdApi.Chat> chats`, `HashMap<String, TdlibChatList>
chatLists`, `HashSet<Long> knownChatIds`, guarded by `dataLock` (`Tdlib.java:433-443` [V]).
`updateNewChat` puts the TDLib object as-is and registers the chat in every list whose position has
`order != 0` (`:7760-7791` [V]). Every later chat update **mutates the cached object in place** under
`dataLock`: `updateChatReadInbox` sets `lastReadInboxMessageId`/`unreadCount` (`:8332-8350` [V]),
`updateChatLastMessage` sets `lastMessage` and diffs `positions` (`:7896-7911` [V]),
`updateChatDraftMessage` (`:8381-8392` [V]), `updateChatPosition` (`:8084-8107` [V]), `updateChatAddedToList`
(`:8109-8133`, with a `// TODO: sort?`). Every handler starts with `TdlibUtils.assertChat(chatId, chat,
update)` and returns if the chat is unknown.

Read path: `chat(id)` is a locked `HashMap.get` returning the shared mutable object (`:2688-2696` [V]);
`chatStrict` throws `"updateChat not received for id"` (`:2698-2707` [V]); `chat(id, callback)` hops
to the TDLib thread and falls back to `GetChat` (`:2709-2719` [V]).

**Users, groups, supergroups, secret chats** live in `TdlibCache` as `HashMap`s plus per-id listener
maps (`TdlibCache.java:116-156` [V]), guarded by its own `dataLock` (`:165`). `onUpdateUser` **copies
the new user into the existing object** with `Td.copyTo(newUser, oldUser)` so that references held by UI
stay valid, then notifies (`:437-489` [V]). `user(id)` logs `Log.bug("updateUser missing")` on a miss
(`:933-942` [V]); `userStrict` throws (`:943-951`). **Lazy fetches:** `userFull(id, allowRequest)` always
sends `GetUserFullInfo` (and `GetUser` first if the user is unknown) even when a cached value is
returned, because TDLib only sends `updateUserFullInfo` for full infos the client has requested
(`:1129-1143` [V], [I] on the reason). `onUpdateSupergroup` distinguishes "important" changes to update
notification channels (`:634-658` [V]).

**User status timers.** `onUpdateUserStatus` writes the status on the TDLib thread and dispatches to the
UI handler (`:542-553` [V] → `Tdlib.dispatchUserStatus :7234` → `onUpdateUserStatusInternal :556-569`).
`checkUserStatus` schedules a main-looper message when the "last seen" text has to change
(`Lang.getNextRelativeDateUpdateMs`, `:509-540` [V]); timers run only while the UI is `RESUMED`
(`setPauseStatusRefreshers`, `:264-266, :343-384` [V]).

**Options** are parsed by `TdlibOptions.handleUpdate` and a `switch (name)` with side effects
(`my_id` → `onUpdateMyUserId`, `version`, `authentication_token`, …) (`Tdlib.java:9597-9640` [V]).
`resetOptions` rebuilds them from the cached `GetApplicationConfig` JSON (`:7291-7299` [V]).

**Reset.** On `AuthorizationStateClosed`: `client.stop()` (drops further updates), `resetState()`,
`resetContextualData()` (chats, lists, calls, stories, options, reactions …), `listeners.performRestart()`
(→ `TdlibCache.clear()` `TdlibCache.java:329-331`, `TdlibStatusManager` cancels typing `:499-505`),
`ImageLoader.instance().clear(accountId, true)`, then a new client unless paused (`Tdlib.java:1226-1272`
[V]). Chats are also cleared when the auth status goes from `UNKNOWN` to known, i.e. on **every instance
start**, because TDLib re-sends `updateNewChat` for everything it knows (`:1215-1219` [V]), and again
when the state becomes unauthorized without logging out (`:1282-1288` [V]).

**Memory bounds:** none. The maps are unbounded and only cleared on restart [V]. `ImageCache` is the
only bounded store (not read in detail, [U] on its policy).

**Listener lists.** `ReferenceList`/`ReferenceLongMap` hold **weak references**; `iterator()` is
non-reentrant and must not be nested, and must be constructed `isThreadSafe` if iterated from several
threads (`ReferenceList.java:36-45` [V], from the Yukagram copy).

### C3. Chat lists

**Key and lifetime.** `Tdlib.chatList(TdApi.ChatList)` creates one `TdlibChatList` per key
`"main"`, `"archive"`, `"folder<N>"` (`Tdlib.java:2763-2774`, `TD.java:923-936` [V]).

**Order.** `Entry.compareTo` sorts by `effectivePosition.order` descending, then `chat.id` descending
(`TdlibChatList.java:61-65` [V]). Each entry carries its **own copy** of `TdApi.ChatPosition`
(`:45-58`), so re-sorting works even though the shared `TdApi.Chat` is mutated in place.

**Position diffing.** `Tdlib.setChatPosition` / `setChatPositions` compute `ChatChange` flags `ORDER`,
`SOURCE`, `PIN_STATE` by comparing old and new positions, and return one `ChatListChange` per affected
list, including lists the chat **left** (position with `order = 0`) (`Tdlib.java:7928-8081` [V]).
`updateChatLastMessage` and `updateChatDraftMessage` both carry a full `positions[]` and go through
`setChatPositions` (`:7896-7911, :8381-8392` [V]); `updateChatPosition` carries one position (`:8084-8107`).

**Applying to the list.** `TdlibListeners.updateChatLastMessage` first notifies per-chat
`ChatListener`s (`onChatTopMessageChanged`, `onChatPositionChanged`), then calls
`list.onUpdateChatPosition(chat, change)` for each `ChatListChange`, then `onChatListItemChanged(…,
LAST_MESSAGE)` (`TdlibListeners.java:942-962` [V]). `TdlibChatList.onUpdateChatPosition` does a linear
`indexOfEntry`, removes, copies the position into the entry, binary-searches the new index (throws
`IllegalStateException` on a duplicate), and emits `updateChatMoved` or `updateChatChanged`
(`TdlibChatList.java:399-432` [V]). Adds/removes are `addChatToList`/`removeChatFromList`
(`:455-477`). The whole thing runs on the TDLib thread; `synchronized(list)` only protects readers on
other threads (`count`, `listCopy`, `iterate`, `:152-256`).

**Paging.** `loadMore(limit)` sends `LoadChats(chatList, limit)`; **an `Error` answer means the end
is reached** (TDLib returns 404 when there is nothing more to load), success means `END_NOT_REACHED`
and callers re-check (`:309-338` [V]). `loadAtLeast(filter, minCount, desiredCount)` loops until the
filtered count is reached or the end hit (`:287-306`). `loadAll` pages by 100 with a per-chat callback
(`:341-366`). Nothing in the answer carries chats: they arrive as `updateNewChat` + `updateChatPosition`
before the `Ok`.

**Snapshot + subscribe atomically.** `initializeList` hops to the TDLib thread, copies the list
(`listCopyImpl`), delivers it, **then** subscribes the listener, all in one TDLib-thread runnable so no
update can be lost or duplicated (`:261-279` [V]; comment "No need to sync, as all changes are made on
tdlib thread").

**Filtering and windowing.** `TdlibChatListSlice` wraps a list with a `Filter<TdApi.Chat>`, a
`filteredList`, a display window `displayCount ≤ maxSize`, and re-sorts only when filtered or modified
(`needSort`, `TdlibChatListSlice.java:384-386` [V]). Its inner `ChatListListener` translates source
events into window-relative events: an add beyond the window just calls `dispatchChats(0)` to grow the
window if `maxSize` allows (`:162-190`); a move across the window edge becomes an add or remove
(`:207-247`); `onChatListItemChanged` can also **add** a chat that newly passes the filter (`:261-288`).
`bringToTop` pins an entry with a timestamp used by `Entry.compareTo` (`:53-75, :388-433`).
`totalCount()` is `max(counter.totalChatCount, loaded)` so the UI can show "N chats" before loading all
(`TdlibChatList.java:144-146`, `Tdlib.getTotalChatsCount :9564-9569` [V]).

**Diffing to UI.** There is no DiffUtil. `ChatsController` implements `ChatListListener` and hops each
granular callback to the UI thread with `runOnUiThreadOptional` (skips if destroyed,
`ViewController.java:547-568` [V]), then calls `adapter.addChat / removeChatById / moveChat /
updateChat` which issue `notifyItemInserted/Removed/Changed` and a custom "appeared" move
(`ChatsController.java:2677-2710`, `ChatsAdapter.java:635-730` [V]). `addMore` dedupes by
`presentChatIds` and `notifyItemRangeInserted` (`ChatsAdapter.java:374-404` [V]). Initial chunk size is
`Screen.calculateLoadingItems(72dp, 5) + 5`, load-more `…25` (`v/ChatsRecyclerView.java` ~:69-70 [V]).

**Row content (`TGChat`).** The constructor takes `Td.copyOf(chat)` (a **snapshot**, not the shared
object), creates a `TdlibStatusManager.Helper` for typing, counters, and optionally builds the layout
(`TGChat.java:149-213` [V]). `buildLayout` = `setCounter, setTime, setTitle, setText, setAvatar`
(`:332-341` [V]); `setText` decides draft vs last message vs restriction vs passcode and builds a
`ContentPreview` (`:1288+` [V]). Online dot: `checkOnline` asks `tdlib.status().isOnline(userId)`
(`:318-326`; `TdlibStatusManager.java:641-645` [V]). Typing: `attachToView` → `statusHelper.attachToChat`
subscribes to `TdlibStatusManager` **only while a view is attached** (`:1526-1551` [V]); `layoutChatAction`
builds the typing `Text` (`:1558-1580`). Per-field updates (`updateTopMessage`, `updateDraftMessage`,
`updateChatReadInbox`, `updateUser`, …) return whether the row changed (`:416-720`).

Typing path: `updateChatAction` → `ui().sendMessage(MSG_ACTION_UPDATE_CHAT_ACTION)` → UI thread
`TdlibStatusManager.onUpdateChatUserAction` creates/updates a `ChatState` per `chatId_topic`
(`Tdlib.java:8654-8658`, `:7194-7197`, `TdlibStatusManager.java:546-561` [V]). Expiry relies on TDLib's
`ChatActionCancel` (`:555`) [V]; on restart all states are force-cancelled (`:499-505`).

**Counters.** `updateUnreadChatCount`/`updateUnreadMessageCount` update `TdlibCounter`s and dispatch to
the UI handler (`Tdlib.java:9164-9176` [V]).

### C4. Message history

**Open/close.** `Tdlib.openChat(chatId, controller)` refcounts controllers per chat and sends
`OpenChat` only for the first one (`Tdlib.java:4645-4677` [V]); `closeChat(..., needDelay)` posts
`CloseChat` after **1 s** so a quick re-open does not toggle (`:4683-4703` [V]).
`MessagesManager.openChat` calls it, inits pinned messages, sets the loader, attaches a
`TdlibMessageViewer.Viewport` to the RecyclerView with callbacks that decide what counts as "viewed",
and subscribes to per-chat message/edit/member-status listeners (`MessagesManager.java:712-795,
:3375-3402` [V]). `destroy` unsubscribes, `closeChat` with delay, `loader.reuse()`, clears the adapter
(`:538-573` [V]).

**View tracking.** `viewMessages(byScroll)` runs the viewer, then triggers paging: `first -
BOTTOM_PRELOAD_COUNT <= 0 → loadMore(false)`, `last + TOP_PRELOAD_COUNT >= count → loadMore(true)`
(`:288-313` [V]). The viewer sends `ViewMessages(chatId, ids, messageSource, forceRead)`
(`TdlibMessageViewer.java:767-777` [V]) and reacts to send-succeeded to remap ids (`:515-527`).

**Anchor.** `getAnchorHighlightMode` picks between the saved scroll position (`Settings.getScrollMessageId`)
and the unread anchor; `canGoUnread` requires `unreadCount >= CHATS_THRESHOLD`, a valid
`lastReadInboxMessageId`, and `lastMessage.id > lastReadInboxMessageId` and not outgoing
(`MessagesManager.java:3308-3373` [V]). The saved position is remapped on `updateMessageSendSucceeded`
(`Tdlib.java:7554-7557` [V]) and saved by `saveScrollPosition` on focus/measure (`:2557+`).

**Loading.** Constants: `CHUNK_SIZE_BIG 50`, `CHUNK_SIZE_SMALL 19`, `CHUNK_SEARCH_OFFSET -19`,
`CHUNK_SIZE_SEARCH 33`, `CHUNK_SIZE_BOTTOM 31`, `CHUNK_BOTTOM_OFFSET -30` (`MessagesLoader.java:64-71`
[V]). `loadFromStart` sends `GetChatHistory(chatId, from, 0, 19, onlyLocal = true)`
(`:1060-1069, :1100-1222` [V]); `loadFromMessage` sends `GetChatHistory(from=messageId, offset=-19,
limit=33, onlyLocal=false)` (`:1071-1079`). Threads use `GetMessageThreadHistory`, filters
`SearchChatMessages`, scheduled `GetChatScheduledMessages` (`:1150-1192`). `loadMore(top)` uses the
adapter's top/bottom **active** message ids as `from` (`:1229-1256, :1773-1781`).

**Result handling (on the TDLib thread).** `newHandler` captures `contextId` and `lastHandler`; a
result is ignored if either changed (`:244-257` [V]). An `Error` becomes an empty chunk (`:310-316`).
Album completion at chunk edges (`Config.NEED_MEDIA_GROUP_MERGE_REQUESTS`) is **disabled**
(`Config.java:125` false) [V]; the pinned-messages filter still fetches missing album members with
the FIXME "why … server thinks it's client's job to fetch these messages" (`:442-466` [V]).

`processMessages` (`:1309-1720` [V], TDLib thread) builds `TGMessage.valueOf` per message, folds albums
with `cur.combineWith(messages[j-1], true)`, sets read state from `chat.lastReadInbox/OutboxMessageId`,
finds the unread badge (first incoming message above `lastReadInboxMessageId`), groups with the
previous message via `mergeWith(top)`, **calls `cur.prepareLayout()`** (text measurement off the UI
thread), then `UI.post`s `manager.displayMessages`. Local-first fallback: if `loadingLocal` returned
nothing it re-sends the same request with `onlyLocal = false` (`:1627-1636`); if an initial chunk is
smaller than requested it immediately loads more (`:1644-1651`). End detection: `MODE_MORE_*` with an
empty remote answer flips `canLoadTop/Bottom` (`:1587-1626`); `isEndReached` compares the bottom id
with `chat.lastMessage.id` (`:1800-1826`).

**Display.** `displayMessages` adds or resets adapter items and scrolls to the anchor; `MODE_MORE_BOTTOM`
preserves the first visible offset (`MessagesManager.java:1208-1276` [V]). The RecyclerView is
**reversed** (`LinearLayoutManager(..., VERTICAL, true)`, `MessagesController.java:717` [V]), so index 0
is the newest message and `findFirstVisibleItemPosition() == 0` means "at bottom".

**Live merge.** `onNewMessage` (TDLib thread) skips messages already in `sentMessages`, builds the
`TGMessage` with `chatStrict`, calls `prepareLayout()` and posts to UI with a `loader.getChatId() ==
chatId` guard (`:3423-3443` [V]). `updateNewMessage` (UI) checks topic, lets the bottom album absorb it
(`combineWith`), then: if `!loader.canLoadBottom()` it inserts and scrolls when the user is at bottom
or the message is outgoing; if the user is **not at the end of history** and the message is outgoing it
`loadFromStart()`s (`:1897-1958` [V]). Edits/contents/deletes/reactions/interaction info are all
`MessageListener` callbacks that hop with `tdlib.ui().post` and the same chat-id guard
(`:3488-3625`). Deletes: `Tdlib.updateMessagesDeleted` ignores `fromCache` and sorts ids (`Tdlib.java:7706-7715`
[V]); `MessagesManager.updateMessagesDeleted` asks each `TGMessage.removeMessage(id)` → `REMOVE_NOTHING /
REMOVE_COMBINATION / REMOVE_COMPLETELY`, reloads if the list became empty (`:2276-2365` [V]).
Send state: `updateMessageSendSucceeded` → `TGMessage.setSendSucceeded` → invalidated / changed / replaced
(`:2024-2050`); re-sorting by new id is **disabled** (`checkPositionInList` begins with `if (true) return;`,
`:2053-2055` [V]).

**Pending `TdApi.Message` copies.** `TdlibListeners.subscribeToUpdates(TdApi.Message)` registers
UI-held message objects by `chatId_messageId`; on send-succeeded/failed `Td.copyTo(message, dst)`
rewrites them in place and re-keys them, and `updateMessageContent` replaces `content`
(`TdlibListeners.java:117-134, :637-680` [V]); used by `MessagesController` when sending several
messages (`MessagesController.java:9866, :9885` [V]).

**Albums.** `TGMessage.wouldCombineWith` requires same `mediaAlbumId`, same self-destruct, same forward
origin and the same `TD.getCombineMode`; `combineWith` keeps `combinedMessages` and rebuilds layout only
if already laid out (`TGMessage.java:4806-4840` [V]). `Tdlib.getAlbum` completes an album with two
`GetChatHistory` calls around it, local first (`Tdlib.java:2095-2200` [V]). Max album size is
`TdConstants.MAX_MESSAGE_GROUP_SIZE`.

**Threads/topics.** `ThreadInfo` is a listener target (`:3396-3401`), `GetMessageThreadHistory` for
loading, `Td.matchesTopic` filters live messages (`:1904`). `topicId == null /*FIXME TDLib*/`
(`TGMessage.java:4685`) marks a schema wart.

**Per-message view model and layout precomputation.** `TGMessage` subclasses are chosen by content
type in `valueOf` (`TGMessage.java:8113-8175` [V]; unknown → `valueOfError :8514`). `buildLayout(width)`
computes bubble geometry, header, forward, reply, content, footer, reactions, markup and sets
`FLAG_LAYOUT_BUILT` (`:1207-1291` [V]); `prepareLayout` uses `manager.getRecyclerWidth()` (`:1199-1205`).
`rebuildAndUpdateContent` checks `Looper.myLooper()` to choose `postInvalidate` vs `invalidate`
(`:1368-1383` [V]). Text widths are measured with `U.measureText` at build time (`:3064, :3359, …`).

### C5. Files and media

**Download operations.** `TdlibFilesManager.DownloadOperation` keeps a list of `Chunk(priority,
offset, limit, source)` per file; `prepareNewRequest` decides whether a new `DownloadFile` must be
sent: first request, seeking backwards, limited/unlimited switch, priority change, or a sequential/≤128 KiB
gap growth of the limit (`TdlibFilesManager.java:130-260` [V]). `addCloudReference(file, priority,
offset, limit, source)` / `updateCloudReference` / `removeCloudReference` manage the chunks and send
`CancelDownloadFile(fileId, false)` when the last reference goes and the operation started the download
(`cancelOnRemoval`) (`:601-664` [V]). Priorities: `PERSISTENT_IMAGE 1`, `IMAGE 15`,
`USER_REQUEST_DOWNLOAD 16`, `USER_REQUEST_SHARE 17`, streaming 20-25 (`:77-94` [V]).

**Simple downloads and state.** `downloadFile(file)` → `downloadFileInternal` marks
`pendingOperations[fileId] = OPERATION_DOWNLOAD`, notifies `STATE_IN_PROGRESS`, sends `DownloadFile(id,
priority, 0, LIMIT_MAX, synchronous=false)` with `fileHandler` (`:751-806` [V]). `updateFile` is
dispatched from `Tdlib.updateFile` to: listeners, player, `files().onFileUpdate`, then either
`onFileProgress` + `ImageLoader.onProgress` / `GifBridge.onProgress` while active, or `onFileLoaded` +
`ImageLoader.onLoad` when `TD.isFileLoaded`, else `onFileUpdated` (paused) (`Tdlib.java:8935-8966` [V]).
`onFileUpdate` also **adopts downloads it did not start** (`isDownloadingActive` with no pending op →
`OPERATION_DOWNLOAD`) (`TdlibFilesManager.java:883-905` [V]). Subscribing a listener to an in-progress
file copies the latest `TdApi.File` into the caller's object (`Td.copyTo(pendingFile, file)`,
`:677-696` [V]). Cancellation: `cancelDownloadOrUploadFile(fileId, weak, byUserRequest)` remembers
`manuallyCancelledFiles` (`:816-828`), `cancelAllPendingDownloads` on data saver (`:838-850`).

**Image loader.** `ImageReceiver.requestFile(file)` first checks `ImageStrictCache`/`ImageCache`; on a
hit it sets the bitmap synchronously, otherwise alpha 0 and `ImageLoader.requestFile` (`ImageReceiver.java:685-756`
[V]; detached receivers just remember the request, `:686-689`). `ImageLoader.requestFile` runs on
`ImageThread`, dedupes by `file.toString()` in `watchers`, creates an `ImageActor`; `actor.act()` reads
immediately if the file is local/loaded, otherwise returns `true` and the loader sends
`DownloadFile(fileId, PRIORITY_IMAGE, 0, 0, false)` with `tdlib.imageLoadHandler()`, grouping actors by
`getFileLoadKey` (`ImageLoader.java:75-150`, `ImageActor.java:80-96` [V]). `ImageLoader.onLoad(tdlib,
file)` (from `updateFile` with `isDownloadingCompleted`, or from the `DownloadFile` answer `Tdlib.java:239-249`)
dispatches `thread.onLoad(actor, file)` → decode on `ImageReaderThread` → `onResult` → each
`WatcherReference.imageLoaded` → `ImageReceiver.setBitmap` + fade animator → `view.invalidate(drawRegion)`
(`:281-360`, `ImageReceiver.java:111-125, :758+` [V]). `removeWatcher` cancels the download with
`CancelDownloadFile(id, onlyIfPending)` when the last watcher leaves (`:188-235` [V]). `clear(accountId,
withMemcache)` runs on restart (`:363-420`).

**Thumbnails.** `MediaWrapper` turns `TdApi.Minithumbnail` into `ImageFileLocal(minithumbnail)` (JPEG
bytes, decoded locally, drawn blurred) and pairs it with the `TdApi.Thumbnail`/`PhotoSize` preview in a
`DoubleImageReceiver` (`MediaWrapper.java:697-712, :805-811`, `ImageFileLocal.java:47-48` [V]). The full
photo is loaded by `ImageReceiver` after auto-download rules (`TdlibFilesManager.downloadAutomatically`
`:667-675`).

**Streaming.** `TdlibDataSource` (ExoPlayer) reads `file.local.downloadedPrefixSize`/`downloadOffset`,
asks `GetFileDownloadedPrefixSize` synchronously via `clientExecuteT(…, 1000 ms)`, and drives the
download window with `addCloudReference/updateCloudReference(file, priority, offset, limit)`
(`TdlibDataSource.java:297-340, :431-440` [V]).

**Uploads.** `onFileUpdate` tracks `remote.isUploadingActive` the same way; `Tdlib.UploadFutureSimple`
exists (`Tdlib.java:12283`) [U, not read].

### C6. Notifications and background

**TDLib notification updates** go `Tdlib.onUpdateActiveNotifications / onUpdateNotificationGroup /
onUpdateNotification` (`Tdlib.java:7470-7495`) → `TdlibNotificationManager` (`@TdlibThread`) →
`sendLocked(..)` onto `NotificationQueue`, a `SerialQueue` thread shared per process
(`TdlibNotificationManager.java:323-420, :2275-2298` [V]) → `processActiveNotifications /
processNotificationGroup / processNotification` → `rebuildNotification` (`:2438-2452, :2159`). The
TDLib thread never builds a notification. `onUpdateNewMessage` only plays the in-chat sound when the
message is for the chat currently on screen (`:2170-2186` [V]).

**Push.** `PushHandler.onMessageReceived` (`service/PushHandler.kt:22`) → `PushProcessor.processPush`
takes a wake lock, decodes the account id, and calls `TdlibManager.processPushOrSync` with a latch and a
7 s timer that promotes to a foreground service if TDLib is slow (`service/PushProcessor.java:79-86,
:147-281, :302-345` [V]). `Tdlib.processPushOrSync` increments a notification reference, sends
`ProcessPushNotification(payload)`; on `Ok` it waits for `awaitNotificationInitialization` then
`notifications().releaseTdlibReference` then `awaitClose` (in case the push triggered a log out); on
error 401 it waits for `Closed`; on other errors it sets `hasUnprocessedPushes` and runs a full `sync`
(`Tdlib.java:6660-6711` [V]).

**Sync.** `Tdlib.sync` takes `REFERENCE_TYPE_SYNC`, `awaitConnection`, optionally does a network request
(`GetCountryCode`; `Config.NEED_NETWORK_SYNC_REQUEST` is false) and, if notifications are needed, waits
for `havePendingNotifications == false` (`:6614-6644` [V]). Entry points: WorkManager `SyncTask`
(`sync/SyncTask.java:83-92`), the system `SyncAdapter` (`sync/SyncAdapter.java:50`), boot
(`SYNC_CAUSE_BOOT`), and `TdlibManager.makeSync` which can block on a latch with timeout
(`TdlibManager.java:97-137` [V]). At most `Config.MAX_RUNNING_TDLIBS = 5` instances run a task at once
(`Config.java:96`, `TdlibManager.java:1606-1608` [V]).

**Conditions as executors.** `ConditionalExecutor`s express readiness: initialized, my user or
unauthorized, connected, notifications initialized (`status == UNAUTHORIZED || (READY &&
haveInitializedNotifications)`), no pending notifications, closed, no references
(`Tdlib.java:10743-10830` [V]). Every background job is phrased as "await X, then do Y".

**registerDevice.** `checkDeviceTokenImpl` sends `RegisterDevice(token, otherUserIds)` and on success
unregisters the same token on other accounts; on failure it retries with `SetAlarm(seconds)`
(`Tdlib.java:663-712` [V]); `TdlibManager.checkDeviceToken` drives it for all accounts with a retry count
(`TdlibManager.java:1711-1790` [V]).

**Process dead.** FCM starts the process → `TdlibManager.instance()` loads accounts → the `Tdlib` for
the push's account creates its `ClientHolder` lazily on first `client()` → `ProcessPushNotification`;
if nothing is shown within 7 s a foreground notification keeps the process alive (`PushProcessor.java:214-267`
[V]). The authorization/`isLoggingOut` state decides whether the processor may be aborted
(`Tdlib.notifyPushProcessingTakesTooLong :6646-6658` [V]).

### C7. Lifecycle

- **online**: `Tdlib.setOnline(bool)` sends `SetOption("online")` through `performOptional` (skipped
  when paused) (`Tdlib.java:6584-6598` [V]); `BaseActivity.onPause/onResume` toggle it for the
  **current** account only, and `setTdlib` moves the online flag from the old to the new account on a
  switch (`BaseActivity.java:338-352, :1098, :1147, :1180-1184` [V]). A new `ClientHolder` re-sends
  `online = true` if the account is online (`Tdlib.java:285-289` [V]). `Config.NEED_ONLINE = true`
  (`Config.java:280`).
- **network type**: `WatchDog` maps connectivity to `NetworkTypeWiFi/Mobile/MobileRoaming/Other/None`
  (`core/WatchDog.java:450-475` [V]) → `TdlibManager.setNetworkType` → every `Tdlib.setNetworkType`
  (`TdlibManager.java:746-752`, `Tdlib.java:6431-6446` [V]); `modifyClient` re-sends it to each new client
  (`TdlibManager.java:571-579` [V]).
- **connection state**: `updateConnectionState` maps to an int, starts a 5 s / 9 s "connection
  resolver" to try proxies, and calls `watchDog().helpDogeIfInBackground()` when a connection is lost
  (`Tdlib.java:8991-9056` [V]).
- **UI state**: `TdlibManager.onUiStateChanged` only feeds the pause timers (`TdlibManager.java:428-437`);
  `TdlibCache` pauses "last seen" timers when not `RESUMED` (`TdlibCache.java:264-266`).
- **closing**: `ClientHolder.close()` does nothing ("Nothing to do anymore?", `Tdlib.java:404-410` [V]);
  instances are closed only by `Close` for restart/erase/pause, never at process exit.
- **cold-start tricks**: `GetOption("version")` in the `Client` constructor (`TDJ Client.java:271`),
  eager `SetTdlibParameters` + `SetAlarm(0)` + `GetApplicationConfig` in `ClientHolder.init`
  (`Tdlib.java:314-366`), "optimizing" UI after 1 s without a parameters answer (`:330-332`),
  `awaitInitialization` gates for every blocking call, first chat-list chunk laid out on the TDLib
  thread before the UI sees it (C3), `TGMessage` layout on the TDLib thread (C4).

### C8. UI delivery

- **Subscription:** screens subscribe to typed, keyed listener lists: per chat id
  (`specificChatListeners`, `messageChatListeners`), per list key (`chatListListeners`), per file id,
  per user id in `TdlibCache` (`TdlibListeners.java:40-74`, `TdlibCache.java:121-154` [V]). References
  are weak, so a forgotten unsubscribe leaks nothing but may still deliver a callback until GC.
- **Threading rule:** callbacks arrive on the TDLib thread; **each listener hops** with
  `runOnUiThreadOptional` (drops if the controller is destroyed, `ViewController.java:547-568`) or
  `tdlib.ui().post` plus a "is this still my chat" guard (`MessagesManager.java:3438-3442`). Nothing in
  the data layer hops for them, so pure bookkeeping listeners stay cheap.
- **Granularity:** per-row methods on `ChatsAdapter`/`TGChat` return whether a row changed and the
  list `invalidateViewAt` vs `notifyItemChanged` depending on whether size changed
  (`v/ChatsRecyclerView.java:136-149` [V]). Within a row, `MultipleViewProvider` tracks attached views
  and invalidates only them (`TGChat.java:139`, `TGMessage.java:311, :2689-2748` [V]);
  `ChatView.invalidateTypingPart` has a `TODO invalidate only needed part` (`ChatView.java:541-542`).
- **Jank countermeasures:** custom-drawn rows (`ChatView.onDraw :566`, `MessageView`); text layout
  precomputed off the UI thread (`processMessages` → `prepareLayout`, `displayChats` → `TGChat(…,
  makeMeasures)` for the first chunk, `ChatsController.java:794-805` [V]); live-added rows measured on UI
  (`ChatsAdapter.java:714`); status timers paused in background; `> 100 ms` update logging; the test
  README's rule "code that runs on every frame doesn't allocate" (`tests/README.md`, Drawing section [V]).

### C9. Model layer

TDLib objects are used **directly** everywhere: caches hold `TdApi.User/Chat/…`, listeners pass
`TdApi.*`, `TGChat` keeps a `TdApi.Chat` copy and `TGMessage` a `TdApi.Message`. There is no mapped
domain model; the only "view models" are layout objects (`TGChat`, `TGMessage`, `MediaWrapper`,
`ContentPreview`). Two tricks make the raw-TDLib approach survive schema bumps:

1. **Generated compile-time assertions.** `tdlib-utils` generates `assertX_<hash>()` stubs per
   TDLib union type; call sites that must handle every constructor call them in `default:` so a new
   constructor breaks the build (`UTILS/../generated/kotlin/tgx/td/TdCompileAssert.kt:27-29`,
   used at `Tdlib.java:9014, :7616, :10765`, `TD.java:933` [V]), and `Td.unsupported(data)` groups
   crash reports (`TdUnsupported.kt:27-29` [V]).
2. **Copy helpers with constructor echoes.** `copyTo`/`copyOf` in `TdCopy.kt` re-invoke the full
   constructor under `if (COMPILE_CHECK)` so any added field is a compile error
   (`UTILS/TdCopy.kt:25-45` [V]); `Message.copyTo`, `User.copyTo`, `Chat.copyOf`, `File.copyTo` exist
   (`:139, :288, :555, :81`).

A custom `TdApiExt.MessageChatEvent` content subclass turns chat events into messages for the event log
(`MessagesLoader.java:1270-1285` [V]).

### C10. Errors

- Transport: every answer may be a `TdApi.Error`; `Tdlib.ResultHandler<T>` splits into `(result,
  error)` (`Tdlib.java:1672-1690` [V]).
- Surfacing: `UI.showError(obj)` toasts `TD.toErrorString` and logs with a stack
  (`tool/UI.java:447-456` [V]). Login strings map known messages (`TD.java:2531-2537`).
- FLOOD_WAIT: `TD.getFloodErrorSeconds(code, message)` parses `429 "Too Many Requests: retry after N"`
  (`TD.java:2578-2583` [V]); `getPremiumActiveUntil` parses code 420 (`:2585-2590`). No generic
  automatic retry on 429 was found [V on absence in the files read].
- Retries found: device token with `SetAlarm(seconds)` backoff (`Tdlib.java:702`); `refreshChatMemberStatus`
  re-reads `GetChatMember` up to 3 times at 0.5 s + 0.5 s·n because the server may lag behind a member
  status change (`:5668-5696` [V]); `LoadChats` error = end (`TdlibChatList.java:328`); history error =
  empty chunk (`MessagesLoader.java:310-316`).
- 401 during push processing means "a log out is coming" and TGX waits for `Closed`
  (`Tdlib.java:6686-6695` [V]).
- Fatal: `SetTdlibParameters` error → `Tracer.onTdlibFatalError` (`:325`); TDLib log verbosity 0 → stored
  crash + recovery mode (`TdlibManager.java:350-358`); `isDatabaseBrokenError`, `isDiskFullError`
  classify TDLib log messages (`UTILS/TdError.kt:23-40` [V]).

### C11. Testing

`tests/README.md` describes unit (JVM), integration (device, inside the app), UI (UI Automator, separate
process), macro- and microbenchmarks and baseline profiles, with fixtures `FakeChats.kt`,
`MessageTypes.kt`, `PushContents.kt` under `tests/ui/app/kotlin/tgx/test/` [V on existence; contents
not read, U]. The wrapper offers `Client.Interceptor` to answer queries in tests while preserving
ordering via the `SetAlarm(0)` substitution (`TDJ Client.java:60-69, :100-108` [V]). No unit tests of the
update pipeline or caches were found in the clone [U: `tests/unit` is not present in the shallow clone
listing].

### C12. Code structure and regrets

Issues are disabled on the GitHub repository (`gh issue list` → "has disabled issues") and the clone
is shallow, so this is from comments and dead code:

- `FIXME TDLib: AuthorizationStateLoggedOut` — TGX keeps its own `isLoggingOut` flag because TDLib
  reports `Closed` for both a log out and a plain close (`Tdlib.java:1226` [V]).
- `FIXME TDLib: Proper database optimization` — erasing the database requires `Close` + delete + new
  client (`:1065` [V]).
- `FIXME[tdlib]: it is safe as long as there's just one thread for all apps` / `more reliable way` —
  thread identity by name (`:2268-2271` [V]).
- `FIXME send as a single update` — `SendMessageAlbum` answers are fed one by one into
  `updateNewMessage(…, isUpdate=false)` (`:212` [V]).
- `FIXME why the fuck server thinks it's client's job to fetch these messages` — pinned filter returns
  partial albums (`MessagesLoader.java:444` [V]).
- `FIXME once TDLib will have onlyLocal flag in SearchChatMessages` (`MessagesManager.java:1171` [V]).
- `/*FIXME TDLib*/` on `msg.topicId == null` (`TGMessage.java:4685` [V]); `FIXME when TDLib will get a
  field` (`TD.java:4619`).
- Dead or disabled logic: `checkPositionInList` starts with `if (true) return;` (`MessagesManager.java:2053`),
  `Config.NEED_MEDIA_GROUP_MERGE_REQUESTS = false`, `ALLOW_MORE_CACHED_MESSAGES = false`
  (`Config.java:124-125`), `UpdateStopMessageDraft` → `// TODO?` (`Tdlib.java:10189`), `updateChatAddedToList`
  `// TODO: sort?` (`:8128`), `TdlibChatListSlice.indexOfChat` linear `// TODO lookup without increase in
  number of operations` (`:119`).
- `Tdlib.java` is 12.4k lines and `TdlibUi.java` 8.3k: the "account" object absorbed request helpers,
  update handlers, caches and UI glue; comments admit it (`// TODO move myUserId management to
  TdlibContext`, `:2560-2565`; `// TODO migrate all tdlib.client().send(..) to tdlib.send(..)`, `:1647`).

---

## 3. Four flows

### (a) Cold start to chat list on screen

1. `TdlibManager` constructor: TDLib log handler, load accounts, watchdog, UI state listener, device
   token (`TdlibManager.java:350-379` [V]).
2. `MainActivity.onCreate` picks the current account and pushes `MainController` → `ChatsController`
   (`MainActivity.java:126-180, :244` [V]).
3. `ChatsController` creates `tdlib.chatList(ChatListMain).slice(filter)` (`ChatsController.java:465`),
   subscribes to global/cache/folder listeners and calls `list.initializeList(this, this::displayChats,
   initialLoadCount, onLoaded)` (`:600-634` [V]).
4. First `tdlib.client()` creates the `ClientHolder`: `Client.create` (starts the TDLib thread),
   `SetTdlibParameters` **before** any auth state, `SetAlarm(0)`, `GetApplicationConfig`
   (`Tdlib.java:1763-1766, :279-366` [V]).
5. TDLib → `updateAuthorizationState(...)`: `updateAuthState` clears chats (prev status `UNKNOWN`),
   fires `onInitialized` so `awaitInitialization` jobs run (`:1215-1219, :1326-1328` [V]).
6. TDLib → `updateNewChat` ×N (with positions), `updateUser` ×N, `updateSupergroup` …: `chats.put`,
   `TdlibChatList.onUpdateNewChat` inserts entries with `order != 0` and emits `updateChatAdded`
   (`:7760-7791`, `TdlibChatList.java:389-396, :455-469` [V]). The slice's listener (subscribed in step
   7, same thread) adds within its window or grows it.
7. `TdlibChatList.initializeList` (on the TDLib thread): snapshot → slice callback (filter, add to
   `filteredList`) → subscribe → `loadAtLeast(initialChunk)` → `LoadChats(main, n)` (`TdlibChatList.java:261-338`
   [V]). The `Ok`/`Error` answer sets the state; `onLoadInitialChunk` → `dispatchChats(0)`
   (`TdlibChatListSlice.java:313-320, :334-350` [V]).
8. `dispatchChats` → `ChatsController.displayChats(entries)` **on the TDLib thread**: `new TGChat(…,
   makeMeasures = true)` for the first `initialLoadCount` rows (title/text/time laid out, avatar file
   chosen) (`ChatsController.java:794-805`, `TGChat.java:149-213, :332-341` [V]).
9. `runOnUiThreadOptional` → `adapter.addMore(chats)` → `notifyItemRangeInserted`
   (`ChatsAdapter.java:374-404`) → `onBindViewHolder` → `ChatView.setChat(TGChat)` attaches the view to
   the `TGChat` (typing helper subscribes) and `avatarReceiver.requestChat` → `ImageLoader` (`ChatView.java:362-375,
   :455-457`) → `onDraw` (`:566`).
10. Later `updateChatPosition`/`updateChatLastMessage`/`updateChatReadInbox` flow through C3 and reach
    `adapter.moveChat/updateChatTopMessage/updateChatReadInbox` on the UI thread.

### (b) Open a chat: first page, scroll up, jump to a message

1. Row click → `tdlib.ui().openChat(controller, chat, params)` (`ChatsController.java:2151`) →
   `MessagesController` → `MessagesManager.openChat` (`MessagesManager.java:712` [V]).
2. `tdlib.openChat(chatId, controller)` → `OpenChat` once per chat (`Tdlib.java:4645-4677`).
3. Anchor: `getAnchorHighlightMode(accountId, chat, thread)` chooses saved position vs unread
   (`:3318-3342`); `loadFromMessage(anchor, HIGHLIGHT_MODE_UNREAD/POSITION_RESTORE, force)` or
   `loadFromStart()` (`:741-745`).
4. `MessagesLoader.load`: `GetChatHistory(chatId, from, 0, 19, onlyLocal=true)` for the start, or
   `GetChatHistory(chatId, anchor, -19, 33, false)` for an anchor (`MessagesLoader.java:1060-1079,
   :1100-1222` [V]).
5. Answer on the TDLib thread → `newHandler.onResult` (context guards) → `processMessages`: `TGMessage`
   per message, albums combined, read flags, unread badge, grouping, **`prepareLayout()`**; `UI.post`
   `displayMessages` (`:244-540, :1309-1720` [V]). If local was empty → remote request; if fewer than 19
   → `loadMore(top)` right away (`:1627-1651`).
6. `displayMessages(MODE_INITIAL)` → `adapter.addMessages(items, true)`, scroll to anchor
   (`scrollToMessage`), `viewMessages(false)` → `ViewMessages` (`MessagesManager.java:1208-1256`).
7. **Scroll up:** `viewMessages(byScroll)` → `last + TOP_PRELOAD_COUNT >= count` → `loader.loadMore(true)`
   → `GetChatHistory(chatId, topSmallestId, 0, 50, onlyLocal=false)` → `MODE_MORE_TOP` → `adapter.addMessages(items,
   top=true)`; an empty remote answer sets `canLoadTop = false` → `onTopEndLoaded`
   (`:288-313`, `MessagesLoader.java:1229-1256, :1608-1626` [V]).
8. **Jump:** `highlightMessage(messageId, HIGHLIGHT_MODE_NORMAL, returnToIds)` →
   `loadFromMessage(id, mode, force=true)` → `MODE_INITIAL` with `canLoadTop = canLoadBottom = true`;
   `processMessages` finds the item (or the nearest larger id for search), `displayMessages` resets the
   adapter and `scrollToMessage(index, item, HIGHLIGHT_MODE_NORMAL)`; `canLoadBottom()` later compares the
   bottom id with `chat.lastMessage.id` to re-enable "scroll to bottom" (`:3142-3165`, `MessagesLoader.java:1071-1079,
   :1468-1520, :1800-1835` [V]). The `returnToMessageIds` stack drives the "back to where I was" button
   (`:3135-3140`).

### (c) A new message arrives

**While the chat is open:**

1. TDLib thread: `processUpdate` → `updateNewMessage(update, true)`: a pending outgoing message is only
   registered as "sending" and returns; otherwise `listeners.updateNewMessage`,
   `notificationManager.onUpdateNewMessage` (sound if this chat is on screen), global listeners
   (`Tdlib.java:7528-7549`, `TdlibNotificationManager.java:2170-2186` [V]).
2. `TdlibListeners.updateNewMessage` → `messageChatListeners[chatId]` → `MessagesManager.onNewMessage`
   (still TDLib thread): skip if it is one of our `sentMessages`, `TGMessage.valueOf(chatStrict(chatId))`,
   `prepareLayout()`, `tdlib.ui().post` with chat-id guard (`TdlibListeners.java:629-633`, `MessagesManager.java:3423-3443`
   [V]).
3. UI: `updateNewMessage(TGMessage)`: topic check, bottom-album absorb (`combineWith`), then if the end
   of history is loaded insert at index 0 and scroll if at bottom / outgoing; if the user is somewhere in
   old history and the message is outgoing → `loadFromStart()` (`:1897-1958` [V]).
4. Same update also mutates the chat list (next section) and `updateChatReadInbox` follows when TDLib
   counts it unread; the `Viewport` sends `ViewMessages` once the view is visible and focused.

**While only the list is visible:**

1. `updateNewMessage` → `MessageListener`s: `ChatsController` is subscribed globally
   (`subscribeForGlobalUpdates`) and only invalidates if it holds the row; `MessagesManager` is not
   subscribed (closed chat).
2. `updateChatLastMessage`: `chat.lastMessage = …`, `setChatPositions` diff → `ChatListener.onChatTopMessageChanged`
   (→ `adapter.updateChatTopMessage` → `TGChat.updateTopMessage` → `setText`, `invalidateViewAt`) and
   `TdlibChatList.onUpdateChatPosition` → `updateChatMoved` → `ChatsController.onChatMoved` →
   `adapter.moveChat` (`Tdlib.java:7896-7911`, `TdlibListeners.java:942-962`, `TdlibChatList.java:399-432`,
   `ChatsController.java:2704-2710`, `ChatsAdapter.java:680-709` [V]).
3. `updateChatReadInbox` → `onChatListItemChanged(READ_INBOX|UNREAD_AVAILABILITY_CHANGED)` →
   `adapter.updateChatReadInbox` → counter animation (`Tdlib.java:8332-8350`, `TdlibListeners.java:1076-1087`
   [V]); `updateUnreadChatCount` → badge (`:9171-9176`).
4. Notification: TDLib emits `updateNotificationGroup` → `NotificationQueue` → `rebuildNotification`
   (C6). The app does not build notifications from `updateNewMessage`.

### (d) An image message, thumbnail to shown

1. `TGMessage.valueOf` → `TGMessageMedia` → `MediaWrapper(photo)`: `setPreviewSize(photo.minithumbnail,
   previewSize)` creates `ImageFileLocal(minithumbnail)` and the `ImageFile` of the small `PhotoSize`;
   the target `ImageFile` is the big size (`MediaWrapper.java:668, :697-712` [V]).
2. On bind/draw: `requestPreview(DoubleImageReceiver)` → `receiver.requestFile(miniThumbnail,
   previewFile)` (`:805-811`). The mini thumbnail is decoded from bytes on `ImageReaderThread`
   (custom file → `act()` immediately, `ImageActor.java:80-96`) and drawn blurred at once; the preview
   `PhotoSize` is requested through `ImageLoader.requestFile` → `DownloadFile(previewFileId, 15, 0, 0)`.
3. The full photo: either auto-download (`TdlibFilesManager.downloadAutomatically` → `downloadFile`,
   priority 16, `:667-675, :791-806`) or on tap; `ImageReceiver.requestFile(target)` adds a watcher.
4. TDLib → `updateFile` while active → `Tdlib.updateFile` → `files().onFileProgress` (the
   `FileProgressComponent` ring) and `ImageLoader.onProgress` → watchers' `imageProgress`
   (`Tdlib.java:8935-8952`, `ImageLoader.java:240-279` [V]).
5. TDLib → `updateFile` with `local.isDownloadingCompleted` (or the `DownloadFile` answer) →
   `files().onFileLoaded` → `STATE_DOWNLOADED_OR_UPLOADED` listeners; `ImageLoader.onLoad` → `thread.onLoad(actor,
   file)` → `ImageReader` decodes/scales/crops on `ImageReaderThread` → `ImageLoader.onResult` → `ImageCache.putBitmap`
   → every `WatcherReference.imageLoaded` (`:281-360`, `ImageActor.java:110-130` [V]).
6. `ImageReceiver.setBitmap` on UI, alpha animator, `view.invalidate(drawRegion)`; `MediaWrapper.showImage()`
   switches from preview to target when `TD.isFileLoaded(targetFile)` (`ImageReceiver.java:758+, :111-125`,
   `MediaWrapper.java:813-815` [V]).
7. Scrolling away: `MessageView` detach → receivers `detach()` → `ImageLoader.removeWatcher` →
   `CancelDownloadFile(id, onlyIfPending)` when no watcher remains (`ImageLoader.java:188-235` [V]).

---

## 4. Hidden rules

TDLib quirks, races and ordering constraints that the code works around. "TGX" marks the workaround;
"[I]" marks the inferred TDLib behavior behind it.

1. **Updates and answers are one ordered stream per client; process them on one thread.** TGX applies
   caches synchronously on the receive thread and lets each listener hop. Posting updates to another
   thread before caching them would let `chat(id)` miss a chat that `updateNewMessage` already refers to
   (`Tdlib.java:381-399`; `chatStrict` throws `"updateChat not received"` `:2698-2707`).
2. **`SetAlarm(0)` is a barrier**: its answer arrives after every earlier answer and update. TGX uses it
   to "post to the TDLib thread" and to sequence `after` callbacks (`Tdlib.java:302-312, :4668-4672`;
   `TDJ Client.java:104-107`).
3. **TDLib re-sends `updateNewChat` for every known chat on each instance start**, so the chat cache must
   be cleared when the first auth state of an instance arrives (`:1215-1219`) [I on TDLib behavior].
4. **`updateNewChat` precedes any other update about that chat; `updateUser`/`updateSupergroup`
   precede `updateNewChat`**, so a cache may be `assertChat`-guarded and `user(id)` may `Log.bug` instead
   of fetching (`TdlibUtils.assertChat` in every handler; `TdlibCache.java:933-942`) [I].
5. **A chat's position is in `positions[]` of `updateNewChat`, `updateChatLastMessage`,
   `updateChatDraftMessage` and in `updateChatPosition`**; `order == 0` means "not in this list"; a chat
   leaving a list shows up as a position missing from the new array. TGX diffs old vs new arrays and
   emits a change per list (`Tdlib.java:8012-8081`).
6. **`LoadChats` returns an error when the list is fully loaded** and otherwise delivers chats only via
   updates before `Ok`; the `Ok` itself carries nothing (`TdlibChatList.java:327-329`) [V on handling, I on
   TDLib].
7. **Snapshot and subscribe must be atomic** on the update thread, or the first chats are either lost or
   duplicated (`TdlibChatList.java:261-279`).
8. **Sorted inserts must be unique.** Binary search hitting an existing entry is treated as a bug
   (`IllegalStateException`, `TdlibChatList.java:416-417, :460-461`), and `presentChatIds` dedupes at the
   adapter (`ChatsAdapter.java:642-645`).
9. **`updateUserFullInfo` only arrives for full infos the client has asked for**; TGX re-requests
   `GetUserFullInfo` every time it reads one with `allowRequest` (`TdlibCache.java:1129-1143`) [I].
10. **`updateUserStatus` is frequent and must not re-layout rows**; TGX routes it through a UI handler
    and only invalidates the online dot; "last seen" text refreshes run on a timer that is paused in
    background (`TdlibCache.java:509-540, :343-384`).
11. **Typing ends by `ChatActionCancel`, not by timeout** in TGX (`TdlibStatusManager.java:555`); on
    restart all actions are force-cancelled (`:499-505`).
12. **`openChat` is reference-counted and `closeChat` delayed by 1 s** so navigation back-and-forth
    does not toggle TDLib's "open" state and lose `updateChatAction`/read-marking (`Tdlib.java:4645-4703`).
13. **Load local history first (`onlyLocal = true`), then remote**, and if the local chunk is short,
    load more immediately; a remote empty answer is the only "end reached" signal
    (`MessagesLoader.java:1060-1069, :1627-1651, :1587-1626`).
14. **`GetChatHistory` with a negative offset is how you center on a message** (`offset = -19, limit =
    33`), and "limit must be greater than -offset" (`:1245-1247, :67-71`).
15. **A history answer may belong to a previous chat or request**: guard by `contextId` and by
    `lastHandler` identity, and ignore late answers (`:244-257`).
16. **Albums can be split across chunk boundaries and across search results**; TGX merges by
    `mediaAlbumId` within a chunk, fetches missing pinned-album members, and has (disabled) code to
    request more around an edge album (`:380-466`, `Tdlib.getAlbum :2095-2200`).
17. **`updateMessageSendSucceeded` changes the message id**: every structure keyed by message id must be
    remapped (saved scroll position `Tdlib.java:7554-7557`, pending UI copies `TdlibListeners.java:637-645`,
    viewer `TdlibMessageViewer.java:515-527`, return-to stack `MessagesManager.java:2013-2022`). TGX
    deliberately does **not** re-sort the list by the new id (`:2053-2055`).
18. **`updateNewMessage` for your own outgoing message arrives twice in spirit**: once as the pending
    message (`sendingState = Pending`) and once as the `SendMessage` answer; TGX treats pending ones as
    "sending" only and dedupes via `sentMessages` (`Tdlib.java:7529-7534`, `MessagesManager.java:3425-3427`).
19. **`updateDeleteMessages` with `fromCache = true` must be ignored** (TDLib evicting memory, not a
    deletion) (`Tdlib.java:7707-7709`).
20. **Chats without the end of history loaded must not append a live message**: if `canLoadBottom`,
    an outgoing message triggers `loadFromStart()` instead (`MessagesManager.java:1951-1953`).
21. **Mutating the shared `TdApi.Chat`/`TdApi.User` in place (`copyTo`) is how TGX keeps every holder
    current without a diff**, at the cost of `dataLock` around every read; `TGChat` instead takes a
    `copyOf` snapshot because it caches layout derived from the fields (`TdlibCache.java:437-456`,
    `TGChat.java:154`).
22. **A `DownloadFile` that is already running for another consumer with a different window or
    priority must be re-issued**, TDLib keeps the last request's parameters; TGX merges all consumers'
    chunks into one request (`TdlibFilesManager.java:223-260`). `CancelDownloadFile(id, onlyIfPending)`
    ("weak") avoids killing a download another consumer still wants (`ImageLoader.java:231`).
23. **`updateFile` is the only truth for progress and completion; the `DownloadFile` answer may already
    say completed.** Both paths feed the same `onFileLoaded` (`TdlibFilesManager.java:543-575`).
24. **TDLib may start downloads the app never asked for** (auto-download, prefetch); `onFileUpdate`
    adopts them into `pendingOperations` (`:883-905`).
25. **Minithumbnails are JPEG bytes, decode them locally and show immediately**; the preview
    `PhotoSize` and the full size are two more files (`MediaWrapper.java:697-712`).
26. **Notifications: TDLib's `updateActiveNotifications` arrives once after start and may be ignored
    by design (`ignoreNotificationUpdates`); push handling must wait for "notifications initialized"
    and for `havePendingNotifications == false` before releasing the process** (`Tdlib.java:6670-6684,
    :10776-10780`).
27. **`ProcessPushNotification` → 401 means a log out follows**; wait for `Closed` instead of syncing
    (`:6686-6695`).
28. **`SetOption("online")` and `SetNetworkType` must be re-sent to every new client instance**
    (`:285-289`, `TdlibManager.java:571-579`).
29. **`Closed` is ambiguous**: TGX keeps `isLoggingOut` to know whether to erase the database
    (`:1226`), and keeps `pendingEraseActor` to erase after a `Close` it requested (`:1065-1081`).
30. **A `Chat` fetched by `GetChat` does not refresh the cached object's flags**; TGX hand-copies
    `canBeDeleted*`, `canBeReported`, and detects mute changes to fake an `updateChatNotificationSettings`
    (`updateChatState :7811-7826`).
31. **Member status changes are eventually consistent on the server**; `GetChatMember` is retried up to
    three times with growing delay (`:5668-5696`).
32. **Some server-side lists are enforced only by TDLib's `Error` answers**; `clientExecute` whitelists the
    functions allowed before `SetTdlibParameters` has been answered (`:2319-2334`).
33. **Weak listener lists**: the iterator is non-reentrant; notifying inside a notification deadlocks or
    throws (`ReferenceList.java:42-45`). TGX never nests `runUpdate` calls for the same list.
34. **Layout must not depend on the UI thread**: `TGMessage.buildLayout` and `TGChat.buildLayout` run on
    the TDLib thread, and `rebuildAndUpdateContent` checks the Looper to choose `postInvalidate`
    (`TGMessage.java:1368-1383`).
35. **Message list is reversed**: index 0 is the newest; all "at bottom" logic uses
    `findFirstVisibleItemPosition() == 0` (`MessagesController.java:717`, `MessagesManager.java:1915`).

---

## 5. Transplant list for Kotogram (coroutines + Compose on the given `TdClient`)

Reference for the target: `TdClient.updates: SharedFlow<Update>` (replay 0, unbounded buffer, never
completes), `authorizationState: StateFlow`, `suspend send/execute/close`
(`tdlib-kmp-sources-1.8.69-1.0.0-alpha02/client/commonMain/io/xbot/tdlib/client/TdClient.kt:31-53`,
`TdReceiver.kt:50-62, :84-98` [V]). The receive loop runs on `Dispatchers.Default` and emits with
`tryEmit` into an unbounded buffer, so **collectors see updates in order but asynchronously**, and
different collectors can be at different points of the stream.

### Carry over (essential to the problem)

| TGX entity / rule | Kotogram shape | Why |
|---|---|---|
| One ordered application of updates to caches, before anyone else reacts (rules 1, 3, 4, 7) | One `@Scoped` **`UpdateProcessor`** per account that is the **only** collector of `TdClient.updates`, applies updates to the caches in order, and emits derived state. Repositories expose `StateFlow`/`Flow` from the caches; UI never collects raw `updates` for anything that depends on cache contents. `docs/architecture.md §5` already says this. | With several collectors the UI could see `UpdateNewMessage` before the cache saw `UpdateNewChat`. TGX's whole correctness rests on rule 1. |
| `Tdlib.chats` + `TdlibCache` (users, basic groups, supergroups, secret chats, options) | `ChatCache`, `UserCache`, … as `MutableStateFlow<Map<Long, Chat>>` or `MutableStateFlow` per entity id, mirrored 1:1 into domain models (`Chat`, `User`). Immutable `data class` copies replace `copyTo`-in-place; `dataLock` disappears because only the processor writes and readers get snapshots. | Same cache set is needed; immutability replaces TGX's lock discipline. |
| Clear on instance start / `Closed` (rule 3, C2 reset) | Processor resets all caches on `UpdateAuthorizationState(Closed)` and when the first auth state of a new instance arrives; `ClientHolder` (Kotogram's) already surfaces the restart. | TDLib re-sends the world; stale entries would survive otherwise. |
| `TdlibChatList` (sorted entries with own position copy, `LoadChats` end-by-error, snapshot+subscribe) and its position diff (rule 5, 6, 8) | `ChatListRepository.observe(chatList): Flow<List<ChatListEntry>>` backed by a per-list sorted structure inside the processor; `loadMore(chatList, limit): Either<Error, Boolean>` where TDLib's 404 maps to "end reached". Keep `ChatPosition` per entry and sort by `(order desc, chatId desc)`. | Order is TDLib's contract; the structure is the minimal correct one. Compose's `LazyColumn` with `key = chatId` does the diffing TGX does by hand, so `TdlibChatListSlice`'s window bookkeeping is **not** needed. |
| `TGChat` composition: draft vs last message, counters, pin/mute/scam flags, online, typing | A `ChatListItemState` produced in the ViewModel by `combine(chatEntries, users, chatActions, userStatuses)`; Compose recomposes only rows whose item changed. | The *content* rules carry over; the layout caching does not. |
| `TdlibStatusManager` chat actions keyed by `chatId_topic` with `ChatActionCancel` ending them (rule 11) | `ChatActionRepository.observe(chatId, topicId)` fed by the processor; no own timers unless TDLib misbehaves. | Same TDLib contract. |
| `openChat`/`closeChat` refcount with delayed close (rule 12) | `ChatSessionRepository.open(chatId)` returning a handle; close debounced ~1 s; counted per account. | Still TDLib's contract: `openChat` must be balanced; navigation back-and-forth is just as fast in Compose. |
| `MessagesLoader`: local-first, chunk sizes, negative offset for centering, context guard, end-by-empty-remote, album merge (rules 13-16) | `MessageHistoryRepository` with `suspend fun history(chatId, from, offset, limit, onlyLocal)`; a per-screen `MessageWindow` state holder (in the ViewModel) that owns `canLoadTop/Bottom`, dedupes by id, folds albums by `mediaAlbumId`, and cancels stale loads via coroutine cancellation (replaces `contextId`). | The window logic is the essence of a TDLib chat screen; coroutines give the cancellation TGX fakes with `contextId`/`lastHandler`. |
| Live merge rules (17-20): remap ids on send-succeeded, ignore `fromCache` deletes, don't append when the end isn't loaded, dedupe own sent messages | Same rules inside the `MessageWindow`. | TDLib behavior, independent of UI toolkit. |
| Unread anchor and saved scroll position (C4 anchor) | `getAnchorHighlightMode` logic ported; scroll position stored in DataStore keyed by (account, chat, topic) and remapped on send-succeeded. | Product behavior that depends only on TDLib fields. |
| `TdlibMessageViewer`: visible → `ViewMessages(source, forceRead)` only while focused | `LazyListState.layoutInfo.visibleItemsInfo` → debounce → `ViewMessages`; gate on window focus / app foreground. | TDLib needs explicit views; Compose exposes visibility directly. |
| `TdlibFilesManager` operations: merge consumers' (priority, offset, limit) into one `DownloadFile`, weak cancel, adopt foreign downloads, `updateFile` as truth (rules 22-24) | `FileRepository`: `download(fileId, priority, offset, limit): Flow<File>` with reference counting inside and `CancelDownloadFile(onlyIfPending)` on last unsubscribe; `updates` of type `UpdateFile` routed by file id through the processor. | TDLib's single-request-per-file model forces an arbiter; the chunk merge is the correct minimal one. |
| Minithumbnail → preview → full (rule 25) | Image composable with three layers: decoded minithumbnail bytes, preview `PhotoSize` file, full file; Coil/own loader keyed by `(accountId, fileId)`. | Same TDLib data shape. |
| Notification pipeline on its own serial executor; await "notifications initialized" and "no pending notifications" before releasing background work; 401 on push = wait for `Closed` (rules 26, 27) | A `NotificationProcessor` collecting `UpdateNotificationGroup/UpdateActiveNotifications` from the processor into a `Channel`/single-threaded dispatcher; background work expressed as `suspend` `first { condition }` on state flows (TGX's `ConditionalExecutor`). | Contract with TDLib and the OS; coroutines express "await condition" natively. |
| Re-send `online` and network type to each new instance (rule 28) | Done in Kotogram's `ClientHolder` on instance start from an `AppLifecycle` state flow. | TDLib per-instance options. |
| `isLoggingOut` and pending erase around `Closed` (rule 29) | Already in `docs/design/sessions.md` v2 as TDLib-state rules. | Ambiguous `Closed`. |
| Compile-time schema guards (C9) | tdlib-kmp DTOs are `@Serializable` sealed hierarchies: exhaustive `when` over sealed interfaces gives the same guarantee as `assertX_hash()` for free; `data class copy()` replaces `copyTo`. Rule: no `else ->` on TDLib unions in caches. | Same goal, native Kotlin means. |
| Error surface: `TdApi.Error` → typed; parse `429 "Too Many Requests: retry after N"`; retry `GetChatMember` eventually (C10) | `TdException` → `Either<Error, T>` in repositories; a `FloodWait(seconds)` case parsed from code 429; explicit retries only where TGX has them. | Same server behavior. |
| Weak-listener discipline → replaced by structured concurrency | Collectors live in `viewModelScope`/account scope; no weak lists. | Coroutines make the leak problem TGX solves with weak refs disappear. |

### Do not carry over (Java and Views legacy)

| TGX piece | Why it is legacy |
|---|---|
| `Tdlib`'s 12k-line god object and `TdlibUi extends Handler` | It conflates account, caches, request helpers and UI glue; Kotogram's split into `ClientHolder`, processor, caches, repositories, ViewModels already covers it. |
| `TdlibListeners` typed listener lists, `ReferenceList` weak refs, per-listener `runOnUiThreadOptional` hops | Replaced by `Flow`s per entity/list/file and `collectAsStateWithLifecycle`; cancellation replaces "is destroyed" checks. |
| `TdlibChatListSlice` window (`displayCount`/`maxSize`), `ChatsAdapter.notifyItem*`, `presentChatIds` | `LazyColumn` with stable keys diffs the whole list; paging is "when the last visible index approaches the end, call `loadMore`". Filtering is a `map` on the flow. Keep only `bringToTop` if a product need appears. |
| `TGChat`/`TGMessage` pre-laid-out `Text` objects, `MultipleViewProvider`, `invalidateViewAt` vs `notifyItemChanged`, custom `onDraw` rows | Compose lays text out itself and recomposes per changed item; precomputing on the TDLib thread is unnecessary and would fight Compose's text pipeline. Keep **data** precomputation (content preview string, entities, sender name resolution) in the ViewModel, off the main thread, as a `derived` state. |
| `runOnTdlibThread` via `SetAlarm` and thread-name detection | There is no "TDLib thread" to post to; the processor coroutine is the serialization point. `send(SetAlarm(0))` as a barrier remains valid for "TDLib has processed earlier requests", but it does **not** guarantee that the processor has already applied their updates, because `updates` is buffered. If a barrier against the cache is needed, the processor must expose one (e.g. a `Flow` of applied update sequence numbers). Own design, flagged. |
| `clientExecute` blocking with a latch | `suspend send`. |
| `ConditionalExecutor` | `StateFlow.first { predicate }`. |
| `Client.Interceptor` test hook | Not present in `TdClient`; tests need a fake `TdLib` behind `TdClient.create(tdLib)` [U whether `TdLib` is an interface open enough; report as a library question]. |
| `ImageLoader`'s own threads, `ImageCache`, `ImageReceiver` fade animators | Use a Compose image loader (Coil 3 supports KMP) with a custom `Fetcher` that turns a `fileId` into a path via `FileRepository.download(...)`. Keep TGX's **dedupe by file id** and **weak cancel** rules inside the fetcher. |
| In-place `copyTo` of shared `TdApi` objects under a global lock | Immutable models; the processor is the only writer. |
| `Settings`-based scroll position per chat in SharedPreferences | DataStore, same key shape. |
| Android `SyncAdapter`, WorkManager `SyncTask`, FCM `PushProcessor` foreground promotion | Android-only; Kotogram's desktop/iOS targets have no equivalent. Keep the *sequence* (process push → await notifications → release) as a platform-neutral use case and plug platform schedulers in `androidMain`. |

### Own designs introduced above (flagged, not from a primary source)

- A single `UpdateProcessor` collector per account that also emits derived flows. Backed by TGX's
  synchronous handler model and by `docs/architecture.md §5`; the "one collector" part is TGX's rule 1
  re-expressed, the "derived `StateFlow`s" part is ours.
- Replacing `contextId`/`lastHandler` with coroutine cancellation in `MessageWindow`.
- An applied-update barrier if a "wait until the cache has seen my request's updates" primitive is
  needed; TGX never needed it because its handlers and caches share a thread.

---

## 6. Findings about the library (not changes, just facts the design must respect)

- `TdClient.updates` is `extraBufferCapacity = Int.MAX_VALUE` with `tryEmit` (`TdClient.kt:31-33, :47-50`
  [V]). A late subscriber misses everything before it subscribed (replay 0), so the processor must
  subscribe **before** the first `send`; `authorizationState` covers only auth. Kotogram's
  `ClientHolder` already does this for auth; it must do the same for the processor.
- Because updates are buffered per collector, TGX's "answer arrives after earlier updates" barrier (rule
  2) weakens to "TDLib has produced earlier updates"; the processor may not have applied them yet. Design
  consequence noted in the transplant table.
- `TdReceiver.dispatch` drops events it cannot decode (`runCatching { … }.getOrNull() as? Update ?:
  return`, `TdReceiver.kt:88-92` [V]). A new TDLib constructor unknown to the generated DTOs would be
  silently dropped rather than crash; TGX would crash via `Td.unsupported`. Worth a log at least [U
  whether the serializer is lenient].
- No interceptor/fake hook on `TdClient`; testing the processor needs a fake `TdLib` (web `WorkerTdLib`
  shows the interface is pluggable, `TdClient.kt:56-57` [V]) [U on `TdLib` openness].

## 7. What I could not verify

- vkryl `ReferenceList` was read from a third-party copy, not the TGX submodule (same header and
  author; marked [V] on that basis).
- `ImageCache` eviction policy, `UploadFutureSimple`, `TdlibNotificationManager.rebuildNotification`
  internals, and the test sources under `tests/` were not read.
- No issue tracker or commit history is available for TGX (issues disabled, shallow clone), so §C12
  rests on comments and dead code only.
