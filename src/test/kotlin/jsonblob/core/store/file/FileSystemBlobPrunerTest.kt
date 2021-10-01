package jsonblob.core.store.file

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jsonblob.config.FileSystemJsonBlobStoreConfig
import jsonblob.core.id.IdHandler
import jsonblob.model.JsonBlob
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import javax.inject.Inject


private val log = KotlinLogging.logger {}

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class FileSystemBlobPrunerTest: TestPropertyProvider {
    companion object {
        @TempDir
        lateinit var tempDir: File
    }

    @Inject
    lateinit var config: FileSystemJsonBlobStoreConfig

    @Inject
    lateinit var store : FileSystemJsonBlobStore

    @Inject
    lateinit var idGenerator: IdHandler<*>

    @Inject
    lateinit var fileSystemBlobPruner: FileSystemBlobPruner

    @Test
    fun testPurging() {
        val jsonBlobOne = JsonBlob(
            id = idGenerator.generate(),
            json = String(javaClass.classLoader.getResourceAsStream("large-file.json").readBytes()).trimIndent()
        )
        val jsonBlobTwo = JsonBlob(
            id = idGenerator.generate(),
            json = """
                {
                    "name" : "bob",
                    "age" : 1
                }
            """.trimIndent()
        )
        store.write(jsonBlobOne)
        store.write(jsonBlobTwo)
        Thread.sleep(6000)
        store.read(jsonBlobOne.id)
        fileSystemBlobPruner.removeUnAccessedFilesSince()
        val remainingFiles = File(config.basePath)
            .walkBottomUp()
            .filterNot{ it.isDirectory }
            .asSequence()
            .toList()
        log.info { "Remaining files are $remainingFiles" }
        assertThat(remainingFiles.size).isEqualTo(1)
        assertThat(remainingFiles.map { it.blobId() }).contains(jsonBlobOne.id)
    }

    override fun getProperties(): MutableMap<String, String> = mutableMapOf(
        "file-system-blob-store.base-path" to tempDir.absolutePath,
        "json-blob.prune-enabled" to "false",
        "json-blob.delete-after" to "5s"
    )

}