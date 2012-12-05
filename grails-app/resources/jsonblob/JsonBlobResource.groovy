package jsonblob

import javax.ws.rs.*
import javax.ws.rs.core.Response

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

