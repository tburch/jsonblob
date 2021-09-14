package jsonblob.api.http.view

import jsonblob.config.GoogleAnalyticsConfig
import java.time.Instant

open class JsonBlobView(
    val gaWebPropertyID: String,
    val pageName: String,
    val gaCustomTrackingCodes: Set<GoogleAnalyticsConfig.CustomTrackingCode>,
    val now: Instant = Instant.now()
)