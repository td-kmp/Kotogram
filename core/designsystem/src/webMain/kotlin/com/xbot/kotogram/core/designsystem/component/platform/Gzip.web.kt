package com.xbot.kotogram.core.designsystem.component.platform

import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.js.Promise

internal actual suspend fun gunzip(bytes: ByteArray): ByteArray {
    val input = Uint8Array(bytes.size)
    bytes.forEachIndexed { index, byte -> input[index] = byte }
    val output = Uint8Array(decompress(input).await<ArrayBuffer>())
    return ByteArray(output.length) { output[it] }
}

@Suppress("UnusedParameter")
private fun decompress(bytes: Uint8Array): Promise<ArrayBuffer> = js(
    "new Response(new Blob([bytes]).stream().pipeThrough(new DecompressionStream('gzip'))).arrayBuffer()",
)
