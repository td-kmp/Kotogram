package com.xbot.kotogram.core.designsystem.component.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.zlib.MAX_WBITS
import platform.zlib.ZLIB_VERSION
import platform.zlib.Z_NO_FLUSH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2_
import platform.zlib.z_stream

@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun gunzip(bytes: ByteArray): ByteArray = withContext(Dispatchers.IO) {
    memScoped {
        val stream = alloc<z_stream>()
        // inflateInit2 is a macro; 16 over the window bits reads a gzip header.
        check(
            inflateInit2_(
                stream.ptr,
                GZIP_HEADER + MAX_WBITS,
                ZLIB_VERSION,
                sizeOf<z_stream>().toInt()
            ) ==
                Z_OK
        )
        val parts = mutableListOf<ByteArray>()
        val chunk = ByteArray(CHUNK)
        try {
            bytes.usePinned { input ->
                chunk.usePinned { output ->
                    stream.next_in = input.addressOf(0).reinterpret()
                    stream.avail_in = bytes.size.convert()
                    do {
                        stream.next_out = output.addressOf(0).reinterpret()
                        stream.avail_out = CHUNK.convert()
                        val status = inflate(stream.ptr, Z_NO_FLUSH)
                        check(status == Z_OK || status == Z_STREAM_END) {
                            "inflate failed: $status"
                        }
                        parts += chunk.copyOf(CHUNK - stream.avail_out.toInt())
                    } while (status != Z_STREAM_END)
                }
            }
        } finally {
            inflateEnd(stream.ptr)
        }
        ByteArray(parts.sumOf { it.size }).also { result ->
            var offset = 0
            parts.forEach {
                it.copyInto(result, offset)
                offset += it.size
            }
        }
    }
}

private const val GZIP_HEADER = 16
private const val CHUNK = 64 * 1024
