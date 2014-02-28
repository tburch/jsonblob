package com.lowtuna.jsonblob.view;

import com.lowtuna.dropwizard.extras.config.GoogleAnalyticsConfig;
import lombok.Data;

import java.util.Set;

@Data
public class EditorView extends JsonBlobView {
    private String blobId;
    private String jsonBlob;

    public EditorView(String gaWebPropertyID, String pageName, Set<GoogleAnalyticsConfig.CustomTrackingCode> customTrackingCodes) {
        super("/editor.hbs", gaWebPropertyID, pageName, customTrackingCodes);
    }
}
