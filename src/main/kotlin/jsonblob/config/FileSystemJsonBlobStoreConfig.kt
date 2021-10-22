package jsonblob.config

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Requires
import javax.validation.constraints.NotBlank

@ConfigurationProperties(FileSystemJsonBlobStoreConfig.PREFIX)
@Requires(property = FileSystemJsonBlobStoreConfig.PREFIX + ".base-path")
class FileSystemJsonBlobStoreConfig {
    companion object {
        const val PREFIX = "file-system-blob-store"
    }

    var deleteConcurrency = 250

    var stripes = 512

    @get:NotBlank
    var basePath = ""
}
