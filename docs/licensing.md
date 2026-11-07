# Licensing

Kotogram is to be GPL (decided 2026-11-06); the version isn't fixed yet and there is no `LICENSE` file. Nothing is distributed for now.

## Borrowed material

| File | Source | Its license |
|---|---|---|
| `core/resources/src/commonMain/composeResources/values/strings.xml` (entries with a `key`) | Keys and English texts of Telegram's `macos` language pack, from Telegram's servers through TDLib (the texts of telegram-macos's `en.lproj`) | Not stated by the servers; GPL-2.0, version 2 only in telegram-macos |
| `core/resources/src/commonMain/composeResources/files/qr_placeholder.lottie` | Telegram for macOS, `Resources/tgs/qrcode_matrix.tgs` ([telegram-macos](https://github.com/TelegramMessenger/telegram-macos)), unchanged, repacked from gzip into a dotLottie zip | GPL-2.0, version 2 only by its README |
| `core/designsystem/.../component/QrCodeShapes.kt`, `QrCodeGrid.kt` | The way telegram-macos draws its QR codes (`packages/QRCodeUtils/Sources/QRCodeUtils/QRCode.swift`: module corners, cutout, whole-pixel grid) and the finders of `qrcode_matrix.tgs`, written anew as qrose shapes; no code copied | GPL-2.0, version 2 only in telegram-macos, if the geometry counts |

Before anything is distributed: GPL-2.0-only material allows Kotogram only GPL-2.0, while GPL-3.0 suits the Apache-2.0 dependencies (AndroidX, Compose, Koin). For GPL-3.0, replace the animation with Telegram for Android's `TMessagesProj/src/main/res/raw/qr_matrix.json` ("GPL v. 2 or later", but it renders worse) or Unigram's `Telegram/Assets/Animations/Qr.tgs` (GPL-3.0, same animation as macOS with small differences).

## Libraries used for the QR code

- `io.github.alexzhirkevich:qrose` and `qrose-encoder-matrix` 1.3.1, MIT: the code and its drawing, with shapes of our own.
- `io.github.alexzhirkevich:compottie` and `compottie-dot` 2.3.3, MIT: the placeholder animation.
