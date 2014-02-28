package com.lowtuna.jsonblob.view;

import com.lowtuna.dropwizard.extras.config.GoogleAnalyticsConfig;
import io.dropwizard.views.View;
import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public abstract class JsonBlobView extends View {
    private final String gaWebPropertyID;
    private final String pageName;
    private final Set<GoogleAnalyticsConfig.CustomTrackingCode> gaCustomTrackingCodes;
    private final Date now = new Date();

    protected JsonBlobView(String view, String gaWebPropertyID, String pageName, Set<GoogleAnalyticsConfig.CustomTrackingCode> customTrackingCodes) {
        super(view);
        this.gaWebPropertyID = gaWebPropertyID;
        this.pageName = pageName;
        this.gaCustomTrackingCodes = customTrackingCodes;
    }

}
