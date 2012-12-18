package jsonblob

import javax.ws.rs.*
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder


@Consumes(['application/json'])
@Produces(['application/json'])
@Path('/api/jsonBlob')
class JsonBlobCollectionResource  {

    def jsonBlobResourceService
    def jsonService

    @POST
    Response create(String json) {
        def newBlob = jsonBlobResourceService.create(json)
        def objectId = newBlob["_id"]
        if (objectId) {
            URI uri = UriBuilder.fromPath(objectId.toString()).build()
            Response.created(uri).entity(jsonService.objectMapper.writeValueAsString(newBlob["blob"])).build()
        } else {
            Response.serverError().build()
        }
    }

    @Path('/{id}')
    JsonBlobResource getResource(@PathParam('id') String id) {
        new JsonBlobResource(jsonBlobResourceService: jsonBlobResourceService, objectMapper: jsonService.objectMapper, id: id)
    }


}
