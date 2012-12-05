package jsonblob

import static org.grails.jaxrs.response.Responses.*

import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.PUT
import javax.ws.rs.core.Response

import org.grails.jaxrs.provider.DomainObjectNotFoundException

@Consumes(['application/json'])
@Produces(['application/json'])
class JsonBlobResource {

    def jsonBlobResourceService
    def objectMapper
    def id
    
    @GET
    Response read() {
        def blob = jsonBlobResourceService.read(id)
        Response.ok(objectMapper.writeValueAsString(blob)).build()
    }
    
    @PUT
    Response update(String json) {
        def updatedBlob = jsonBlobResourceService.update(id, dto)
        Response.ok(objectMapper.writeValueAsString(updatedBlob)).build()
    }
    
    @DELETE
    void delete() {
        jsonBlobResourceService.delete(id)
    }
    
}

