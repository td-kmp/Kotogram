package com.xbot.kotogram.core.designsystem.component.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.zip.GZIPInputStream

internal actual suspend fun gunzip(bytes: ByteArray): ByteArray =
    withContext(Dispatchers.IO) { GZIPInputStream(bytes.inputStream()).use { it.readBytes() } }
