package jsonblob.api.http.view

import io.micronaut.core.annotation.Introspected
import jsonblob.config.GoogleAnalyticsConfig

@Introspected
class AboutView(
    gaWebPropertyID: String,
    pageName: String,
    gaCustomTrackingCodes: Set<GoogleAnalyticsConfig.CustomTrackingCode>,
    val deleteAfterDays: Long
) : JsonBlobView(gaWebPropertyID, pageName, gaCustomTrackingCodes)