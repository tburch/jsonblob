package jsonblob.api.http

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.views.View
import jsonblob.api.http.view.EditorView
import jsonblob.config.AdsenseConfig
import jsonblob.config.GoogleAnalyticsConfig
import jsonblob.core.store.JsonBlobStore
import jsonblob.core.store.file.FileSystemJsonBlobStore

@Controller("/")
class EditorController(
    private val jsonBlobStores: List<JsonBlobStore>,
    private val fileSystemJsonBlobStore: FileSystemJsonBlobStore,
    private val googleAnalyticsConfig: GoogleAnalyticsConfig,
    private val adsenseConfig: AdsenseConfig
) {
    companion object {
        private const val pageName = "editor"
    }

    private fun readJsonBlob(blobId: String) = jsonBlobStores.first().read(blobId)
        ?: if (jsonBlobStores.size > 1) {
            jsonBlobStores.last().read(blobId).also {
                it?.let {
                    jsonBlobStores.first().write(it)
                }
            }
        } else {
            null
        }

    @Get("ads.txt")
    fun ads() : String {
        if (adsenseConfig.adsConfig.value.isNotBlank()) {
            return "google.com, ${adsenseConfig.publisherId}, ${adsenseConfig.adsConfig.type}, ${adsenseConfig.adsConfig.value}"
        }
        throw HttpStatusException(HttpStatus.NOT_FOUND, "ads.txt doesn't exist")
    }

    @Get
    @View("editor")
    fun get() = EditorView(
        googleAnalyticsConfig.webPropertyId,
        pageName,
        googleAnalyticsConfig.customTrackingCodes
    )

    @Get("{blobId}")
    @View("editor")
    fun getBlob(@PathVariable blobId: String) : EditorView {
        if (blobId == "new") {
            return EditorView(
                googleAnalyticsConfig.webPropertyId,
                pageName,
                googleAnalyticsConfig.customTrackingCodes,
                "{}"
            )
        } else if (blobId == "ads.txt") {
            throw HttpStatusException(HttpStatus.OK, "google.com, pub-6168248064103889, DIRECT, f08c47fec0942fa0")
        }

        val blob = readJsonBlob(blobId)

        if (blob == null) {
            throw HttpStatusException(HttpStatus.NOT_FOUND, "Blob with id $blobId does not exist")
        } else {
            return EditorView(
                googleAnalyticsConfig.webPropertyId,
                pageName,
                googleAnalyticsConfig.customTrackingCodes,
                blob.json,
                blob.id
            )
        }
    }

}