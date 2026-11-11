package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.ui.text.input.PlatformImeOptions

// Workaround: Compose's ContentType doesn't reach UIKit's textContentType.
internal expect val OneTimeCodeImeOptions: PlatformImeOptions?
