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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class GeneratedPhotos : AccountUpdates {
    private val pending = MutableStateFlow(emptyMap<String, ByteArray>())

    @OptIn(ExperimentalUuidApi::class)
    fun add(jpeg: ByteArray): InputFile {
        val conversion = CONVERSION_PREFIX + Uuid.random()
        pending.update { it + (conversion to jpeg) }
        return InputFileGenerated(
            originalPath = "photo.jpg",
            conversion = conversion,
            expectedSize = jpeg.size.toLong(),
        )
    }

    override fun start(
        scope: CoroutineScope,
        client: TdClient,
    ) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            client.updatesOf<UpdateFileGenerationStart>()
                .filter { it.conversion.startsWith(CONVERSION_PREFIX) }
                .collect { generate(client, it) }
        }
    }

    private suspend fun generate(
        client: TdClient,
        update: UpdateFileGenerationStart,
    ) {
        val jpeg = pending.value[update.conversion]
        val error = if (jpeg == null) {
            Error(code = GONE, message = "The photo is no longer in memory")
        } else {
            client.sendCatching(
                WriteGeneratedFilePart(generationId = update.generationId, data = jpeg),
            ).leftOrNull()
        }
        client.sendCatching(FinishFileGeneration(generationId = update.generationId, error = error))
        pending.update { it - update.conversion }
    }
}

private const val CONVERSION_PREFIX = "kotogram_photo#"
private const val GONE = 400
