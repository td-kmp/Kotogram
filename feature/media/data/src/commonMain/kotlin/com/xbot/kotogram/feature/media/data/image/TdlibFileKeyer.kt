package com.xbot.kotogram.feature.media.data.image

import coil3.key.Keyer
import coil3.request.Options
import io.xbot.tdlib.api.File

internal class TdlibFileKeyer(private val prefix: String) : Keyer<File> {
    override fun key(data: File, options: Options): String? =
        data.remote.uniqueId.takeIf { it.isNotEmpty() }?.let { prefix + it }
}
