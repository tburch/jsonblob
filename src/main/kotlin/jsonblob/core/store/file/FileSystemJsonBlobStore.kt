package jsonblob.core.store.file

import com.google.common.util.concurrent.Striped
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.Order
import io.micronaut.core.order.Ordered
import jsonblob.config.FileSystemJsonBlobStoreConfig
import jsonblob.core.compression.BlobCompressorPicker
import jsonblob.core.compression.compressor.BlobCompressor
import jsonblob.core.id.IdHandler
import jsonblob.core.store.JsonBlobStore
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.time.Instant
import java.time.temporal.Temporal
import javax.inject.Singleton


private val log = KotlinLogging.logger {}

@Singleton
@Primary
@Order(Ordered.HIGHEST_PRECEDENCE)
@Requires(beans = [FileSystemJsonBlobStoreConfig::class])
open class FileSystemJsonBlobStore(
    private val config: FileSystemJsonBlobStoreConfig,
    idResolvers: List<IdHandler<*>>,
    compressorPicker: BlobCompressorPicker,
    private val blobCompressors: List<BlobCompressor>,
) : JsonBlobStore(config.basePath, idResolvers, compressorPicker) {

    init {
        log.info { "Creating ${config.basePath} if it doesn't already exist" }
        File(config.basePath).mkdirs()
    }

    private val blobStripedLocks = Striped.lazyWeakReadWriteLock(config.stripes)

    override fun retrieve(id: String): InputStream? {
        return kotlin.runCatching {
            val created = resolveTimestamp(id)
            val file = getBlobFile(created, id) ?: throw NoSuchElementException("No blob with id $id")

            val compressor = blobCompressors.find { it.handles(file.extension) }
                ?: throw IllegalStateException("No Blob compressor for .${file.extension}")

            log.debug { "Reading blob from ${file.absolutePath} using ${compressor::class.simpleName}" }

            val lock = blobStripedLocks[file.absolutePath].readLock()
            try {
                lock.lock()

                compressor.getInputStream(FileInputStream(file))
            } finally {
                lock.unlock()
            }
        }.onFailure {
            log.warn(it) { "Couldn't retrieve JsonBlob with id=$id " }
        }.getOrNull()
    }

    override fun store(id: String, json: String, compressor: BlobCompressor): Boolean {
        val created = resolveTimestamp(id)
        val file = File(getDataDirectory(created).apply { mkdirs() }, "$id.${compressor.getFileExtension()}")

        mutableListOf(*blobFiles(created, id).toTypedArray()).apply {
            remove(file)
        }.forEach {
            it.delete()
        }

        log.debug { "Storing blob to ${file.absolutePath} using ${compressor::class.simpleName}" }

        val lock = blobStripedLocks[file.absolutePath].writeLock()

        try {
            lock.lock()

            DataOutputStream(compressor.getOutputStream(FileOutputStream(file))).use {
                it.write(json.toByteArray())
            }

            return true.apply {
                log.debug { "Stored blob to ${file.absolutePath}" }
            }
        } finally {
            lock.unlock()
        }
    }

    override fun remove(id: String): Boolean {
        val created = resolveTimestamp(id)
        getBlobFile(created, id)?.let {
            val lock = blobStripedLocks[it.absolutePath].writeLock()
            try {
                lock.lock()
                return kotlin.runCatching {
                    it.delete()
                }.getOrElse { false }
            } finally {
                lock.unlock()
            }
        }
        return false
    }

    override fun path(id: String): String? {
        val created = resolveTimestamp(id)
        val file = getBlobFile(created, id) ?: return null
        return if (blobCompressors.any { it.handles(file.extension) }) {
            file.absolutePath
        } else {
            null
        }
    }

    private fun getDataDirectory(timestamp: Temporal) = File(getPrefix(timestamp))

    private fun getBlobFile(
        created: Instant,
        id: String
    ): File? {
        return kotlin.runCatching {
            runBlocking {
                blobFiles(created, id).maxByOrNull {
                    it.lastModified()
                }!!
            }
        }.getOrNull()
    }

    private fun blobFiles(created: Instant, id: String) = blobCompressors
        .map {
            File(getDataDirectory(created), "$id.${it.getFileExtension()}")
        }.filter {
            it.exists()
        }

}

data class FileSystemJsonBlobAccessedEvent(
    val id: String,
    val timestamp: Instant = Instant.now()
)