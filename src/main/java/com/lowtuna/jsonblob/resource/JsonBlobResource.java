package com.lowtuna.jsonblob.resource;

import com.codahale.metrics.annotation.Timed;
import com.lowtuna.jsonblob.core.BlobManager;
import com.lowtuna.jsonblob.core.BlobNotFoundException;
import com.mongodb.DBObject;
import com.sun.jersey.api.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
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
    private final ObjectId blobId;
    private final BlobManager blobManager;

    public JsonBlobResource(ObjectId blobId, BlobManager blobManager) {
        this.blobId = blobId;
        this.blobManager = blobManager;
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Response read() {
        try {
            DBObject object = blobManager.read(blobId);
            ObjectId id = (ObjectId) object.get("_id");
            return Response.ok(object.get("blob")).header("X-jsonblob", id).build();
        } catch (BlobNotFoundException e) {
            throw new NotFoundException();
        }
    }

    @PUT
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(String json) {
        if (!blobManager.isValidJson(json)) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            DBObject object = blobManager.update(blobId, json);
            ObjectId id = (ObjectId) object.get("_id");
            return Response.ok(object.get("blob")).header("X-jsonblob", id).build();
        } catch (BlobNotFoundException e) {
            throw new NotFoundException();
        }
    }

    @DELETE
    @Timed
    public Response delete() {
        if (blobManager.isDeleteEnabled()) {
            try {
                blobManager.delete(blobId);
                return Response.ok().build();
            } catch (BlobNotFoundException e) {
                throw new NotFoundException();
            }
        }
        return Response.status(HttpStatus.METHOD_NOT_ALLOWED_405).build();
    }
}
