package jsonblob.api.http.view

import io.micronaut.core.annotation.Introspected
import jsonblob.config.GoogleAnalyticsConfig

@Introspected
class EditorView(
    gaWebPropertyID: String,
    pageName: String,
    gaCustomTrackingCodes: Set<GoogleAnalyticsConfig.CustomTrackingCode>,
    val jsonBlob: String? = null,
    val blobId: String? = null
) : JsonBlobView(gaWebPropertyID, pageName, gaCustomTrackingCodes)