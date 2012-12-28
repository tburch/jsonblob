package jsonblob

import grails.converters.JSON
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException

import javax.ws.rs.*
import javax.ws.rs.core.Response

@Consumes(['application/json'])
@Produces(['application/json'])
class JsonBlobResource {

    def jsonBlobResourceService
    def jsonService
    def id
    
    @GET
    Response read() {
        def blob = jsonBlobResourceService.read(id)
        Response.ok(jsonService.writeValueAsString(blob?.blob)).build()
    }

    @PUT
    Response update(String json) {
        try {
            JSON.parse(json)
            def updatedBlob = jsonBlobResourceService.update(id, json)
            Response.ok(jsonService.writeValueAsString(updatedBlob?.blob)).build()
        } catch (ConverterException ce) {
            Response.serverError().build()
        }
    }

//    @DELETE
//    void delete() {
//        jsonBlobResourceService.delete(id)
//    }
    
}

