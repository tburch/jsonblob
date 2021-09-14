package jsonblob.core.store.file

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jsonblob.core.id.IdHandler
import jsonblob.model.JsonBlob
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import org.skyscreamer.jsonassert.JSONAssert
import java.io.File
import java.time.Instant
import javax.inject.Inject
import kotlin.io.path.ExperimentalPathApi


@ExperimentalPathApi
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class FileSystemJsonBlobBaseTest : TestPropertyProvider {
    companion object {
        @TempDir
        lateinit var tempDir: File
    }

    @Inject
    lateinit var store : FileSystemJsonBlobStore

    @Inject
    lateinit var idGenerator: IdHandler<*>

    @Test
    fun testBasicFunctionality() {
        val jsonBlob = JsonBlob(
            id = idGenerator.generate(),
            json = """
                {
                    "name" : "bob",
                    "bob" : "name",
                    "foo" : "age",
                    "age": 38
                }
            """.trimIndent(),
            created = Instant.now()
        )
        Assertions.assertThat(store.path(jsonBlob.id)).isNull()
        val writtenBlob = store.write(jsonBlob)
        Assertions.assertThat(store.path(jsonBlob.id)).isNotNull
        Assertions.assertThat(writtenBlob.id).isEqualTo(jsonBlob.id)
        JSONAssert.assertEquals(jsonBlob.json, writtenBlob.json, true)
        val receivedBlob = store.read(jsonBlob.id) ?: Assertions.fail("Didn't retrieve blob")
        Assertions.assertThat(receivedBlob.id).isEqualTo(jsonBlob.id)
        JSONAssert.assertEquals(jsonBlob.json, receivedBlob.json, true)
        Assertions.assertThat(store.remove(jsonBlob.id)).isTrue
        Assertions.assertThat(store.path(jsonBlob.id)).isNull()
    }

    override fun getProperties(): MutableMap<String, String> = mutableMapOf(
        "file-system-blob-store.base-path" to tempDir.absolutePath
    )
}