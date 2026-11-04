# Red team: mechanics of `docs/design/messaging-core.md` (v1)

Date 2026-11-03. Read-only. Sources: kotlinx.coroutines 1.11.0 sources jar (`~/.gradle/caches/.../kotlinx-coroutines-core-1.11.0-sources.jar`, unpacked to `scratchpad/redteam/coroutines/`, paths below are `commonMain/...`), Koin 4.2.2 (`Scope.kt`, `InstanceRegistry.kt` at tag 4.2.2; local clone for the rest), Orbit `main` (`RealContainer.kt`, `Syntax.kt`, `ContainerHostExtensions.kt`, raw GitHub), the published tdlib-kmp sources (`NEW/`), Kotogram working tree (`K/`), Android Compose stability docs and Kotlin/Native binaries docs (fetched earlier, quoted in `05-kotogram-fit.md`). `D §n` = the draft's section. CONFIRMED = behaviour read in source; PLAUSIBLE = follows from read facts but not exercised.

Severity: **blocker** (the design does not work as written), **serious** (works until a realistic event, then corrupts or leaks), **minor**.

---

## 1. D §3 — the `runInstances` loop

### 1.1 What is right (one line, so the attack is fair)

`SharedFlowImpl.collect` allocates the slot, then runs the `onSubscription` action, then takes values (`flow/SharedFlow.kt:384-387`). The KDoc guarantees "all emissions to the upstream flow that happen inside or immediately after this onSubscription action will be collected by this subscription" (`flow/operators/Share.kt:427-429`). So `onSubscription { launch { session.setUp(client) } }` does register before the first `send`. CONFIRMED. `launch` inside the action compiles: the action's receiver is `FlowCollector`, which has no `launch`, so resolution falls through to the enclosing `coroutineScope { }` receiver. CONFIRMED (language rule; the lambda is `suspend FlowCollector<T>.() -> Unit`, `Share.kt:433`).

### 1.2 BLOCKER — `coroutineScope` never returns while `setUp` is alive; the restart never happens

`coroutineScope` completes only when the block *and all children* complete. The `launch { session.setUp(client) }` is a child of that scope. D §8 step 3 says `setUp` sends `SetNetworkType` "again on every connectivity change", i.e. it collects `NetworkMonitor` for the instance's life. After `Closed`, `transformWhile` ends the collect, but `coroutineScope` waits for the `setUp` child, which is still collecting connectivity. The next instance is never created; the account is stuck at `Closed`. CONFIRMED (structured concurrency; `CoroutineScope.kt` KDoc for `coroutineScope`).

Failure scenario: QR screen → "log in by phone" → `LogOut` → `Closed` → nothing restarts; `authorizationState` stays `Closed`; `RootViewModel` keeps the splash (`K/shared/.../RootViewModel.kt:38-45`).

### 1.3 BLOCKER — any failure in `setUp` or in `store.apply` cancels the collector and leaks a live TDLib instance

A failed child cancels `coroutineScope` and rethrows. Two realistic sources: `SetTdlibParameters` fails (`TdException`: wrong `api_id`, directory busy, disk full); `reduce` throws (a bug in one of ~50 branches; the design says "no `else`", so a missed field path throws). Then:

1. The `collect` is cancelled mid-stream; the instance is **not** closed (nothing sends `Close`).
2. `while (currentCoroutineContext().isActive)` is still true (the holder's own scope is alive), so the loop creates a **second** `TdClient` on the **same** `databaseDirectory`.
3. TDLib refuses a second instance on a locked directory: the second `SetTdlibParameters` fails [PLAUSIBLE; TDLib's own rule, not read here] → `setUp` throws → step 1 again. The loop spins, creating one unclosed `TdClient` per iteration; each registers in `TdReceiver.clients` (`NEW/client/.../TdReceiver.kt:37-43`) and is never removed because it never reaches `Closed`.

Or, if the exception propagates out of `runInstances` instead (it does: `coroutineScope` rethrows), the lane coroutine dies, the live instance keeps running with **no subscriber**, and every update is dropped (replay 0, `tryEmitNoCollectorsLocked`, `flow/SharedFlow.kt:424-426`). Either way: CONFIRMED by coroutine semantics.

### 1.4 SERIOUS — ordering between `setUp` and `apply`

`setUp` runs on the lane (the `launch` inherits the collect's dispatcher). Its `send` suspends on `answer.await()`; the continuation resumes on the lane. The design accepts answers overtaking updates (D §4.3). But `setUp` step 2 "re-sent every time" writes options that `reduce` also tracks via `updateOption`; a `setUp` that reads `store.state` to decide (e.g. "is online already set") reads a snapshot behind the live instance. Minor in itself; it becomes serious once `setUp` and `apply` share the lane and one of them stalls the other (a long `setUp` step delays applying the burst). PLAUSIBLE.

### 1.5 SERIOUS — scope close during `send`, and the JVM shutdown path

`DefaultClientHolder.close()` today: `runBlocking { withTimeoutOrNull(3s) { instances.replayCache.lastOrNull()?.close() } }; scope.cancel()` (`K/core/tdlib/.../DefaultClientHolder.kt:89-95`). With the §3 loop:

- If `close()` keeps that order (close the client, then cancel), `TdClient.close()` sends `Close`, awaits `Closed` on `authorizationState` (`NEW/.../TdClient.kt:50-55`). The lane sees `Closed` in-stream, `coroutineScope` returns (if 1.2 is fixed), `isActive` is still true → the loop **creates a new instance** on a scope that is being closed, after `DefaultAccountRepository.remove()` deletes the directories (`DefaultAccountRepository.kt:162-167`). TDLib recreates the directory. CONFIRMED ordering, PLAUSIBLE consequence (an orphan `account-N` directory reappears on disk and is listed as an account on next start, `load():119-124`).
- If `close()` cancels the lane first, `Closed` is never applied to the store (fine, the scope dies) but `client.close()` must then run outside the lane: it only needs `authorizationState`, so it works. Required order: cancel lane → `client.close()` → done. Not stated in the draft.
- `runBlocking` inside a Koin `ScopeCallback` on the JVM shutdown hook thread is unchanged; the receive loop may still be inside `td_receive` for ≤1 s after `Closed` (05 §2.7). Unchanged risk, correctly listed in D §8.

Koin runs callbacks in registration order, then deletes the scope (`Scope.kt:555-568` at 4.2.2). Because `DefaultAccountStore` is a constructor parameter of the holder, the store is constructed first and its callback (if any) runs **before** the holder's; a store that cancels its own scope in its callback dies before the holder applies `Closed`. Harmless only if the store has no callback. PLAUSIBLE.

### 1.6 MINOR — `TdClient.create` failure

`TdClient.create(tdLib)` calls `tdLib.createClientId()` → `NativeBridge.createClientId()`; a native load failure throws out of `runInstances` and kills the lane with no restart and no surfaced error. The account then has `authorizationState == null` forever and the root shows the splash. PLAUSIBLE. Needs a terminal state surfaced to `AccountRepository`.

### 1.7 MINOR — `limitedParallelism(1)` per account

Each lane is a view over `Dispatchers.Default`, "a shared pool … at least two" threads (`Dispatchers.common.kt:14-15`); views "limit only their own parallelism" (`CoroutineDispatcher.kt:109-130`). `apply` is CPU-bound and never suspends, so during a cold start each account's lane holds one Default worker for the whole burst. `DefaultAccountRepository.load()` opens **all** accounts at once (`:117-130`), so on a 2-core device two accounts saturate Default; the `TdReceiver` dispatch coroutine (also Default, `TdReceiver.kt:58`), Orbit intents and the row mappers (D §5 "on Default") queue behind them. No deadlock (nothing blocks), but UI mapping latency equals the burst length. PLAUSIBLE. Telegram X avoids this with one dedicated thread.

### 1.8 Corrected sketch

```kotlin
private suspend fun runInstances() {
    while (true) {
        val client = TdClient.create(tdLib)            // failure → terminal state, not a spin
        instance.value = client
        try {
            supervisorScope {                          // setUp failure must not cancel the collector
                val setup = launch { session.setUp(client) }
                client.updates
                    .onSubscription { /* nothing: the subscription exists; start is below */ }
                    .transformWhile { update -> emit(update); !update.isClosed() }
                    .collect { update -> runCatching { store.apply(update) }.onFailure(::reportReducerBug) }
                setup.cancel()                         // the instance is gone; stop its per-instance work
            }
        } finally {
            if (client.authorizationState.value !is AuthorizationStateClosed) client.close()  // never leak an instance
        }
        if (!currentCoroutineContext().isActive) return
    }
}
```

Points: `supervisorScope` so a failed `setUp` surfaces (log + `authorizationState` stays truthful) without cancelling the collector; `setup.cancel()` after the stream ends so connectivity listeners don't keep the scope open; `finally { client.close() }` so a cancelled or failed iteration closes its instance before the next one is created; the reducer exception is caught per update (a reducer bug must not take the account down; log and skip is what Telegram X's `assert` stubs amount to in release). `onSubscription` is still the right trigger for the first `send`; put the `launch { setUp }` there if you want the guarantee by construction, but with the `supervisorScope` receiver, not the `coroutineScope` one.

---

## 2. D §4 — `AccountStore`, `AccountState`, selectors

### 2.1 BLOCKER — `AccountStore.updates.tryEmit(update)` drops updates unless a buffer is declared

D §1 and §4.1 define `updates: SharedFlow<Update>` fed by `tryEmit`. With the default `MutableSharedFlow()` (`replay = 0, extraBufferCapacity = 0, SUSPEND`, `flow/SharedFlow.kt:277-281`) and at least one subscriber, `tryEmitLocked` returns `false` whenever `bufferSize >= bufferCapacity && minCollectorIndex <= replayIndex` (`:424-432`), i.e. whenever the subscriber is not parked waiting at that instant. `tryEmit` then returns `false` and the update is **silently lost for `ChatHistory`**, which the design makes depend on this stream ("Subscribe, then request", D §6). CONFIRMED. The draft never states the buffer size. With `extraBufferCapacity = Int.MAX_VALUE` it works (the library's own choice) but then a `ChatHistory` that falls behind during a burst holds the whole burst in memory per open chat.

Fix: declare `MutableSharedFlow<Update>(extraBufferCapacity = Int.MAX_VALUE)` **and** have `ChatHistory` filter early (`filter { it.chatId == id }` on the lane side is not possible on a SharedFlow; so accept the buffer) — or give `ChatHistory` a `Channel` per open chat registered with the store (bounded, per chat, filtered on the lane). The second is the Telegram X shape (per-chat listeners); it is also [O].

### 2.2 SERIOUS — `StateFlow<AccountState>` with a plain class: correct dedupe, but every selector wakes on every update

`MutableStateFlow` conflates with `Any.equals` (`flow/StateFlow.kt:51-57, 332`). A plain (non-data) class compares by identity, so `reduce` returning `this` suppresses the emission and any new instance emits. Correct, and the reason to avoid the DTO `equals`/`ByteArray` hole is sound (05 §3.2). CONFIRMED.

Cost: each emission resumes **every** collector of `state` (`findSlotsToResumeLocked`); each `map { it.chats[id] }.distinctUntilChanged { a, b -> a === b }` selector does one HAMT lookup and one identity compare per emission. With S selectors (one per visible row that observes its chat, plus list, plus users) and U updates/s during the burst, that is S×U resumptions on Default, all competing with the lanes (1.7). StateFlow conflation helps only when a collector is slower than the writer; a fast collector sees every value. PLAUSIBLE; magnitude depends on N12, which the draft defers. Mitigation that keeps the design: selectors `conflate()`/`sample()` on the reader side, and **one** `entries(list)` selector per screen rather than per-row selectors (D §5 does this for the list; D §4.3's `chat(id)` must not be used per row).

### 2.3 MINOR — persistent-map GC at the measured sizes

`PersistentHashMap.put` copies the path from root to leaf of a 32-way HAMT: ~`log32(n)` nodes (2–3 for 10k entries), each node an array of up to 64 references [I; kotlinx.collections.immutable `TrieNode`, not re-read this session]. Per update: ~3 small arrays + one `AccountState` (13 fields) + the changed DTO. For 5k updates in a burst that is a few MB of short-lived garbage — fine on JVM/ART, acceptable on Kotlin/Native's GC. The draft's "O(log n) copy" is right. Where it bites: `chatLists: PersistentMap<ChatList, ChatListOrder>` and `ChatListOrder` holding `chatId → order` — every `updateChatPosition` creates a new `ChatListOrder` **and** a new `chatLists` map; fine. PLAUSIBLE, low.

### 2.4 SERIOUS — "sorts lazily, once per instance that someone reads" is safe but not cheap

`by lazy` defaults to `SYNCHRONIZED` on JVM and on Kotlin/Native (new memory model) [I; stdlib `lazy()` contract], so a cached sort on an immutable value is thread-safe on both. The cost is the one the draft underestimates: during a burst each `updateChatPosition` yields a new `ChatListOrder`; a reader conflated to ~60 Hz still triggers up to 60 full sorts/s of N entries (`O(N log N)`, N = loaded chats, allocates a `List<Long>` each) on Default, while the lane keeps producing. With N≈2k that is ~2k×11×60 ≈ 1.3M comparisons/s: fine. With `loadChats` paging the whole list (10k+) it is noticeable but still bounded by reader rate. The real risk is **two readers** (list screen + folder badge + notification controller) each paying the sort on different instances; make `ChatListOrder` hold the sorted list incrementally (a persistent sorted set keyed by `(order, chatId)`, updated in `reduce`, O(log N) per update) instead of a lazy full sort. PLAUSIBLE.

### 2.5 MINOR — `store.state.first { id in it.chats }` can hang

D §4.3: "TDLib guarantees the update exists (CS§1.2), so this can't hang on a live instance." It hangs when: the update was dropped by the library's decode failure (`TdReceiver.kt:83`, acknowledged in D §12); or the instance restarts between `send` and the wait (`reduce(Closed) = Empty`, D §4.2) and the chat is not re-sent until something references it. Needs a timeout or `authorizationState`-aware cancellation. CONFIRMED (two concrete paths).

### 2.6 MINOR — `AccountState.options: PersistentMap<String, OptionValue>` and `connectionState` in the snapshot

`updateOption` arrives at a high rate at startup and `updateConnectionState` flaps on mobile; both are in the snapshot, so every selector wakes for them (2.2). Keep them, but this is another argument for reader-side `conflate()`.

---

## 3. D §5 — chat list, row models, memoisation, Orbit

### 3.1 CONFIRMED mechanics that work

- Orbit `reduce` is `internalStateFlow.update(reducer)` on a `MutableStateFlow` (RealContainer.kt); `collectAsState()` is `container.externalRefCountStateFlow.collectAsStateWithLifecycle(minActiveState = STARTED)` (orbit-compose `ContainerHostExtensions.kt`). `externalRefCountStateFlow` is a `MappedStateFlow` that suppresses repeats with `value != previous`. So an Orbit `ChatListScreenState(rows: List<ChatListRow>)` data class with rows as **plain classes** compares `rows` by `List.equals` → element identity → emits only when some row instance changed. Correct for the memoisation story.
- `repeatOnSubscription` cancels the block "after a short delay" when subscribers reach zero and reruns it when they return (`Syntax.kt` KDoc). On restart the mapper recomputes against the memo cache held in the ViewModel → same row instances → `reduce { copy(rows = same) }` → no emission. Correct.
- Strong skipping compares unstable parameters by `===` and memoises lambdas keyed by captures (Android docs, quoted in 05 §3.4). `ChatRow(row)` with a reused `row` skips; `onClick = { onChatClick(row.chatId) }` is memoised on `row`. Correct.

### 3.2 SERIOUS — the memo cache keyed by input identity leaks and is wrong after `Closed`

"Reuses the previous row object when its inputs are identical" needs a map `inputs → row`. Keyed by the previous emission (compare row i's inputs with the previous row i's inputs by `===`) it is O(N) per emission and needs stable positions — positions change on every `updateChatPosition`, so the comparison must be by `chatId`, i.e. a `Map<Long, (inputs, row)>`. That map grows with every chat ever shown unless pruned to the current `entries`, and after `reduce(Closed) = Empty` followed by a re-login of a **different** user it holds rows of another account's chats under the same chat ids (private chat ids are user ids and collide across accounts only by accident; basic-group/supergroup ids do not — but the *same* account re-logging reuses ids with stale senders). Prune to the current id set on every emission and clear on `Closed`. PLAUSIBLE, cheap to fix, not in the draft.

### 3.3 SERIOUS — the mapper "on Dispatchers.Default" inside an Orbit intent

Orbit intents run on `settings.eventLoopDispatcher()` and are launched one coroutine each from an `UNLIMITED` channel (RealContainer.kt): "intents are dequeued in send order, but … the code doesn't guarantee their execution order beyond that start order". A mapper that runs inside `repeatOnSubscription { entries.collect { reduce { … } } }` is one long-lived intent, so ordering inside it is sequential — fine. But if the design also issues `loadChats`/paging as separate intents that `reduce` paging flags, those run concurrently with the mapping intent; two `reduce` calls interleave arbitrarily. `reduce` is atomic (`update` CAS loop) so state is not corrupted, but a paging flag computed from a stale `rows` can be. Use `subIntent`/single intent or keep paging flags derived from the entries flow itself. PLAUSIBLE.

### 3.4 MINOR — previews and `key`

Plain-class rows need a `PreviewParameterProvider` that constructs them by hand (no `copy`); fine. `key = { it.chatId }` is a `Long`, stable. `contentType` by row kind, fine. `equals` on a plain class is identity, so `LazyColumn` cannot detect a "same content, new instance" row — which is exactly the case the memo must prevent (3.2).

---

## 4. D §6 — `ChatHistory` owned by a ViewModel

### 4.1 SERIOUS — module rule: presentation cannot own a `feature:chats:data` class

D §2 puts `ChatHistory` in `feature:chats:data` with owner `ViewModel` (presentation). `architecture.md` §1.1: `presentation` "never sees `data`". So `ChatHistory` needs an interface in `feature:chats:domain` plus a factory the ViewModel can call, and `HistoryWindow.messages: List<Message>` exposes the DTO to presentation — allowed only under D1-A. With D1-B/C the window must carry the mirror type; the draft's "works either way" is not true for this class. CONFIRMED (rule text).

### 4.2 SERIOUS — Koin: `@Factory` with a runtime `chatId`

A `ChatHistory(chatId: Long, holder: ClientHolder, store: AccountStore)` needs `@Factory` + `@InjectedParam chatId` in the account scope; "Compile safety validation skips `@InjectedParam` parameters" (koin docs `annotations-inventory.md:476`), and the ViewModel resolves it with `scope.get<ChatHistory> { parametersOf(chatId) }` — a lambda body, invisible to the check (architecture §2.5 rule 3). Alternatively a plain constructor from a `@Scoped ChatHistoryFactory` (an annotated class with `fun create(chatId)`) keeps the graph checkable: the factory's dependencies are validated, `create` is ordinary code. Prefer the factory class. The ViewModel itself is `@KoinViewModel @Scope(AccountId::class)` with `@InjectedParam chatId` — same skip; acceptable, that's today's pattern. PLAUSIBLE (plugin behaviour from docs, not from a build).

### 4.3 SERIOUS — lifetime and the lane

The `ChatViewModel` lives in the **nav entry's** `ViewModelStore`, created by `SharedViewModelStoreNavEntryDecorator` with the account owner as parent (`K/core/navigation/api/.../SharedViewModelStoreNavEntryDecorator.kt:60-66, 78`) and cleared on pop (`:78 onPop = clearKey`). So `ChatHistory`'s lane must be `viewModelScope + Dispatchers.Default.limitedParallelism(1)` and dies in `onCleared`. Two consequences the draft doesn't state: (a) `closeChat` must run in `onCleared` with `NonCancellable`/a scope that outlives the ViewModel (D §6 "survives cancellation (B26)" says so but gives no owner — `OpenChats` in the account scope is the right owner and must take a plain `fun close(chatId)` that launches on *its* scope); (b) on an **account switch** the whole account `ViewModelStore` is cleared (`AccountViewModelStoreOwner.kt:24`), so every open chat's history is torn down; on switch-back, `requestFirstPage` again — fine, but `presentationKey` re-keying state is lost, so a message that was pending across the switch shows a double bubble once. MINOR within SERIOUS.

### 4.4 MINOR — `store.updates.onSubscription { requestFirstPage() }`

Same `coroutineScope`/`launch` question as §1: `requestFirstPage` is a `send` + merge; running it inline in the action blocks the collector until the answer, so updates during the page load **buffer** (if 2.1 is fixed) rather than being "recorded and replayed" as D §6 rule 2 wants — the two mechanisms are redundant; pick one. If run via `launch` on the history lane, the lane serializes it with the collector anyway. Fine once 2.1 is fixed.

---

## 5. D §7 — files, `subscriptionCount`, Coil

### 5.1 SERIOUS — releasing a per-file `StateFlow` on `subscriptionCount == 0` races with a new subscriber

`MutableSharedFlow.subscriptionCount` is a `StateFlow<Int>` that is "not conflated" (`flow/SharedFlow.kt:234-240`) and exists on all targets (common code). The race is structural: the release decision (count 0 after debounce) runs on the lane or a timer; a new `observe(file)` on another thread looks up the registry, finds the flow, subscribes — the release then removes it from the registry and stops forwarding `updateFile` to it; the subscriber holds a flow that never updates. PLAUSIBLE (classic registry/refcount race; the design gives no lock). Fix: `stateIn(scope, SharingStarted.WhileSubscribed(stopTimeoutMillis), initial)` per file, created under a mutex, with the lane writing into a per-file `MutableStateFlow` that is dropped from the registry **under the same mutex** only after `WhileSubscribed` stopped and `subscriptionCount.value == 0` re-checked.

### 5.2 SERIOUS — `TdImageFetcher` in `core:designsystem` needs `core:tdlib`, and leaks DTOs into composables

`core:designsystem` depends only on Compose (`K/core/designsystem/build.gradle.kts:30-34`). A fetcher for `TdImage(accountId, file: io.xbot.tdlib.api.File)` that calls the account's `FileDownloads` must resolve the account scope: `Koin.accountScope(id)` is public in `core:tdlib` (`AccountDirectories.kt:19`). So `core:designsystem → core:tdlib` (allowed by the `core:*` rule, no cycle: tdlib → domain only). But `TdImage` in designsystem's **public** API carries the DTO, and composables (`Avatar(file)`) then see TDLib types — D §13 D1-A says "composables still don't". Contradiction. Either `TdImage` carries only `(accountId, fileId, uniqueId, minithumbnail bytes)` primitives (then the fetcher needs `GetFile` to re-hydrate — one extra TDLib call per image, cached), or accept DTOs in composables and drop that sentence. CONFIRMED (dependency facts), the choice is the developer's.

A process-wide `ImageLoader` resolving a Koin scope by `accountId` must handle a **closed** scope (image of a logged-out account still in a composition) → fetch failure, not a crash. `getScopeOrNull` returns null; the fetcher must return an error result. MINOR.

### 5.3 MINOR — key `remote.unique_id` across accounts

Correct per TDLib ("the same for the same file even for different users"), but `unique_id` "may be empty if unknown" (td_api.tl `remoteFile`); the Keyer must fall back to `(accountId, file.id)` for an empty unique id or two different empty-id files collide in the memory cache. PLAUSIBLE.

---

## 6. D §2 — module table, `ClientHolder.updates` removal

- **Removing `ClientHolder.updates` is safe for auth.** `DefaultAuthRepository` uses `authorizationState` and `send` only (`K/feature/auth/data/.../DefaultAuthRepository.kt:48-60`); `DefaultAccountRepository` likewise (`:179-187`); `ClientHolderTest` the same (`:40-57`). The only reference is the definition (`grep` over the tree). CONFIRMED.
- **Store as constructor parameter of the holder** (`DefaultClientHolder(…, store: AccountStore)`): same module, annotated class, fine for compile safety (architecture §2.5 rule 2). But `TestKoin.kt:21` constructs `DefaultClientHolder(checkNotNull(getOrNull<AccountId>()), get(), this)` by hand — M0 must change the test module or `ClientHolderTest` won't compile. MINOR, CONFIRMED.
- **Cycle check**: `core:tdlib → core:domain`; `core:designsystem → core:tdlib` (new, §5.2) → no cycle. `feature:chats:data → core:tdlib, feature:chats:domain`; `feature:chats:presentation → feature:chats:domain, core:designsystem, core:navigation:api` → no cycle. But under D1-A `core:domain` interfaces return DTOs → `core:domain` must `api(libs.tdlib.client)` → **every** presentation module gets the client artifact on its compile classpath; the detekt `ForbiddenImport` on `io.xbot.tdlib.client.*` does not forbid `io.xbot.tdlib.api.*`, so "composables don't see DTOs" is convention only. MINOR, CONFIRMED (dependency graph).
- `AppPresence`/`NetworkMonitor` as "`expect` function" in `core:tdlib` with a `@Singleton class` wrapper follows rule 1/2 of §2.5. Fine. `TdLib` in the Koin root: `NativeTdLib` is a library `object`; binding the interface needs `@Singleton class KotogramTdLib : TdLib by NativeTdLib` (a provider `fun` would hit rule 2). MINOR, not in M0.
- `ChatHistory` row in the table: module `feature:chats:data`, owner ViewModel → rule violation (§4.1).

---

## 7. D §14 M0 and D §13 D1-A

### 7.1 M0 coverage

The three compile breaks (05 §1.3) are listed. Missing from M0:

1. `TestKoin.kt:21` manual construction (above).
2. `TdlibLog.install()` becomes `suspend` — fine at the call site, but `TdlibLog` is an `object` with `AtomicBoolean` guard; two accounts starting concurrently both call `install()` — the guard still works. No change needed; just noting M0's "ClientHolderTest passes" requires the suspend signature.
3. The `TdLib` interface binding in Koin (above) if `TdClient.create(tdLib)` is used as D §3 writes it.
4. `AuthErrorMapper.request` catching `SerializationException` is in D §10 but not in M0; without it the first undecodable answer crashes the login flow. MINOR.
5. The `ClientHolder.kt` KDoc and the three stale doc statements (05 §7) — listed.

### 7.2 D1-A and the iOS framework

`shared` builds `SharedUI` as a static framework with **no `export(...)`** (`K/shared/build.gradle.kts:24-28`), although it declares `api(projects.core.domain)` and `api(projects.core.tdlib)` (`:37-38`). Kotlin docs: "You can export only api dependencies", and without `export` only the module's own declarations are in the framework API; "When you export a dependency, it includes all of its API to the framework API … This disables dead code elimination for the exported dependency". So: **no DTO reaches the Swift header today or under D1-A** unless someone adds `export(projects.core.tdlib)`. CONFIRMED (build file + docs). The ~3,500 classes are nevertheless **in the binary already**, D1-A or not: the sealed `@Serializable` hierarchies reference every subclass serializer, so Kotlin/Native DCE cannot drop them [I]. Binary size is a tdlib-kmp cost, not a D1-A cost. `api(projects.core.tdlib)` in `shared` is unnecessary for Swift (nothing is exported) and only widens the Kotlin compile classpath of `shared`; MINOR.

The real D1-A rule change is in Kotlin, not Swift: `core:domain` and every `feature:*:domain` gain the DTO dependency, and `architecture.md` §1.1's "`feature:x:domain` must not contain TDLib types" and "`feature:x:presentation` must not contain TDLib types" both fall, not only the first as D §13 says. CONFIRMED (table text).

---

## 8. Summary table

| # | Section | Severity | Status | One line |
|---|---|---|---|---|
| 1.2 | §3 | blocker | CONFIRMED | `coroutineScope` waits for the `setUp` child; a connectivity collector keeps it alive; no restart after `Closed` |
| 1.3 | §3 | blocker | CONFIRMED | a failing `setUp` or `reduce` cancels the collector, leaks the instance, and the loop re-creates instances on a locked directory |
| 2.1 | §4.1/§6 | blocker | CONFIRMED | `updates.tryEmit` on a default `MutableSharedFlow` drops when the subscriber is not parked; buffer size unspecified |
| 1.5 | §3/§8 | serious | CONFIRMED order / PLAUSIBLE effect | `close()` order unspecified; loop restarts an instance during scope close and recreates deleted directories |
| 2.2 | §4.3 | serious | PLAUSIBLE | every `state` emission resumes every selector; per-row `chat(id)` selectors × burst rate |
| 2.4 | §4.3 | serious | PLAUSIBLE | lazy full sort per read instance; multiple readers each pay it; incremental sorted set instead |
| 3.2 | §5 | serious | PLAUSIBLE | memo cache by `chatId` grows unbounded and survives `Closed` |
| 3.3 | §5 | serious | PLAUSIBLE | paging flags reduced from separate intents race the mapping intent |
| 4.1 | §6/§2 | serious | CONFIRMED | `ChatHistory` in `data` owned by presentation violates §1.1; `HistoryWindow` exposes DTOs |
| 4.2 | §6 | serious | PLAUSIBLE | `@InjectedParam chatId` skips compile safety; use a `@Scoped` factory class |
| 4.3 | §6 | serious | CONFIRMED | lane = `viewModelScope`; `closeChat` needs an owner outliving the ViewModel; account switch clears all histories |
| 5.1 | §7 | serious | PLAUSIBLE | refcount release races a new subscriber; use `WhileSubscribed` + mutex |
| 5.2 | §7/§2 | serious | CONFIRMED | fetcher in `core:designsystem` needs `core:tdlib` and puts a DTO in a composable parameter |
| 1.4 | §3/§8 | serious | PLAUSIBLE | `setUp` shares the lane with `apply`; one stalls the other |
| 1.6 | §3 | minor | PLAUSIBLE | `TdClient.create` failure kills the lane silently |
| 1.7 | §3 | minor | PLAUSIBLE | N lanes saturate `Dispatchers.Default` on a 2-core device during cold start |
| 2.3 | §4.1 | minor | PLAUSIBLE | HAMT path-copy garbage is small; fine |
| 2.5 | §4.3 | minor | CONFIRMED | `first { id in it.chats }` hangs after a dropped decode or a restart |
| 2.6 | §4.1 | minor | PLAUSIBLE | `options`/`connectionState` in the snapshot wake selectors |
| 3.4 | §5 | minor | CONFIRMED | plain-class rows: identity equals; previews by hand |
| 4.4 | §6 | minor | CONFIRMED | `onSubscription { requestFirstPage() }` duplicates the record/replay rule |
| 5.3 | §7 | minor | PLAUSIBLE | empty `unique_id` collides in the Coil key |
| 6 | §2 | minor | CONFIRMED | `TestKoin.kt:21` breaks when the holder takes the store; `TdLib` Koin binding needs a class; D1-A puts the client artifact on every presentation classpath |
| 7.2 | §13 | minor | CONFIRMED | no `export` in the framework: D1-A adds nothing to the Swift header; the DTOs are already in the binary; `architecture.md` §1.1 changes in two rows, not one |

Not found: any problem with `onSubscription` itself (it does what the draft says), with `StateFlow` identity dedupe on a plain class, with Orbit `collectAsState` and strong skipping over reused rows, or with removing `ClientHolder.updates`.
