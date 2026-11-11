# Report 02: how Kotlin, coroutine and Compose TDLib clients go from update to pixel

Date: 2026-11-03. Read-only research; nothing was built or run. Every cited line was read in this session, so a citation with no mark is **[V]**. **[I]** marks an inference from read code (user-visible consequences are almost always [I], since nothing was executed). **[U]** marks what I could not verify.

Rule references come from the two yardstick reports: **CS§n** = contract sheet, `04-unigram-tdlib-contract.md` §1.n; **UNI#n** = its hidden rule n (§5); **TGX#n** = `03-telegram-x.md` §4 hidden rule n. **TL:n** = `td-pack/td/td/generate/scheme/td_api.tl`, **MM:n** = `td-pack/td/td/telegram/MessagesManager.cpp`, both at c15d3f5 (1.8.68).

---

## 0. Findings in one screen

1. **No Kotlin client has the shape the question asks for.** One ordered applier, atomic state across entities, off the main thread, on coroutines, in `commonMain`: nobody has all of these. The three closest each miss something: Pingogram (immutable snapshots, but about 15 independent collectors, Android only), nngGram (one actor and one global snapshot, rebuilt in full per event, JVM only), pixelGram (one serial queue for updates and answers, but on the main thread with Compose state as the cache, JVM only).
2. **Pingogram is the best reference, and its size is real.** A file scan gives 249,974 main and 142,905 test lines. Only theme tables are generated (`core/ui/.../theme/Color.kt` 4.3k lines, `SplashPaletteThemes.kt`, per `docs/ARCHITECTURE.md`); the rest reads as hand-written. It has almost no field evidence (3 stars, Play release dated 2026-11-02). Its `UpdateStore`, `HistoryWindow.changesWhileLoading`, memoising row mappers and Coil fetcher are the most valuable code in this survey.
3. **Multiple collectors have a measurable cost.** Two independent clients added race buffers for "a field update arrived before its `updateNewChat`" (Hortay `pendingLastMessages`, TGWrist `_pendingFieldUpdates`). TDLib guarantees that order, so their own pipeline destroyed it.
4. **Lossy transport is common.** `DROP_OLDEST` or replay on the update flow appears in Mategram, td-ktx, tellygram, TeleDrive, prismGram, telegramedia, Compound and GhostGram.
5. **The largest 1:1 mirror proves little.** In Mategram, 77% of the Kotlin main code is the mirror plus mappers, and it still drops `positions` from `updateChatLastMessage`.
6. **Nobody handles `updateChatLastMessage(last_message = null)` for an open history** (CS§1.6). Only Pingogram separates all three delete kinds. Only Telegram X arbitrates downloads between consumers.
7. **A stable list key across the temporary-to-final message id change appears independently three times** (Aether, pixelGram, nngGram). Pingogram lacks it.
8. **No Compose client runs TDLib on iOS.** GhostGram's iOS target is a `println` stub. No chat-grade client has its data layer in `commonMain`.
9. **TDLib source settles three disputes between clients:** `openChat` is a counter, not a boolean (§5); `from_cache` deletes happen only for chats with zero opens; chat actions cancel themselves after 5.5 s.

---

## 1. Sources and path prefixes

| Tag | Repo | Commit, date | Prefixes used below |
|---|---|---|---|
| PG | kyra-labs/pingogram-android | 99455eb, 2026-11-02 | `R/` = `core/data/src/main/kotlin/dev/kyralabs/pingogram/core/data/repository/`, `CL/` = `feature/chats/src/main/kotlin/dev/kyralabs/pingogram/feature/chats/ui/`, `CH/` = `feature/chat/src/main/kotlin/dev/kyralabs/pingogram/feature/chat/ui/`, `T/` = `core/tdlib/src/main/kotlin/dev/kyralabs/pingogram/core/tdlib/`, `IMG/` = `core/ui/src/main/kotlin/dev/kyralabs/pingogram/core/ui/image/` |
| NNG | badnng/nngGram | b772228, 2026-11-02 | `J/` = `shared/src/jvmMain/kotlin/com/badnng/moe/tdlib/`, `C/` = `shared/src/commonMain/kotlin/com/badnng/moe/` |
| PXG | JettaXP/pixelGram | d918cf4, 2026-11-02 | `P/` = `src/main/kotlin/com/pixelgram/desktop/` |
| HOR | LyoSU/hortay-android | 3f56827, 2026-07-16 | `H/` = `app/src/main/kotlin/dev/lyo/hortay/` |
| AET | iamaritrasaha/aether | a01f5be, 2026-09-15 | `A/` = `app/src/main/java/com/foresightlabs/aether/` |
| MAT | MG-Devs-Team/Mategram | 0aea2da, 2026-04-09 | `MD/` = `mategram/data/src/main/java/com/xxcactussell/data/`, `MP/` = `mategram/presentation/src/main/java/com/xxcactussell/presentation/` |
| GHO | vdggrtff/GhostGram | 89e8714, 2026-10-21 | `GT/` = `core/tdlib/src/`, `GD/` = `data/src/commonMain/kotlin/com/ghostgram/data/repository/` |
| TMP | dracu-lah/TMPlayer | aa78090, 2026-11-03 | `D/` = `core/src/commonMain/kotlin/com/tmplayer/`, `U/` = `ui/src/shared/kotlin/com/tmplayer/` |
| TGW | TGWrist/TGWrist | d33e51d, 2026-08-30 | `W/` = `app/src/main/java/com/tgwrist/app/` |

Other repos are cited as `owner/repo path:line`. All come from `scratchpad/clones/`, except the three in §2.3, which were never cloned and were read through the GitHub API.

---

## 2. Step 1: triage

Measured by my own scan over non-generated `.kt` files. **Main/test** is thousands of lines. **API columns** are grep hits in main code only, so they show presence, not quality: LC `loadChats`, GCH `getChatHistory`, UCP `updateChatPosition`, ULM `updateChatLastMessage`, UNM `updateNewMessage`, UF `updateFile`, VM `viewMessages`, OC `openChat`, RD `registerDevice`, UNG `updateNotificationGroup`. Mategram's numbers exclude its mirror. The inventory's `feature_count` is not useful for these repos, as the coordinator warned: every Telegram X fork and every library with generated bindings scores 16/16.

### 2.1 Clients

| Repo | Last commit | ★ | Main/test | Binding, UI | LC/GCH/UCP/ULM/UNM/UF/VM/OC/RD/UNG | Verdict |
|---|---|---|---|---|---|---|
| kyra-labs/pingogram-android | 2026-11-02 | 3 | 250/143 | JNI, Compose, Android | 13/12/11/8/24/19/15/80/2/5 | **Deep.** Best architecture in the survey |
| badnng/nngGram | 2026-11-02 | 0 | 88/131 | JNI, CMP, JVM (Windows) | 21/3/1/1/3/4/6/2/0/2 | **Deep.** Actor, global snapshot, clean common/JVM seam |
| JettaXP/pixelGram | 2026-11-02 | 10 | 54/0 | tdlight JNI, Compose Desktop | 6/2/1/1/1/1/1/18/0/0 | **Deep.** Snapshot-state cache, serial queue |
| LyoSU/hortay-android | 2026-07-16 | 81 | 65/9 | JNI, Compose, Android | 33/49/1/15/41/41/40/9/1/0 | **Deep.** Channel feed, not a messenger. Best file cache; cautionary pipeline |
| iamaritrasaha/aether | 2026-09-15 | 0 | 60/29 | JNI, Compose, Android | 2/3/1/1/3/8/7/5/5/8 | **Deep, cautionary.** Hand-written god object with unordered dispatch |
| MG-Devs-Team/Mategram | 2026-04-09 | 100 | 16 plus 54 mirror/0.3 | JNI, Compose, Android | 10/6/4/2/23/25/1/6/0/0 | **Deep, cautionary.** 1:1 mirror, lossy flows |
| vdggrtff/GhostGram (added) | 2026-10-21 | 1 | 14/0 | td_json via JNA, raw JSON strings, CMP | 3/9/1/1/1/5/1/5/0/0 | **Deep, cautionary.** Only chat client with its data layer in `commonMain` |
| dracu-lah/TMPlayer | 2026-11-03 | 170 | 193/56 | tdl-coroutines, Compose, Android+JVM | 12/1/1/0/1/7/1/38/0/0 | **Deep.** Closest stack (JSON library, KMP), but a media player |
| TGWrist/TGWrist | 2026-08-30 | 38 | 32/0 | JNI, Wear Compose | 6/12/4/3/1/10/3/1/7/4 | Cited. Refcounted chat binding, race buffer |
| TGwear/TGwear | 2026-06-06 | 237 | 29/1 | JNI, Wear Compose | 12/2/4/4/7/10/3/1/1/6 | Cited. `MutableState` written from the TDLib thread |
| julfy-0/Yougram | 2026-11-02 | 18 | 49/0 | tdl-coroutines, Compose | 7/3/5/4/1/2/2/14/0/2 | Cited. Per-type collectors, 3.5k-line repository |
| 6xingyv/compound | 2026-07-29 | 36 | 81/0.4 | JNI, Compose | 10/1/0/0/3/3/3/9/9/1 | Cited. Replay 500, `GetChats` plus `GetChat` fan-out |
| cheipstudio-cmyk/novagram | 2026-09-12 | 12 | 35/0 | JNI, Compose | 5/29/4/4/12/33/15/11/8/4 | Cited. `ChatScreen.kt` 9.3k lines, `runBlocking`, `GlobalScope` |
| GazizVR/BirGram | 2026-10-01 | 3 | 10/0.4 | JNI, Compose | 4/4/2/2/2/0/0/2/0/0 | Cited. One dispatcher plus N updaters on the TDLib thread (right shape, weak body) |
| omaeewa/TGram | 2024-09-09 | 17 | 6/0.5 | td-ktx, Compose | 2/2/4/4/2/6/0/0/0/0 | Cited. `merge` of per-type flows |
| paranoiacXD/prismGram | 2026-09-13 | 0 | 7/0 | JNI, Compose | 4/3/1/1/1/3/1/0/0/0 | Cited. `DROP_OLDEST` |
| ilhomsoliev/EasyTelegram | 2023-10-05 | 0 | 9/0.4 | JNI, Compose | 6/2/9/5/4/7/1/7/0/0 | Cited. Paging 3 misuse |
| indritbashkimi/TelegramExample | 2021-05-11 | 48 | 2/0 | JNI, Compose | 0/1/0/0/0/0/0/0/0/0 | Cited. Frozen Paging 3 list |
| void-tech-shiv/Telegram | 2026-10-24 | 0 | 7/0.4 | JNI, Compose | 2/2/2/1/2/1/1/3/1/3 | Skimmed, nothing new |
| u-myds/50gram, logopek/50gramm | 2025 | 0 | 2/0 | JNI, Compose | low | Cited. Per-update `launch`; last commit says "fixed message recomposition(still bad)" |
| tolvana/weargram | 2022-12-12 | 5 | 4/0 | JNI, Wear | 3/1/6/1/1/0/2/1/0/4 | Cited. Normalized ids plus map, broken comparator |
| feduss/TelegramWear | 2024-01-04 | 19 | 4/0 | JNI, Wear | 1/16/1/1/1/1/0/0/0/0 | Discard. Its last commit says history is not updated in real time |
| solomonBoltin/TelegramTv | 2024-03-12 | 15 | 3/0 | JNI, TV | low | Cited. `Thread.sleep` polling for answers |
| saieshshirodkar/Tele | 2026-10-12 | 84 | 3/0 (+70 Java) | JNI, Views | low | Discard. Not Compose |
| bling0390/tellygram | 2026-10-30 | 5 | 13/3 | JNI, TV | 4/0/1/1/1/11/1/5/0/0 | Cited. `DROP_OLDEST` |
| p-vorobyev/telegram-desktop-compose-multiplatform | 2026-01-08 | 11 | 2/0 (+3 Java) | Spring backend, CMP | in the Java backend | Cited. UI polls a local REST server |
| Mahmud0808/TeleDrive | 2026-11-03 | 430 | 48/5 | JNI plus tdlight, CMP | 2/2/0/0/2/11/0/8/0/0 | Cited. Drive app; Coil fetcher in `jvmCommonMain` |
| kernoeb/telegramedia | 2026-07-11 | 4 | 4/0 | JNI, Compose | 8/0/0/0/0/1/0/0/0/0 | Cited. `DROP_OLDEST` |
| nVoxel/TGDriveDemo (added) | 2024-05-23 | 2 | 9/0 | td-ktx fork; iOS via Swift TDLibKit | 14/9/0/0/0/3/0/0/0/0 | Cited. Unfinished; iOS repositories written in Swift |
| Kaleaon/Teleflip | 2026-10-26 | 0 | 21/10 | JNI, no Compose | 4/5/4/4/3/2/1/5/0/0 | Discard. Views; 5.7k-line manager |
| TimurCravtov/Telegraft | 2026-07-30 | 0 | 1/0 | SimpleTelegramClient | none | Discard. A Minecraft plugin |
| ihorvitruk/Telegram-Client | 2018-09-20 | 24 | 1/0 | JNI | none | Discard. One demo repository |
| daino-selvatico/DainoGram | 2022-06-01 | 14 | 0.3/0 (+4.6 Java) | Fragments | none | Discard |
| kcvabeysinghe/EasyTDlib | 2026-04-16 | 12 | 0.6/0 | JNI | low | Discard. Boilerplate |

### 2.2 Libraries and forks

- **g000sha256/tdl-coroutines** (d8c0f01, 2026-10-31): the closest neighbour of tdlib-kmp, with generated DTOs and JSON transport. It offers 197 typed update flows and an `allUpdates` flow, and its README says to start collecting before the first request. `updates` completes on `Closed` (`tdl-coroutines/src/commonMain/kotlin/dev/g000sha256/tdl/TdlRepository.kt:52-95`), unlike tdlib-kmp. The typed flows invite the per-type-collector anti-pattern, and the two consumers I read fell into it (TMP, Yougram; §4.2 A2).
- **tdlibx/td-ktx** (3fe5236): `DROP_OLDEST` with 64 slots by default (`libtd-ktx/src/main/java/kotlinx/telegram/core/ResultHandlerStateFlow.kt:12-22`), then `buffer(64).filterIsInstance` per type (`.../core/TelegramFlow.kt:48-49`). Do not imitate it.
- **whyoleg/ktd** (7284eea, 2021): solved late subscribers by caching every update forever and replaying it to new subscribers (`client/coroutines/src/commonMain/kotlin/dev/whyoleg/ktd/UpdatesChannel.kt:15-37`). No loss, unbounded memory.
- **tdlight-team/tdlight-java** `SimpleTelegramClient`: synchronous fan-out on the receive thread (`tdlight-java/src/main/java/it/tdlight/client/SimpleTelegramClient.java:54,156-165`). Same model as TGX.
- **TGX-Android/tdlib-utils**: pure helpers. `TdSort.kt:34-39` sorts by `(order, id)` descending, which is the rule several clients below miss. Report 03 covers the rest.
- **Forks, by diff against TGX's Kotlin file list:** `moeCrafters/moeGramX` adds 0 `.kt` files. `Capricornus007/moregramX` adds one (`app/src/main/java/ni/shikatu/rex/Whisper.kt`); its only Compose mention is `gradle/libs.versions.toml`, so the inventory's `compose` label is a false positive. `Ivan-k0/plumagram-android` adds 6 and `AmanoTeam/Telegram-X-FOSS` adds 5, none using Compose. `fahadalijaved/XGram` adds 83 files (23.9k lines), 49 with `@Composable` (15.5k lines), but they are settings pages, an "XDrive" page and pickers hosted in TGX `ViewController`s (`app/src/main/java/com/fahadali/xgram/compose/XgramSettingsPages.kt:26-54`, `compose/XgramR5ChatPickers.kt:252-257`). Chat list and history stay Views. The one bridge worth noting is a `MessageListener` writing `mutableStateListOf` (`xdrive/XDriveData.kt:275-276,302,312`). No fork teaches anything about update-to-pixel in Compose.

### 2.3 Candidates that could not be read normally, and one surprise

- **SpaceXC/Neogram** was never cloned (478 MB). Read via GitHub API at 66197fe (2026-03-08): `object TdClient` runs `MainScope().launch { …all handlers… }` per update (`app/src/main/java/cn/spacexc/neogram/data/TdClient.kt:26-43`), which stays ordered only because `Dispatchers.Main` is FIFO [I]. `ChatListRepository.kt:31-32` sorts a combined map per change. Global singletons, nothing to take.
- **codegeasse1/hikari** was never cloned. It is a streaming app with a small Telegram module (`app/src/main/java/com/hikari/app/telegram/Td.kt`); not a chat client [V via API tree].
- **NavoMessenger/Navo** (250★): the README claims open source and describes building, but the clone at fe45da4 holds only docs, screenshots and `workers/theme-bg`, with zero `.kt` files. No source to judge.
- **monogram-android/monogram** (974★, not in the candidate list, read through the API tree at 04ce208): it has no TDLib engine at HEAD. It runs its own Rust MTProto through UniFFI plus Room (`native/mtproto/...`, `core/database/...`); the only TDLib path is `app/src/main/java/org/monogram/TdlibSessionImport.kt`. Do not cite it as a TDLib reference. I did not read it further, because the contract is different.
- **td-kmp/Kotogram and xephosbot/TelegramCompose** are the developer's own earlier work, so they are excluded.

---

## 3. Step 2: deep reads

### 3.1 Pingogram: the strongest design, Android only

**Verdict.** The most disciplined data layer in the survey: immutable reducers, explicit loads, recomposition tests. It breaks `docs/architecture.md` §5 literally, with one collector per cache rather than per account. It is Hilt and Android-module code, but most of the data logic has no Android dependency.

**Entity map**

| Class | Responsibility | Lifetime | Dispatcher | Owner |
|---|---|---|---|---|
| `DefaultTdlibClient` (`T/DefaultTdlibClient.kt:46`) | JNI callback → `inbox` Channel(UNLIMITED) → one pump → `SharedFlow(4096)`; restarts after `Closed` | account | pump on Default | `Accounts` / `AccountComponent` |
| `UpdateStore<S>` (`R/TdlibUpdateCache.kt:36-58`) | subscribes UNDISPATCHED in its constructor; pure `reduce`; resets on `Closed` | account | own collector on `@ApplicationScope` (Default) | each repository |
| `TdlibChatRepository` + `ChatCache` (`R/TdlibChatRepository.kt:72,247`) | `PersistentMap<Long, CachedChat>`, folders, unread counts; `chatList(list)` derived | account | store on Default | account graph |
| `TdlibUserRepository`, group, secret chat, chat action repositories | same pattern | account | Default | account graph |
| `TdlibMessageRepository` + `HistoryCache` + `HistoryWindow` + `HistoryLoads` + `HistoryPages` | windows per `HistoryKey`, LRU 3 unobserved (`R/HistoryCache.kt:24,119-128`); loads in app scope | account; window by collector count and LRU | Default; per-history `Mutex` | account graph |
| `OpenChats` (`R/OpenChats.kt:21-38`) | counts opens; read by notifications | account | any (CAS) | account graph |
| `TdlibFileRepository` (`R/TdlibFileRepository.kt:36-90`) | caches only observed files, with refcount | account | Default | account graph |
| `TdlibFileFetcher`, `TdlibFileImage`, keyer (`IMG/`) | Coil 3 bridge | process | Coil | singleton `ImageLoader` |
| `TdlibNotificationRepository` → `NotificationRenderer` | groups reduced → Android notifications | account | Default | account graph |
| `ChatListViewModel` + `ChatListItemMapper` | one `combine`, memoised rows | screen + 5 s | `flowOn(Default)` | navigation |
| `ChatViewModel` + `ChatWindow` + `ChatItemMapper` + `MessageViews` + `ChatFiles` + ~15 helpers | conversation | screen + 5 s | Main, mapping on Default | navigation |
| `ChatListScreen`/`ChatRow`, `ChatScreen`/`ChatItemRow` | stateless composables | composition | Main | |

```
JNI callback ─trySend→ inbox(UNLIMITED) ─pump(Default)→ updates: SharedFlow(4096)      T/DefaultTdlibClient.kt:135,140-149
      answers: continuation.resume on the JNI thread (a separate path)                 :113-117
                                   │ ≈15 independent collectors, each in order, no order between them
   ┌───────────────┬───────────────┼───────────────────┬───────────────────────┐
UpdateStore     UpdateStore     UpdateStore         UpdateStore             UpdateStore
 ChatCache       UserCache      HistoryCache       ObservedFiles        NotificationCache
   │ map/sort      │              │ window            │ observe(fileId)        │
chatList(list)   users        history(key)         Flow<FileState> ──► TdlibFileFetcher → Coil → AsyncImage
   └──── combine ─┘               │                     ChatFiles.statesOf
 ChatListViewModel.uiState     ChatWindow + ChatViewModel.uiState (20+ combines)  CH/ChatViewModel.kt:1084-1103
   ChatListItemMapper          ChatItemMapper (reuse previous item if inputs equal)
   │ StateFlow<UiState>            │
 LazyColumn(key = id)          LazyColumn(reverseLayout, key = item.key) → ChatItemRow → MessageBubbleRow (skips)
```

**How it solves each concern**

- **Pipeline and order.** The queue guarantees delivery order per store and never drops. `reduce` runs inside `MutableStateFlow.update`, which is why it must be pure (`R/TdlibUpdateCache.kt:33-34,51`). `Closed` is read from the stream, not from the conflated auth flow (`:28-30`). All stores exist before `start()` (`docs/ARCHITECTURE.md`, Repositories). About 15 files construct a store.
- **Caches.** Reducers return the same instance for ignored updates, so identity tells whether anything changed (`R/TdlibUserRepository.kt:47-48,86-87`; `R/TdlibChatRepository.kt:84-87`). Chats and users are kept for the session.
- **Chat list and row.** All three position-carrying updates are applied (`R/TdlibChatRepository.kt:336-351`). `order == 0` removes that one position only (`:445-449`). Sort is `(order, id)` descending (`:465-468`). `loadChats` 404 is `Success(false)` (`:92-102`), with paging from the VM while near the end (`CL/ChatListViewModel.kt:377-396`). Rows are mapped in `ChatListItemMapper`: inputs compared, previous row reused, same list instance returned when nothing changed (`CL/ChatListItemMapper.kt:62-91`).
- **History and live merge.** Window invariant: newest first, consecutive, no gaps (`R/HistoryCache.kt:157-174`). A live message enters only if it fits; otherwise pages bring it later (`:251-264`). Local-first paging (`R/HistoryLoads.kt:176-189`); 50 per page, `offset = -30` around an anchor (`:264,270`); one load per history, shared by key, in app scope (`:46-63`). Updates during a load are also recorded and re-applied after the merge (`R/HistoryCache.kt:163-166,189-204`), and page merges only extend the ends (`:304-316`). Tested at `core/data/src/test/kotlin/dev/kyralabs/pingogram/core/data/repository/TdlibMessageRepositoryTest.kt:590,605,705`. A replaced window carries `windowId`; `ShownWindow` delivers messages and scroll anchor in one state (`CH/ChatWindow.kt:258-270`). Albums are keyed by album id, so a bubble grows as pages arrive (`CH/ChatItemMapper.kt:161,167`).
- **Files.** Only observed files are cached (`R/TdlibFileRepository.kt:51-69,103-121`). `putIfUnknown` stops an older answer overwriting an update (`:128-134`). The fetcher observes until downloaded, then hands Coil the local path; no disk cache; cancelling does not stop TDLib (`IMG/TdlibFileFetcher.kt:31-48`).
- **Notifications.** Enabled at every `Ready` (`R/TdlibNotificationRepository.kt:53-60,101-105`). A reducer tracks groups, `isRestored` and pending state (`:122-191`). The renderer combines this with locks and open chats (`core/notifications/src/main/kotlin/dev/kyralabs/pingogram/core/notifications/NotificationRenderer.kt:289-298`). Push is FCM in the `gms` flavor with `getPushReceiverId` routing (`R/TdlibPushRepository.kt:39-58,143`) and a foreground service in `foss` (`docs/adr/0003-keep-connected-foreground-service.md`). The ADR admits the server side cannot be verified from the repo (`docs/adr/0004-push-delivery.md`).
- **Lifecycle.** `openChat`/`closeChat` follow Started/Stopped and are launched UNDISPATCHED, so a close survives scope cancellation (`CH/ChatViewModel.kt:1259-1278`). Accounts are per-account Dagger components, and a switch reopens the activity in a new task without killing the process (`app/src/main/kotlin/dev/kyralabs/pingogram/account/AccountRestart.kt:30-48`).
- **UI delivery.** One `StateFlow` per screen. Policy: no `collectAsState` inside items (`docs/PERFORMANCE.md` §4). High-frequency state bypasses it: playback position lives in snapshot state, "so it re-emits nothing" (`CH/ChatViewModel.kt:207`).
- **Mapping.** A hand-written domain subset that keeps TDLib's names but is not total (`core/domain/src/main/kotlin/dev/kyralabs/pingogram/core/domain/model/Message.kt:14-47`). The update collector does the mapping.
- **Errors.** `TdResult` → `Outcome`/`DomainError`; a 30 s timeout becomes `Network` (`R/TdlibRequest.kt:12-27`). No automatic retry.
- **Tests.** 819 files. `FakeTdlibClient` (`core/testing/src/main/kotlin/dev/kyralabs/pingogram/core/testing/FakeTdlibClient.kt:27-80`), a full `DemoTdlibClient`, recomposition counters (`feature/chats/src/test/kotlin/dev/kyralabs/pingogram/feature/chats/ui/RecompositionCounter.kt:25-67`, `ChatListRecompositionTest.kt`) and scroll cost tests. I found no test that stresses interleavings between stores [U on completeness].

**What breaks**

| Rule | Code | Consequence [I] |
|---|---|---|
| CS§1.6, UNI#3 | `R/HistoryCache.kt:389-402` has no `UpdateChatLastMessage`; no hit for last-message handling in `CH/ChatWindow.kt` or `CH/ChatViewModel.kt` | When the last message becomes unknown, TDLib may add messages with no `updateNewMessage` (TL:10867). `reachedEnd` stays true, so an open chat silently stops showing new messages until reopened |
| TGX#1, CS§1.1, `architecture.md` §5 | ≈15 collectors; comments admit overtaking (`R/TdlibChatRepository.kt:109`, `R/TdlibUserRepository.kt:55`); rows read `users::get` (`CL/ChatListItemMapper.kt:110-125`) | Transient tearing: a row shows a chat before its sender exists. Stores reset on `Closed` at different moments. It converges on the next emission, so it is flicker rather than corruption |
| UNI#12, TGX#13 | `HistoryPages.newest` hardcodes `reachedEnd = true` on a local page (`R/HistoryLoads.kt:108-113`), while `newer` and `around` compare with the chat's last id (`:134-149,156-170`) | If TDLib's local newest lags the real last message, a hole opens and `loadNewer` refuses to fill it (`R/TdlibMessageRepository.kt:108-111`). Telegram X compares ids here (report 03, C4) |
| TGX#17, UNI#6 | Key is the id (`CH/ChatItemMapper.kt:161`) with `animateItem` (`CH/ChatScreen.kt:639-650`) | At send success the bubble's key changes: remove plus insert animate together, the "double message" Aether describes (`A/data/telegram/TelegramClient.kt:2793-2801`) |
| UNI#5 | Window ordered by id only (`R/HistoryCache.kt:483-486,502`) | A pending bubble can jump when its final id arrives. Cosmetic |
| TGX#22 | Each consumer sends its own `DownloadFile` (`R/TdlibFileRepository.kt:78`); auto-download uses `Prefetch` (`CH/ChatAutoDownloads.kt:54`) while the fetcher uses `Visible` | The later request's parameters win, so a prefetch can downgrade a file on screen. A fling also keeps every started download running (`IMG/TdlibFileFetcher.kt:31-33`) |
| Multi-account, CS§1.8 | `@Singleton ImageLoader` captures `accounts.active.fileRepository()` once (`app/src/main/kotlin/dev/kyralabs/pingogram/di/ImageLoaderModule.kt:33-43`, `core/data/src/main/kotlin/dev/kyralabs/pingogram/core/data/di/ActiveAccountModule.kt:116-117`); key is the numeric file id (`IMG/TdlibFileImage.kt:28-37`); a switch does not end the process | After switching accounts, images are fetched from the old account's TDLib and numeric ids can collide in memory cache. Nothing resets the loader; the only singleton-loader code I found is `PingogramApplication.kt:171` |
| Cost | `flatMapLatest` over the id set, then `combine` over one flow per file (`CH/ChatFiles.kt:46-60`) | Any change in the id set cancels all observers; each release removes the entry and each re-observe sends `GetFile` (`R/TdlibFileRepository.kt:55-69`). Every `updateFile` wakes N collectors |

**Passes where others fail:** `from_cache` vs. permanent vs. inaccessible (`R/HistoryCache.kt:293-298,455-459`), draft echoes (`CH/composer/ChatComposer.kt:37-44`), chat actions without timers (`R/TdlibChatActionRepository.kt:24-33`; verified in §5).

### 3.2 nngGram: actor plus one global snapshot, on a clean seam

**Verdict.** The cleanest KMP boundary among chat clients. Models and UI are in `commonMain`, TDLib is in `jvmMain`. Ordering is right. Rendering cost is the weak point. Windows-only desktop.

| Class | Responsibility | Lifetime | Dispatcher | Owner |
|---|---|---|---|---|
| `TdTransport`, `OfficialTdTransport` (`J/TdTransport.kt:48-120`) | creates the client; fan-out to mappers on the JNI thread | instance | TDLib thread | `TdEngine` |
| `TdChatUpdateMapper` (`J/TdChatSession.kt:462`) | TDLib update → common `ChatEvent`; holds permission and mute bits | instance | TDLib thread | transport |
| `TdEngine` (`J/TdEngine.kt:151-173`) | one actor over `Channel(UNLIMITED)`; owns client, config, journal | app | IO, serial actor | app |
| `ChatController` (`J/ChatController.kt:11-25`) | reducer with 130+ mutable fields; "all entry points and completions run on TdEngine's actor" | generation | actor | engine |
| `ChatEvent`, `ChatItem`, `ChatMessage`, `ChatSnapshot` (`C/chat/ChatModels.kt:115-168`, `C/chat/ChatSnapshot.kt:6-112`) | common models; one immutable screen state with 100+ fields | snapshot | any | |
| `ChatConversationPane`, `RemoteMedia` (`C/ChatConversationPane.kt:709-713`, `C/ChatMediaUi.kt:57-78`) | composables reading the snapshot | composition | Main | |

```
JNI thread: Client.create { mapper.accept(update) } → ChatEvent → postAction(Action.ChatUpdate(generation, event))   J/TdTransport.kt:80-100, J/TdEngine.kt:2514
answers:    client.send(request, { result(it) }) → enqueue onto the same actor                                        J/ChatController.kt:11,15
actor:      chatController.event(event) → mutate 130 fields → emit() → sort everything → publish(ChatSnapshot)        J/ChatController.kt:316,3611-3665
UI:         one ChatSnapshot → items(snapshot.messages.asReversed(), key = { it.presentationId }) → RemoteMedia(media, snapshot, …)
```

- **Order.** Updates and answers reach one actor, so TDLib's relative order survives. Stale work is dropped by `ChatToken` and generation (`J/TdEngine.kt:502-504`, `J/ChatController.kt:25`). This also gives TGX#15 for free.
- **Caches and row.** Plain mutable maps confined to the actor, projected per `emit()`. List sort is correct, `(position, id)` descending (`J/ChatController.kt:3634-3635`).
- **History.** Only the selected chat is held. Live messages are refused while the window is detached: "temporary send ids and late updates are not evidence that the intervening range is loaded" (`J/ChatController.kt:3288-3295`). This is the clearest statement of UNI#4 and TGX#20 in Kotlin.
- **Stable key.** `presentationId` survives send success (`C/chat/ChatModels.kt:54`, `J/ChatController.kt:3232-3237`).
- **Deletes.** `from_cache` filtered, `permanent` kept as a separate meaning (`J/TdChatSession.kt:606`, `J/ChatController.kt:493-512`).
- **Files.** Own pipeline. A `mediaFiles` map in the snapshot, read in each composable (`C/ChatMediaUi.kt:74-75`).
- **Notifications.** TDLib engine enabled (`J/TdDeviceNotifications.kt:57-59`), groups mapped to Windows toasts (`:200-201`). No push.
- **Mapping.** Own domain subset. TDLib types never reach `commonMain`.
- **Errors.** `ChatError`; "native error text is never exposed" (`J/TdTransport.kt:20`).
- **Tests.** 526 files, 131k lines; `FakeChatSession` (`shared/src/jvmTest/kotlin/com/badnng/moe/tdlib/ChatControllerTest.kt:2108`).

| Rule | Code | Consequence [I] |
|---|---|---|
| Update storm cost | `event()` ends in `emit()` (`J/ChatController.kt:316,618`); `emit` re-sorts all chats twice and all messages and copies maps (`:3633-3665`); no coalescing (grep for coalesce, debounce, throttle finds none) | O(n log n) per event on the one actor. At cold start this delays everything the actor owns |
| Compose stability | `RemoteMedia(media, snapshot, …)` takes the whole snapshot (`C/ChatMediaUi.kt:57-59`); `ChatSnapshot` referenced in 52 common files | Every publish, including each file progress event, recomposes all visible media |
| CS§1.6, UNI#3 | `LastMessage` event maps to a null message (`J/TdChatSession.kt:529-530`) with no history invalidation | Same silent stall as PG |
| CS§1.10 | No `"online"` option in `jvmMain` (grep) | The account looks offline while the app is in use; PG reports TDLib backs off reconnects when offline (`docs/PERFORMANCE.md` §3) |
| Cloud drafts | `UpdateChatDraftMessage` emits positions only (`J/TdChatSession.kt:527`) | A draft typed on another device is not shown |

### 3.3 pixelGram: Compose state as the cache

**Verdict.** The simplest correct delivery path in the survey, and the only one where a row subscribes per field with no flows. It would need rethinking under Kotogram's rules: the cache is Compose runtime state, mutated on the main thread. Compose Desktop on tdlight, no tests.

| Class | Responsibility | Lifetime | Dispatcher | Owner |
|---|---|---|---|---|
| `Telegram` (`P/telegram/Telegram.kt:248-253`, 3.4k lines) | client, cache, requests, `handleUpdate` | account | Main | app |
| `inbox` + `drain()` (`:417-438`) | one FIFO for updates and answers; drains at most 8 ms per dispatch (`:228`) | account | Main | `Telegram` |
| `ChatState` (`:97-125`) | one `mutableStateOf` per field | session | Main | `chats: mutableStateMapOf` (`:262`) |
| `KeyedState` (`P/telegram/KeyedState.kt:9-42`) | one `MutableState` per key, so reading user A does not subscribe to user B | session | Main | `users` (`:263`) |
| `chatList(key)` (`:1376-1396`) | memoised sort; invalidated at most every 100 ms (`:1398-1405`, `:214`) | session | Main | |
| `messages: Map<Long, SnapshotStateList<UiMessage>>` (`:301`) | history per opened chat; `detached` flag | session (cleared only on logout, `:1268`) | Main | |
| `ChatRow`, `ChatView` (`P/ui/main/ChatList.kt:267-279,420-428`; `P/ui/main/ChatView.kt:227-228,746`) | read fields directly | composition | Main | |

```
tdlight callback: ResultHandler { update -> onMain { handleUpdate(update) } }            :337
answers:          ResultHandler { result -> onMain { onResult(result) } }                :447
drain() on Main, ≤ 8 ms → handleUpdate → chat.title = … / users[id] = … / list.add(0, …)   :429-438, 1013+
Compose snapshot system → only composables that read that field recompose
chatListsChanged() → (100 ms) listCache.clear(); listsVersion++ → LazyColumn re-reads      :1398-1405
```

- **Order.** One queue, in TDLib's order, for updates **and** answers. This is the strongest ordering of any Compose client, shared only with NNG.
- **Consistency.** All state is written on Main, so a composition sees a consistent world without any combine [I].
- **Chat list.** `updateChatLastMessage` and `updateChatDraftMessage` apply positions (`:1019-1032`); 404 ends paging (`Telegram.kt` `loadChats`, line ~1410).
- **History.** Respects the gap rule: `detached[chatId]` blocks live inserts (`:1129`) and `loadNewer` re-attaches (`:2316-2337`). Key survives send success (`:153,1158`).
- **Files.** A `filePaths` map and `transfers` progress map, one `DownloadFile` per file at priority 1 (`:1101-1115,3185-3203`). Images via own `ImageCache` and `produceState` (`P/ui/components/Avatar.kt:47,79-106,181-183`). No Coil.
- **Notifications.** Built from `updateNewMessage` (`:1137-1139`, `P/telegram/Notifications.kt:8-9`), with the TDLib notification engine off.
- **Mapping.** `TdApi` in UI, plus a partial `UiMessage` (`:145-179`).

| Rule | Code | Consequence [I] |
|---|---|---|
| CS§1.3 | `sortByDescending { it.second }` with no id tie-break (`:1391`) | Two chats with equal `order` can swap |
| CS§1.8 / TL:11061 | `if (update.isPermanent)` is the only delete path (`:1214-1216`) | Messages that became inaccessible (`is_permanent=false`, `from_cache=false`) stay visible |
| CS§1.6, UNI#3 | No history reaction to a null last message | Silent stall |
| CS§1.9, TGX (report 03 flow c) | Notifications from `updateNewMessage` | Muted-scope, mention and group logic must be re-derived; no restore across launches |
| CS§1.10 | No `setNetworkType` in Kotlin main (my scan) | Slow recovery after a network change |
| Cost | O(n) `indexOfFirst` and `none` on Main per message update (`:1130,1157,1174`); `SnapshotStateList` per chat never evicted (`:301,1268`) | Main-thread cost grows with history; memory grows during a session |
| Backlog | 8 ms budget means answers queue behind a cold-start burst (`:429-438`) | A tap's result can lag during the burst. Correct but slow |

### 3.4 Hortay: the best file cache, on a cautionary pipeline

**Verdict.** The most TDLib-literate comments in the survey, with real issue citations, several of which I verified (§5). A merged channel feed, so no chat list or history in the messenger sense. Take `MediaCache`. Do not take `PostsRepository`.

| Class | Responsibility | Lifetime | Dispatcher | Owner |
|---|---|---|---|---|
| `TdClient : TdSender` (`H/data/TdClient.kt:34`, `H/data/TdSender.kt:12-15`) | JNI → `incoming` Channel(UNLIMITED) → `SharedFlow(64)`, suspending `emit` (`:127-128,148-150`); global flood-wait deadline (`:178`) | process | Default | `AppGraph` |
| `PostsRepository` (`H/data/posts/PostsRepository.kt:119`, 3.1k lines) | one feed `PersistentList`; 14 per-type collectors (`:616-771`) | process | Default (`H/AppGraph.kt:90`) | `AppGraph` |
| `MediaCache` (`H/data/MediaCache.kt:85`) | single-writer reducer over `fileEvents`; per-file `MutableStateFlow`; stall watchdog; debounced cancel | process | IO reducer | `AppGraph`, via `LocalMediaCache` |
| `ChatPresence` (`H/data/ChatPresence.kt:69`) | refcounted open/close; picks `force_read` | process (object) | any, Mutex | |
| `TimelineViewModel` (`H/ui/timeline/TimelineViewModel.kt:55`) | `combine` feed + seen marks; `distinctUntilChanged` | screen | Default | navigation |
| `rememberMediaBinding` (`H/ui/media/MediaBinding.kt:54-66`) | per-composable file subscription, ensure, cancel on dispose | composition | Main | |

```
JNI → incoming → _updates(64, emit)
   ├─ collector UpdateNewMessage ──┐
   ├─ collector UpdateDeleteMessages├─ 14 launchIn(appScope, Default) → _posts.update { pure }
   ├─ collector UpdateNewChat ─────┤     pendingLastMessages (race buffer)   PostsRepository.kt:267,723-729
   └─ …                            ┘
   └─ UpdateFile collector → fileEvents(UNLIMITED) → reducer → states[fileId].value  MediaCache.kt:198-235
TimelineViewModel.posts → LazyColumn(key = item.key)
item: remember(fileId){ cache.observe(fileId) }.collectAsStateWithLifecycle()  MediaBinding.kt:60-62
```

- **Files.** `reduce` is the only writer (`H/data/MediaCache.kt:140-158,352`). A priority ladder of Foreground 32, VisibleCenter 24, VisibleMedia 16, Prefetch 8, Avatar 2 (`:34-53`). Upgrades re-issue `DownloadFile` (`:584-602`). Progress throttled to 100 ms (`:383-390,991`). Stall watchdog (`:252-267,493`), resting on levlam's position in tdlib/td#2585 (§5). Post-completion resync for `completed=true, path=""` (`:127-137,414`) [U whether TDLib really does this; the comment reports an observation]. Cancel only after a debounce and only when `subscriptionCount == 0` (`:880-919`), so the StateFlow's subscriber count acts as the refcount. Slot eviction uses `compute` to avoid orphaning a late observer (`:282-315`).
- **Compose.** Per-item `derivedStateOf { centerKey.value == item.key }`, so a centre change recomposes only two items (`H/ui/timeline/TimelineFeedColumn.kt:142-153`, `H/ui/timeline/TimelineScreen.kt:1688-1710`). A stability conf and `@Immutable` chain are declared (`H/data/TimelinePost.kt:14-21`).
- **Coalescing.** Interaction updates buffer per message and flush in one `mutate` every 200 ms (`H/data/posts/PostsRepository.kt:339-358`).
- **Albums.** Silence-based debounce plus repair (`:289-318`). levlam confirms there is no end signal: "You need to use some timeout" (tdlib/td#1482).
- **Presence.** Dominant-visible post's chat is the one open chat (`H/data/ChatPresence.kt:35-46`).
- **Notifications.** None by design: `notification_group_count_max = 0` (`H/data/TdClient.kt:400-413`).
- **Tests.** 62 files, including `FakeTdSender` counting RPCs (`app/src/test/kotlin/dev/lyo/hortay/data/FakeTdSender.kt:17-25`).

| Rule | Code | Consequence [I] |
|---|---|---|
| CS§1.1, CS§1.2, TGX#1 | 14 `launchIn(appScope)` on Default (`H/data/posts/PostsRepository.kt:618-771`, `H/AppGraph.kt:90`) | Order across types is lost. A delete can run before its insert, leaving a ghost post |
| Same, self-inflicted | Race buffer "`UpdateChatLastMessage` that arrives before matching `UpdateNewChat`" (`:96-99,267,723-729`) | TDLib guarantees that order (TL `updateNewChat`, CS§1.2). The buffer exists only because of the row above |
| Backpressure | `_updates.emit` with 64 slots (`H/data/TdClient.kt:128,149`) | The slowest collector stalls all others once the buffer fills |
| CS§1.6 | `update.lastMessage ?: return@onEach` (`:725`) | Null is ignored. Tolerable for a feed, not a template |
| TL:11061 | `if (!update.isPermanent) return` (`:2367`) | Inaccessible posts stay in the feed |
| §5 fact | "TDLib's open-chat state … is a boolean, not a counter" (`H/data/ChatPresence.kt:25-29`) | Wrong (MM:17104-17108). Harmless, since the client refcount is merely redundant |
| Cost | Structural `distinctUntilChanged` on up to 1000-item lists per update (`H/ui/timeline/TimelineViewModel.kt:84-101`) | O(n) equality per emission; `ByteArray` in the model (`H/data/TimelinePost.kt:32`) compares by reference |

### 3.5 Aether: a cautionary god object, with good pieces around it

**Verdict.** 60k main and 29k test lines, hand-written (no generated-file pattern in the scan), all API families present, Android only. The core dispatch is the worst in the serious group. Its best parts are `TelegramFileManager`, `presentationKey` and `NotificationWorkQueue`.

| Class | Responsibility | Lifetime | Dispatcher | Owner |
|---|---|---|---|---|
| `TelegramClient` (`A/data/telegram/TelegramClient.kt:62`, 4k lines) | client, all caches as `ConcurrentHashMap` (`:69-80`), `handleUpdate` (`:2575`), conversation flows (`:2544`) | process | one coroutine **per update** on Default (`:64,289`) | app |
| `TelegramFileManager` (`A/data/telegram/TelegramFileManager.kt:20`) | file truth, per-file StateFlow, version guard | account | any | client |
| `ConversationHistoryManager` (`A/data/telegram/ConversationHistoryManager.kt:12`) | one chat's paging state machine | chat | Mutex | VM |
| `NotificationWorkQueue` (`A/data/push/NotificationWorkQueue.kt`) | serial queue with `awaitDrained` for push | process | chain of jobs | client |
| `ChatsViewModel`, `ConversationViewModel` (`A/ui/home/ChatsViewModel.kt:19`, `A/ui/conversation/ConversationViewModel.kt:66`) | `stateIn` over client flows | screen | Main | navigation |

```
JNI → dispatchUpdate:  notification updates → notificationWork.submit { handleUpdate }   TelegramClient.kt:283-287
                       everything else     → scope.launch { handleUpdate(update) }       :289   (Default, no order)
handleUpdate → mutate shared TdApi.Chat in place → publishChats() (40 ms) → re-map every chat → _chatList  :2724-2738, :3522-3539
             → upsertConversation(chatId) → conversationFlows[chatId]                     :2434-2470, :2544
ChatsViewModel → items(visibleChats, key = { it.id })                                     ui/home/HomeScreen.kt:279
```

- **Stable key.** A pending message gets `presentationKey = "pending_<id>"`, carried across the id swap (`:2793-2808,2472-2482`, `A/domain/messages/MessageGrouping.kt:26`, `A/domain/model/Models.kt:274`). Its comment describes the bug it prevents: `animateItem` renders exiting and entering bubbles at once.
- **File version guard.** `update()` bumps a version, and a request's answer is applied only if no update arrived meanwhile (`A/data/telegram/TelegramFileManager.kt:70-85,87-103`).
- **Notification queue.** Its doc records the discovery of the concurrency bug: on a multi-threaded dispatcher two coroutines touched one group's map, giving `ConcurrentModificationException` (`A/data/push/NotificationWorkQueue.kt`, header). They fixed that one path and left the rest unordered.
- **Tests.** 170 files, but `handleUpdate` is called directly and sequentially ("exercised directly with constructed TdApi updates", `:2574`), so they cannot observe the dispatch race.

| Rule | Code | Consequence [I] |
|---|---|---|
| CS§1.1, TGX#1 | `scope.launch { handleUpdate(update) }` on Default (`:64,289`) | No order at all. An older position or preview can overwrite a newer one. A delete can run before its insert. Send-succeeded can run before the pending insert |
| Thread safety | In-place mutation of shared DTOs from concurrent coroutines (`:2732-2735,2790,3438`) | Lost writes |
| CS§1.6 | `chats[msg.chatId]?.lastMessage = msg` on `updateNewMessage` (`:2790`) | The row is driven by the wrong stream |
| CS§1.3 | `compareUnsigned` with no id tie-break (`A/data/telegram/ChatOrdering.kt:46-48`) | Equal orders can swap |
| UNI#7, TGX#19 | Deletes ignore `fromCache`, with a comment claiming gating hid real deletes (`:2897-2919`) | `from_cache` comes only from the unload path (§5). Retained windows of closed chats lose rows, and holes appear on reopen |
| UNI#4, TGX#20 | `upsertConversation` always merges live messages (`:2809`), and a jump window is merged into the same list (`A/ui/conversation/ConversationViewModel.kt:1487-1491`) | Holes with no marker: scrolling down from a jump passes silently over missing messages |
| Non-monotonic merge | A page overwrites current entries (`:2456-2461`) | A stale page can undo a newer update |
| Unbounded | `conversationFlows` and `rawMessages` have no removal (grep), and `upsertConversation` runs for every chat | One flow and message map per chat ever touched |
| List rebuild | `doPublishChats` re-maps all chats with no row reuse (`:3534-3539`) | Every visible row gets a new instance, so skipping fails unless `equals` passes |

### 3.6 Mategram: the mirror experiment, and lossy flows

**Verdict.** 100 stars, a clean module layout, and the most relevant cautionary data point for the "1:1 domain" rule. `domain/` has 2,972 files mirroring TDLib, including functions such as `ViewMessages` and `RegisterDevice` that nothing uses (grep) (`mategram/domain/src/main/java/com/xxcactussell/domain/`). `data/utils/mappers/` has 468 files. Together they are 54k of 70k main lines. Almost no tests (8 files, 297 lines).

| Class | Responsibility | Lifetime | Dispatcher | Owner |
|---|---|---|---|---|
| `TdClientManagerImpl` (`MD/impl/TdClientManagerImpl.kt:30`) | JNI → five `SharedFlow`s split by type, each `replay=25, buffer=256, DROP_OLDEST` (`:41-54`) | process | TDLib thread `tryEmit` | Hilt |
| `ChatsRepositoryImpl` (`MD/impl/ChatsRepositoryImpl.kt:41`) | `ConcurrentHashMap<Long, Chat>`, 200 ms sampled publish (`:81-89`) | process | IO + Default | Hilt |
| `MessagesRepositoryImpl` (`MD/impl/MessagesRepositoryImpl.kt:58`) | `chatProcessors` per chat (`:61,138-142`) | process | IO + Default | Hilt |
| `ChatHistoryProcessor` (`MD/utils/ChatHistoryProcessor.kt:30`) | window as `StateFlow<List<Item>>`, senders cache, album delay | until explicit clear | `scope.launch` per message | repository |
| `FileRepositoryImpl` (`MD/impl/FileRepositoryImpl.kt:28`) | one `Map<Int, File>` (`:32,49-51`) | process | IO | Hilt |
| `RootViewModel` (`MP/root/viewmodel/RootViewModel.kt:33-35`) | exposes the global file map to every composable | activity | Main | `LocalRootViewModel` |
| `ChatsViewModel` (`MP/chats/viewmodel/ChatsViewModel.kt:49-54`) | maps and sorts all chats into all folders | screen | Default | navigation |

```
JNI ─ tryEmit → chatsUpdatesFlow / newMessagesFlow / filesUpdatesFlow / … (replay 25, DROP_OLDEST)  TdClientManagerImpl.kt:94-109
chats   → merge → buffer(UNLIMITED) → withContext(Default) → chatsCache → refreshTrigger.sample(200) → List → ChatsViewModel sort per folder
messages→ getProcessor(chatId) (created for ANY chat) → scope.launch { GetMessage(reply) … } → _items.value = …
files   → currentMap + (id to file) → every ChatAvatar/PhotoMessage: rootViewModel.files.collectAsState()  ChatAvatar.kt:47,68
```

| Rule | Code | Consequence [I] |
|---|---|---|
| CS§1.1, §1.2 | `DROP_OLDEST` with 256 slots, `tryEmit` (`MD/impl/TdClientManagerImpl.kt:41-54,98-105`) | A cold-start burst silently loses `updateNewChat` and position updates. `replay = 25` re-applies old updates to late collectors |
| CS§1.1 | Five separate flows (`:94-109`) | Order across chats, users, messages is gone |
| CS§1.3 | `updateChatLastMessage` keeps no positions (`MD/impl/ChatsRepositoryImpl.kt:119-124`); `updateChatDraftMessage` is not routed (`MD/impl/TdClientManagerImpl.kt:98-105`, grep) | TL:10871: positions may arrive only there. A new message may not move the chat up |
| CS§1.2 | Unknown chat → `fetchChat` (`:110-112,122,128`), answer overwrites the cache (`:222-231`) | A stale answer undoes newer updates |
| CS§1.3 | Sort by `isPinned` then order, no id (`MP/chats/viewmodel/ChatsViewModel.kt:96-101`) | `is_pinned` is already folded into `order` (CS§1.3). No tie-break |
| CS§1.3 | `loadInitialChats` uses `folderId = 0` (`:202-205`), mapped to `ChatListFolder(0)` (`MD/impl/ChatsRepositoryImpl.kt:233-240`), result ignored; no paging to 404 | First load targets a folder that does not exist. The main list loads only when a tab selects it, at most one batch of 100 per selection |
| UNI#4, TGX#20 | `processNewMessage` never checks `isAtLiveHead` (`MD/utils/ChatHistoryProcessor.kt:57-84`); `loadHistoryAround` merges without replacing and has offset 0 (`:243-260`) | Holes in history; jump does not centre (TGX#14) |
| UNI#7 | `processDeleteMessages(update.messageIds)` ignores flags (`MD/impl/MessagesRepositoryImpl.kt:118-120`) | Unloaded messages vanish |
| Order, TL:3429 | `sortedByDescending { date }` (`MD/utils/ChatHistoryProcessor.kt:348`), with new messages appended after old in the input (`:321`) | Messages in the same second swap |
| Concurrency | `val current = _items.value … _items.value = updated` from many coroutines (`:87-110,174-194,326-328`) | Lost updates |
| Unbounded | `getProcessor` for any chat with any message update (`MD/impl/MessagesRepositoryImpl.kt:95-96,138-142`) | One processor per chat ever heard from, plus `GetMessage` per reply and sender lookups, for closed chats |
| N+1 | `getChatHistory` enriches each message with `GetMessage`, `getUser` (`:273-300`) | Requests per page grow with the page; pressure on the flood limit |
| Compose | Global file map in each row; `currentMap + (…)` is O(n) per `updateFile` (`MD/impl/FileRepositoryImpl.kt:49-51`, `MP/chats/screen/ChatAvatar.kt:47,68`, `MP/messages/screen/PhotoMessage.kt:68`) | Any file's progress recomposes every avatar and photo |
| Decode | `BitmapFactory.decodeFile` in a per-item `LaunchedEffect` with no cache (`MP/chats/screen/ChatAvatar.kt:71-82`) | Re-decoded on every list re-entry; Android-only |

Its one idea worth keeping: `getMessagesWithAlbumBoundary` drops a partial album at the page edge so it loads whole next time (`MD/impl/MessagesRepositoryImpl.kt:225-242`).

### 3.7 GhostGram: what `commonMain` looks like when nobody checks the contract

**Verdict.** The only chat client with repositories, handlers and UI in `commonMain`, with Android and JVM targets. It proves the module shape compiles, not much more. It uses raw JSON strings and hardcodes another client's API credentials (`GT/commonMain/kotlin/com/ghostgram/core/tdlib/TelegramFlowClient.kt:81-82`). iOS is a stub.

```
JNA td_json_client_receive loop on Default → _updates: SharedFlow<String>(replay=50, buffer=100) (.emit)  TelegramFlowClient.kt:22-25,42-77
ChatRepositoryImpl: ONE collector → parse JSON → for (handler in handlers) if (handler.handle(type, json)) break   GD/ChatRepositoryImpl.kt:78-125
   ChatUpdateHandler → _chatsMap.update { current + … } → observeChats: map { filter; sortedByDescending(order) } :127-136
   MessageUpdateHandler → repoScope.launch { messageDao.insert… } → Room KMP → observeMessages
ViewModels: repo.observe….collect { state } (no flowOn) → LazyColumn(items(state.chats, key = { it.id }))
```

- **The good.** One collector and a fixed handler list: the right order inside the handlers. Answers arrive in the same stream, so `"messages"` and `"chat"` are handled in order (`GD/handlers/MessageUpdateHandler.kt:80`, `GD/handlers/ChatUpdateHandler.kt:32`).
- **Lifecycle.** A session object per account (`shared/src/commonMain/kotlin/com/ghostgram/app/presentation/chats/ChatListViewModel.kt:29,45`).

| Rule | Code | Consequence [I] |
|---|---|---|
| iOS | `GT/iosMain/kotlin/com/ghostgram/core/tdlib/TelegramNativeClient.kt:3-15` prints and returns null; README: iOS not presented as ready | No iOS evidence at all |
| CS§1.1 | `jsonResponse.contains("authorizationStateWaitTdlibParameters")` on the raw event (`TelegramFlowClient.kt:65`) | Any message containing that text re-sends parameters |
| CS§1.3 | `updateChatLastMessage` ignores positions (`GD/handlers/ChatUpdateHandler.kt:133-153`); drafts not handled; main list only (`:121-122`); `sortedByDescending` without id (`GD/ChatRepositoryImpl.kt:133-134`); one `loadChats(30)` with no paging to 404 (`:129`) | Stale order; incomplete list |
| "TDLib is the source of truth" | History lives in own Room table (`GD/handlers/MessageUpdateHandler.kt:72-76,125-134`) | Two sources of truth; each write is its own `launch`, so order at the DB is lost (`:73,163-171,181-185`) |
| UNI#7, TL:11062 | `updateDeleteMessages` always `markAsDeleted` (`:175-187`) | Unloaded messages get flagged as deleted |
| Replay | `replay = 50` (`TelegramFlowClient.kt:23`) | A late collector re-applies 50 old events |
| Deprecated API | `td_json_client_create` (`GT/jvmMain/kotlin/com/ghostgram/core/tdlib/TelegramNativeClient.kt:12-15`) and `checkDatabaseEncryptionKey` (`TelegramFlowClient.kt:70-71`) | Single-client API; the second call no longer exists in the schema [I] |

### 3.8 TMPlayer: the closest stack, not a messenger

**Verdict.** Compose, KMP and a JSON library (tdl-coroutines), 193k main and 56k test lines. Android and JVM only. It has no entity cache by design, and that makes it instructive on what is lost without one. Its streaming code is the best in Kotlin.

| Class | Responsibility | Lifetime | Dispatcher | Owner |
|---|---|---|---|---|
| `Td` (`D/data/Td.kt:75-87`) | process-wide client loop, replaces the client after close; per-type collectors (`:439-501`) | process (object) | IO | global |
| `TdSession` (`D/data/Td.kt:1704`) | client + generation | instance | | `Td` |
| `ChatRepository`, `ChatListLoad` (`D/data/ChatRepository.kt:26`, `D/data/ChatListLoad.kt:90,180,242`) | stateless requests | call | IO | VM |
| `BrowseViewModel` (`U/ui/browse/BrowseViewModel.kt:177-246`) | holds `ChatSummary` rows; one collector per update type | screen | | navigation |
| `Thumbnails` (`U/data/Thumbnails.kt:48,119,259-263`) | own LRU, limited-parallelism decode, download | process | Default limited | global |
| `TdByteWindow`, `StreamWarmup` (`D/player/TdByteWindow.kt:203-217`) | byte window over a downloading file | stream | IO | player |

```
tdl-coroutines allUpdates → typed flows
  Td.clientLoop: launch { authorizationStateUpdates.collect } … 8 collectors        D/data/Td.kt:438-501
  BrowseViewModel: launch { chatTitleUpdates.collect { edit(id){…} } } … 5 collectors  U/ui/browse/BrowseViewModel.kt:177-246
  chatPositionUpdates.conflate → delay → reorder: getChats + fetchNewChats             :196-207,275
ChatSummary list → UiState → LazyColumn
fileUpdates.filter { id } per consumer                                                  U/data/Thumbnails.kt:287, D/player/TdByteWindow.kt:213
```

- **KMP layout.** `core/commonMain` has 103 files plus a `pureMain`. `ui` shares code by compiling `src/shared/kotlin` into both targets, because a Compose Multiplatform dependency in `commonMain` would lift the Android app onto CMP's versions (`ui/build.gradle.kts:4-16`). A desktop `jvmMain` player exists alongside. It is a working workaround for version conflicts, and Kotogram does not need it since it is CMP everywhere [I].
- **Download discipline.** Subscribe UNDISPATCHED before asking, check the answer first, withdraw on cancellation (`U/data/Thumbnails.kt:265-322`). `limitedParallelism` caps decodes at 3, or 2 on TV (`:259-263,355-356`).
- **Tests.** 397 files.

| Rule | Code | Consequence [I] |
|---|---|---|
| CS§1.2 | Chat rows from `getChat`, order from `getChats` (`D/data/ChatRepository.kt:73-77,117-125`); TL:11976 says "for informational purposes only" | Round trips where a cache needs none; every sync is a re-read |
| CS§1.3 | Only `chatPositionUpdates` are collected, with the comment "Every message is a position update" (`U/ui/browse/BrowseViewModel.kt:192-199`); no `chatLastMessageUpdates` in the repo (grep) | TL:10871 says the position may arrive inside `updateChatLastMessage` instead. A new message may not reorder the list |
| CS§1.1 | A collector per type, and only while the VM lives | Updates missed while the VM is gone must be repaired by re-reading |
| Mute | `!useDefaultMuteFor && muteFor > 0` (`D/data/ChatRepository.kt:103-105`) | Scope defaults ignored; PG resolves them correctly (`R/TdlibChatRepository.kt:285-295`) |
| Singleton | `object Td` | One account at a time in this object, though `D/data/AccountSessions.kt` exists [U on how it switches] |

---

## 4. Step 3: cross-client synthesis

### 4.1 Concerns versus clients

Citations for these cells are in §3.

| Concern | PG | NNG | PXG | HOR | AET | MAT | GHO | TMP |
|---|---|---|---|---|---|---|---|---|
| Receive → app | Channel → SharedFlow(4096), suspends, no loss | JNI → mapper → actor Channel | JNI → FIFO to Main | Channel → SharedFlow(64), suspends | `launch` per update | `tryEmit`, DROP_OLDEST ×5 | SharedFlow<String> replay 50 | tdl-coroutines unbounded |
| Collectors | one per cache (≈15) | one actor | one queue | 14 per type + files | none, per-update coroutine | ≥3 merged flows | one + handlers | per type, per VM |
| Order across entities | no | yes, with answers | yes, with answers | no (race buffer) | no | no | yes in handlers, not in DB | no |
| Entity cache | `PersistentMap` in `StateFlow` per store | actor-confined maps | `ChatState`, `KeyedState` (snapshot state) | `chatCache` only | `ConcurrentHashMap` of DTOs, in-place | `ConcurrentHashMap` | `StateFlow<Map>` + Room | none, rows in VM |
| Chat list | `(order, id)` sort on change, on Default | sort per `emit` on actor | memo sort, 100 ms, on Main | feed | 40 ms remap all | 200 ms sample, all folders | sort per change, no flowOn | `getChats` re-read |
| Row composition | VM mapper with reuse | snapshot projection | row reads state | `TimelinePost` | remap all | remap all | small model | `ChatSummary` |
| History | window, LRU 3, replay during load | one selected chat, detach gate | list per chat, `detached` | merged feed + `loadOlder` | merged list, no gap model | merged list, flags unused | Room table | none (media search) |
| Page vs update race | `changesWhileLoading` | token + request-versioned guards | same queue | CAS + filtering | stale page overwrites | first wins, racy | same stream, then racy DB | n/a |
| Stable key on id swap | no | `presentationId` | `key` | n/a | `presentationKey` | no | no [U] | n/a |
| Files | observed only, refcount | map in snapshot | `filePaths`, `transfers` | per-file StateFlow + reducer | per-file StateFlow + version guard | global map | fire-and-forget + tracker | per-consumer filter |
| Images | Coil Fetcher + Keyer | own pipeline | own LRU, `produceState` | Coil + binding | paths in models | `BitmapFactory` per item | Coil (README) | own LRU |
| Notifications | TDLib engine + renderer + FCM | TDLib engine + toasts | `updateNewMessage` | off | TDLib engine + queue + FCM | none wired (grep) | none (grep) | none |
| Lifecycle | start/stop → open/close; `online`; network | generations; network; no `online` | `DisposableEffect` → open/close; focus reads | focus tracker; network; `online` | open/close; `online` | open at load, close explicit | open in `observeMessages` | network, `online` |
| UI delivery | StateFlow per screen | one global snapshot | snapshot state per field | list StateFlow + per-file flows | list + per-chat + per-file | per-chat + global files | per-repo flows, VM state | VM `UiState` |
| Mapping | hand-written domain subset | common subset | TdApi + `UiMessage` | `TimelinePost` | domain subset | 1:1 mirror | hand-parsed JSON | `ChatSummary` etc |
| Errors | `Outcome`, timeout | `ChatError` | log + inline | global flood gate | `TdErrors` | exceptions, `printStackTrace` | `println` | typed helpers |
| Tests (files/kLoC) | 819/143 | 526/131 | 0 | 62/9 | 170/29 | 8/0.3 | 4/0.05 | 397/56 |

### 4.2 Anti-pattern catalog (only what I read)

| # | Pattern | Evidence | Consequence |
|---|---|---|---|
| A1 | **`launch` per update** | `iamaritrasaha/aether` `A/data/telegram/TelegramClient.kt:289`; `logopek/50gramm` `app/src/main/java/ni/shikatu/a50gramm/tdlib/Tdlib.kt:65`; `u-myds/50gram` `app/src/main/java/u/composegram/Tdlib/Tdlib.kt:33`; `tolvana/weargram` `app/src/main/java/xyz/tolvanen/weargram/client/TelegramClient.kt:33-34`; TGW per-callback `W/runtime/TgClient.kt:375` | No order, even per chat. TGW's comment admits it (`:457-461`) |
| A2 | **Per-type collectors on one stream** | HOR `H/data/posts/PostsRepository.kt:618-771`; `julfy-0/Yougram` `app/src/main/java/app/yougram/feature/chat/data/ChatRepository.kt:786-1026`; TMP `D/data/Td.kt:439-501`, `U/ui/browse/BrowseViewModel.kt:177-246`; `omaeewa/TGram` `core/data/src/main/java/com/miracle/data/updateshandler/ChatsUpdateHandler.kt:42-54`. PG is the milder variant, per store | Cross-type order lost; see A3 |
| A3 | **Race buffers compensating for A1/A2** | HOR `pendingLastMessages` (`:267,723-729`); TGW `_pendingFieldUpdates` (`W/runtime/ChatsRepository.kt:51-53,440,761-762`) | Machinery for an order TDLib already guarantees (CS§1.2) |
| A4 | **Lossy or replaying transport** | `DROP_OLDEST`: MAT `MD/impl/TdClientManagerImpl.kt:41-54`; td-ktx `ResultHandlerStateFlow.kt:13-16`; `bling0390/tellygram` `app/src/main/kotlin/tv/telegram/td/TdClient.kt:30-34`; TeleDrive `shared/src/desktopMain/kotlin/com/drdisagree/teledrive/core/telegram/DesktopTelegramClient.kt:84-87`; `paranoiacXD/prismGram` `app/src/main/java/com/prismgram/tdlib/TdClient.kt:19-23`; `kernoeb/telegramedia` `tdlib/src/main/java/app/telegramedia/tdlib/TdlibClient.kt:45-49`. Replay: `6xingyv/compound` `app/src/main/java/com/mocharealm/compound/di/DataModule.kt:46` (500), GHO `TelegramFlowClient.kt:23` (50), MAT (25) | Dropped `updateNewChat`, or old updates re-applied to late collectors. tellygram logs what it drops (`:259-265`) |
| A5 | **Dropping `positions` from last-message and draft updates** | MAT `MD/impl/ChatsRepositoryImpl.kt:119-124`; GHO `GD/handlers/ChatUpdateHandler.kt:133-153`; TMP collects no such flow | TL:10871: chat does not move on a new message |
| A6 | **Wrong position semantics** | `GazizVR/BirGram` `core/telegram/src/main/java/org/gaziz/birgram/core/telegram/internal/updater/ChatUpdater.kt:34-36` removes the chat from the whole cache on `order == 0`; TGram `ChatsUpdateHandler.kt:90-94` only ever appends positions; `tolvana/weargram` `ChatProvider.kt:22-23` has a comparator that never returns 0, with a comment that `remove` "didn't work" | Archiving loses the chat; stale orders pile up; broken set semantics |
| A7 | **Sort without `chat.id`, or with `is_pinned`** | PXG `:1391`; AET `ChatOrdering.kt:46-48`; GHO `GD/ChatRepositoryImpl.kt:134`; MAT `ChatsViewModel.kt:96-101` | Violates TL:3851. Rows can swap |
| A8 | **Whole-list rebuild per update** | NNG `J/ChatController.kt:3633-3665` (per event); AET `:3534-3539`; MAT `ChatsViewModel.kt:72-106` (all folders); `TGwear/TGwear` `app/src/main/java/com/gohj99/tgwear/utils/telegram/UpdateHandle.kt:353,500`; PG `R/TdlibChatRepository.kt:83-90`, mitigated by conflation and row reuse | CPU per update. Without instance reuse every visible row recomposes. PG shows rebuild is survivable *if* rows are memoised |
| A9 | **Global map read in every row** | MAT `ChatAvatar.kt:47,68`, `PhotoMessage.kt:68`; NNG `C/ChatMediaUi.kt:57-59,74` | One file's progress recomposes all rows |
| A10 | **`combine` over N per-entity flows inside `flatMapLatest`** | PG `CH/ChatFiles.kt:50-54`, `CH/ChatViewModel.kt:981`; `indritbashkimi/TelegramExample` `app/src/main/java/com/ibashkimi/telegram/data/chats/ChatsRepository.kt:33-40` | Restart of all subscriptions on any id change; O(N) per emission |
| A11 | **Unbounded per-chat state** | MAT `MD/impl/MessagesRepositoryImpl.kt:61,138-142`; AET `:2544,2552`; PXG `:301` (per opened chat). Contrast PG's LRU 3 and TGW's observer count (`W/runtime/ChatMessagesRepository.kt:66,114`) | Memory grows with every chat touched |
| A12 | **Non-atomic `StateFlow` read-modify-write** | MAT `MD/utils/ChatHistoryProcessor.kt:87-110`; weargram `ChatProvider.kt:34-39` (also two emissions, the second after a transient removal); TGwear `chatsList.value = chatsList.value.toMutableList()…` from the TDLib callback (`.../utils/telegram/TgApi.kt:46,134`) | Lost updates |
| A13 | **In-place DTO mutation inside "immutable" state** | weargram `ChatProvider.kt:51-53`; AET `:2732-2735`; TGram `ChatsUpdateHandler.kt:105-113` | Same instance means `StateFlow` and `===` see no change. Not reachable with tdlib-kmp if its DTOs are `val` [I from `05-kotogram-fit.md` §3] |
| A14 | **Non-monotonic merges** | AET `:2456-2461` (page overwrites); MAT `ChatsRepositoryImpl.kt:222-231` (`GetChat` answer overwrites); MAT `ChatHistoryProcessor.kt:321-323` (first wins, so a fresh page loses to a stale row). Contrast PG `R/HistoryCache.kt:304-316`, `R/TdlibFileRepository.kt:128-134`, and AET's own file guard | Newer state undone |
| A15 | **Live message merged into a detached window** | AET `:2809`; MAT `ChatHistoryProcessor.kt:57-84`. Respected by PG, NNG, PXG | Holes in history |
| A16 | **Delete-flag errors** | Treat `from_cache` as deleted: AET `:2897-2919`, MAT `:118-120`, GHO `:175-187`. Gate on `is_permanent` (so inaccessible rows stay): PXG `:1214`, HOR `:2367`, TGW `W/runtime/ChatMessagesRepository.kt:1358`, Yougram `:958-960`, TMP `D/data/Td.kt:480-484` | Vanishing rows, or rows that should be gone. Only PG and NNG separate the cases |
| A17 | **Paging 3 over live data** | TelegramExample `data/chats/ChatsPagingSource.kt:15-34`, `data/messages/MessagesPagingSource.kt:15-31`, `ui/chat/ChatScreenViewModel.kt:37-39` (no `invalidate`, no updates); `ilhomsoliev/EasyTelegram` `data/chat/src/main/java/com/ilhomsoliev/chat/messages/paging/MessagesPagingSource.kt:22-29` (refetches from 0 with `limit = loadSize * page`, past TDLib's cap of 100, CS§1.4) | Frozen lists, or errors after a few pages. Fair use exists: TeleDrive pages its own Room table (`shared/src/commonMain/kotlin/com/drdisagree/teledrive/data/local/dao/FileDao.kt:100`). No serious chat client pages history with Paging 3 |
| A18 | **`getChats`/`getChat` as the read path** | Compound `.../data/repository/telegram/ChatRepositoryImpl.kt:25-43`; TMP `D/data/ChatRepository.kt:73-77,117-125`; TelegramExample | N+1 requests, stale lists (CS§1.2, TL:11976) |
| A19 | **Own database for what TDLib stores** | GHO Room (`GD/handlers/MessageUpdateHandler.kt:72-76`) | Two truths; contradicts the developer's rule |
| A20 | **Global message map keyed by id** | BirGram `.../internal/updater/MessageUpdater.kt:14-18` | TL:3429: ids are unique per chat only. Messages of two chats collide |
| A21 | **Substring match on raw JSON** | GHO `TelegramFlowClient.kt:54-71` | Message text can trigger protocol logic |
| A22 | **Blocking** | `solomonBoltin/TelegramTv` `app/src/main/java/com/solomonboltin/telegramtv/vms/ClientVM.kt:96-112` (`Thread.sleep` loop); TGwear `app/src/main/java/com/gohj99/tgwear/ChatActivity.kt:88,104,218` (`runBlocking`); `cheipstudio-cmyk/novagram` `app/src/main/kotlin/com/secondream/novagram/notifications/NotificationHelper.kt:191,202,297` | Main-thread stalls |
| A23 | **Local REST server as the data layer** | `p-vorobyev/...` `desktop/src/main/kotlin/transport/Http.kt:25`, `desktop/src/main/kotlin/common/api/Api.kt:23-29`, polling delays in `desktop/src/main/kotlin/chat/composable/Chat.kt:68-84` | Discards push. This is the one "CMP desktop" candidate named in the brief |
| A24 | **God objects** | AET 4.0k lines; `cheipstudio-cmyk/novagram` `td/TdClient.kt` 3.6k, `ui/screens/ChatScreen.kt` 9.3k; Yougram `ChatRepository.kt` 3.5k; HOR `PostsRepository.kt` 3.1k; NNG `ChatController.kt` 3.7k with 130+ fields | Every feature touches one class; AET's tests bypass the concurrency that matters |
| A25 | **Second TDLib client on the same directory** | TGwear `.../utils/notification/TgApiForPushNotification.kt:54,83` | UNI#15; fails if the main client is alive [I] |

**Asked about, not seen:** `stateIn` per chat or per row (the closest is `stateIn` per screen and PG's cold derived `chat(id)` flows). `distinctUntilChanged` hiding a reorder (I saw it only as an O(n) cost on large lists, HOR `TimelineViewModel.kt:84-101`).

### 4.3 Best ideas worth taking

"KMP" assumes the code is rewritten against tdlib-kmp DTOs. Own-design adaptations are flagged.

| # | Idea | Reference | KMP? | Caveat |
|---|---|---|---|---|
| B1 | **Subscribe with `CoroutineStart.UNDISPATCHED` so the collector is registered before anything can send; read `Closed` from the stream, not the conflated flow** | PG `R/TdlibUpdateCache.kt:25-30,47-53`; test `core/data/src/test/kotlin/dev/kyralabs/pingogram/core/data/repository/UpdateStoreTest.kt` | Yes, coroutines only | Same fix `05-kotogram-fit.md` §2.2 derives independently |
| B2 | **Pure reducer `(state, update) → state` returning the same instance when nothing changed** | PG `:33-34`, `R/TdlibUserRepository.kt:64-87`; AET's `File` manager | Yes | Purity is required by `MutableStateFlow.update`, and makes tests trivial |
| B3 | **Record updates during a load; merge page; replay them. Reducers set state, so replay is harmless** | PG `R/HistoryCache.kt:163-166,189-204`; tests `TdlibMessageRepositoryTest.kt:590,605` | Yes | The best answer to the page-vs-update race on this stack. tdlib-kmp has the same topology (§6) |
| B4 | **Window invariant with `reachedStart`, `reachedEnd`; page merges only extend ends; live messages only if they fit** | PG `R/HistoryCache.kt:157-174,251-264,304-316`; NNG `J/ChatController.kt:3288-3295`; PXG `detached` | Yes | Add a null-last-message rule, which nobody has |
| B5 | **Load coordinator: one load per history, deduped by key, in a scope that outlives the caller** | PG `R/HistoryLoads.kt:28-77` | Yes (replace `ConcurrentHashMap`, `AtomicLong`) | Lets a caller leave without cancelling a request TDLib cannot cancel anyway |
| B6 | **New window and scroll anchor delivered in one state** | PG `CH/ChatWindow.kt:177-188,258-270` | Yes | Solves "new messages drawn where old ones were" |
| B7 | **First-measure anchor placement: `requestScrollToItem` from a layout modifier, list hidden until anchored** | PG `CH/MessageJumpState.kt:162,254-294`, `CH/ChatScreen.kt` `hideUntilAnchored` | Yes [I: `Modifier.Node`, `LazyListState` are common API] | `reverseLayout` |
| B8 | **Memoising row mapper: reuse the previous row object when its inputs are equal; return the same list instance when nothing changed** | PG `CL/ChatListItemMapper.kt:62-91`, `CH/ChatItemMapper.kt:105-147` | Yes (replace `java.time` with kotlinx-datetime) | This is what makes `===` strong skipping work when one user status re-emits everything. Not thread-safe; one collection at a time |
| B9 | **Stable presentation key across the id change** | AET `A/domain/model/Models.kt:274`, `A/data/telegram/TelegramClient.kt:2793-2808,2472-2482`; PXG `P/telegram/Telegram.kt:153,1158`; NNG `C/chat/ChatModels.kt:54`, `J/ChatController.kt:3234` | Yes | Three independent inventions. It is an own field, so flag it against the "as few flags as possible" rule |
| B10 | **Album as one item keyed by album id, growing as pages arrive** | PG `CH/ChatItemMapper.kt:161,167`; AET `A/domain/messages/MessageGrouping.kt:21` | Yes | Avoids the 500 ms grouping delay (MAT) and three-layer repair (HOR) |
| B11 | **Identity-based distinct over persistent maps** | PG `R/TdlibChatRepository.kt:84-90` | Yes (kotlinx-collections-immutable) | Keep it at store level; see `05-kotogram-fit.md` §3.4 on `ImmutableList` in lazy-list models |
| B12 | **Per-field snapshot state and per-key slot** | PXG `P/telegram/Telegram.kt:97-125`, `P/telegram/KeyedState.kt:9-42` | Yes (`compose.runtime` is common) | Per-field recomposition for free, but the cache depends on Compose and writes belong on Main. Conflicts with `core:domain` having no Compose. Usable as a *presentation-side* row holder [own design] |
| B13 | **One queue for answers and updates** | PXG `:337,447`; NNG `J/TdEngine.kt:151-164`, `J/ChatController.kt:11` | Yes, as a wrapper around `send` [own design] | The only way to keep TDLib's relative order past tdlib-kmp's two paths. Probably needed only for specific flows, given B3 |
| B14 | **File reducer: single writer; per-file `StateFlow`; `subscriptionCount` as the refcount; debounced cancel; progress throttle; stall watchdog; idempotent eviction** | HOR `H/data/MediaCache.kt:140-235,282-315,383-390,880-919` | Mostly (replace `ConcurrentHashMap`, `Log`, `System.currentTimeMillis`). `subscriptionCount` is common API | The watchdog rests on tdlib/td#2585 (§5). Add TGX's merge of consumer windows |
| B15 | **Priority ladder** | HOR `:34-53`; PG `DownloadPriority` (`core/domain/src/main/kotlin/dev/kyralabs/pingogram/core/domain/model/FileState.kt:60-70`) | Yes | levlam: ongoing part requests are not cancelled by a higher priority (tdlib/td#2585) |
| B16 | **An answer must not overwrite a newer update** | PG `R/TdlibFileRepository.kt:128-134`; AET `A/data/telegram/TelegramFileManager.kt:70-103` | Yes | General rule for all request-and-update entities |
| B17 | **Coil 3 `Fetcher` + `Keyer` over the file repository; no disk cache** | PG `IMG/TdlibFileFetcher.kt:35-90`, `IMG/TdlibFileImage.kt:19-37`, `app/src/main/kotlin/dev/kyralabs/pingogram/di/ImageLoaderModule.kt:56-69`; `Mahmud0808/TeleDrive` `ui/src/jvmCommonMain/kotlin/com/drdisagree/teledrive/core/media/ThumbnailFetcher.kt:12-29` | Yes: imports only coil3, okio and `java.io.IOException` | Needs a per-account loader or an account in the key (§3.1). `FileSystem.SYSTEM` is unavailable on web |
| B18 | **Bounded decode: `Dispatchers.Default.limitedParallelism(n)`** | TMP `U/data/Thumbnails.kt:259-263` | Yes | |
| B19 | **Visible-message reporting from `layoutInfo`, by key, comparing only both ends per frame; batch `viewMessages` at most every 300 ms, once each, only while open** | PG `CH/MessageListEffects.kt:66+`, `CH/MessageViews.kt:17-70` | Yes | Runs outside items |
| B20 | **Per-item `derivedStateOf { key.value == item.key }`** | HOR `H/ui/timeline/TimelineFeedColumn.kt:142-153` | Yes | Selection or centre change recomposes two rows, not all |
| B21 | **High-frequency state stays out of `UiState`** | PG `CH/ChatViewModel.kt:207` | Yes | Applies to playback, progress, upload percentage |
| B22 | **`LazyLayoutCacheWindow`** | PG `core/ui/src/main/kotlin/dev/kyralabs/pingogram/core/ui/component/ScrollingListState.kt:18-34`; its numbers in `docs/PERFORMANCE.md` §4 | [U] on CMP 1.12 availability | Experimental API |
| B23 | **Recomposition counter via `CompositionObserver`** | PG `feature/chats/src/test/kotlin/dev/kyralabs/pingogram/feature/chats/ui/RecompositionCounter.kt:25-67`, `ChatListRecompositionTest.kt` | Likely on JVM tests [I]; PG runs it on Robolectric | Assert "scrolling recomposes none of its composables" |
| B24 | **Fake at the client seam; a full demo client answering functions and emitting updates** | PG `core/testing/.../FakeTdlibClient.kt:27-80`, `core/demo/`; HOR `FakeTdSender.kt:17-25` | Yes behind `ClientHolder`; for `TdClient` itself fake the `TdLib` interface (§6) | HOR's RPC budget assertions are worth copying |
| B25 | **Notification reducer plus serial work queue with `awaitDrained`** | PG `R/TdlibNotificationRepository.kt:122-191`; AET `A/data/push/NotificationWorkQueue.kt` | Reducer and queue yes; renderer per platform | `awaitDrained` is the coroutine form of TGX's wait before releasing a push |
| B26 | **Keep `openChat` running through a cancelled scope** | PG `CH/ChatViewModel.kt:1259-1278` (UNDISPATCHED); HOR `H/data/ChatPresence.kt:148-153` (`NonCancellable`) | Yes | Unbalanced opens otherwise |
| B27 | **Separate `ListLoad` state per list, in the VM: `isLoading`, `hasLoadedPage`, `isComplete`, `error`** | PG `CL/ChatListViewModel.kt:154,377-396`; a page that leaves the end near loads the next | Yes | The only own state chat-list paging needs |

### 4.4 What nobody has solved on this stack

| # | Gap | Evidence | Difficulty |
|---|---|---|---|
| N1 | **One ordered applier + atomic cross-entity state + off main + coroutines** | PG: not ordered across stores. PXG: on Main. NNG: ordered and off main, but a single screen snapshot rebuilt per event. BirGram has one dispatcher but separate flows and no atomic view (`.../internal/UpdateDispatcher.kt:22-62`) | **Medium.** No precedent to copy, though each part exists. The risk is a slow branch in the single writer (NNG shows it) |
| N2 | **iOS** | GHO stub (`GT/iosMain/.../TelegramNativeClient.kt:3-15`); `nVoxel/TGDriveDemo` writes iOS repositories in Swift over TDLibKit (`iosApp/iosApp/data/repository/IosAuthRepository.swift:9-13`); `g000sha256/tdl-coroutines` has only a verification app (`example/verification/app/ios/`) | **High, unknown depth.** Kotlin/Native `td_json` in a shipping chat app has no evidence here. Same for lifecycle, push and paths |
| N3 | **Push and background on KMP** | Android only: PG, AET, TGW, Compound, novagram. Desktop toasts: NNG. iOS: nothing | **High.** Also blocked on my.telegram.org access (report 04 §2 and PG's own ADR 0004 cannot verify it either) |
| N4 | **Gap handling for `last_message == null`** | Grep across deep-read history code finds none; PXG's one hit is unrelated (`P/telegram/Telegram.kt:593`) | **Low to design, hard to test** (needs a contrived server state). Unigram's tri-state is the model |
| N5 | **Local page lagging the chat's last message** | PG `R/HistoryLoads.kt:108-113` assumes it cannot; only TGX and Unigram compare | **Low.** Compare with `chat.last_message.id`; re-ask remotely |
| N6 | **Download arbitration** | Kotlin clients: last request wins (PG, AET), first wins (PXG `requestedFiles`), or a per-file ladder with no range merge (HOR). TMP handles one stream window. Only TGX merges | **Medium** |
| N7 | **Cross-account images** | PG's singleton loader bug (§3.1). Others are single-account or use paths in models | **Low**, once chosen: per-account loader or account in key |
| N8 | **Runtime account switch without restarting the UI** | PG restarts the task, by design. GHO swaps a session object and VMs re-collect (`shared/src/commonMain/kotlin/com/ghostgram/app/presentation/chats/ChatListViewModel.kt:29,45`). TMP is a global `object` | **Medium, and Kotogram is already ahead** with Koin scopes and `AccountViewModelStoreOwner` (`docs/architecture.md` §2.4) |
| N9 | **Off-main text measurement** | TGX prepares layouts off the UI thread (report 03, C8). Compose measures in the frame: PG reports 189 ms of row measurement on a low-end phone (`docs/PERFORMANCE.md` §2), and uses the cache window instead | **High, not fully solvable.** Reduce by prefetching and reuse |
| N10 | **Incremental sorted chat list** | All re-sort. PG's "a sort of about 1000 chats costs far less than a frame" is a docstring (`R/TdlibChatRepository.kt:62-64`) with no benchmark under update storm found in `docs/PERFORMANCE.md` (grep for sort finds none) | **Low to medium.** Measure first; `PersistentList` plus binary search is available |
| N11 | **Ordering stress tests** | PG's fake delivers updates to each subscriber before returning (`FakeTdlibClient.kt:22-23`); AET calls handlers directly. No interleaving test found [U on completeness] | **Low.** Deterministic with one applier, which is another argument for it |
| N12 | **JSON decode cost in a cold-start storm** | Only GHO, TMP and Yougram use the JSON path. None measures it. Unigram's parser bench is C# (report 04 §7) | **Unknown.** Measure on tdlib-kmp before designing around it |

### 4.5 Entities that keep recurring

**Likely essential: independently present in good clients**

| Entity shape | Appears in | Reading |
|---|---|---|
| Client seam: `send` + ordered `updates` + auth state | PG `TdlibClient`, HOR `TdSender`, NNG `TdSession`, GHO `TelegramFlowClient`, TeleDrive `TelegramClient` | Kotogram has it: `ClientHolder` |
| Reducer of updates into immutable or confined state | PG `UpdateStore`, NNG `ChatController`, PXG `handleUpdate`, BirGram `*Updater`, HOR `MediaCache.reduce` | The core entity; differs only in number |
| Chat cache by id; user, supergroup, basic group, secret chat caches | every serious client (mandatory, CS§1.2) | |
| Chat-list projection `f(cache, list) → ordered rows` with a 404 flag | PG `chatList` + `ListLoad`, PXG `chatList(key)` + `listsComplete`, NNG `items` + `ListPage`, Neogram | Derived, not stored; held per list |
| History window per open chat with both end flags and a load guard | PG `HistoryWindow`, NNG `historyCursor`/`newerCursor`/`historyHasNewer`, PXG `messages` + `detached`, AET `ConversationHistoryManager`, TGW `newerAnchorMessageId`/`olderHistoryEndReached`, MAT `ChatHistoryProcessor` | Same as Telegram X's and Unigram's; appears in everything |
| Open-chat tracker | PG `OpenChats`, HOR `ChatPresence`, PXG `openChats` | Read by notifications; TDLib itself also counts (§5) |
| Visible-to-read reporter | PG `MessageViews`, NNG `visibleMessages`, HOR read-ack dwell | A small class, per conversation |
| File state store per file id, with refcount or observation | PG, HOR, AET, NNG; simplified in PXG | |
| Image bridge from file id to pixels | PG, TeleDrive (Coil); PXG, TMP (own LRU) | |
| Row model plus mapper in presentation | PG `ChatListItemModel` + mapper, AET `ConversationEntry`, PXG `UiMessage`, NNG `ChatItem`/`ChatMessage`, HOR `TimelinePost` | Every good client has one regardless of its domain model, including those that keep TdApi in UI (PXG). A 1:1 domain does not remove it |
| Presentation key separate from message id | AET, PXG, NNG | §4.3 B9 |
| Notification state + platform renderer | PG, AET, NNG | |
| Reporter for `online` and `setNetworkType` | PG `TdlibNetworkTypeReporter`, HOR `TdLifecycleBridge`, Yougram `registerNetworkMonitor` | |
| Generation or session token | NNG `ChatToken`, PG `loadId`/`windowId`, TMP `TdSession(generation)`, TGW `generation`, TeleDrive `clientGeneration` | Needed for restarts and stale answers. Coroutine cancellation does not cover answers already in flight |
| Global flood-wait state | HOR `_floodWaitUntilMs`; PG `TdError.retryAfter` for UI | Optional |

**Only in weak clients, or symptoms**

| Shape | Where | Why it is not essential |
|---|---|---|
| Race buffer for early field updates | HOR, TGW | Disappears with one applier |
| Per-chat processor created on any update | MAT | Windows should exist only for observers |
| Sender cache inside a window | MAT `sendersCache` | User and chat caches exist; TDLib guarantees them |
| Own message or chat database | GHO | TDLib stores it |
| `PagingSource` over history or chat list | TelegramExample, EasyTelegram | No push path |
| File-to-chat routing tracker | GHO `DownloadTracker` | Follows from storing a path in the chat model instead of the file |
| Local server | p-vorobyev | |
| 1:1 mirror of functions and updates | MAT | Nothing consumes most of it |

---

## 5. TDLib source and issue facts that resolve disputes between clients

| Dispute | Fact | Source |
|---|---|---|
| Is `openChat` a counter or a boolean? PG says counter, HOR says boolean | **Counter.** `d->open_count++; if (d->open_count != 1) return;` and `d->open_count--; if (d->open_count > 0) return;`. A client refcount is not needed for TDLib. It may still be useful for delayed close (TGX#12) or for app logic | MM:17104-17108, 17212-17216 |
| When does `from_cache = true` happen? AET says ignoring it hides real deletes; TGX, UNI#7 ignore it; PG drops older rows | **Only for unloads, only when the chat has no opens.** `can_unload_message` requires `d->open_count == 0`; `unload_dialog` sends `updateDeleteMessages(..., false, true)`; every other sender passes `false` for `from_cache`. So an open chat never receives it, and a *retained* window of a closed chat goes stale. That justifies PG's rule and shows AET's comment is wrong | MM:13193, 8872-8923, 27193-27203 |
| Must inaccessible messages be removed? | `is_permanent` is false for messages "just becoming inaccessible" with `from_cache` false. Those must leave the list, so gating on `is_permanent` alone (A16) is wrong | TL:11060-11063 |
| Do chat actions need timers? | No. TDLib cancels after `DIALOG_ACTION_TIMEOUT = 5.5` s. PG's claim holds | `td/telegram/DialogActionManager.h:46`, `.cpp:325,467` |
| Is a stuck download a bug? | levlam: "There are no 'stailed' downloads. The download is in progress and it should eventually succeed." So a watchdog is client policy. Also: "Priority always matters for yet unsent file part request, but ongoing requests aren't canceled" | https://github.com/tdlib/td/issues/2585 |
| Is there an "album complete" signal? | "There is no way to know this. You need to use some timeout." | https://github.com/tdlib/td/issues/1482 |
| Can history answers disagree with updates? | "`getChatHistory` returns messages, which state is consistent with all other updates and request responses." Also: "Only the first message in an album can receive reactions." | https://github.com/tdlib/td/issues/2312 |
| How many chats open? | "Usually, users have at most one chat opened." | https://github.com/tdlib/td/issues/2695 |
| History rate limit | "`getChatHistory` requests currently are limited to at most 30 requests in 30 seconds." | https://github.com/tdlib/td/issues/743 |

---

## 6. Reading this for the given library (own reading, flagged)

- **tdlib-kmp has the same topology as PG.** Answers complete a `CompletableDeferred` on the receive loop (`client/commonMain/io/xbot/tdlib/client/TdReceiver.kt:79-82`), while updates go through `tryEmit` into a buffered `SharedFlow` (`:86`, `TdClient.kt:57-60`). So an answer can overtake earlier updates that are still queued for a collector. PG faces the identical race (`T/DefaultTdlibClient.kt:113-117,135`), and B3 is the tested answer. Set-state reducers converge whichever arrives first, which pairs well with TDLib's consistency statement in #2312 [I].
- **`TdClient.updatesOf<T>()` (`TdClient.kt:47`) is a public invitation to A2.** tdl-coroutines' typed flows produced exactly that in TMP and Yougram. Keep it from leaving the applier module [own design].
- **Fakes.** `TdClient` is final, but `TdLib` is an interface (`core/commonMain/io/xbot/tdlib/TdLib.kt:14-34`), and `TdClient.create(tdLib)` accepts any implementation (`TdClient.kt:64`). A JSON-level `FakeTdLib` is possible, with repository tests staying behind `ClientHolder`. Report 05 §6.4 reaches the same testability conclusion from its side.
- **Cross-check with `05-kotogram-fit.md`, judged independently.** Its Design A (single reducer, immutable snapshots) has no exact precedent here. The evidence supports its parts: ordering (PXG, NNG), reducers and snapshots (PG), derived lists off the writer (PG). NNG is direct evidence for its stated weakest point, that a slow branch in the single writer stalls everything; PG's memoised mapper is the mitigation at the presentation end. It also agrees on `Keyer` needing care, though PG keys by numeric id and clears on close, whereas report 05 proposes `remote.uniqueId`. Evidence adds one item it does not list: a stable presentation key.
- **Mirror.** MAT is evidence on cost only. It says nothing on whether a smaller mirror is right, but PG's hand-written subset keeps TDLib's names at about 12k domain lines and 22k data lines for a nearly full client. That is a third option between report 05 §3's options 1 and 4.
- **Two schools for UI delivery.** VM-derived rows with memoisation (PG) suit Orbit and the current rules. Per-item state (PXG fields, HOR files) needs no mapping but ties row state to composition. PG itself uses per-item only through Coil. Files are where every good client goes per-item or per-file: progress must not pass through the list state [I].

---

## 7. What I could not verify

- **Nothing was run.** Every user-visible consequence is inferred from code, and no client's runtime or performance claim was tested. PG's performance numbers are its own.
- **Field behaviour.** No issue trackers were mined for the Kotlin clients, and clones are shallow, so "regrets" come from comments only.
- **TDLib version.** Source and schema lines are 1.8.68; the library is 1.8.69 (same caveat as report 04).
- **Completeness of negative claims.** "Not found" means my grep across the named paths found nothing. This applies to N4, N11, `online` in NNG, `setNetworkType` in PXG, and notifications in MAT and GHO. GHO's stable key and Coil use are from its README and imports, not call chains.
- **Read lightly.** nngGram's 88k main lines were sampled along the chat path only, not its media pipeline or settings. PG's calls and media features, TMP's player and accounts (`D/data/AccountSessions.kt`) and HOR's archive were not read. TGDriveDemo was read at tree level.
- **CMP availability.** `LazyLayoutCacheWindow`, `CompositionObserver` and `requestScrollToItem` in CMP 1.12.1 are assumed from common Compose APIs, not confirmed in CMP sources.
- **HOR's claim** that TDLib may report `isDownloadingCompleted = true` with an empty path was not checked against TDLib.
- **Neogram and hikari** were sampled through the API, not cloned; line numbers for Neogram were counted by hand from the fetched file.
