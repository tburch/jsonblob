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

@Controller("/")
class EditorController(
    private val jsonBlobStore: JsonBlobStore,
    private val googleAnalyticsConfig: GoogleAnalyticsConfig,
    private val adsenseConfig: AdsenseConfig
) {
    companion object {
        private const val pageName = "editor"
    }

    private fun readJsonBlob(blobId: String) = jsonBlobStore.read(blobId)

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