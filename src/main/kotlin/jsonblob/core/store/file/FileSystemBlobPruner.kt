package jsonblob.core.store.file

import com.google.common.base.Stopwatch
import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jsonblob.config.FileSystemJsonBlobStoreConfig
import jsonblob.config.JsonBlobConfig
import jsonblob.core.id.IdHandler
import jsonblob.core.store.JsonBlobBase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


private val log = KotlinLogging.logger {}

@Singleton
@Requires(beans = [FileSystemJsonBlobStoreConfig::class])
class FileSystemBlobPruner(
    private val idResolvers: List<IdHandler<*>>,
    private val config: FileSystemJsonBlobStoreConfig,
    private val jsonBlobConfig: JsonBlobConfig,
): JsonBlobBase(config.basePath, idResolvers) {

    @Scheduled(fixedDelay = "6h", initialDelay = "1m")
    fun removeUnAccessedFiles() {
        if (jsonBlobConfig.pruneEnabled) {
            Stopwatch.createStarted().apply {
                removeUnAccessedFilesSince()
                log.info { "Removing unaccessed files took ${this.elapsed(TimeUnit.SECONDS)} seconds" }
            }
        }
    }

    @OptIn(FlowPreview::class)
    internal fun removeUnAccessedFilesSince() {
        val deleteBefore = Instant.now().minus(jsonBlobConfig.deleteAfter)
        log.info { "Removing blobs not accessed since $deleteBefore" }
        val baseDir = File(config.basePath)
        val blobFiles = baseDir
            .walkBottomUp()
            .filterNot { it == baseDir }
            .asSequence()
            .asFlow()

        val count = runBlocking {
            flow {
                emitAll(blobFiles)
            }.flatMapMerge(config.deleteConcurrency) { file ->
                flow {
                    try {
                        val attrs = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
                        if (attrs.isDirectory && file.listFiles().isEmpty()) { // empty directory
                            emit(file)
                        } else if (!attrs.isDirectory) {
                            if (idResolvers.any { it.handles(file.blobId()) }) { // a json blob
                                val lastAccessed = attrs.lastAccessTime().toInstant()
                                when {
                                    lastAccessed == Instant.EPOCH -> log.warn { "Last Access Time was the Epoch, which typically means lastAccessTime cannot be determined" }
                                    lastAccessed.isBefore(deleteBefore) -> emit(file)
                                }
                            } else { // some other file... likely old metadata about blobs from previous versions of json blob
                                emit(file)
                            }
                        }
                    } catch (e: Exception) {
                        log.warn(e) { "Caught exception while checking $file to see if it needed to be pruned" }
                    }
                }
            }.count {
                log.info { "Deleting ${it.path}" }
                it.delete()
            }
        }
        log.info { "Completed removing $count files not accessed since $deleteBefore" }
    }

}

internal fun File.blobId(): String = runCatching {
    name.substring(0, name.indexOf("."))
}.getOrElse { throw IllegalStateException("$name doesn't look like a JSON Blob Id") }