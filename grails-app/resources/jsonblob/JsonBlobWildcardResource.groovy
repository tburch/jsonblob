package jsonblob

import org.bson.types.ObjectId

import javax.ws.rs.*
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder


@Consumes(['application/json'])
@Produces(['application/json'])
@Path('/api')
class JsonBlobWildcardResource {

    def jsonBlobResourceService
    def jsonService

    @Path('/{path: .*}')
    JsonBlobResource getResource(@PathParam('path') String path) {
        def pathParts = path.split("/")
        def id = null
        pathParts.each { part ->
            if (ObjectId.isValid(part)) {
               id = part
            }
        }
        new JsonBlobResource(jsonBlobResourceService: jsonBlobResourceService, objectMapper: jsonService.objectMapper, id: id)
    }


}
