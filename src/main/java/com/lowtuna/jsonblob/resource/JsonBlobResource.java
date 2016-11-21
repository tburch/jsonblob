package com.lowtuna.jsonblob.resource;

import com.codahale.metrics.annotation.Timed;
import com.lowtuna.jsonblob.core.JsonBlobManager;
import com.lowtuna.jsonblob.core.BlobNotFoundException;
import com.sun.jersey.api.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/{blobId}")
@Slf4j
public class JsonBlobResource {
    private final String blobId;
    private final JsonBlobManager jsonBlobManager;

    public JsonBlobResource(String blobId, JsonBlobManager jsonBlobManager) {
        this.blobId = blobId;
        this.jsonBlobManager = jsonBlobManager;
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Response read() {
        log.debug("Reading blob with id {} from {}", blobId, jsonBlobManager.getClass().getName());
        try {
            String object = jsonBlobManager.getBlob(blobId);
            return Response.ok(object).header("X-jsonblob", blobId).build();
        } catch (BlobNotFoundException e) {
            throw new NotFoundException();
        }
    }

    @PUT
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(String json) {
        log.debug("Updating blob with id {} from {}", blobId, jsonBlobManager.getClass().getName());
        try {
            boolean updated = jsonBlobManager.updateBlob(blobId, json);
            if (updated) {
                return Response.ok(json).header("X-jsonblob", blobId).build();
            } else {
                return Response.serverError().build();
            }
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (BlobNotFoundException e) {
            throw new NotFoundException();
        }
    }

    @DELETE
    @Timed
    public Response delete() {
        log.debug("Deleting blob with id {} from {}", blobId, jsonBlobManager.getClass().getName());
        try {
            jsonBlobManager.deleteBlob(blobId);
            return Response.ok().build();
        } catch (BlobNotFoundException e) {
            throw new NotFoundException();
        }
    }
}
