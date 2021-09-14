package jsonblob.core.compression

import jsonblob.core.compression.compressor.BlobCompressor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import javax.inject.Singleton


private val log = KotlinLogging.logger {}

@Singleton
class BlobCompressorPicker(
    private val compressors: List<BlobCompressor>
) {
    fun bestCompressor(json: String) : BlobCompressor {
        return runBlocking {
            val compressorsToBytes = compressors.map {
                async {
                    val bytes = ByteArrayOutputStream().apply {
                        it.getOutputStream(this).use {
                            it.write(json.toByteArray())
                        }
                    }
                    log.debug { "${it::class.simpleName} compressed JSON (${json.toByteArray().size} bytes) to ${bytes.size()} bytes" }
                    Pair(it, bytes)
                }
            }.awaitAll()
            val sorted = compressorsToBytes.sortedBy { (_, value) -> value.size()}.toMap()
            val best = sorted.entries.first()
            best.key
        }
    }
}