package jsonblob.api.http.view

import io.micronaut.core.annotation.Introspected
import jsonblob.config.GoogleAnalyticsConfig

@Introspected
class ApiView(
    gaWebPropertyID: String,
    pageName: String,
    gaCustomTrackingCodes: Set<GoogleAnalyticsConfig.CustomTrackingCode>
) : JsonBlobView(gaWebPropertyID, pageName, gaCustomTrackingCodes)