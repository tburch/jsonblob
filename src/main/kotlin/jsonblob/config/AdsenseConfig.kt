package jsonblob.config

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("adsense")
class AdsenseConfig {
    var publisherId = ""

    var adsConfig = AdsConfig()

    class AdsConfig {
        var type : String = "DIRECT"

        var value: String = ""
    }
}