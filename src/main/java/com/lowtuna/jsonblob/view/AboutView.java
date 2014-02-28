package com.lowtuna.jsonblob.view;

import com.lowtuna.dropwizard.extras.config.GoogleAnalyticsConfig;
import io.dropwizard.util.Duration;
import lombok.Data;

import java.util.Set;

@Data
public class AboutView extends JsonBlobView {
    private final Duration blobAccessTtl;

    public AboutView(String gaWebPropertyID, String pageName, Set<GoogleAnalyticsConfig.CustomTrackingCode> customTrackingCodes, Duration blobAccessTtl) {
        super("/about.hbs", gaWebPropertyID, pageName, customTrackingCodes);
        this.blobAccessTtl = blobAccessTtl;
    }
}
