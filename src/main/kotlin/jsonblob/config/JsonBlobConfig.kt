package jsonblob.config

import io.micronaut.context.annotation.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("json-blob")
class JsonBlobConfig {
    var deleteConcurrency = 100

    var deleteAfter: Duration = Duration.ofDays(180)

    var deleteEnabled = true

    var pruneEnabled = true
}