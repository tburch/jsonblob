package jsonblob.api.http

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpRequest.DELETE
import io.micronaut.http.HttpRequest.POST
import io.micronaut.http.HttpRequest.PUT
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jsonblob.config.S3ClientBuilderListener
import jsonblob.core.compression.compressor.GZIPBlobCompressor
import jsonblob.core.id.Type1UUIDJsonBlobHandler
import jsonblob.core.store.JsonBlobStore
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.shaded.com.google.common.io.Files
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.services.s3.S3Client
import java.util.UUID
import javax.inject.Inject


private val log = KotlinLogging.logger {}

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiTest: TestPropertyProvider {
    private val tempDir = Files.createTempDir().apply { deleteOnExit() }

    private val bucket = "fubar"

    private val json = """
        {
            "name" : "bob",
            "age": 38
        }
    """.trimIndent()

    private val updateJson = json.replace("38", "39")

    @Inject
    lateinit var type1UUIDJsonBlobHandler: Type1UUIDJsonBlobHandler

    @Inject
    lateinit var blobStore : JsonBlobStore

    @Inject
    lateinit var gzipBlobCompressor: GZIPBlobCompressor

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Test
    fun `new blobs created via API POST are created`() {
        val resp = client
            .toBlocking()
            .exchange(POST("/api/jsonBlob", json).contentType(MediaType.APPLICATION_JSON_TYPE), String::class.java)
        assertThat(resp.code()).isEqualTo(201)
        val locationHeader = resp.header("Location").also { log.info { "Location header was $it" } }
        assertThat(locationHeader).isNotEmpty
        assertEquals(json, resp.body(), true)

        val id = locationHeader.substringAfterLast("/")
        assertThat(blobStore.exists(id)).isTrue
    }

    private fun validateGet(request: (blobId: String) -> String) {
        val id = type1UUIDJsonBlobHandler.generate()
        assertThat(blobStore.store(id, json, gzipBlobCompressor)).isTrue
        assertThat(blobStore.exists(id)).isTrue
        val body = request.invoke(id)
        assertEquals(json, body, true)

        assertThat(blobStore.exists(id)).isTrue
    }

    @Test
    fun `blob is retrieved for standard API GET`() {
        validateGet {
            client.toBlocking().retrieve("/api/jsonBlob/$it")
        }
    }

    @Test
    fun `blob is retrieved for custom API GET`() {
        validateGet {
            val request = HttpRequest.GET<String>("/api/company/$it/employees/engineers")
            client.toBlocking().retrieve(request)
        }
    }

    @Test
    fun `blob is retrieved for custom API GET with X-jsonblob header`() {
        validateGet {
            val request = HttpRequest.GET<String>("/api/company/employees/engineers").header("X-jsonblob", it)
            client.toBlocking().retrieve(request)
        }
    }

    private fun validateUpdate(request: (blobId: String) -> HttpResponse<String>) {
        val id = type1UUIDJsonBlobHandler.generate()
        assertThat(blobStore.store(id, json, gzipBlobCompressor)).isTrue
        assertThat(blobStore.exists(id)).isTrue

        val resp = request.invoke(id)

        assertThat(resp.code()).isEqualTo(200)
        assertEquals(updateJson, resp.body(), true)

        assertThat(blobStore.exists(id)).isTrue
        assertEquals(updateJson, blobStore.read(id)!!.json, true)
    }

    @Test
    fun `blob is updated on API PUT`() {
        validateUpdate {
            client
                .toBlocking()
                .exchange(PUT("/api/jsonBlob/$it", updateJson).contentType(MediaType.APPLICATION_JSON_TYPE), String::class.java)
        }
    }

    @Test
    fun `blob is created on API PUT`() {
        val resp = client
            .toBlocking()
            .exchange(PUT("/api/jsonBlob/${type1UUIDJsonBlobHandler.generate()}", json).contentType(MediaType.APPLICATION_JSON_TYPE), String::class.java)
        assertThat(resp.code()).isEqualTo(200)
    }

    @Test
    fun `blob is not created on bad API PUT`() {
        val resp = client
            .toBlocking()
            .exchange(PUT("/api/jsonBlob/${UUID.randomUUID()}", json).contentType(MediaType.APPLICATION_JSON_TYPE), String::class.java)
        assertThat(resp.code()).isEqualTo(400)
    }

    @Test
    fun `blob is updated on custom API PUT`() {
        validateUpdate {
            client
                .toBlocking()
                .exchange(PUT("/api/company/$it/employees/engineers", updateJson).contentType(MediaType.APPLICATION_JSON_TYPE), String::class.java)
        }
    }

    @Test
    fun `blob is updated on custom API PUT with X-jsonblob header`() {
        validateUpdate {
            client
                .toBlocking()
                .exchange(PUT("/api/company/employees/engineers", updateJson).contentType(MediaType.APPLICATION_JSON_TYPE).header("X-jsonblob", it), String::class.java)
        }
    }

    private fun validateFsDelete(request: (blobId: String) -> HttpResponse<Any>) {
        val id = type1UUIDJsonBlobHandler.generate()
        assertThat(blobStore.store(id, json, gzipBlobCompressor)).isTrue
        assertThat(blobStore.exists(id)).isTrue

        val resp = request.invoke(id)

        assertThat(resp.code()).isEqualTo(200)
        assertThat(blobStore.exists(id)).isFalse
    }

    private fun validateS3Delete(request: (blobId: String) -> HttpResponse<Any>) {
        val id = type1UUIDJsonBlobHandler.generate()
        assertThat(blobStore.store(id, json, gzipBlobCompressor)).isTrue
        assertThat(blobStore.exists(id)).isTrue

        val resp = request.invoke(id)

        assertThat(resp.code()).isEqualTo(200)
        assertThat(blobStore.exists(id)).isFalse
    }

    @Test
    fun `blob is deleted on API DETELE`() {
        validateFsDelete {
            client
                .toBlocking()
                .exchange(DELETE<Any>("/api/jsonBlob/$it"), Any::class.java)
        }
    }

    @Test
    fun `blob is deleted on custom API DETELE`() {
        validateS3Delete {
            client
                .toBlocking()
                .exchange(DELETE<Any>("/api/company/$it/employees/engineers"), Any::class.java)
        }
    }

    @Test
    fun `blob is deleted on custom API DETELE with X-jsonblob header`() {
        validateS3Delete {
            client
                .toBlocking()
                .exchange(DELETE<Any>("/api/company/employees/engineers").header("X-jsonblob", it), Any::class.java)
        }
    }

    override fun getProperties(): MutableMap<String, String> {
        return mutableMapOf(
            "file-system-blob-store.base-path" to tempDir.absolutePath,
        )
    }
}