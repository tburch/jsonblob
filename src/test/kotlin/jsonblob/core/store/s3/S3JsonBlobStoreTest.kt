package jsonblob.core.store.s3

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jsonblob.config.S3ClientBuilderListener
import jsonblob.core.id.IdHandler
import jsonblob.model.JsonBlob
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import org.skyscreamer.jsonassert.JSONAssert
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.S3
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.services.s3.S3Client
import java.io.File
import javax.inject.Inject


private val log = KotlinLogging.logger {}

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class S3JsonBlobStoreTest : TestPropertyProvider {
    companion object {
        private val localstack = LocalStackContainer(DockerImageName.parse("localstack/localstack:0.11.3"))
            .withServices(S3)
            .apply { start() }

        @TempDir
        lateinit var tempDir: File

        private const val bucket = "test-bucket"
    }

    private val json = """
        {
            "name" : "bob",
            "age": 38
        }
    """.trimIndent()

    @AfterAll
    fun stopLocalStack() {
        localstack.stop()
    }

    @BeforeAll
    fun createBucket(s3Client: S3Client) {
        log.info { "Creating bucket named $bucket" }
        s3Client.createBucket {
            it.bucket(bucket)
        }
    }

    @Inject
    lateinit var s3Store : S3JsonBlobStore

    @Inject
    lateinit var idHandler : IdHandler<*>

    @Test
    fun test() {
        val id = idHandler.generate()
        assertThat(s3Store.path(id)).isNull()

        s3Store.write(JsonBlob(
            id,
            json
        ))

        val retrievedJson = s3Store.read(id)
        JSONAssert.assertEquals(json, retrievedJson!!.json, true)
    }

    override fun getProperties() = mutableMapOf(
        S3ClientBuilderListener.endpointProp to localstack.getEndpointOverride(S3).toString(),
        "aws.accessKeyId" to localstack.accessKey,
        "aws.secretAccessKey" to localstack.secretKey,
        "aws.region" to localstack.region,
        "file-system-blob-store.base-path" to tempDir.absolutePath,
        "s3-blob-store.bucket" to bucket,
    ).apply {
        log.info { "Test Properties are $this " }
    }

}
