package com.lowtuna.jsonblob.resource;

import com.codahale.metrics.annotation.Timed;
import com.lowtuna.dropwizard.extras.config.GoogleAnalyticsConfig;
import com.lowtuna.jsonblob.core.BlobManager;
import com.lowtuna.jsonblob.view.ApiView;
import com.mongodb.DBObject;
import com.sun.jersey.api.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("/api")
@Slf4j
public class ApiResource {
    private final BlobManager blobManager;
    private final boolean deleteEnabled;
    private final GoogleAnalyticsConfig gaConfig;

    public ApiResource(BlobManager blobManager, boolean deleteEnabled, GoogleAnalyticsConfig gaConfig) {
        this.blobManager = blobManager;
        this.deleteEnabled = deleteEnabled;
        this.gaConfig = gaConfig;

        log.info("Blob deletion is {}", deleteEnabled ? "enabled" : "disabled");
    }

    @GET
    @Timed
    public ApiView getApiView() {
        return new ApiView(gaConfig.getWebPropertyID(), "api", gaConfig.getCustomTrackingCodes());
    }

    @POST
    @Path("jsonBlob")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response createJsonBlob(String json) {
        if (!blobManager.isValidJson(json)) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        DBObject newBlob = blobManager.create(json);
        ObjectId id = (ObjectId) newBlob.get("_id");
        if (id == null) {
            return Response.serverError().build();
        }

        return Response.created(UriBuilder.fromResource(JsonBlobResource.class).build(id)).entity(newBlob.get("blob")).build();
    }

    @Path("{path: .*}")
    @Timed
    public JsonBlobResource getJsonBlobResource(@PathParam("path") String path, @HeaderParam("X-jsonblob") String jsonBlobId) {
        ObjectId blobId = null;
        try {
            blobId = new ObjectId(jsonBlobId);
        } catch (IllegalArgumentException e) {
            for (String part : path.split("/")) {
                try {
                    blobId = new ObjectId(part);
                } catch (IllegalArgumentException iae) {
                    //try the next part or fall out of the loop
                }
            }
        }

        if (blobId == null) {
            throw new NotFoundException();
        }

        return createJsonBlobResource(blobId);
    }

    private JsonBlobResource createJsonBlobResource(ObjectId id) {
        return new JsonBlobResource(id, blobManager, deleteEnabled);
    }

}
