package com.lowtuna.jsonblob.view;

import com.lowtuna.dropwizard.extras.config.GoogleAnalyticsConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiView extends JsonBlobView {

    public ApiView(String gaWebPropertyID, String pageName, Set<GoogleAnalyticsConfig.CustomTrackingCode> customTrackingCodes) {
        super("/api.hbs", gaWebPropertyID, pageName, customTrackingCodes);
    }

}
