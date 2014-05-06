package com.lowtuna.jsonblob.view;

import com.lowtuna.dropwizard.extras.config.GoogleAnalyticsConfig;
import lombok.Data;

import java.util.Set;

@Data
public class EditorView extends JsonBlobView {
    private boolean showTour;
    private String blobId;
    private String jsonBlob;

    public EditorView(String gaWebPropertyID, String pageName, Set<GoogleAnalyticsConfig.CustomTrackingCode> customTrackingCodes, boolean showTour) {
        super("/editor.hbs", gaWebPropertyID, pageName, customTrackingCodes);
        this.showTour = showTour;
    }
}
