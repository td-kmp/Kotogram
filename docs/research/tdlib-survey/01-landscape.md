# TDLib clients: what exists (2026-11-03)

**Reliability.** The planned manual verification of this catalog was cut off. The numbers below come from a script over 1021 shallow
clones and are a lead, not a fact. Only the projects in the first table were read from source (02 §2). Everything else is "the code
mentions TDLib and these APIs".

## Method

| Step | Count |
|---|---|
| GitHub search: 18 languages, 23 phrases, 8 topics by repository; 53 code queries such as `TdApi.UpdateChatPosition`, `td_create_client_id` | 1440 unique repositories |
| Had a TDLib signal in the name, description or code | 1169 |
| After dropping obvious noise, bot-like repositories and repositories over 400 MB; plus manual additions and Telegram X forks | 1021 cloned |
| Source files match TDLib markers | 839 |

Classification from `inventory.tsv` and marker paths:

| Class | Repositories |
|---|---|
| Telegram X forks and copies (`.../challegram/telegram/Tdlib.java`) | 22, of which 13 had commits in 2026 |
| UI + at least 10 of 16 TDLib API families | 129 |
| UI + 4 to 9 families | 162 |
| Libraries, bindings, bots, tools, or too shallow to count | 513 |
| Telegram-iOS copies (not TDLib) | 13 |
| No TDLib evidence in source | 182 |

Heuristic "Kotlin-first client" (at least 2k Kotlin lines, more Kotlin than Java): 106, of which 89 alive in 2026 and 93 with Compose. This
set has known noise, such as AI-agent projects that merely mention TDLib. Primary language of the other client classes: Dart 52, TypeScript or
JavaScript 47, Swift 22, C++ 19, Python 13, Rust 9, C# 1.

Known scanner false positives: generated bindings score 16/16; Telegram-iOS copies score 16/16; the `compose` label can come from a version catalog alone.

Not covered: anything outside GitHub (F-Droid, GitLab, Codeberg), and forks not surfaced by search or the Telegram X fork listing.

## Android and Kotlin, read from source

Verdicts are from 02 §2 and §3.

| Project | Lines (main k) | Stack | Verdict |
|---|---|---|---|
| kyra-labs/pingogram-android | 250 | JNI, Compose, Android | Best architecture among Kotlin clients: reducers, history window, memoised rows, Coil fetcher. About 15 collectors |
| badnng/nngGram | 88 | JNI, CMP, JVM | One actor, one global snapshot, clean common/JVM seam. Full rebuild per event |
| JettaXP/pixelGram | 54 | tdlight, Compose Desktop | One queue for updates and answers; snapshot state as cache, on Main |
| LyoSU/hortay-android | 65 | JNI, Compose | Channel feed, not a messenger. Best file cache; cautionary pipeline |
| dracu-lah/TMPlayer | 193 | tdl-coroutines, Compose, Android+JVM | Closest stack, a media player. Best streaming; no entity cache |
| iamaritrasaha/aether | 60 | JNI, Compose | Cautionary dispatch. Good file manager, stable key, notification queue |
| MG-Devs-Team/Mategram | 70 | JNI, Compose | 1:1 mirror experiment: 77% of main code; lossy flows |
| vdggrtff/GhostGram | 14 | td_json via JNA, CMP | Only chat client with its data layer in `commonMain`; iOS is a stub |
| TGWrist/TGWrist, TGwear/TGwear | 32, 29 | JNI, Wear Compose | Constrained clients; race buffers |
| julfy-0/Yougram, 6xingyv/compound, cheipstudio-cmyk/novagram | 49, 81, 35 | Compose | Cited for anti-patterns |
| GazizVR/BirGram, omaeewa/TGram, paranoiacXD/prismGram, bling0390/tellygram, solomonBoltin/TelegramTv, ilhomsoliev/EasyTelegram, indritbashkimi/TelegramExample, tolvana/weargram | 2–13 | Compose | Small; cited for one specific point each |
| Mahmud0808/TeleDrive, nVoxel/TGDriveDemo | 48, 9 | CMP | Drive apps; Coil fetcher; the latter writes iOS repositories in Swift |
| p-vorobyev/telegram-desktop-compose-multiplatform | 2 | Spring backend, CMP | UI polls a local REST server |

Checked and not relevant to update-to-pixel: monogram-android/monogram (974★, own Rust MTProto, TDLib only for session import), NavoMessenger/Navo (250★, no source in the
repository), SpaceXC/Neogram, codegeasse1/hikari, Kaleaon/Teleflip, TimurCravtov/Telegraft, feduss/TelegramWear, daino-selvatico/DainoGram.

## Telegram X and forks

TGX-Android/Telegram-X (6158★, Java + Views) is the reference, covered in 03. Forks by diff of Kotlin files (02 §2.2): moeCrafters/moeGramX adds none;
Capricornus007/moregramX one; Ivan-k0/plumagram-android six; AmanoTeam/Telegram-X-FOSS five; fahadalijaved/XGram 83, of which Compose is settings pages only, while chat list and
history stay Views. No fork teaches anything about update-to-pixel in Compose. Others found: Arseny271, nikita-toropov, JasonKhew96, logopek/reX, iMiKED/TopicsGram-X, CalledLuca.

## Other platforms

Unverified unless a report covers them.

| Platform | Projects |
|---|---|
| Windows | UnigramDev/Unigram (5494★, covered in 04) |
| Linux | paper-plane-developers/paper-plane (1066★, covered in 04), shinycake/quill |
| Web | evgeny-nadymov/telegram-react (2605★, last commit 2021), DIBgram/DIBgram |
| Flutter | iebb/mithka (689★), HandyGram/HandyGram (254★), ivk1800/telegram-flutter (82★), daveragos/gramx |
| iOS, macOS, watchOS | levochkaa/BetterTG (94★), mock-foundation/moc (147★), mercurytelegram/Mercury |
| Sailfish, MeeGo | Wunderfitz/harbour-fernschreiber, blacksailer/depecher, RootGPT-YouTube/RooTelegram-SailfishOS, qtinsider/meegram2 |
| Terminal, editors | zevlg/telega.el (1270★), d99kris/nchat, paul-nameless/tg, k4dy/telegramtui, ChuYanLon/telegram.nvim |
| HarmonyOS | hubery-code/Telegram-X-HarmonyNext |

## Libraries

Kotlin and JVM (02 §2.2): g000sha256/tdl-coroutines (KMP, closest neighbour of tdlib-kmp; typed flows invite per-type collectors), tdlibx/td-ktx (`DROP_OLDEST`, don't imitate), whyoleg/ktd (2021,
replays everything), tdlight-team/tdlight-java (`SimpleTelegramClient`, synchronous fan-out), TGX-Android/tdlib-utils (pure helpers), tdlib-kmp itself.

Other languages (comparison in [../tdlib-clients.md](../tdlib-clients.md)): eilvelia/tdl, pytdbot/client, FedericoBruzzone/tdlib-rs, zelenin/go-tdlib, egramtel/tdsharp, Swiftgram/TDLibKit, ivk1800/tdlib-dart.

## Look-alikes that are not TDLib

Telegram-iOS copies and DrKLO/Telegram forks (Nekogram, Cherrygram and the like) use their own MTProto stacks. So do MadelineProto, gotd/td and WTelegramClient. Official Telegram clients were not cloned, for the same reason.

## Worth studying, for Kotlin Multiplatform and Compose

1. **Telegram X**: the rules. What TDLib really does, learned the hard way (03 §4).
2. **Pingogram**: the closest working design, and what to change about it (02 §3.1).
3. **Unigram**: td_json in production; history windows; DI per account (04 §3).
4. **nngGram**: single-writer plus common/JVM seam, and direct evidence of that design's weakness (02 §3.2).
5. **TMPlayer**: tdl-coroutines stack and streaming (02 §3.8).
6. **Hortay**: file cache and the TDLib issue citations in its comments (02 §3.4).

Unread and possibly worth a look: iebb/mithka, shinycake/quill, levochkaa/BetterTG (the only serious-looking iOS TDLib candidate here).
