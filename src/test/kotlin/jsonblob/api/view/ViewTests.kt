package jsonblob.api.view

import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jsonblob.config.S3ClientBuilderListener
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.shaded.com.google.common.io.Files
import org.testcontainers.utility.DockerImageName
import javax.inject.Inject

private val log = KotlinLogging.logger {}

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiMidMigrationTest: TestPropertyProvider {
    private val tempDir = Files.createTempDir().apply { deleteOnExit() }

    companion object {
        private val localstack = LocalStackContainer(DockerImageName.parse("localstack/localstack:0.11.3"))
            .withServices(LocalStackContainer.Service.S3)
            .apply { start() }
    }

    @AfterAll
    fun stopLocalStack() {
        localstack.stop()
    }

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Test
    fun `api view`() {
        val html = client.toBlocking().retrieve("/api")
        log.info { "html=$html" }
        assertThat(html).isNotBlank
    }

    @Test
    fun `about view`() {
        val html = client.toBlocking().retrieve("/about")
        log.info { "html=$html" }
        assertThat(html).isNotBlank
    }

    override fun getProperties() = mutableMapOf(
        "file-system-blob-store.base-path" to tempDir.absolutePath,
        "s3-blob-store.bucket" to "fubar",
        S3ClientBuilderListener.endpointProp to localstack.getEndpointOverride(
            LocalStackContainer.Service.S3
        ).toString(),
        "aws.accessKeyId" to localstack.accessKey,
        "aws.secretAccessKey" to localstack.secretKey,
        "aws.region" to localstack.region,
        "blob-migrator.enabled" to "false",
        "micronaut.http.client.read-timeout" to "1m"
    )
}