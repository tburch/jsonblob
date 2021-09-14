package jsonblob.model

import java.time.Instant

data class JsonBlob(
    val id: String,
    val json: String,
    val created: Instant = Instant.now()
)
