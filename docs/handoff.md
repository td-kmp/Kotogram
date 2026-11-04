# Handoff (2026-11-04)

For the next agent or machine. Durable rules are in [CLAUDE.md](../CLAUDE.md). This file is status: replace it when it goes stale.

## Where things stand

The developer could not settle the app's architecture around `tdlib-kmp`: which entities exist, and how they relate from an update to a pixel. We surveyed the
TDLib clients and wrote a proposal.

- **Read first:** [design/messaging-core.md](design/messaging-core.md), the proposal. Nothing in it is implemented. Then [research/tdlib-survey/](research/tdlib-survey/) as needed.
- **Its core idea:** one ordered applier, one immutable per-account snapshot as a pure fold of updates, a separate after-apply update stream, a message window per open chat, memoised row models, a file
  store outside the snapshot.
- Next task number: **KTG-4** (KTG-3 is this survey and proposal).

## This branch does not compile right now

`tdlib-kmp` was bumped from `1.8.68-1.0.0-alpha02` (Maven Local, a prototype with an interface `TdClient`) to the published `1.8.69-1.0.0-alpha02` (a final class), and `mavenLocal()` was
removed. That was asked for, because the other machine won't have the Maven Local artifact, but the code was not adapted. All breaks are in `core:tdlib` (table in [05-kotogram-fit.md](research/tdlib-survey/05-kotogram-fit.md) §1.3):

| Where | Fix |
|---|---|
| `DefaultClientHolder.kt:81` `TdClient.create()` | `import io.xbot.tdlib.client.create` |
| `TdlibLog.kt:24-25` `TdClient.execute(...)` | suspend; `parseTdObject(checkNotNull(NativeTdLib.execute(fn.toTdJson())))` |
| `TdlibLog.kt:4,28` `TdLib.setLogMessageHandler` | `NativeTdLib.setLogMessageHandler` |
| `DefaultClientHolder.kt:58` `flatMapConcat { it.updates }` | **Compiles, silently wrong**: `updates` never completes. Nothing collects it yet. Replaced by design §3 |

These are milestone M0 in the design. None of this analysis has been compiled: no build was run in this work.

## Waiting on the developer

Design §13. Don't decide these alone:

1. **D1, the model above `data`.** Recommendation: generated DTOs as the domain model, composables get row models. This revisits the existing 1:1 mirror rule, whose cost was measured at about 8.5k lines for
   M1–M4, so it is the developer's call. It was only presented, not answered.
2. **D2, where the store lives.** Recommendation: `core:tdlib`.
3. **D3, push.** The developer has to log in at my.telegram.org and check whether a third-party `api_id` can upload FCM/APNs credentials. Nobody could verify it from outside. It decides the whole notification design.

## Next steps, in order

1. Present the proposal, and get D1 and D3.
2. Optional, one agent at most: a focused review of design §4.2, §6 and §7 against the TDLib contract (`td_api.tl`, `MessagesManager.cpp`). Of the four planned reviews, only mechanics finished ([06-review-mechanics.md](research/tdlib-survey/06-review-mechanics.md)). The reviews against Telegram X's rules, the
   contract and the Kotlin clients were cut off by a usage limit and left nothing.
3. M0 on a new task. Its tests decide whether `onSubscription` and Koin behave as the design assumes.
4. M1, including the measurements the design defers: cold-start update count, JSON decode cost, sort cost, and TDLib on an iOS device.

## What is not done, or weak

- [01-landscape.md](research/tdlib-survey/01-landscape.md) is a scripted classification, not the verified catalog that was planned. Only the Kotlin clients in report 02 were read.
- §6's table "everything else a chat screen needs" comes from Pingogram and hasn't been reviewed against the design.
- The clones (66 GB) exist only on the first Mac, in a temporary folder. `inventory.tsv` lists every repository.

## Left on the first Mac, not pushed

Uncommitted work that predates this session and isn't part of this push: `@KotogramPreview` previews for the auth and chats screens, `core/designsystem/.../KotogramPreview.kt`, `shared/.../root/SplashScreen.kt`, `AppFeatureModule.kt`,
`strings.xml`, and a section in `docs/architecture.md` §6.3. If those files look missing on another machine, that's why.

## Also true

- `docs/design/sessions.md`, `docs/research/tdlib-clients.md` and the `ClientHolder.kt` KDoc still say `updates` completes on `Closed`, and `sessions.md` §7 still expects ordered `getAll`. Both are out of date (05 §7). M0 updates them.
- The local `tdlib-kmp` clone is on `feature/web-target`, which diverges from `main`; only `main` is published.
