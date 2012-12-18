package jsonblob

import org.bson.types.ObjectId
import org.grails.jaxrs.provider.DomainObjectNotFoundException

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
    JsonBlobResource getResource(@PathParam('path') String path, @HeaderParam("X-jsonblob") String jsonBlobId) {
        def pathParts = path.split("/")
        def id = jsonBlobId?:null
        if (!id) {
            pathParts.each { part ->
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
