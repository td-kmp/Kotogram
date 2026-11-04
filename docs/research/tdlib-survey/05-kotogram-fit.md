# 05 — Kotogram fit: from a TDLib update to a pixel, given tdlib-kmp 1.8.69-1.0.0-alpha02

Date: 2026-11-03. Scope: Kotogram at `feature/KTG-1-login` (working tree, version catalog already bumped) against the published `io.github.xephosbot:tdlib-kmp-client:1.8.69-1.0.0-alpha02`. Everything comes from reading code; no build or test was run.

Paths: `K/` = `/Users/xephosbot/AndroidStudioProjects/Kotogram`, `NEW/` = `scratchpad/tdlib-kmp-sources-1.8.69-1.0.0-alpha02`, `OLD/` = `scratchpad/tdlib-kmp-sources-1.8.68-1.0.0-alpha02`, `koin/` = `~/AndroidStudioProjects/koin` (clone at `4.2.0-alpha2-155-g664b7994`; the two files that matter were re-read at tag `4.2.2` on GitHub). Scripts: `scratchpad/scripts/fit/{parse_api.py,diff_api.py,closure.py}`, outputs `api_new.json`, `api_old.json`, `diff_all.txt`.

Legend: [V] read in source, [I] inferred, [U] unverified.

---

## 0. Findings in one screen

1. **Three compile breaks, all in `core:tdlib`** (§1): `TdClient.create()` needs `import io.xbot.tdlib.client.create`; `TdClient.execute(...)` (static, non-suspend) no longer exists; `TdLib` is an interface now, so `TdLib.setLogMessageHandler` must become `NativeTdLib.setLogMessageHandler`. The 98 generated-API symbols Kotogram imports are byte-for-byte identical between the two versions [V, script].
2. **One silent break that matters most**: `ClientHolder.updates = instances.flatMapConcat { it.updates }` (`K/core/tdlib/.../DefaultClientHolder.kt:58`) never advances past the first instance, because `TdClient.updates` is a `SharedFlow` that never completes [V]. After the first QR→phone restart, `ClientHolder.updates` is dead. Nothing collects it yet, so nothing visible broke.
3. **Early updates are lost by construction** (§2): `MutableSharedFlow(replay = 0)` drops emissions with zero subscribers [V, kotlinx docs]; `DefaultClientHolder.start()` sends `SetTdlibParameters` from a separate `launch` with no subscriber on `client.updates` [V]. Any store that later collects `ClientHolder.updates` misses the whole startup burst (options, `updateNewChat`/`updateUser` from the database). The subscription must happen inside `start()`, `CoroutineStart.UNDISPATCHED`, before `send`.
4. **Decode failures are invisible**: `TdReceiver.dispatch` drops an update whose JSON doesn't decode (`value.getOrNull() as? Update ?: return`, `NEW/.../TdReceiver.kt:83`) and a failed *answer* surfaces as a `SerializationException`, which Kotogram's `request { }` (catches only `TdException`) does not handle [V]. The native TDLib and the Kotlin API ship in one artifact, so schema skew is unlikely but the failure mode is silent. Library finding, §2.6.
5. **A 1:1 domain mirror is 3–4k lines per milestone, plus the same again in mappers** (§3). Even a chat-list-only projection pulls ~540 types if sealed hierarchies are followed, because `Chat → lastMessage: Message → content: MessageContent` (108 subtypes) and `Chat → draftMessage → InputMessageContent` (320 types) are hubs. The mirror also doesn't buy Compose stability: `core:domain` isn't compiled with the Compose compiler either.
6. **`getAll<UpdateHandler>()` gives no order**: Koin's `InstanceRegistry._instances` is a `ConcurrentHashMap` on JVM and a `ConcurrentMutableMap` on native; `getAll` iterates it unsorted (the sort is commented out) [V, 4.2.2]. Handler order must be fixed in code, not collected from DI.
7. **Recommendation** (§6): one single-writer reducer per account inside `core:tdlib`, owning the one ordered update channel the holder fills from inside `start()`, writing immutable snapshots into typed stores; feature `data` modules read the stores and map lazily. Weakest point: the reducer is a single serialization point; a slow handler stalls everything, and all account state converges on one class.

---

## 1. Delta from the bump

### 1.1 Client API, old → published

| Member | OLD (`OLD/client/.../TdClient.kt`, `TdClientImpl.kt`) | NEW (`NEW/client/.../TdClient.kt`, `TdClient.nonWeb.kt`) |
|---|---|---|
| Type | `public interface TdClient` (:28) with `internal class TdClientImpl` | `public class TdClient private constructor(id, receiver)` (:31) — final class |
| `create` | `TdClient.create(parent: Job? = null)` companion member (:50) | `TdClient.create(tdLib: TdLib)` companion member (:64); no-arg `TdClient.Companion.create()` is a **top-level extension** in `nonWebMain` (`TdClient.nonWeb.kt:6`), package `io.xbot.tdlib.client` |
| `execute` | `TdClient.execute(fn)` companion, non-suspend (:53) | `suspend fun execute(fn)` **instance** method (:44); no companion `execute` |
| `updates` | `Flow<Update>`, completes after Closed via `transformWhile` (`TdClientImpl.kt:26-31`) | `SharedFlow<Update>` from `MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)` (:34-35), **never completes** |
| `isClosed` | `val isClosed: Boolean` (:37) | removed |
| `parent` | closes the instance when the parent Job completes (`TdClientImpl.kt:55-57`) | removed |
| `updatesOf` | top-level extension (:63) | inline member (:47) |
| `TdLib` | `object TdLib` with non-suspend `receive/execute` (`OLD/core/.../TdLib.kt:3-12`) | `interface TdLib` with `suspend receive/execute` (`NEW/core/.../TdLib.kt:14-34`); `object NativeTdLib : TdLib` (`NativeTdLib.kt:10`) |
| Receive loop | `newSingleThreadContext("tdlib-receiver")`, loop ends when `clients.isEmpty() && pending.isEmpty()` (`OLD/.../TdReceiver.kt:64-79`) | `CoroutineScope(Dispatchers.Default).launch`, each `receive` hops to `Dispatchers.IO.limitedParallelism(1, "tdlib-receiver")` (`NEW/.../TdReceiver.kt:58-61`, `NativeTdLib.kt:12,19`); loop ends when `clients.isEmpty()` only (:64) |
| Web failure path | — | `fail()` completes pending exceptionally and synthesises `Closed` for orphans (:92-101); `TdLibUnavailableException` is thrown only by the web `TdLib` [I from KDoc `TdLib.kt:25`] |

### 1.2 Generated API

`parse_api.py` parsed 3,538 declarations in OLD and 3,583 in NEW. `diff_api.py` over the 98 symbols Kotogram imports (`scripts/fit/imported_symbols.txt`): **98 same, 0 changed** [V]. Whole-API diff: 46 added, 1 removed (`StoryAreaPosition` → `AreaPosition`), 23 changed; none of the changed ones (`Gift`, `UserFullInfo`, `SupergroupFullInfo`, `TextQuote`, `InternalLinkType*`, `GetUserLink`, …) is used by Kotogram (`scripts/fit/diff_all.txt`) [V].

Side observation: `required()` in `K/core/tdlib/.../mapper/FormattedTextMapper.kt:128-129` ("Generated TDLib classes make nested objects nullable with a default") is already dead code in OLD: `AuthorizationStateWaitCode.codeInfo: AuthenticationCodeInfo` is non-null in both versions; the generator makes a field nullable only where td_api.tl says "may be null" (`NEW/.../AuthorizationState.kt:77-81`, `Message.kt:62-64`) [V]. Not a break; a cleanup.

### 1.3 Break table (nothing applied)

| File:line | What | Breaks? | Minimal fix |
|---|---|---|---|
| `K/core/tdlib/src/commonMain/kotlin/com/xbot/kotogram/core/tdlib/DefaultClientHolder.kt:81` | `TdClient.create()` | **Compile**: unresolved; the no-arg form is an extension in another package | `import io.xbot.tdlib.client.create` (or `TdClient.create(NativeTdLib)`). [I] The `nonWebMain` extension is visible from Kotogram's `commonMain` because Kotogram's targets (android, iosArm64, iosSimulatorArm64, jvm) are a subset of nonWeb; confirm with a compile |
| `TdlibLog.kt:24-25` | `TdClient.execute(SetLogStream(...))`, `TdClient.execute(SetLogVerbosityLevel(...))` | **Compile**: no companion `execute` | `TdlibLog.install()` becomes `suspend`; call `parseTdObject(checkNotNull(NativeTdLib.execute(fn.toTdJson())))` (`NEW/.../TdJson.kt:29-31`, `NativeTdLib.kt:21`). The only call site, `DefaultClientHolder.kt:80`, is already in a `suspend` function |
| `TdlibLog.kt:4,28` | `import io.xbot.tdlib.TdLib`; `TdLib.setLogMessageHandler(...)` | **Compile**: `TdLib` is an interface | `import io.xbot.tdlib.NativeTdLib`; `NativeTdLib.setLogMessageHandler(...)` (`NativeTdLib.kt:23`) |
| `DefaultClientHolder.kt:58` | `instances.flatMapConcat { it.updates }` | **Silent**: `flattenConcat` collects inner flows "sequentially, without interleaving" [V kotlinx docs]; the first `updates` never completes, so the second instance is never collected | Replace with a holder-owned forwarding collector per instance, started before the first `send` (§2.3). Not a one-liner |
| `DefaultClientHolder.kt:84` | `scope.launch { runCatching { client.send(parameters()) } }` with no `updates` subscriber | **Silent**: the startup burst is emitted to zero subscribers and dropped (§2.1) | Subscribe `UNDISPATCHED` before the send (§2.3) |
| `ClientHolder.kt:19-23` KDoc, `docs/design/sessions.md` §1 table ("`updates` continuous; a restart shows up as Closed, then the new instance's states"), `docs/research/tdlib-clients.md` §2 ("completes on Closed") | contract text | **Docs out of date** | Rewrite after §2.3 is implemented |
| `DefaultClientHolder.kt:89-95` `close()` | `runBlocking { withTimeoutOrNull(3s) { client.close() } }` | compiles; behavior same (`NEW/.../TdClient.kt:50-55` sends `Close`, awaits `Closed`) | none; see §2.7 for the receive-loop tail |
| `DefaultAccountRepository.kt`, `DefaultAuthRepository.kt`, `DefaultCountryRepository.kt`, `DefaultPasskeyRepository.kt`, mappers, `ClientHolderTest.kt` | only `io.xbot.tdlib.api.*` DTOs and `TdException` | none [V diff] | — |
| `isClosed`, `create(parent)`, top-level `updatesOf` | not used anywhere in Kotogram (`grep`) | none | — |

Silent behavior changes with no code impact today: dispatch now runs on `Dispatchers.Default` worker threads with two hops per event (Default → IO slot for `td_receive` → Default) instead of one dedicated thread [V]; the loop no longer waits for pending answers before stopping, so an answer arriving after the last client's `Closed` is delivered only when the next client registers and restarts the loop [I; TDLib queues it].

---

## 2. Mechanics of the published `TdClient`, and what the app must do

### 2.1 What a replay-0 `SharedFlow` loses

`MutableSharedFlow<Update>(extraBufferCapacity = Int.MAX_VALUE)` (`NEW/.../TdClient.kt:34`) has `replay = 0`. kotlinx docs: "In the absence of subscribers only the most recent replay values are stored and the buffer overflow behavior is never triggered and has no effect" [V, https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-mutable-shared-flow.html]. With replay 0 that is *nothing*: `tryEmit` returns `true` and the value is gone. The buffer only exists for subscribers that are slower than the loop.

The instance "sends nothing until its first send" (`NEW/core/.../TdLib.kt:16` KDoc; TDLib's own contract). `TdClient.create()` only registers the client and starts the loop (`TdReceiver.kt:37-43`). So every update between the first `tdLib.send` and the first `collect` on `client.updates` is lost: `updateAuthorizationState(WaitTdlibParameters)`, the `updateOption` burst (`version`, `commit_hash`, limits…), and — on an existing database right after `SetTdlibParameters` — the database replay of `updateNewChat`, `updateUser`, `updateSupergroup`, `updateBasicGroup`, `updateChatLastMessage`, `updateChatPosition`, `updateUnreadChatCount`, … and `updateAuthorizationState(Ready)`. Only `authorizationState` is safe: `onUpdate` sets the `StateFlow` before `tryEmit` (`TdClient.kt:57-60`), and a `StateFlow` always holds its last value.

Kotogram today: `start()` emits the client into `instances`, then `scope.launch { client.send(parameters()) }` (`DefaultClientHolder.kt:82-84`). A collector of `instances.flatMapConcat { it.updates }` runs on another coroutine; nothing orders its `collect` before the launched `send`. Both race on `Dispatchers.Default`. Even if a store existed, it would be lossy.

### 2.2 How to guarantee the subscription

A `SharedFlow` subscriber is registered synchronously when `collect` starts, before it suspends (SharedFlowImpl allocates the slot at the start of `collect`). `launch(start = CoroutineStart.UNDISPATCHED) { client.updates.collect { … } }` runs the body "immediately … until its first suspension point" [V kotlinx `CoroutineStart.UNDISPATCHED` KDoc] — that first suspension is inside `collect`, after registration. The old library's KDoc recommended exactly this (`OLD/.../TdClient.kt:21-23`); the new KDoc keeps the rule ("subscribe to updates before that", `NEW/.../TdClient.kt:25-26`). So, inside `start()`, in this order and in the same coroutine:

```kotlin
val client = TdClient.create()
val forward = scope.launch(start = CoroutineStart.UNDISPATCHED) {
    client.updates
        .transformWhile { emit(it); !(it is UpdateAuthorizationState && it.authorizationState is AuthorizationStateClosed) }
        .collect(channel::send)          // channel = Channel<Update>(UNLIMITED), one per holder
}
instances.emit(client)
client.send(parameters())                // first send; everything from here on is captured
```

### 2.3 Replacing `flatMapConcat`, with ordering across a restart

Own design, grounded in the two facts above and in `ClientHolder`'s existing contract (`ClientHolder.kt:19-23`):

- The holder owns **one `Channel<Update>(UNLIMITED)`** for the account's life and exposes `updates = channel.receiveAsFlow()` (single consumer, documented). The channel buffers until the reducer attaches, so the reducer may be constructed after the holder (Koin resolves scoped beans lazily) without loss. Memory is bounded by "startup burst until the reducer attaches", i.e. milliseconds in practice [I].
- Per instance, **one forwarding collector** (above) that ends on that instance's `Closed`. The holder waits for the forwarding job (`forward.join()`), not for `authorizationState.first { Closed }`, before starting the next instance. Otherwise the next instance's early updates could interleave before the previous instance's `Closed` reached the channel.
- Result order in the channel: `[instance N updates…, Closed(N), instance N+1 updates…]` — the contract the KDoc promises, and the point where caches reset.
- `authorizationState` stays `instances.flatMapLatest { it.authorizationState.filterNotNull() }` (`DefaultClientHolder.kt:54-56`), unchanged.

Alternative considered: `flatMapLatest { it.updates }` — fixes the "stuck" problem but drops the per-instance subscription timing and can lose the `Closed` boundary when the switch happens; rejected.

### 2.4 Following an event through `TdReceiver`

1. Loop coroutine on `Dispatchers.Default` (`TdReceiver.kt:58`). `tdLib.receive(1.seconds)` → `withContext(Dispatchers.IO.limitedParallelism(1, "tdlib-receiver")) { NativeBridge.receive(1.0) }` (`NativeTdLib.kt:12,18-19`): the blocking `td_receive` runs on one IO slot, not on a Default worker — the task statement's "blocking receive inside Dispatchers.Default" is not quite what the code does; the *decode and dispatch* run on Default.
2. `runCatching { dispatch(event) }` (:63): any exception from dispatch is swallowed, including a malformed-JSON `parseToJsonElement` failure.
3. `dispatch`: `TdJson.parseToJsonElement(event).jsonObject` (:75) builds the full tree; `runCatching { TdJson.decodeFromJsonElement(TdObject.serializer(), json) }` (:77) is the second pass, polymorphic over the sealed `TdObject` by `@type` (`TdJson.kt:14`). Two allocations of the event's size per event.
4. Mutex: `mutex.withLock` once for answers (`pending.remove`, :79), once for updates (`clients[clientId]`, :85), and again on `Closed` (:88). Uncontended except against `request()` (:47).
5. Answers: decode failure → `answer.completeExceptionally(SerializationException)` (:80) → `send` throws a **non-`TdException`**. Kotogram's `Raise<AuthError>.request { }` catches `TdException` only (`K/feature/auth/data/.../AuthErrorMapper.kt:15-16`); a decode failure of an answer propagates as a crash in `either { }`.
6. Updates: `value.getOrNull() as? Update ?: return` (:83) — silently dropped; then `client.onUpdate(update)` (:86), and `clients.remove` after `Closed` (:87-89).

`TdJson` has `ignoreUnknownKeys = true` (`TdJson.kt:15`), so a *new field* is harmless; an *unknown `@type`* anywhere in the tree (a new `MessageContent` subtype inside `updateNewMessage`, `updateChatLastMessage`, or inside a `Chat` answer) fails the whole update or answer.

### 2.5 Consequence for caches of one dropped update, and how to survive it

TDLib's rule: "all updates and responses to requests must be applied in the order they were received for consistency" (`scratchpad/clones/tdlib__td/td/telegram/td_json_client.h:31`). TDLib also sends `updateChatLastMessage` *instead of* `updateChatPosition` ("An updateChatLastMessage or updateChatDraftMessage update might be sent instead of the update", `td_api.tl:11316`) and guarantees `updateNewChat`/`updateUser` come "before the chat/user identifier is returned to the application" (`td_api.tl:11288, 11555`). So one dropped `updateChatLastMessage` (because its `lastMessage.content` has an unknown `@type`) leaves the chat's **preview and its positions** stale: the chat list order is wrong until the next update for that chat. A dropped `updateNewChat` leaves later `updateChatPosition`/`updateChatReadInbox` referencing a chat the store doesn't have.

Schema skew is improbable here: the native `td_json` and the generated API come from one artifact version, both from td_api.tl 1.8.69 [V: artifact coordinates; generator header `NEW/.../TdObject.kt:1`]. It can still happen if the generator omits a constructor, or if native and Kotlin artifacts are ever mixed.

What the app can do without touching the library:
- **Startup guard**: `getOption("version")` (Kotogram already calls `GetOption("my_id")`, `DefaultAccountRepository.kt:187`) and compare with the library's TDLib version constant; refuse to run on mismatch. Own design.
- **Referential repair in the reducer**: when an update references a chat/user/supergroup the store lacks, request it (`getChat`, `getUser`, `getSupergroup`) — TDLib answers from its database, and the `updateNewChat`/`updateUser` guarantee means the entity exists. The reducer applies the answer as if it were an update. Telegram X does similar null checks [U — other agents].
- **Map `SerializationException` in repositories** to a domain error next to `TdException` (`AuthErrorMapper.kt:15` pattern), and log it.
- **Prefer `MessageUnsupported`-style degradation** in UI models: anything the mapper doesn't recognise renders as "unsupported", never crashes.

### 2.6 Library finding (report separately, don't change now)

`TdReceiver` has no hook for a decode failure: the update is dropped at `TdReceiver.kt:83` with no log, no counter, no synthetic `UpdateUnknown(json)`. The app cannot detect schema skew from the stream. A `decodeFailures: SharedFlow<String>` or a `TdClient` option to deliver raw JSON for undecodable updates would make §2.5 detectable. Second finding: `TdClient.close()` returns on `Closed`; there is no way to await the receive loop's exit (§2.7).

### 2.7 JVM shutdown and multi-account

- **Shutdown**: `Main.kt:17` registers `stopKoin` as a shutdown hook → scope callbacks → `DefaultClientHolder.close()` (`:89-95`) → `runBlocking { withTimeoutOrNull(3s) { client.close() } }` → `send(Close)`, await `Closed` (`TdClient.kt:50-55`). After `Closed`, `TdReceiver` removes the client (`:88`); the loop keeps calling `td_receive(1s)` until a null return, then exits (`:64-66`). The hook returns right after `Closed`, so the JVM can exit while the IO slot is inside `td_receive` — the very window the code comment warns about (`DefaultClientHolder.kt:88`). This was equally true with OLD (`OLD/.../TdReceiver.kt:67-79`); the bump changes nothing here [V]. Whether TDLib actually crashes in that window is the developer's observation [U]. Workaround until the library exposes loop completion: after `close()`, sleep up to ~1.1s before returning from the hook, or send a cheap request on a scratch client to wake `td_receive` (pytdbot's trick, `docs/research/tdlib-clients.md` §1).
- **Multi-account**: `TdReceiver.of(NativeTdLib)` keys receivers by the `TdLib` object; `NativeTdLib` is a singleton → one loop per process, clients routed by `@client_id` (`TdReceiver.kt:84-86, 103-108`) [V]. `DefaultAccountRepository.load()` opens every account scope synchronously (`:117-130`), each `DefaultClientHolder.init` starts an instance; N instances share one loop. Same as Telegram X's model. One account's cold-start storm delays dispatch for the others (single loop) — same as OLD and as Telegram X.
- **Late update for a closed account**: after `scope.close()`, the holder's `scope.cancel()` (`:94`) cancels the forwarding collector; `TdReceiver` drops the client after `Closed` (`:88`), so `onUpdate` is never called again for that id. The account's ViewModels are cleared by `AccountViewModelStoreOwner`'s callback (`K/shared/.../AccountViewModelStoreOwner.kt:21-27`). A late `send` fails with `IllegalStateException` (`DefaultClientHolder.kt:75`, tested in `ClientHolderTest.kt:56-57`).

---

## 3. Domain-mirror cost

### 3.1 Method

`parse_api.py` turns the 819 generated files into a model (name, kind, serial name, supertypes, fields with type/nullability/default). `closure.py` follows field types (unwrapping `List<>`/`?`) and, for a sealed interface, all its subtypes — a 1:1 mirror must cover every case. Cost model, stated so it can be challenged: mirror lines = 2 + fields per data class, 1 per data object, 2 per sealed interface; mapper lines = 2 + fields per data class (named arguments), 1 per object, 2 + subtypes per sealed `when`. No KDoc, no imports counted. Numbers below are for NEW.

### 3.2 Results

| Scope | Types | Data classes | Objects | Sealed | Fields | Mirror lines | Mapper lines |
|---|---|---|---|---|---|---|---|
| `Message, Chat, User, File, Supergroup, BasicGroup`, fields only (sealed types left opaque) | 71 | 42 | 0 | 29 | 321 | ~460 | ~690 |
| same six, sealed subtypes followed | 837 | 591 | 168 | 78 | 1,856 | ~3,360 | ~3,990 |
| **M1 chat list** (13 entity roots + 49 updates), `MessageContent` limited to 10 preview types | 769 | 546 | 149 | 74 | 1,612 | ~3,000 | ~3,530 |
| M1 with full `MessageContent` | 965 | 680 | 197 | 88 | 2,049 | ~3,780 | ~4,470 |
| **M2 history, common content** (21 content roots + 14 updates) | 464 | 313 | 107 | 44 | 894 | ~1,715 | ~2,145 |
| **M3 files and media** (25 roots + 7 updates) | 679 | 465 | 155 | 59 | 1,383 | ~2,590 | ~3,100 |
| **M4 everything in `MessageContent`** | 664 | 454 | 152 | 58 | 1,349 | ~2,525 | ~3,040 |
| Union M1–M4 | 992 | 703 | 200 | 89 | 2,115 | ~3,900 | ~4,590 |
| Whole API minus functions | 2,507 | 1,696 | 584 | 226 | 5,089 | ~9,500 | ~13,100 |
| "Only fields a chat-list row uses" (own pick of 44 fields across 6 types), `MessageContent` limited to 6 | 543 | 390 | 104 | 49 | 1,097 | ~2,190 | ~2,700 |

Why even the projection is large — the hubs (closure size from each root): `Chat` 808, `Message` 664, `MessageContent` 602, `DraftMessage` 389 (via `InputMessageContent` 320), `ReplyMarkup` 178, `FormattedText` 34 (24 `TextEntityType`s), `Sticker` 27, `User` 31, `ChatPhotoInfo` 5, `ChatPosition` 8 [V script]. Cutting `MessageContent`, `InputMessageContent` and `ReplyMarkup` to UI-sized sealed types is what makes a mirror small — and at that point it is no longer 1:1.

Churn: 1.8.68 → 1.8.69 changed 23 types and added 46 (§1.2). Every TDLib bump re-opens the mirror.

Also relevant for any copy: 474 `List<>` fields in 408 classes and 75 `ByteArray` fields [V]. The generated data classes suppress `ArrayInDataClass` (`Message.kt:2`), so `equals` on anything holding a `ByteArray` compares the array by reference. `Minithumbnail.data: ByteArray` sits inside `ChatPhotoInfo` inside `Chat` [V]: two `Chat` instances with identical content are `!=` whenever a minithumbnail is present. This affects `StateFlow` deduplication, `distinctUntilChanged`, and any equals-based Compose skipping.

### 3.3 Options, with honest cost

1. **Hand-written 1:1 mirror in `core:domain`** (the current rule, `docs/design/sessions.md` §3, `docs/architecture.md` §5). M1+M2 ≈ 4.7k mirror + 5.7k mapper lines before reuse between them; union of M1–M4 ≈ 8.5k lines. Mechanical, error-prone in the mappers, re-synced on every TDLib bump. Gains: `Duration`/`Instant`/enums where Kotlin is better, a package the UI may import, independence from the library's naming. Does **not** gain Compose stability by itself (§3.4).
2. **Generated mirror**: a small Gradle task (the library already generates from td_api.tl, `TdObject.kt:1`) emitting `core:domain` classes + mappers with rules (int seconds → `Duration`, `List` → `List`, closed sets → enums). ~0.5–1k lines of generator, zero marginal cost per bump, same output size. The domain then *is* TDLib with a different package; the value is isolation and the conversions, not modelling. Own design; precedent is the library itself.
3. **DTOs used directly above `data`** (`io.xbot.tdlib.api` as the domain): zero mirror, zero mappers; repositories return DTOs; presentation depends on `tdlib-kmp-client` (Kotlin only; the native library is in the core artifact). Contradicts `architecture.md` §1.1 ("presentation must not contain TDLib types") and `sessions.md` principle 4. Telegram X and Unigram use TdApi objects in UI [U — other agents]. Equality caveats of §3.2 apply unchanged.
4. **Mirror only what screens use, with cut sealed types** (`ChatListItem`, `MessagePreview` with ~10 cases + `Other`, `UserSummary`): ≈ 100–200 lines of models and 150–300 lines of mappers per screen family, growing per screen. Cheapest, most readable UI, but it's a presentation/UI model, not a 1:1 domain, and the "no own enums" rule bends.

A hybrid is common: option 3 or 4 for `Message`/`MessageContent` (the hub), option 1 for the small, stable types already mirrored (`AuthorizationState`, `FormattedText`, `CountryInfo`). The decision is the developer's; the numbers above are the input.

### 3.4 What the Compose compiler does with the DTOs

- Classes from a module not compiled with the Compose compiler are **unstable**: "The Compose compiler can only infer whether a class is stable if all of the non-primitive types that it references are either explicitly marked as stable or in a module that was also built with the Compose compiler" and "classes from non-Compose modules are treated as unstable" [V, https://developer.android.com/develop/ui/compose/performance/stability/fix, "Multiple modules"]. This applies to `io.xbot.tdlib.api.*` **and to `com.xbot.kotogram.core.domain.*`**: `core/domain/build.gradle.kts` applies only the KMP and Android plugins, no `compose.compiler` [V `K/core/domain/build.gradle.kts:3-5`]. Only `presentation`/`shared` modules apply it (`K/feature/chats/presentation/build.gradle.kts:6`).
- Strong skipping is the default since Kotlin 2.0.20 [V, https://developer.android.com/develop/ui/compose/performance/stability/strongskipping]; Kotogram is on 2.4.20. "With strong skipping enabled, all restartable composable functions become skippable", "Unstable parameters are compared using instance equality (===)". So a `ChatRow(chat: Chat)` taking a DTO (or an unstable domain class) **is skippable** as long as the same instance is passed again. Stores that keep instances until the entity changes make this work; a mapper that allocates a new domain object per emission defeats it (the docs' explicit caveat).
- `List` fields: "the Compose compiler cannot be completely sure that collection interfaces such as List, Map, and Set are truly immutable and therefore marks them as unstable" — and under strong skipping "unstable collections and classes containing collections already skip recomposition using instance equality (===) whenever the same reference is passed". The docs now explicitly warn against `ImmutableList`/`@Immutable` on collection-holding models used in lazy lists, because every element is then compared with `equals()` per recomposition [V, same page].
- Stability configuration file: `composeCompiler { stabilityConfigurationFiles }` (`ListProperty<RegularFile>`, "List of paths to stability configuration files") [V, https://kotlinlang.org/api/kotlin-gradle-plugin/compose-compiler-gradle-plugin/.../-compose-compiler-gradle-plugin-extension/]; one class per line, `com.datalayer.**` wildcards, "allows for considering classes you don't control … as stable"; "Incorrectly configuring a class could cause recomposition to break" [V, Android docs]. Listing `io.xbot.tdlib.api.**` would switch comparison to `equals()` — with the `ByteArray` reference-equality hole of §3.2 and the per-element `List` cost. Not recommended for the whole package; acceptable for small flat types (`File`, `LocalFile`, `RemoteFile`, `ChatPosition`, `UserStatus*`) if benchmarks show churn.
- Net: the mirror question and the stability question are independent. Whichever model type reaches Compose, the lever is **instance preservation in the stores**, not the package the class lives in.

---

## 4. Module and DI constraints

### 4.1 Where per-account stores live

Rules in force: TDLib types never leave `data` (`architecture.md` §5), `core:*` may depend on `core:*` only, features see each other only through `api` (§1.1), a model two features need moves to `core:domain` (§1.1). Chat list, chat screen, profile, contacts, search all read chats, users, messages and files. Options:

| Option | Fits the rules? | Cost |
|---|---|---|
| **A. Inside `core:tdlib`** (holder + reducer + stores of DTOs; feature `data` modules read them and map) | Yes: `core:tdlib` is already the only module allowed to hold `TdClient`; it already hosts `DefaultAccountRepository`, the implementation of a `core:domain` interface (`DefaultAccountRepository.kt:52-56`) — the precedent exists | `core:tdlib` becomes "the data layer"; its tests need TDLib binaries (today's `ClientHolderTest` already does) |
| **B. New `core:store`** (or `core:tdlib:client` + `core:tdlib:store`) depending on `core:tdlib` | Yes | Same visibility as A with a cleaner boundary: the client adapter stays small and the stores can be tested with a `FakeClientHolder` without TDLib. Preferred if `core:tdlib` grows past a few files |
| **C. Per feature** (`feature:chats:data` owns chats, `feature:users:data` owns users…) | **No**: one ordered collector per account (§5 rule) needs every handler in one place; features can't share it without depending on each other's `data`; and the chat list needs chats + users + files in one consistent snapshot | — |

Cross-feature entities (`Chat`, `User`, `Message`, `File`, `Supergroup`, `BasicGroup`) therefore follow `Account`: interfaces (`ChatRepository`, `UserRepository`, `FileRepository`, …) in `core:domain`, `Default…Repository` in `core:tdlib`/`core:store`, feature `data` modules only for feature-specific operations (as `feature:auth:data` does today). Flag: this makes `core:domain`'s surface large if the models are 1:1 (§3).

### 4.2 Collecting ordered handlers from Koin

`Scope.getAll(clazz)` at 4.2.2: `_koin.instanceRegistry.getAll<T>(clazz, context) + linkedScopes.flatMap { it.getAll(clazz) }` [V, raw GitHub tag 4.2.2 `Scope.kt`]. `InstanceRegistry.getAll` filters `_instances.values` by `scopeQualifier == instanceContext.scope.scopeQualifier || == scopeArchetype` and by primary/secondary type, `.distinct()`, then the ordering line is **commented out**: `// .sortedWith(compareBy({ it.beanDefinition.toString() }))` [V, raw GitHub tag 4.2.2 `InstanceRegistry.kt`]. `_instances = safeHashMap<IndexKey, InstanceFactory<*>>()` = `ConcurrentHashMap` on JVM (`koin/projects/core/koin-core/src/jvmMain/kotlin/org/koin/mp/KoinPlatformTools.kt:37`), `ConcurrentMutableMap` on native (`nativeMain/.../KoinPlatformTools.kt:26`) [V]. Conclusions:

- Scoped `getAll` **works**: called on the account scope it returns every `@Scope(AccountId::class) @Scoped` definition bound to the type (primary or `binds`), and not root singletons [V].
- Order is **hash-map iteration order**: undefined, platform-dependent, changes with the set of definitions. `sessions.md` §7's "fed in order" cannot be had from Koin. A `priority` property and an explicit `sortedBy` would be own ordering machinery on top of DI; the simpler route is no handler list at all: one reducer class whose `when (update)` dispatches to stores in source order.
- Compile safety: Kotogram already uses `koin.getAll<PolymorphicSerializerConfig<NavKey>>()` (`K/core/navigation/impl/.../KoinNavigation.kt:14`) and builds, so `getAll` is at least tolerated by the plugin; whether it is validated is not documented in the plugin README [U]. The architecture's rule 3 (DSL from other modules invisible to the check) and rule 2 (define with classes) favour one annotated `@Scoped` reducer with constructor-injected stores anyway.

### 4.3 What runs before the first `send`; the race

Sequence today [V]: `DefaultAccountRepository.init` → `scope.launch { load() }` (`:87-92`) → `open(id)`: `koin.createScope(...).get<ClientHolder>()` (`:172`) → `DefaultClientHolder` constructor: `registerCallback`, then `scope.launch { while (!isClosed) start().authorizationState.first { Closed } }` (`:60-72`) → `start()`: `TdlibLog.install()`, `TdClient.create()`, `instances.emit(client)`, `scope.launch { client.send(parameters()) }` (`:79-86`).

Nothing else in the account scope exists at that moment: `get<ClientHolder>()` is the only resolution in `open()`; `accountOf()` resolves the same holder again (`:176-178`). Koin scoped beans are lazy, so a store/reducer resolved later — by a repository, by a ViewModel — attaches after the storm. The race is not only "late consumer"; even a consumer that exists cannot order its `collect` before the launched `send` (§2.1). The only race-free point is inside `start()` (§2.2–2.3): the holder itself subscribes `UNDISPATCHED` before `send` and buffers into its channel. With that, the reducer can be resolved whenever the scope's first repository needs it, with no loss. If the reducer should start eagerly, `DefaultAccountRepository.open()` can `get<AccountReducer>()` right after `get<ClientHolder>()` — the same compile-safety-visible pattern as today.

Second race, benign: `DefaultAccountRepository.keepTidy()` reads `authorizationState` only; unaffected.

---

## 5. Libraries for Kotlin 2.4.20 / CMP 1.12.1 on android, iosArm64, iosSimulatorArm64, jvm

| Library | Version / status | Fit |
|---|---|---|
| **androidx.paging** `paging-common`, `paging-compose` | 3.5.3 stable, 2026-10-12 [V, https://developer.android.com/jetpack/androidx/releases/paging]. KMP since 3.3 ("paging-common has moved all Paging 3 APIs to common and is now compatible with jvm and iOS"); 3.4 added "JVM (Android and Desktop), Native (… iOS …), and Web" for `paging-common`, `paging-testing`, `paging-compose` [V same page]. AnilibriaRefresh uses 3.5.1 with CMP 1.13.0-alpha01 and Kotlin 2.4.20 (`AnilibriaRefresh/gradle/libs.versions.toml:11-13,26,99-100`) [V] | **Poor fit for message history.** Paging is pull-by-key with `invalidate()` → new `PagingSource` → reload; TDLib pushes `updateNewMessage`, `updateDeleteMessages`, `updateMessageContent`, `updateMessageEdited` into an already-loaded window and expects in-place application. No in-place mutation API in Paging 3; every push would invalidate and reload from TDLib (`getChatHistory` may return fewer than `limit`, so windows are not stable). Fine for pull-only lists (search results, members). Own `MessageWindow` (anchor message id, `loadOlder/loadNewer`, applies updates) is the TDLib-native shape; Telegram X/Unigram do this [U — other agents] |
| **Coil 3** | 3.6.3, 2026-09-18 [V GitHub releases]; 3.6.0 "Update Kotlin to 2.4.10", "Update Compose to 1.12.0"; 3.6.2 fixes a CMP 1.13 JS/WASM linkage [V CHANGELOG]. Non-Android JVM artifact exists ("Bundle ProGuard rules with the non-Android JVM artifact") [V]. Kotlin 2.4.20 / CMP 1.12.1 compatibility [I: same minor line] | **Good fit** with a custom `Fetcher.Factory<T>` + `Keyer<T>`: "If you add a Fetcher that uses a custom data type, you need to also need to provide a custom Keyer"; components registered in `ImageLoader.Builder.components { add(...) }` [V, https://coil-kt.github.io/coil/image_pipeline/]. Design (own): data = domain `File` (or `FileId` + account); Fetcher asks the account's `FileRepository` to download (`downloadFile(…, synchronous = true)`) and returns an `ImageSource` from `local.path`; Keyer = `remote.uniqueId` (stable across instances), never the numeric file id; `Minithumbnail.data` as placeholder. Multi-account: one `ImageLoader` per account scope, or encode the account in the data type |
| **kotlinx-collections-immutable** | v0.5.2, 2026-08-28 [V `gh api …/releases/latest`]; 0.5.0 built with Kotlin 2.3.0; all Kotlin/Native targets since 0.3.6 [V releases page] | Useful for **store snapshots** (persistent maps make an atomic, cheap `AccountSnapshot` copy per update), not for Compose stability: the Android docs now advise against `ImmutableList` on lazy-list models under strong skipping (§3.4) |
| **Lifecycle** (JetBrains) | `org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.11.0` in `commonMain` [V, https://kotlinlang.org/docs/multiplatform/compose-lifecycle.html]; `collectAsStateWithLifecycle` is in androidx `lifecycle/lifecycle-runtime-compose/src/commonMain/kotlin/androidx/lifecycle/compose/FlowExt.kt` [V `gh api search/code`], so it is common API. iOS maps `viewWillAppear`→`ON_START`, `didBecomeActive`→`ON_RESUME`…; desktop maps Swing window events [V same page]. Desktop caveat: "Lifecycle.coroutineScope is tied to Dispatchers.Main.immediate, which might be unavailable on desktop targets by default … add kotlinx-coroutines-swing" [V]. Kotogram's desktop uses Nucleus, not Swing windows (`K/jvm-app/src/main/kotlin/Main.kt:9-10,18`) — whether Nucleus drives the CMP lifecycle states is **[U]** | Kotogram already depends on `lifecycle-runtime-compose` in `feature:chats:presentation` (`build.gradle.kts:38`); neither Kotogram nor AnilibriaRefresh calls `collectAsStateWithLifecycle` in common code today [V grep]. Orbit screens use `viewModel.collectAsState()` (`architecture.md` §6.3) |
| **Orbit MVI 12.0.1** | `repeatOnSubscription(block)`: "Starts and stops the provided block of code based on the number of subscribers to the refCountStateFlow and refCountSideEffectFlow. If the number of subscribers reaches non-zero, the block is run. When the number of subscribers reaches zero, the block is cancelled after a short delay" (`SettingsBuilder.repeatOnSubscribedStopTimeout`) [V, raw `orbit-core/src/commonMain/kotlin/org/orbitmvi/orbit/syntax/Syntax.kt`]; docs: "Infinite Flows are best collected within a repeatOnSubscription block" [V website/docs/Core/index.md] | This is the ViewModel-side counterpart of NIA/kotlinconf's `stateIn(viewModelScope, WhileSubscribed(5_000))` (`nowinandroid/feature/foryou/impl/.../ForYouViewModel.kt:75`, `kotlinconf-app/app/shared/.../FeedbackViewModel.kt:33`) [V]. A ViewModel collects a store's `StateFlow` inside `repeatOnSubscription`, so a hidden screen stops reducing into its own state while the account store keeps applying updates |

Reference apps' data layers and applicability:
- **nowinandroid**: `core/data` repositories are interface + `OfflineFirst…Repository` over Room DAO `Flow`s with a network sync (`core/data/src/main/.../OfflineFirstNewsRepository.kt:48-58`); separate `core/database`, `core/network`, `core/model`, `core/data-test`, `core/datastore` modules [V]. Room re-evaluates queries on invalidation and pushes full result sets; TDLib pushes **diffs** and gives no query engine, so the app must maintain the projection Room would compute. Applicable: the `Default…Repository`/`core:model`/`data-test` split and `WhileSubscribed(5_000)`.
- **kotlinconf-app**: one `ConferenceService` holding `StateFlow`s fed by the API and local storage, ViewModels doing `stateIn(viewModelScope, WhileSubscribed(5000))` [V]. The single-service shape is the closest to "one account store"; it has no per-entity push stream.
- **AnilibriaRefresh**: `shared/core/domain` interfaces, `shared/core/data` `Default…Repository`, `CommonPagingSource` over REST, a `test-fixtures` module of `Fake…Repository`/`FakePagingSource` [V file list]. Applicable: the fixtures module and naming; Paging fits REST, not TDLib history.

---

## 6. Three candidate architectures

Problem restated: one ordered stream per account; thousands of updates at cold start [U number; TDLib replays its database on `setTdlibParameters`]; entities that reference each other (chat row = chat + last message + sender user + draft + typing action + online status + photo file); several screens; several accounts; instance restarts; three platforms. Common ground for all three: `ClientHolder` fixed as in §2.3 (channel filled from inside `start()`), everything per account inside the `AccountId` Koin scope, UI reads `StateFlow`s on the main thread.

### 6.1 Design A — single-writer reducer feeding typed stores (Telegram X / Unigram shape, as `@TdlibThread` handlers do; `docs/research/telegram-x-sessions.md` §1)

```kotlin
// core:tdlib (or core:store) — all @Scope(AccountId::class) @Scoped, internal
interface ClientHolder {                      // unchanged surface
    val authorizationState: StateFlow<AuthorizationState?>
    val updates: Flow<Update>                 // single consumer; ordered; buffered from create()
    suspend fun <R : TdObject> send(function: TdFunction<R>): R
}

/** The only writer of every store. One coroutine, one thread, applies updates in arrival order. */
internal class AccountReducer(holder: ClientHolder, private val stores: Stores, koinScope: Scope) {
    private val thread = Dispatchers.Default.limitedParallelism(1)   // own choice; a shared slot is also fine
    private val scope = CoroutineScope(SupervisorJob() + thread)
    init { scope.launch { holder.updates.collect(::apply) }; koinScope.registerCallback { scope.cancel() } }
    private fun apply(update: Update) = when (update) {               // source order = handler order
        is UpdateAuthorizationState -> if (update.authorizationState is AuthorizationStateClosed) stores.reset()
        is UpdateNewChat -> stores.chats.put(update.chat)
        is UpdateChatLastMessage -> stores.chats.update(update.chatId) { it.copy(lastMessage = update.lastMessage, positions = update.positions) }
        is UpdateChatPosition -> stores.chats.updatePosition(update.chatId, update.position)
        is UpdateUser -> stores.users.put(update.user)
        is UpdateUserStatus -> stores.users.update(update.userId) { it.copy(status = update.status) }
        is UpdateFile -> stores.files.put(update.file)
        is UpdateNewMessage -> stores.messages.insert(update.message)
        else -> Unit
    }
}

/** Immutable snapshot; readers never see a half-applied update. */
data class AccountSnapshot(
    val chats: PersistentMap<Long, Chat>, val users: PersistentMap<Long, User>,
    val supergroups: PersistentMap<Long, Supergroup>, val basicGroups: PersistentMap<Long, BasicGroup>,
    val files: PersistentMap<Int, File>, val chatActions: PersistentMap<Long, ChatAction>,
)
internal class Stores { val snapshot: StateFlow<AccountSnapshot>  /* MutableStateFlow written only by the reducer */ }

// core:domain interfaces, core:tdlib implementations
interface ChatListRepository { fun chatList(list: ChatList): Flow<List<ChatListItem>>; suspend fun loadMore(list: ChatList): Either<ChatError, Unit> }
internal class DefaultChatListRepository(stores: Stores, holder: ClientHolder) : ChatListRepository {
    override fun chatList(list: ChatList) = stores.snapshot.map { it.sortedChatRows(list) }.distinctUntilChanged()
    override suspend fun loadMore(list: ChatList) = either { request { holder.send(LoadChats(list, 20)) } }
}
```

Lifetimes: `ClientHolder` and `AccountReducer` live as long as the account scope; `Stores` too; `MessageStore` windows are created per open chat and dropped when the last reader goes (`WhileSubscribed`). Threads: reducer on one serial slot; readers map on `Dispatchers.Default` via `map`; UI on main. Ownership: scope → holder, reducer, stores, repositories, ViewModels; holder → `TdClient` instances + forwarding job + channel; reducer → its coroutine; stores → snapshot.

### 6.2 Design B — independent per-entity stores, each collecting the stream

```kotlin
internal class ChatStore(holder: ClientHolder) {
    val chats: StateFlow<Map<Long, Chat>>
    init { scope.launch { holder.updates.filterIsInstance<UpdateNewChat>().collect { … } } ; /* one launch per update type */ }
}
internal class UserStore(holder: ClientHolder) { /* same with UpdateUser, UpdateUserStatus */ }
internal class FileStore(holder: ClientHolder)  { /* UpdateFile */ }
```

Needs `ClientHolder.updates` to be a `SharedFlow` (fan-out) rather than a channel, and each store subscribed before the first send — i.e. every store constructed eagerly in `open()` and subscribed `UNDISPATCHED`, or the holder replays. Ownership: scope → holder, stores (independent), repositories compose stores with `combine`.

### 6.3 Design C — repositories are reducers behind a shared dispatcher

```kotlin
interface UpdateHandler { fun onUpdate(update: Update) }                 // core:tdlib
internal class UpdateDispatcher(holder: ClientHolder, handlers: List<UpdateHandler>) { /* one collector; calls handlers in list order, synchronously */ }
internal class DefaultChatRepository(holder: ClientHolder) : ChatRepository, UpdateHandler {
    private val cache = MutableStateFlow(persistentMapOf<Long, Chat>())
    override fun onUpdate(update: Update) { when (update) { is UpdateNewChat -> …; is UpdateChatLastMessage -> … } }
    override fun chat(id: Long): Flow<Chat?> = cache.map { it[id] }
}
```

The handler list is the problem: Koin `getAll` is unordered (§4.2); a hand-written list in the dispatcher means `core:tdlib` names every repository, including feature ones — which is C collapsing into A with the state spread over N classes. (A Paper-Plane-style variant — per-entity observable objects with a `StateFlow` per field — is a UI-binding choice on top of A's stores rather than a fourth data architecture; it multiplies flows per entity and is omitted here, other agents cover it.)

### 6.4 Scenario matrix

| Scenario | A: single reducer + snapshot stores | B: independent stores | C: repositories as handlers |
|---|---|---|---|
| Subscriber misses early updates | Solved once in the holder (§2.3); the reducer reads a buffered channel whenever it attaches | Every store must be subscribed before the first send; any store added later (new feature) silently misses the burst unless the holder replays | Same as A if the dispatcher reads the holder's channel; same as B if handlers subscribe themselves |
| `updateChatPosition` before the chat exists; `updateChatLastMessage` carrying positions | One `when`, one thread: order is arrival order; a missing chat is repaired by `getChat` from the reducer (§2.5) | Chat and position handled in different coroutines; `updateChatLastMessage` must touch "chat" and "position" state owned by different stores → either duplicated state or cross-store calls with no ordering guarantee | Order = handler list order, fixed in code; cross-repository updates (`updateChatLastMessage` touches chats and the list) need the dispatcher to call two handlers for one update |
| Instance restart / logout | `Closed` arrives in-stream → `stores.reset()` at exactly the right point; next instance's `updateNewChat`s rebuild | Each store sees `Closed` on its own schedule; a slow store can apply a stale update of instance N after a fast store already holds N+1 state | Same as A if `Closed` is dispatched to all handlers in order |
| Consistent snapshot across chats and users (row = chat + sender user) | `AccountSnapshot` is one immutable value; a row function sees chat and user from the same instant | `combine(chats, users)` emits intermediate pairs (chat present, user not yet) → flicker or null-handling in UI | Per-repository caches → same intermediate-state problem as B |
| Cold-start storm, main-thread jank | Reductions on one background slot; `StateFlow` conflates; UI sees the latest snapshot per frame; cost per update = one `PersistentMap.put` (O(log n)) + the library's double decode | N filter passes over the same stream; each store's `StateFlow` emits separately → N recompositions per burst unless the ViewModel `combine`s and conflates | Like A, but each handler's `MutableStateFlow` emits separately |
| Memory over a long session | Chats/users/supergroups retained (TDLib sends them for anything it mentions; Telegram X keeps them too [U]); message windows dropped on `WhileSubscribed` timeout; persistent maps share structure between snapshots | Same growth, spread over stores | Same |
| Closed account's late update | Reducer scope cancelled from the scope callback; `TdReceiver` already dropped the client (`:88`); ViewModels cleared (`AccountViewModelStoreOwner`) | Each store needs its own callback | Each repository needs its own callback |
| Decode failure (silently dropped update) | One place to add referential repair and the version guard (§2.5) | Repair logic duplicated per store, and a store can't know what another store lacks | Repair per repository |
| Testability with a fake stream | `FakeClientHolder { updates = flowOf(...) }` → `reducer.apply` is a pure `(snapshot, update) → snapshot` sequence; assert the final snapshot; no dispatchers to coordinate | Must drive N collectors and wait for all; ordering assertions need `runTest` with careful `advanceUntilIdle` | Dispatcher + fake handlers is easy; but repositories now mix state and IO, so a repository test needs both a fake stream and a fake `send` |
| Fits the developer's rules | Repositories stay "interface + Default… over `ClientHolder`" and are read-only over stores; TDLib types stay in `core:tdlib`; `@Scoped` classes, no `getAll`, no handler ordering machinery | Needs eager construction lists or a replaying holder: own machinery | Needs an ordered handler list: own machinery, and Koin can't provide it |

### 6.5 Recommendation

**Design A.** It is what every TDLib client with a cache does (one synchronous handler on the receive thread, `telegram-x-sessions.md` §1 and `tdlib-clients.md` §2), it is the only one of the three that gives a consistent cross-entity snapshot without extra machinery, it is the simplest to test (pure reduction), and it uses the existing rules unchanged: `ClientHolder` keeps its interface; `Default…Repository` classes stay thin; `@Scope(AccountId::class) @Scoped` on the reducer and stores; `core:domain` holds the repository interfaces, `core:tdlib` (or a `core:store` split) the implementations, as `AccountRepository` already does.

Concrete entity list, `core:tdlib`/`core:store`: `ClientHolder` (fixed), `AccountReducer`, `Stores`/`AccountSnapshot`, `MessageStore` (per-chat windows, created on demand), `Default{ChatList,Chat,User,Message,File}Repository`. `core:domain`: the repository interfaces and whichever model variant §3 decides. Feature `data` modules: feature-specific operations only. ViewModels: `repeatOnSubscription { repository.flow.collect { reduce { … } } }`, `LazyColumn` keyed by chat id, rows taking the store's instance (so strong skipping's `===` works).

**Weakest point**: the reducer is a single serialization point and a single class. A slow branch — e.g. rebuilding a sorted chat list on every `updateChatPosition` during the cold-start burst — stalls every other entity's updates and lets the channel grow; and the `when` over ~50 update types in one file becomes the place everything touches. Mitigations, both design choices to make explicitly: keep the reducer to O(log n) map writes and derive sorted/combined views lazily in repositories (`snapshot.map { … }` on Default, conflated); split `apply` into per-entity extension functions in separate files while keeping one call site. A second, smaller weakness: the snapshot-per-update allocation (one `AccountSnapshot` + persistent-map path copy per update) is extra GC pressure during the burst; `conflate` on readers hides it from the UI, not from the allocator.

---

## 7. Contradictions and stale statements found

| Where | Statement | Status |
|---|---|---|
| `docs/design/sessions.md` §1, review note | `ClientHolder` "does not implement `TdClient`, because `TdClient`'s contract (updates end at Closed) must stay true" | `TdClient` is a final class; `updates` never ends. The reason is moot, the decision (an own interface) still right |
| `ClientHolder.kt:19-23` KDoc | restart = `Closed` then new instance's updates | False with `flatMapConcat`; true again after §2.3 |
| `docs/research/tdlib-clients.md` §2, §7 | "`Flow<Update>` per client, completes on `Closed`" | Out of date |
| `docs/design/sessions.md` §7 | "`getAll<UpdateHandler>()` fed in order" | Koin gives no order (§4.2) |
| `docs/architecture.md` §5 | "Each account has one collector of `TdClient.updates`" | Right rule; the collector must live inside the holder's `start()` to be loss-free |
| `docs/architecture.md` §1.1 vs §3.3 of this report | presentation never sees TDLib types; 1:1 mirror | Both hold only at 3–4k+ lines per milestone; `core:domain` is not a Compose module either, so the mirror alone doesn't change stability |
| `FormattedTextMapper.kt:128-129` | "Generated TDLib classes make nested objects nullable with a default" | Not in either version; `required()` is dead code |
| `DefaultClientHolder.kt:88` comment / `Main.kt:16-17` | closing TDLib on `stopKoin()` avoids the exit crash | `close()` returns on `Closed`, the receive loop may still be inside `td_receive` for up to 1s; unchanged by the bump, not solved |

## 8. What I could not verify

- That the `nonWebMain` extension `TdClient.Companion.create()` is visible from Kotogram's `commonMain` (KMP source-set metadata resolution) — a compile would tell.
- Whether the Koin compiler plugin's compile safety validates `getAll` calls.
- Whether the desktop Nucleus host drives CMP lifecycle states (the JetBrains docs describe Swing).
- Whether the JVM actually crashes when exiting inside `td_receive` (developer's observation) and whether Dispatchers.IO threads being daemon changes anything.
- Real cold-start update counts for a typical account; the figure "thousands" is TDLib folklore until measured.
- Orbit's `collectAsState` lifecycle behaviour in `orbit-compose` (not read).
