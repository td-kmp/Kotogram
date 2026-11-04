# TDLib client survey (2026-11-03)

Research behind [design/messaging-core.md](../../design/messaging-core.md).

| File | What it is | Status |
|---|---|---|
| [01-landscape.md](01-landscape.md) | All TDLib clients found, by tier | Scripted classification, mostly unverified |
| [02-kotlin-clients.md](02-kotlin-clients.md) | Kotlin and Compose clients: triage, deep reads, anti-patterns (A#), best ideas (B#), gaps (N#) | Read from source |
| [03-telegram-x.md](03-telegram-x.md) | Telegram X: entity map, flows, 35 hidden rules (TGX#) | Read from source |
| [04-unigram-tdlib-contract.md](04-unigram-tdlib-contract.md) | TDLib contract sheet (CS§), push setup, Unigram and Paper Plane (UNI#) | Read from source and official pages |
| [05-kotogram-fit.md](05-kotogram-fit.md) | The published library against Kotogram: breaks, mirror cost, module and DI constraints | Read from source |
| [06-review-mechanics.md](06-review-mechanics.md) | Adversarial review of the design's first draft: coroutines, Koin, Orbit, modules. The design now includes its fixes | Read from source |
| [inventory.tsv](inventory.tsv) | One row per scanned repository, enough to re-clone any of them | Script output |

Reading notes:

- Nothing was built or run. Consequences for other clients are inferred from their code.
- Paths like `scratchpad/...` and `clones/<owner>__<repo>` refer to temporary clones on the original machine. Re-clone with
  `git clone --depth 1 https://github.com/<owner>/<repo>`. Line numbers match the commits named at the top of each report and will drift.
- The reports use the words "the developer" and "the coordinator" for the people and agents who ran the survey.
- Their judgments of other projects are about architecture fit for Kotogram at those commits, not about the projects' worth.
- Where a report disagrees with the design, the design is newer. Report 05's holder sketch was replaced in design §3 after the review in 06.
