package jsonblob.core.compression

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jsonblob.core.compression.compressor.BrotliBlobCompressor
import jsonblob.core.compression.compressor.NoCompressionJsonBlobCompressor
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.inject.Inject
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory


private val log = KotlinLogging.logger {}

@ExperimentalPathApi
@MicronautTest
class BlobCompressorPickerTest : TestPropertyProvider {
    var tempDir = createTempDirectory(javaClass.simpleName)

    @Inject
    lateinit var compressorPicker: BlobCompressorPicker

    @Test
    fun test() {
        val json = """
            {
                "name" : "bob",
                "age": 38
            }
        """.trimIndent()
        val compressor = compress(json)
        assertThat(compressor).isInstanceOf(NoCompressionJsonBlobCompressor::class.java)
    }

    @Test
    fun largeFile() {
        val largeFile = javaClass.classLoader.getResourceAsStream("large-file.json")
        val compressor = compress(String(largeFile.readBytes()))
        assertThat(compressor).isInstanceOf(BrotliBlobCompressor::class.java)
    }

    private fun compress(json: String) = compressorPicker.bestCompressor(json)

    override fun getProperties(): MutableMap<String, String> = mutableMapOf(
        "file-system-blob-store.base-path" to tempDir.absolutePathString()
    )
}