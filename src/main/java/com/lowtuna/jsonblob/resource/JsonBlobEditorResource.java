package com.lowtuna.jsonblob.resource;

import com.codahale.metrics.annotation.Timed;
import com.lowtuna.dropwizard.extras.config.GoogleAnalyticsConfig;
import com.lowtuna.jsonblob.core.BlobManager;
import com.lowtuna.jsonblob.core.BlobNotFoundException;
import com.lowtuna.jsonblob.view.AboutView;
import com.lowtuna.jsonblob.view.EditorView;
import com.mongodb.DBObject;
import com.sun.jersey.api.NotFoundException;
import org.bson.types.ObjectId;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/")
public class JsonBlobEditorResource {
    private final BlobManager blobManager;
    private final GoogleAnalyticsConfig gaConfig;

    public JsonBlobEditorResource(BlobManager blobManager, GoogleAnalyticsConfig gaConfig) {
        this.blobManager = blobManager;
        this.gaConfig = gaConfig;
    }

    @GET
    @Timed
    public EditorView defaultEditor() {
        return new EditorView(gaConfig.getWebPropertyID(), "editor", gaConfig.getCustomTrackingCodes());
    }

    @GET
    @Timed
    @Path("new")
    public EditorView emptyEditor() {
        EditorView view = new EditorView(gaConfig.getWebPropertyID(), "editor", gaConfig.getCustomTrackingCodes());
        view.setJsonBlob("{}");
        return view;
    }

    @GET
    @Timed
    @Path("about")
    public AboutView about() {
        AboutView view = new AboutView(gaConfig.getWebPropertyID(), "editor", gaConfig.getCustomTrackingCodes(), blobManager.getBlobAccessTtl(), blobManager.isDeleteEnabled());
        return view;
    }

    @GET
    @Timed
    @Path("{blobId}")
    public EditorView blobEditor(@PathParam("blobId") ObjectId blobId) {
        try {
            DBObject object = blobManager.read(blobId);
            Object blob = object.get("blob");
            EditorView view = new EditorView(gaConfig.getWebPropertyID(), "editor", gaConfig.getCustomTrackingCodes());
            view.setBlobId(blobId.toString());
            view.setJsonBlob(blob.toString());
            return view;
        } catch (BlobNotFoundException e) {
            throw new NotFoundException();
        }
    }
}
