package jsonblob.core.store;

import jsonblob.core.compression.BlobCompressorPicker
import jsonblob.core.compression.compressor.BlobCompressor
import jsonblob.core.id.IdHandler
import jsonblob.model.JsonBlob
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.io.InputStream


private val log = KotlinLogging.logger {}

abstract class JsonBlobStore(
    basePath: String,
    idResolvers: List<IdHandler<*>>,
    private val compressorPicker: BlobCompressorPicker,
) : JsonBlobBase(basePath, idResolvers) {
    abstract fun retrieve(id: String): InputStream?
    abstract fun store(id: String, json: String, compressor: BlobCompressor): Boolean
    abstract fun remove(id: String): Boolean
    abstract fun path(id: String): String?

    fun exists(id: String) = path(id) != null

    fun read(id: String): JsonBlob? {
        return kotlin.runCatching {
            val created = resolveTimestamp(id)
            JsonBlob(
                id = id,
                json = json(retrieve(id) ?: throw IllegalStateException("Json blob $id doesn't exist")),
                created = created
            )
        }.onFailure {
            log.warn { "Couldn't read JsonBlob with id=$id " }
        }.getOrNull()
    }

    fun write(jsonBlob: JsonBlob): JsonBlob {
        return kotlin.runCatching {
            val compressor = compressorPicker.bestCompressor(jsonBlob.json)
            if (!store(jsonBlob.id, jsonBlob.json, compressor)) {
                throw IllegalStateException("Couldn't store blob with id=${jsonBlob.id}")
            }
            JsonBlob(
                id = jsonBlob.id,
                json = jsonBlob.json,
                created = resolveTimestamp(jsonBlob.id)
            )
        }.onFailure {
            log.warn(it) { "Couldn't write JsonBlob with id=${jsonBlob.id} " }
        }.getOrElse {
            throw IllegalStateException("Could not store json blob ${jsonBlob.id}")
        }
    }

    private fun json(inputStream: InputStream) = ByteArrayOutputStream()
        .apply {
            inputStream.copyTo(this)
        }.toString()
}
