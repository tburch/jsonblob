package jsonblob.config

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Requires
import javax.validation.constraints.NotBlank

@ConfigurationProperties(S3JsonBlobStoreConfig.PREFIX)
@Requires(property = S3JsonBlobStoreConfig.PREFIX + ".bucket")
class S3JsonBlobStoreConfig {
    companion object {
        const val PREFIX = "s3-blob-store"
    }

    @get:NotBlank
    var bucket = ""

    @get:NotBlank
    var basePath = "json-blobs"

    var setupLifecycle = false

    var copyToResetLastModified = false
}
