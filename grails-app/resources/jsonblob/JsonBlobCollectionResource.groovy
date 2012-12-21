package jsonblob

import org.bson.types.ObjectId
import org.grails.jaxrs.provider.DomainObjectNotFoundException

import javax.ws.rs.*
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder


@Consumes(['application/json'])
@Produces(['application/json'])
@Path('/api')
class JsonBlobCollectionResource  {

    def jsonBlobResourceService
    def jsonService

    @POST
    @Path('/jsonBlob')
    Response create(String json) {
        def newBlob = jsonBlobResourceService.create(json)
        def objectId = newBlob["_id"]
        if (objectId) {
            URI uri = UriBuilder.fromPath(objectId.toString()).build()
            Response.created(uri).entity(jsonService.objectMapper.writeValueAsString(newBlob?.blob)).build()
        } else {
            Response.serverError().build()
        }
    }

    @Path('/jsonBlob/{id}')
    JsonBlobResource getResource(@PathParam('id') String id) {
        new JsonBlobResource(jsonBlobResourceService: jsonBlobResourceService, objectMapper: jsonService.objectMapper, id: id)
    }

    @Path('/{path: .*}')
    JsonBlobResource getResource(@PathParam('path') String path, @HeaderParam("X-jsonblob") String jsonBlobId) {
        def id = jsonBlobId?:null
        if (!id) {
            path.split("/").each { part ->
                if (ObjectId.isValid(part)) {
                    id = part
                }
            }
        }
        if (!id) {
            throw new DomainObjectNotFoundException(ObjectId.class, path)
        }
        new JsonBlobResource(jsonBlobResourceService: jsonBlobResourceService, objectMapper: jsonService.objectMapper, id: id)
    }

}
