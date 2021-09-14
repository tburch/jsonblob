package jsonblob.config

import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.NotBlank

@ConfigurationProperties("ga")
class GoogleAnalyticsConfig {
    var webPropertyId = ""

    var customTrackingCodes = emptySet<CustomTrackingCode>()

    class CustomTrackingCode {
        @get:NotBlank
        var key : String = ""

        var value: String = ""
    }
}