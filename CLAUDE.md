# Kotogram

Telegram client on TDLib: Compose Multiplatform for Android, iOS and JVM desktop, using the developer's own library `tdlib-kmp`.

**Start with [docs/handoff.md](docs/handoff.md)**: current status and next steps. Architecture: [docs/architecture.md](docs/architecture.md), accounts: [docs/design/multiaccount.md](docs/design/multiaccount.md).

The developer writes in Russian; answer in Russian. Docs and code are in English.

## Process

- `main` is protected: PRs only. Branches: `feature/KTG-N-short-name`, `bugfix/…`, `refactoring/…`.
- Commits and PR titles: `KTG-N: message`. Next number is in `docs/handoff.md`.
- Commit or push only when asked.
- **Keep subagents few.** A fan-out of five agents with follow-ups burned a full usage window. One or two focused agents, and only when asked. Don't resume an agent with a huge context; brief a new one narrowly.
- Ask before the first build in a session. The developer has been keeping builds out of design work.

## Rules

- **TDLib is the source of truth.** As few own flags, enums and manifests as possible; derive from TDLib states or files on disk.
- **The published `tdlib-kmp` is a given.** Decide how to use it. If it really blocks something, report that as a finding, separate from the design. Read its published sources, not local branches: those were prototypes.
- **No ad-hoc patterns.** Back architecture choices with official docs, library sources or reference apps, and say plainly when something is your own design. Mark unverified claims.
- **Repositories** are an interface plus one `Default…Repository` depending on the account's `TdClient`. Commands return `Either<Error, T>` (Arrow).
- **The generated `io.xbot.tdlib.api` classes are the domain model** (own copies are too expensive). No hand-written mirrors, no `Td` prefix on own classes. List rows get plain row models built off-main by a memoising mapper; simple screens may read DTOs. Only `core:account` and `feature:x:data` touch `io.xbot.tdlib.client.*` (a detekt `ForbiddenImport` enforces it).
- **DI:** Koin's DSL, one `val <feature><Layer>Module` per module (`authDataModule`, `authImplModule`). Everything of one account goes in `scope(AccountScope)`. Screens are `navigation<Route>` entries and keys `polymorphic<NavKey>` in the impl module, as in AnilibriaRefresh; `shared` assembles them. Entries resolve ViewModels with plain `koinViewModel()`, and only inside navigation entries: their stores are what keeps accounts apart. A follower of an account's updates binds `AccountUpdates`. Nothing checks the graph at compile time: `KoinGraphTest` does. `getAll` gives no order.
- **Layout:** `android-app`, `jvm-app`, `web-app`, `shared` at top level; `core:account` (accounts and sessions, no UI, exposes TDLib's classes), `core:navigation:{api,impl}`, `core:designsystem`; `feature:x:{api,domain,data,impl}`. A feature owns its entity: repositories live in features, not in core. `impl` sees other features only through `api` and `domain`, never any `data`, and sees `core:account` only for `AccountScope` in its `di`.
- **Code style:** androidx. Abstraction first (interface, then `Default…`), narrow read interfaces with `Mutable…`/richer internal ones, delegation over inheritance chains, one responsibility per class. Extensions go in each module's `extensions` package, one `<Receiver>Ext.kt` per receiver type; `platform*` expect/actual go in its `di` package.
- **Web:** `wasmJs` target. TDLib runs in the worker in `core/account/worker` (copied from tdlib-kmp; its `package.json` URL must match the `<TDLib>` part of the library version). The worker owns the files, so the web's `AccountStorage` only names directories under `/tdlib`. DataStore holds the account registry everywhere; on the web through `WebLocalStorage`, never `createWithPath`, which means `sessionStorage` there. Only one tab runs TDLib. No `runBlocking`, `SystemFileSystem` or `Dispatchers.IO` in common code.
- **Test DC** is a build flag (`telegram.test.dc` → BuildKonfig `USE_TEST_DC`). API keys: `telegram.api.id` and `telegram.api.hash` in `local.properties`, via BuildKonfig. Never commit them.
- **Desktop:** Nucleus window, no AWT, no WebView. CMP is pinned to 1.12.1 and material3 to 1.12.0-alpha03: Nucleus 2.5 crashes on CMP 1.13 (Nucleus#671).
- **Passkeys can't work** for third-party clients: Telegram's assetlinks and AASA trust only official apps.
- **Reference client:** Telegram X for TDLib behavior. Reproduce behavior behind Kotogram's API; don't copy code.
- **Comments** only for non-obvious hacks and workarounds, one short line. No KDoc walls, no rationale essays.
