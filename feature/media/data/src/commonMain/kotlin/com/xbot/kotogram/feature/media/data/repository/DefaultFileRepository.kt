package com.xbot.kotogram.feature.media.data.repository

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.xbot.kotogram.core.account.extensions.sendCatching
import com.xbot.kotogram.feature.media.domain.model.MediaError
import com.xbot.kotogram.feature.media.domain.repository.FileRepository
import io.xbot.tdlib.api.CancelDownloadFile
import io.xbot.tdlib.api.DownloadFile
import io.xbot.tdlib.api.File
import io.xbot.tdlib.api.ReadFilePart
import io.xbot.tdlib.api.UpdateFile
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class DefaultFileRepository(
    private val client: TdClient,
) : FileRepository {
    private val waiters = Mutex()
    private val waitingFor = mutableMapOf<Int, Int>()

    override suspend fun download(file: File): Either<MediaError, File> {
        if (file.local.isDownloadingCompleted) return file.right()
        waiters.withLock { waitingFor[file.id] = (waitingFor[file.id] ?: 0) + 1 }
        var result: Either<MediaError, File>? = null
        try {
            return awaitDownload(file).also { result = it }
        } finally {
            withContext(NonCancellable) {
                val last = waiters.withLock {
                    val left = waitingFor.getValue(file.id) - 1
                    if (left == 0) waitingFor.remove(file.id) else waitingFor[file.id] = left
                    left == 0
                }
                if (last &&
                    result == null
                ) {
                    client.sendCatching(CancelDownloadFile(fileId = file.id))
                }
            }
        }
    }

    override suspend fun readBytes(file: File): Either<MediaError, ByteArray> =
        client.sendCatching(ReadFilePart(fileId = file.id))
            .mapLeft { MediaError.Unknown(it.message) }
            .map { it.data }

    private suspend fun awaitDownload(file: File): Either<MediaError, File> = coroutineScope {
        // Workaround: updates have no replay, so the wait subscribes before the request.
        val settled = async(start = CoroutineStart.UNDISPATCHED) {
            client.updatesOf<UpdateFile>()
                .map { it.file }
                .first { it.id == file.id && it.isSettled }
        }
        val request = DownloadFile(fileId = file.id, priority = PRIORITY_IMAGE)
        val result = client.sendCatching(request)
            .mapLeft { MediaError.Unknown(it.message) }
            .map { started -> if (started.isSettled) started else settled.await() }
        settled.cancel()
        result.flatMap { downloaded ->
            if (downloaded.local.isDownloadingCompleted) {
                downloaded.right()
            } else {
                MediaError.DownloadFailed.left()
            }
        }
    }
}

private const val PRIORITY_IMAGE = 15

private val File.isSettled: Boolean
    get() = local.isDownloadingCompleted || !local.canBeDownloaded
