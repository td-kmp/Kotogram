package com.xbot.kotogram.feature.media.domain.repository

import arrow.core.Either
import com.xbot.kotogram.feature.media.domain.model.MediaError
import io.xbot.tdlib.api.File

interface FileRepository {
    suspend fun download(file: File): Either<MediaError, File>

    suspend fun readBytes(file: File): Either<MediaError, ByteArray>
}
