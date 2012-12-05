package jsonblob

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import org.bson.types.ObjectId
import org.springframework.beans.factory.InitializingBean

import javax.ws.rs.*
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder

@Path('/api/jsonBlob')
@Consumes(['application/json'])
@Produces(['application/json'])
class JsonBlobCollectionResource implements InitializingBean {

    def jsonBlobResourceService
    def objectMapper
    
    @POST
    Response create(String json) {
        def newBlob = jsonBlobResourceService.create(json)
        def objectId = newBlob["_id"]
        if (objectId) {
            URI uri = UriBuilder.fromPath(objectId.toString()).build()
            Response.created(uri).entity(objectMapper.writeValueAsString(newBlob)).build()
        } else {
            Response.serverError().build()
        }
    }

    @Path('/{id}')
    JsonBlobResource getResource(@PathParam('id') String id) {
        new JsonBlobResource(jsonBlobResourceService: jsonBlobResourceService, objectMapper: objectMapper, id:id)
    }

    void afterPropertiesSet() throws Exception {
        def om = new ObjectMapper()

        def jacksonMongoModule = new SimpleModule("MongoModule", new Version(1, 0, 0, null))
        jacksonMongoModule.addSerializer(ObjectId, new JsonSerializer<ObjectId>() {
            @Override
            void serialize(ObjectId t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
                jsonGenerator.writeString(t.toString())
            }
        })
        om.registerModule(jacksonMongoModule)

        this.objectMapper = om
    }
}
