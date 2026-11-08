package com.xbot.kotogram.feature.auth.data.account

import com.xbot.kotogram.core.account.AccountUpdates
import com.xbot.kotogram.core.account.extensions.sendCatching
import io.xbot.tdlib.api.Error
import io.xbot.tdlib.api.FinishFileGeneration
import io.xbot.tdlib.api.InputFile
import io.xbot.tdlib.api.InputFileGenerated
import io.xbot.tdlib.api.UpdateFileGenerationStart
import io.xbot.tdlib.api.WriteGeneratedFilePart
import io.xbot.tdlib.client.TdClient
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Hands photos in memory to TDLib as generated files, which it asks for by conversion: the web's worker owns
// the file system, so writing a file of our own isn't an option there.
internal class GeneratedPhotos : AccountUpdates {
    private val pending = MutableStateFlow(emptyMap<String, ByteArray>())

    @OptIn(ExperimentalUuidApi::class)
    fun add(jpeg: ByteArray): InputFile {
        val conversion = CONVERSION_PREFIX + Uuid.random()
        pending.update { it + (conversion to jpeg) }
        // TDLib takes the name and MIME type from the original path only.
        return InputFileGenerated(
            originalPath = "photo.jpg",
            conversion = conversion,
            expectedSize = jpeg.size.toLong()
        )
    }

    override fun start(scope: CoroutineScope, client: TdClient) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            client.updatesOf<UpdateFileGenerationStart>()
                .filter { it.conversion.startsWith(CONVERSION_PREFIX) }
                .collect { generate(client, it) }
        }
    }

    private suspend fun generate(client: TdClient, update: UpdateFileGenerationStart) {
        // TDLib asks again after a restart, when the photo is gone with the memory.
        val jpeg = pending.value[update.conversion]
        val error = if (jpeg == null) {
            Error(code = GONE, message = "The photo is no longer in memory")
        } else {
            client.sendCatching(WriteGeneratedFilePart(generationId = update.generationId, data = jpeg)).leftOrNull()
        }
        client.sendCatching(FinishFileGeneration(generationId = update.generationId, error = error))
        pending.update { it - update.conversion }
    }
}

private const val CONVERSION_PREFIX = "kotogram_photo#"
private const val GONE = 400
