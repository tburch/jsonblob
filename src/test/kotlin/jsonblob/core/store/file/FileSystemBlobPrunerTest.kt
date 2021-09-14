package jsonblob.core.store.file

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jsonblob.config.FileSystemJsonBlobStoreConfig
import jsonblob.core.id.IdHandler
import jsonblob.model.JsonBlob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import javax.inject.Inject

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
            json = """
                {
                    "name" : "bob",
                    "age" : 1
                }
            """.trimIndent()
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
            .filter{ !it.isDirectory }
            .asSequence()
            .toList()
        assertThat(remainingFiles.size).isEqualTo(1)
        assertThat(remainingFiles.map { it.nameWithoutExtension }).contains(jsonBlobOne.id)
    }

    override fun getProperties(): MutableMap<String, String> = mutableMapOf(
        "file-system-blob-store.base-path" to tempDir.absolutePath,
        "json-blob.prune-enabled" to "false",
        "json-blob.delete-after" to "5s"
    )

}