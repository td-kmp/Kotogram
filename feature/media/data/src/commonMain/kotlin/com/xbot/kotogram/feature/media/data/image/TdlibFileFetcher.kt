package com.xbot.kotogram.feature.media.data.image

import arrow.core.getOrElse
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.xbot.kotogram.feature.media.data.di.platformReadsFilePaths
import com.xbot.kotogram.feature.media.domain.model.MediaError
import com.xbot.kotogram.feature.media.domain.repository.FileRepository
import io.xbot.tdlib.api.File
import okio.Buffer
import okio.Path.Companion.toPath

internal class TdlibFileFetcher(
    private val file: File,
    private val options: Options,
    private val repository: FileRepository,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val wasLocal = file.local.isDownloadingCompleted
        val downloaded = repository.download(file).getOrElse { throw FileLoadException(it) }
        val source = if (platformReadsFilePaths) {
            ImageSource(downloaded.local.path.toPath(), options.fileSystem)
        } else {
            val bytes = repository.readBytes(downloaded).getOrElse { throw FileLoadException(it) }
            ImageSource(Buffer().write(bytes), options.fileSystem)
        }
        return SourceFetchResult(
            source = source,
            mimeType = null,
            dataSource = if (wasLocal) DataSource.DISK else DataSource.NETWORK,
        )
    }

    class Factory(
        private val repository: FileRepository,
    ) : Fetcher.Factory<File> {
        override fun create(
            data: File,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher = TdlibFileFetcher(data, options, repository)
    }
}

class FileLoadException(
    val error: MediaError,
) : Exception(error.toString())
