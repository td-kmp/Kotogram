# Handoff (2026-11-04)

For the next agent or machine. Durable rules are in [CLAUDE.md](../CLAUDE.md). This file is status: replace it when it goes stale.

## Where things stand

The developer could not settle the app's architecture around `tdlib-kmp`: which entities exist, and how they relate from an update to a pixel. We surveyed the
TDLib clients and wrote a proposal.

- **Read first:** [design/messaging-core.md](design/messaging-core.md) and [design/accounts.md](design/accounts.md), the proposals. Nothing in them is implemented. Then [research/tdlib-survey/](research/tdlib-survey/) as needed.
- **Its core idea:** one ordered applier, one immutable per-account snapshot as a pure fold of updates, a separate after-apply update stream, a message window per open chat, memoised row models, a file
  store outside the snapshot.
- Next task number: **KTG-6** (KTG-3 is the survey and proposals, KTG-4 M0 and accounts, KTG-5 the restructure, chats start and web target).

## What exists

- **`core:data`** (was `core:tdlib`), by entity: `client` (`ClientHolder`, one ordered collector), `store` (`AccountState`, reducers for options, users, groups, chats and chat lists), `account` (accounts as one login's life), `environment`.
- **No mirror.** TDLib's generated classes are the domain model everywhere above `data`. `core:domain` exposes them through `api(libs.tdlib.client)`, and a detekt `ForbiddenImport` keeps `io.xbot.tdlib.client.*` out of presentation.
- **`feature:chats:{api,domain,data,presentation}`.** The chat list is real: `DefaultChatRepository`, `ChatListViewModel`, a memoising `ChatListRowMapper`, `loadChats` paging. No avatars yet. The chat screen only shows its title: history is M2.
- **Web:** `web-app` (`wasmJs`), worker in `core/data/worker`, `AccountStorage` as the platform seam, one tab at a time through `navigator.locks`.

**Checked.** `:core:data:jvmTest` and `:shared:jvmTest` pass, real TDLib included. JVM, Android, iOS and web compile. Headless Chrome: the web bundle started TDLib 1.8.69 in the worker, connected over WebSocket, reached `authorizationStateWaitOtherDeviceConfirmation` and showed the QR screen with a real token; the database is in IndexedDB.

**Not checked: nothing past the login screen has run anywhere.** The chat list has never seen a real account. No device, no logged-in account, no web production bundle. Do this first by hand on a test-DC account: log in, see the list, log out, add an account, go back from its login. `-Ptelegram.test.dc=true` keeps away from real data, because the real data directory holds real accounts and the new rules delete non-active unauthorized ones.

## Decisions

- **D1 decided and applied (2026-11-04):** generated DTOs are the domain model.
- **D2 decided:** the store lives in `core:data`.
- **Accounts design accepted** and implemented.

Waiting on the developer: **D3, push.** Log in at my.telegram.org and check whether a third-party `api_id` can upload FCM/APNs credentials. Nobody could verify it from outside. It decides the whole notification design.

## Next steps, in order

1. Try it by hand with a real account (above), and get D3.
2. Finish M1: avatars (`FileStore`, `FileDownloads`, Coil fetcher, §7 of the design), folders, typing, scope mute settings. Measure cold-start update count, JSON decode cost and sort cost, and run TDLib on an iOS device.
3. M2: `ChatHistory` (design §6).
4. Web: the QR screen shows the link as text everywhere, not an image. `index.html` has no favicon.
5. Optional, one agent at most: a focused review of design §4.2, §6 and §7 against the TDLib contract (`td_api.tl`, `MessagesManager.cpp`). Of the four planned reviews, only mechanics finished ([06-review-mechanics.md](research/tdlib-survey/06-review-mechanics.md)).

## What is not done, or weak

- [01-landscape.md](research/tdlib-survey/01-landscape.md) is a scripted classification, not the verified catalog that was planned. Only the Kotlin clients in report 02 were read.
- §6's table "everything else a chat screen needs" comes from Pingogram and hasn't been reviewed against the design.
- The clones (66 GB) exist only on the first Mac, in a temporary folder. `inventory.tsv` lists every repository.

## Also true

- `docs/research/tdlib-clients.md` and `docs/design/sessions.md` describe older designs and are kept for the reasoning.
- The local `tdlib-kmp` clone is on `feature/web-target`, which diverges from `main`; only `main` is published.
