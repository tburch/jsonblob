package jsonblob.api.http

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.views.View
import jsonblob.api.http.view.AboutView
import jsonblob.config.GoogleAnalyticsConfig
import jsonblob.config.JsonBlobConfig

@Controller("/about")
class AboutController(
    private val jsonBlobConfig: JsonBlobConfig,
    private val googleAnalyticsConfig: GoogleAnalyticsConfig
) {
    @Get
    @View("about")
    fun get() = AboutView(
        googleAnalyticsConfig.webPropertyId,
        "about",
        googleAnalyticsConfig.customTrackingCodes,
        jsonBlobConfig.deleteAfter.toDays()
    )
}