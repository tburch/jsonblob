package jsonblob.core.store.file

import java.time.Instant

data class BlobMetadata(
    val lastAccessedByBlobId: Map<String, Instant>
) {
    companion object {
        const val fileName = "blobMetadata.json.gz"
    }
}