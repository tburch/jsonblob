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
        if (blob) {
            Response.ok(objectMapper.writeValueAsString(blob["blob"])).build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
        }
    }

    @PUT
    Response update(String json) {
        def updatedBlob = jsonBlobResourceService.update(id, json)
        if (blob) {
            Response.ok(objectMapper.writeValueAsString(updatedBlob["blob"])).build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
        }
    }

//    @DELETE
//    void delete() {
//        jsonBlobResourceService.delete(id)
//    }
    
}

