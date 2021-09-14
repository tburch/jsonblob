package jsonblob.core.store.file

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
            removeUnAccessedFilesSince()
        }
    }

    @OptIn(FlowPreview::class)
    internal fun removeUnAccessedFilesSince() {
        val timestamp = Instant.now().minus(jsonBlobConfig.deleteAfter)
        log.info { "Removing blobs not accessed since $timestamp" }
        val baseDir = File(config.basePath)
        val blobFiles = baseDir
            .walkBottomUp()
            .filterNot { it == baseDir }
            .asSequence()
            .asFlow()

        val count = runBlocking {
            flow {
                emitAll(blobFiles)
            }.flatMapMerge(jsonBlobConfig.deleteConcurrency) { file ->
                flow {
                    val attrs = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
                    if (attrs.isDirectory && file.listFiles().isEmpty()) { // empty directory
                        emit(file)
                    } else {
                        if (idResolvers.any { it.handles(file.nameWithoutExtension) }) { // a json blob
                            val lastAccessed = attrs.lastAccessTime().toInstant()
                            when {
                                lastAccessed == Instant.EPOCH -> log.warn { "Last Access Time was the Epoch, which typically means lastAccessTime cannot be determined" }
                                lastAccessed.isBefore(timestamp) -> emit(file)
                            }
                        } else { // some other file... likely old metadata about blobs
                            emit(file)
                        }
                    }
                }
            }.count {
                log.debug { "Deleting ${it.path}" }
                it.delete()
            }
        }
        log.info { "Completed removing $count files not accessed since $timestamp" }
    }

}