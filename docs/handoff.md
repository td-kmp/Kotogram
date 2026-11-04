# Handoff (2026-11-04)

For the next agent or machine. Durable rules are in [CLAUDE.md](../CLAUDE.md). This file is status: replace it when it goes stale.

## Where things stand

The developer could not settle the app's architecture around `tdlib-kmp`: which entities exist, and how they relate from an update to a pixel. We surveyed the
TDLib clients and wrote a proposal.

- **Read first:** [design/messaging-core.md](design/messaging-core.md) and [design/accounts.md](design/accounts.md), the proposals. Nothing in them is implemented. Then [research/tdlib-survey/](research/tdlib-survey/) as needed.
- **Its core idea:** one ordered applier, one immutable per-account snapshot as a pure fold of updates, a separate after-apply update stream, a message window per open chat, memoised row models, a file
  store outside the snapshot.
- Next task number: **KTG-5** (KTG-3 is the survey and proposals, KTG-4 is M0 and accounts).

## What M0 did (KTG-4)

The branch compiles on the published `tdlib-kmp`. `ClientHolder` (ordered, nothing lost before the first `send`), the `AccountStore` skeleton (`state`, `updates`; `reduce` handles only options and `Closed`) and the accounts design ([design/accounts.md](design/accounts.md), §8 lists what is proven) are in.

`:core:tdlib:jvmTest` and `:shared:jvmTest` pass; JVM, Android and iOS compile. **Not run in a real UI, on a device, or with a logged-in account.** Worth doing first by hand on a test-DC account: log in, log out, add an account, go back from its login. `-Ptelegram.test.dc=true` keeps away from real data, because the real data directory holds real accounts and the new rules delete non-active unauthorized ones.

## Decisions

- **D1 decided (2026-11-04):** generated DTOs are the domain model, composables get row models. `architecture.md` §1.1, §2.4, §4, §5 and `research/tdlib-clients.md` §2 still say otherwise. They weren't updated here because `architecture.md` has uncommitted work on the first Mac.
- **Accounts design accepted** (2026-11-04) and implemented.

Waiting on the developer (design §13):

1. **D2, where the store lives.** It went into `core:tdlib`, as recommended. Split it when it grows.
2. **D3, push.** The developer has to log in at my.telegram.org and check whether a third-party `api_id` can upload FCM/APNs credentials. Nobody could verify it from outside. It decides the whole notification design.

## Next steps, in order

1. Try the account flows by hand (above), and get D3.
2. M1: chat list. Reducers, `ChatListOrder`, row mapper, `loadChats`, avatars. Measure cold-start update count, JSON decode cost and sort cost, and run TDLib on an iOS device.
3. Optional, one agent at most: a focused review of design §4.2, §6 and §7 against the TDLib contract (`td_api.tl`, `MessagesManager.cpp`). Of the four planned reviews, only mechanics finished ([06-review-mechanics.md](research/tdlib-survey/06-review-mechanics.md)). The reviews against Telegram X's rules, the contract and the Kotlin clients were cut off by a usage limit and left nothing.

## What is not done, or weak

- [01-landscape.md](research/tdlib-survey/01-landscape.md) is a scripted classification, not the verified catalog that was planned. Only the Kotlin clients in report 02 were read.
- §6's table "everything else a chat screen needs" comes from Pingogram and hasn't been reviewed against the design.
- The clones (66 GB) exist only on the first Mac, in a temporary folder. `inventory.tsv` lists every repository.

## Left on the first Mac, not pushed

Uncommitted work that predates this session and isn't part of any push: `@KotogramPreview` previews for the auth and chats screens, `core/designsystem/.../KotogramPreview.kt`, `shared/.../root/SplashScreen.kt`, `AppFeatureModule.kt`,
`strings.xml`, and a section in `docs/architecture.md` §6.3. If those files look missing on another machine, that's why.

## Also true

- `docs/research/tdlib-clients.md` still says `updates` completes on `Closed` (05 §7).
- The local `tdlib-kmp` clone is on `feature/web-target`, which diverges from `main`; only `main` is published.
